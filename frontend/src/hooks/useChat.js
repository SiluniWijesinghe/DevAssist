import { useEffect, useRef, useState } from 'react';
import { useStored } from './useStored.js';
import { askAgent } from '../lib/api.js';
import { friendlyError } from '../lib/errors.js';

// Per-repository conversations, plus the state of the request in flight.
export function useChat(repo) {
  const [chats, setChats] = useStored('devassist.chats', {});
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const abortRef = useRef(null);
  const lastPrompt = useRef('');

  const messages = (repo && chats[repo.id]) || [];

  // An error belongs to the repository it happened in.
  useEffect(() => { setError(''); }, [repo?.id]);

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

  return {
    messages,
    sending,
    error,
    canRetry: Boolean(lastPrompt.current),
    send,
    retry: () => send(lastPrompt.current, { retry: true }),
    stop: () => abortRef.current?.abort(),
    clear: () => { if (repo) { setChats(c => ({ ...c, [repo.id]: [] })); setError(''); } },
    discard: id => setChats(c => { const { [id]: _gone, ...rest } = c; return rest; }),
    clearError: () => setError(''),
  };
}
