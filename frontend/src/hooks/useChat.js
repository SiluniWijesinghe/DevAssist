import { useEffect, useRef, useState } from 'react';
import { askAgent, fetchMessages, clearMessages } from '../lib/api.js';
import { friendlyError } from '../lib/errors.js';

// Per-repository conversations, plus the state of the request in flight.
// The server (Postgres) is the source of truth; `chats` is only an in-memory cache for this session.
export function useChat(repo) {
  const [chats, setChats] = useState({});
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const abortRef = useRef(null);
  const lastPrompt = useRef('');

  const messages = (repo && chats[repo.id]) || [];

  // An error belongs to the repository it happened in.
  useEffect(() => { setError(''); }, [repo?.id]);

  // Load saved history the first time a repo is opened in this session.
  useEffect(() => {
    if (!repo || chats[repo.id]) return;
    let cancelled = false;
    fetchMessages(repo.id)
      .then(list => { if (!cancelled) setChats(c => ({ ...c, [repo.id]: list })); })
      .catch(() => { /* leave the chat empty; the user can still ask a new question */ });
    return () => { cancelled = true; };
  }, [repo?.id]);

  async function send(text, { retry = false } = {}) {
    const message = text.trim();
    if (!message || !repo || sending) return;
    const id = repo.id;
    const push = msg => setChats(c => ({ ...c, [id]: [...(c[id] || []), msg] }));

    lastPrompt.current = message;
    setError('');
    if (!retry) push({ role: 'user', content: message });
    setSending(true);

    const controller = new AbortController();
    abortRef.current = controller;
    try {
      const answer = await askAgent(id, message, controller.signal);
      push({ role: 'assistant', content: answer });
    } catch (e) {
      setError(e.name === 'AbortError' ? 'Stopped. Retry to ask again.' : friendlyError(e, 'The request failed.'));
    } finally {
      setSending(false);
    }
  }

  // Re-fetch history from the server (used after a re-sync so stale answers get flagged).
  async function reload() {
    if (!repo) return;
    try {
      const list = await fetchMessages(repo.id);
      setChats(c => ({ ...c, [repo.id]: list }));
    } catch {
      // keep current: if the request fails, leave the messages already on screen untouched
    }
  }

  async function clear() {
    if (!repo) return;
    try {
      await clearMessages(repo.id);
      setChats(c => ({ ...c, [repo.id]: [] }));
      setError('');
    } catch (e) {
      setError(friendlyError(e, 'Could not clear the chat.'));
    }
  }

  return {
    messages,
    sending,
    error,
    canRetry: Boolean(lastPrompt.current),
    send,
    retry: () => send(lastPrompt.current, { retry: true }),
    stop: () => abortRef.current?.abort(),
    clear,
    reload,
    discard: id => setChats(c => { const { [id]: _gone, ...rest } = c; return rest; }),
    clearError: () => setError(''),
  };
}