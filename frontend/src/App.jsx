import { useEffect, useMemo, useRef, useState } from 'react';
import { ArrowUp, FileCode2, Github, LoaderCircle, Plus, RotateCw, Square, Trash2, X } from 'lucide-react';
import Markdown from './Markdown.jsx';
import { useStored } from './useStored.js';

const starters = [
  ['Explain the architecture', 'Give me a high-level overview of this repository: main modules, entry points, and how they fit together.'],
  ['Trace a feature end to end', 'Trace one important user-facing feature from its entry point through each layer. Cite file paths.'],
  ['Review for risks', 'Review the repository for likely bugs, fragile areas, and maintainability risks. Ground each finding in specific files.'],
];
const FILE_RE = /`([\w./-]+\.[A-Za-z0-9]{1,6})(?::\d+(?:-\d+)?)?`/g;

const friendlyError = (e, fallback) =>
  e instanceof TypeError ? 'Cannot reach the backend. Check that Spring Boot is running on port 8080.' : e?.message || fallback;
const inferName = url => url.trim().replace(/\/$/, '').match(/github\.com\/[^/]+\/([^/]+?)(?:\.git)?$/i)?.[1] || '';

export default function App() {
  const [repos, setRepos] = useStored('devassist.repos', []);
  const [activeId, setActiveId] = useStored('devassist.active', null);
  const [chats, setChats] = useStored('devassist.chats', {});
  const [modal, setModal] = useState(false);
  const [form, setForm] = useState({ url: '', name: '' });
  const [formError, setFormError] = useState('');
  const [ingesting, setIngesting] = useState(false);
  const [draft, setDraft] = useState('');
  const [sending, setSending] = useState(false);
  const [chatError, setChatError] = useState('');
  const endRef = useRef(null), inputRef = useRef(null), abortRef = useRef(null), lastPrompt = useRef('');

  const repo = repos.find(r => r.id === activeId) || null;
  const messages = (repo && chats[repo.id]) || [];
  const files = useMemo(() => {
    const found = new Set();
    messages.filter(m => m.role === 'assistant').forEach(m => { for (const x of m.content.matchAll(FILE_RE)) found.add(x[1]); });
    return [...found];
  }, [messages]);

  useEffect(() => { endRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages.length, sending]);
  useEffect(() => {
    const el = inputRef.current;
    if (el) { el.style.height = 'auto'; el.style.height = `${Math.min(el.scrollHeight, 160)}px`; }
  }, [draft]);

  const openModal = () => { setFormError(''); setModal(true); };

  async function addRepository(event) {
    event.preventDefault();
    const url = form.url.trim().replace(/\/$/, '');
    const name = form.name.trim() || inferName(url);
    if (!/^https:\/\/github\.com\/[^/]+\/[^/]+(?:\.git)?$/i.test(url)) return setFormError('Enter a public GitHub URL, for example https://github.com/owner/repo.');
    setIngesting(true); setFormError('');
    try {
      const res = await fetch(`/api/repositories?url=${encodeURIComponent(url)}&name=${encodeURIComponent(name)}`, { method: 'POST' });
      const body = await res.json().catch(() => ({}));
      if (!res.ok) throw new Error(body.message || body.error || `Import failed (${res.status}).`);
      if (body.id == null) throw new Error('The backend response has no repository id.');
      const next = { id: body.id, name: body.name || name, url: body.url || url };
      setRepos(list => [next, ...list.filter(r => r.id !== next.id)]);
      setActiveId(next.id); setModal(false); setForm({ url: '', name: '' }); setChatError('');
    } catch (e) { setFormError(friendlyError(e, 'Could not import this repository.')); }
    finally { setIngesting(false); }
  }

  async function send(text = draft, retry = false) {
    const message = text.trim();
    if (!message || !repo || sending) return;
    const id = repo.id;
    const push = msg => setChats(c => ({ ...c, [id]: [...(c[id] || []), msg] }));
    lastPrompt.current = message; setDraft(''); setChatError('');
    if (!retry) push({ role: 'user', content: message });
    setSending(true);
    const controller = new AbortController(); abortRef.current = controller;
    try {
      const res = await fetch('/api/agent/chat', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, signal: controller.signal,
        body: JSON.stringify({ repositoryId: Number(id), message }),
      });
      const body = await res.json().catch(() => ({}));
      if (!res.ok) throw new Error(body.message || body.error || `Request failed (${res.status}).`);
      push({ role: 'assistant', content: body.answer || 'The agent returned an empty answer.' });
    } catch (e) { setChatError(e.name === 'AbortError' ? 'Stopped. Retry to ask again.' : friendlyError(e, 'The request failed.')); }
    finally { setSending(false); inputRef.current?.focus(); }
  }

  const clearChat = () => { if (repo) { setChats(c => ({ ...c, [repo.id]: [] })); setChatError(''); } };
  const removeRepo = id => {
    setRepos(l => l.filter(r => r.id !== id));
    setChats(c => { const { [id]: _gone, ...rest } = c; return rest; });
    if (activeId === id) setActiveId(null);
  };
  const onSelect = e => (e.target.value === '+' ? openModal() : setActiveId(Number(e.target.value) || e.target.value));

  return <div className="app">
    <aside className="rail">
      <div className="brand"><span className="logo">D</span>DevAssist</div>
      <div className="rail-head"><span>Repositories</span><button onClick={openModal} aria-label="Add repository" title="Add repository"><Plus size={15} /></button></div>
      <nav className="repo-list">
        {repos.length === 0 && <p className="rail-empty">Nothing connected yet.</p>}
        {repos.map(r => <div key={r.id} className={`repo ${r.id === activeId ? 'on' : ''}`}>
          <button onClick={() => setActiveId(r.id)}><Github size={15} /><span>{r.name}</span></button>
          <button className="del" aria-label={`Remove ${r.name}`} title="Remove from list" onClick={() => removeRepo(r.id)}><X size={13} /></button>
        </div>)}
      </nav>
      <p className="rail-foot">Public GitHub repositories. Conversations are saved in this browser.</p>
    </aside>

    <main className="stage">
      <header className="bar">
        <select className="repo-select" value={activeId ?? ''} onChange={onSelect} aria-label="Repository">
          {!repo && <option value="">Select repository</option>}
          {repos.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
          <option value="+">Add repository…</option>
        </select>
        <div className="bar-title">{repo ? <><strong>{repo.name}</strong><a href={repo.url} target="_blank" rel="noreferrer">{repo.url.replace('https://', '')}</a></> : <strong>Get started</strong>}</div>
        {repo && messages.length > 0 && <button className="ghost" onClick={clearChat}><Trash2 size={14} /> Clear conversation</button>}
      </header>

      {!repo ? <section className="hello">
        <h1>Ask questions about any codebase.</h1>
        <p>DevAssist clones a public GitHub repository, indexes its files and Git history, then investigates the code before it answers, so every response points to real files.</p>
        <button className="primary" onClick={openModal}><Github size={16} /> Connect a repository</button>
      </section> : <>
        <div className="thread"><div className="col">
          {messages.length === 0 && <div className="starters">
            <h2>What do you want to know about {repo.name}?</h2>
            {starters.map(([title, prompt]) => <button key={title} onClick={() => send(prompt)} disabled={sending}><strong>{title}</strong><span>{prompt}</span></button>)}
          </div>}
          {messages.map((m, i) => m.role === 'user'
            ? <div className="msg user" key={i}><div className="bubble">{m.content}</div></div>
            : <div className="msg bot" key={i}><div className="who"><span className="logo sm">D</span>DevAssist</div><Markdown>{m.content}</Markdown></div>)}
          {sending && <div className="msg bot"><div className="who"><span className="logo sm">D</span>DevAssist</div><p className="working"><LoaderCircle className="spin" size={14} /> Searching the repository…</p></div>}
          <div ref={endRef} />
        </div></div>
        <div className="dock"><div className="col">
          {chatError && <div className="alert"><span>{chatError}</span>{lastPrompt.current && <button onClick={() => send(lastPrompt.current, true)}><RotateCw size={12} /> Retry</button>}</div>}
          <form className="composer" onSubmit={e => { e.preventDefault(); send(); }}>
            <textarea ref={inputRef} rows="1" value={draft} disabled={sending} placeholder={`Ask about ${repo.name}…`} onChange={e => setDraft(e.target.value)}
              onKeyDown={e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); } }} />
            {sending ? <button type="button" className="send" onClick={() => abortRef.current?.abort()} aria-label="Stop"><Square size={13} fill="currentColor" /></button>
              : <button type="submit" className="send" disabled={!draft.trim()} aria-label="Send"><ArrowUp size={17} /></button>}
          </form>
          <p className="fine">Enter to send, Shift+Enter for a new line. AI can make mistakes, so verify important details.</p>
        </div></div>
      </>}
    </main>

    {repo && <aside className="context">
      <h3>Repository</h3>
      <p className="kv"><span>Name</span>{repo.name}</p>
      <p className="kv"><span>Source</span><a href={repo.url} target="_blank" rel="noreferrer">Open on GitHub</a></p>
      <p className="kv"><span>Messages</span>{messages.length}</p>
      <h3>Files referenced</h3>
      {files.length === 0 ? <p className="muted">Files the agent cites will be listed here. Click one to copy its path.</p>
        : <ul className="files">{files.map(f => <li key={f}><button onClick={() => navigator.clipboard?.writeText(f)} title="Copy path"><FileCode2 size={14} /><span>{f}</span></button></li>)}</ul>}
    </aside>}

    {modal && <div className="overlay" onMouseDown={e => { if (e.target === e.currentTarget && !ingesting) setModal(false); }}>
      <form className="dialog" onSubmit={addRepository} role="dialog" aria-modal="true" aria-labelledby="dlg">
        <h2 id="dlg">Connect a repository</h2>
        <p>DevAssist will clone and index it. This can take a minute for large repositories.</p>
        <label htmlFor="u">GitHub URL</label>
        <input id="u" autoFocus required disabled={ingesting} placeholder="https://github.com/owner/repo" value={form.url}
          onChange={e => setForm(f => ({ url: e.target.value, name: !f.name || f.name === inferName(f.url) ? inferName(e.target.value) : f.name }))} />
        <label htmlFor="n">Display name</label>
        <input id="n" required disabled={ingesting} placeholder="my-project" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
        {formError && <div className="alert"><span>{formError}</span></div>}
        <div className="actions">
          <button type="button" className="ghost" disabled={ingesting} onClick={() => setModal(false)}>Cancel</button>
          <button className="primary" disabled={ingesting}>{ingesting ? <><LoaderCircle className="spin" size={15} /> Cloning and indexing…</> : 'Import repository'}</button>
        </div>
      </form>
    </div>}
  </div>;
}
