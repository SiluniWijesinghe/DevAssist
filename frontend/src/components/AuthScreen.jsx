import { useState } from 'react';
import { authenticate } from '../lib/api.js';
import { Logo } from './Logo.jsx';

export default function AuthScreen({ onAuthenticated }) {
  const [mode, setMode] = useState('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  async function submit(event) {
    event.preventDefault(); setBusy(true); setError('');
    try { await authenticate(mode, email, password); onAuthenticated(); }
    catch (e) { setError(e.message || 'Authentication failed.'); }
    finally { setBusy(false); }
  }

  return <main className="flex min-h-dvh items-center justify-center bg-[var(--bg)] px-4 py-10">
    <section className="w-full max-w-md rounded-2xl border border-[var(--line)] bg-[var(--surface)] p-8 shadow-xl">
      <div className="mb-7 flex justify-center"><Logo /></div>
      <h1 className="text-center text-2xl font-semibold">{mode === 'login' ? 'Welcome back' : 'Create your account'}</h1>
      <p className="mt-2 text-center text-sm text-[var(--muted)]">Sign in to securely access your repositories.</p>
      <form className="mt-7 space-y-4" onSubmit={submit}>
        <label className="block text-sm">Email address<input className="mt-1 w-full rounded-lg border border-[var(--line)] bg-transparent px-3 py-2.5 outline-none focus:ring-2 focus:ring-blue-500" type="email" autoComplete="email" required maxLength={254} value={email} onChange={e => setEmail(e.target.value)} /></label>
        <label className="block text-sm">Password<input className="mt-1 w-full rounded-lg border border-[var(--line)] bg-transparent px-3 py-2.5 outline-none focus:ring-2 focus:ring-blue-500" type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} required minLength={8} maxLength={72} value={password} onChange={e => setPassword(e.target.value)} /></label>
        {error && <p role="alert" className="rounded-lg bg-red-500/10 p-3 text-sm text-red-500">{error}</p>}
        <button disabled={busy} className="w-full rounded-lg bg-blue-600 px-4 py-3 font-medium text-white hover:bg-blue-700 disabled:opacity-60">{busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}</button>
      </form>
      <p className="mt-5 text-center text-sm text-[var(--muted)]">{mode === 'login' ? 'New to DevAssist?' : 'Already have an account?'}{' '}
        <button className="font-medium text-blue-500 hover:underline" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}>{mode === 'login' ? 'Create account' : 'Sign in'}</button>
      </p>
    </section>
  </main>;
}
