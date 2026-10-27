import { ApiError, MESSAGES, messageForStatus } from './errors.js';
import { setWaking } from './wake.js';

const API_BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
const TOKEN_KEY = 'devassist.auth.token';

export const getToken = () => sessionStorage.getItem(TOKEN_KEY);
export const setToken = token => sessionStorage.setItem(TOKEN_KEY, token);
export const clearToken = () => sessionStorage.removeItem(TOKEN_KEY);

// The free backend host pauses after ~15 minutes without traffic and needs up to a
// minute to wake. After a quiet spell we check /api/health first and wait for it.
const IDLE_BEFORE_CHECK_MS = 8 * 60 * 1000;
const WAKE_TIMEOUT_MS = 120_000;
const ATTEMPT_TIMEOUT_MS = 10_000;
const SHOW_NOTICE_AFTER_MS = 1_500;

let lastContact = 0;
let wakePromise = null;

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

async function isHealthy() {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), ATTEMPT_TIMEOUT_MS);
  try {
    const res = await fetch(`${API_BASE}/api/health`, { signal: controller.signal, cache: 'no-store' });
    const body = await res.json();
    return res.ok && body?.status === 'UP';
  } catch {
    return false; // network error, timeout, or a non-JSON "starting up" page
  } finally {
    clearTimeout(timer);
  }
}

async function pollUntilHealthy() {
  const started = Date.now();
  const noticeTimer = setTimeout(() => setWaking(true), SHOW_NOTICE_AFTER_MS);
  try {
    for (let attempt = 0; Date.now() - started < WAKE_TIMEOUT_MS; attempt++) {
      if (await isHealthy()) {
        lastContact = Date.now();
        return { waited: attempt > 0 || Date.now() - started > SHOW_NOTICE_AFTER_MS };
      }
      await sleep(Math.min(1500 + attempt * 500, 4000));
    }
    throw new ApiError(MESSAGES.startTimeout, { kind: 'timeout' });
  } finally {
    clearTimeout(noticeTimer);
    setWaking(false);
  }
}

// Resolves once the backend answers. `waited` is true when it had to wake up.
export function ensureAwake({ force = false } = {}) {
  if (!force && Date.now() - lastContact < IDLE_BEFORE_CHECK_MS) return Promise.resolve({ waited: false });
  wakePromise ??= pollUntilHealthy().finally(() => { wakePromise = null; });
  return wakePromise;
}

async function request(path, options = {}, failureLabel = 'Request failed', auth = true) {
  await ensureAwake();

  const headers = new Headers(options.headers || {});
  const token = auth ? getToken() : null;
  if (token) headers.set('Authorization', `Bearer ${token}`);

  // Reads are safe to repeat; writes are only repeated when the server was verifiably asleep.
  const isRead = (options.method || 'GET').toUpperCase() === 'GET';
  let res;
  let body;
  for (let attempt = 0; ; attempt++) {
    try {
      res = await fetch(`${API_BASE}${path}`, { ...options, headers });
    } catch (e) {
      if (e?.name === 'AbortError') throw e; // the user pressed Stop
      // No response at all: the server may have gone to sleep, or the connection dropped.
      lastContact = 0;
      if (attempt === 0 && ((await ensureAwake({ force: true })).waited || isRead)) continue; // asleep (request never ran) or a safe read
      throw new ApiError(MESSAGES.interrupted, { kind: 'network' });
    }

    body = await res.json().catch(() => null);
    // A 502/503/504 without our JSON error body comes from the hosting gateway, not the app.
    const fromGateway = [502, 503, 504].includes(res.status) && !(body?.error || body?.message);
    if (fromGateway && attempt === 0) {
      lastContact = 0;
      if ((await ensureAwake({ force: true })).waited || isRead) continue;
    }
    if (!fromGateway) lastContact = Date.now();
    break;
  }

  if (res.status === 401 && auth) window.dispatchEvent(new CustomEvent('devassist:unauthorized'));
  if (!res.ok) {
    throw new ApiError(
      messageForStatus(res.status, { serverMessage: body?.message || body?.error, path, failureLabel }),
      { status: res.status },
    );
  }
  return body ?? {};
}

export async function authenticate(mode, email, password) {
  const body = await request(`/api/auth/${mode}`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  }, mode === 'login' ? 'Login failed' : 'Registration failed', false);
  setToken(body.token);
  return body;
}

export async function logout() {
  try { await request('/api/auth/logout', { method: 'POST' }, 'Logout failed'); } finally { clearToken(); }
}

export async function fetchRepositories() {
  return request('/api/repositories', {}, 'Could not load repositories');
}

export async function importRepository(url, name) {
  const body = await request(`/api/repositories?url=${encodeURIComponent(url)}&name=${encodeURIComponent(name)}`, { method: 'POST' }, 'Import failed');
  if (body.id == null) throw new Error('The server response was missing the repository id. Please try again.');
  return { id: body.id, name: body.name || name, url: body.url || url, indexedAt: body.indexedAt };
}

export async function deleteRepository(repositoryId) {
  await request(`/api/repositories/${encodeURIComponent(repositoryId)}`, { method: 'DELETE' }, 'Could not remove repository');
}

export async function askAgent(repositoryId, message, signal) {
  const body = await request('/api/agent/chat', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, signal,
    body: JSON.stringify({ repositoryId: Number(repositoryId), message }),
  }, 'Request failed');
  return body.answer || 'The agent returned an empty answer.';
}

export async function fetchMessages(repositoryId) {
  return request(`/api/repositories/${encodeURIComponent(repositoryId)}/messages`, {}, 'Could not load chat history');
}
export async function clearMessages(repositoryId) {
  await request(`/api/repositories/${encodeURIComponent(repositoryId)}/messages`, { method: 'DELETE' }, 'Could not clear chat');
}

export async function syncRepository(id) {
  const body = await request(`/api/repositories/${encodeURIComponent(id)}/sync`, { method: 'POST' }, 'Re-sync failed');
  return { id: body.id, name: body.name, url: body.url, indexedAt: body.indexedAt };
}
