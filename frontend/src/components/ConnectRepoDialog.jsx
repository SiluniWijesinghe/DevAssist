import { useEffect, useState } from 'react';
import Button from './ui/Button.jsx';
import Spinner from './ui/Spinner.jsx';
import TextField from './ui/TextField.jsx';
import ErrorBanner from './ui/ErrorBanner.jsx';
import { GITHUB_URL_RE, inferName, normalizeUrl } from '../lib/github.js';
import { friendlyError } from '../lib/errors.js';

// Mounted only while open, so its state resets every time it is shown.
export default function ConnectRepoDialog({ onClose, onConnect }) {
  const [form, setForm] = useState({ url: '', name: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const onKey = e => { if (e.key === 'Escape' && !busy) onClose(); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [busy, onClose]);

  // Typing a URL fills in the name until the user edits the name themselves.
  const changeUrl = value =>
    setForm(f => ({ url: value, name: !f.name || f.name === inferName(f.url) ? inferName(value) : f.name }));

  async function submit(event) {
    event.preventDefault();
    const url = normalizeUrl(form.url);
    const name = form.name.trim() || inferName(url);
    if (!GITHUB_URL_RE.test(url)) return setError('Enter a public GitHub URL, for example https://github.com/owner/repo.');
    setBusy(true);
    setError('');
    try {
      await onConnect(url, name);
      onClose();
    } catch (e) {
      setError(friendlyError(e, 'Could not import this repository.'));
      setBusy(false);
    }
  }

  return (
    <div
      className="fixed inset-0 z-10 grid place-items-center bg-[#0a0e1480] p-5"
      onMouseDown={e => { if (e.target === e.currentTarget && !busy) onClose(); }}
    >
      <form
        onSubmit={submit}
        role="dialog"
        aria-modal="true"
        aria-labelledby="connect-title"
        className="flex w-full max-w-[440px] flex-col gap-3.5 rounded-[14px] border border-line bg-surface p-6.5 shadow-[0_24px_80px_#0006]"
      >
        <div>
          <h2 id="connect-title" className="text-xl font-semibold">Connect a repository</h2>
          <p className="mt-1.5 text-[13px] text-muted">DevAssist will clone and index it. This can take a minute for large repositories.</p>
        </div>

        <TextField
          label="GitHub URL"
          autoFocus
          required
          disabled={busy}
          placeholder="https://github.com/owner/repo"
          value={form.url}
          onChange={e => changeUrl(e.target.value)}
        />
        <TextField
          label="Display name"
          required
          disabled={busy}
          placeholder="my-project"
          value={form.name}
          onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
        />

        {error && <ErrorBanner message={error} />}

        <div className="mt-1 flex justify-end gap-2.5">
          <Button variant="ghost" disabled={busy} onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={busy}>
            {busy ? <><Spinner /> Cloning and indexing…</> : 'Import repository'}
          </Button>
        </div>
      </form>
    </div>
  );
}
