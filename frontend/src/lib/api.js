const API_BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
const TOKEN_KEY = 'devassist.auth.token';

export const getToken = () => sessionStorage.getItem(TOKEN_KEY);
export const setToken = token => sessionStorage.setItem(TOKEN_KEY, token);
export const clearToken = () => sessionStorage.removeItem(TOKEN_KEY);

async function request(path, options = {}, failureLabel = 'Request failed', auth = true) {
  const headers = new Headers(options.headers || {});
  const token = auth ? getToken() : null;
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const body = await res.json().catch(() => ({}));
  if (res.status === 401 && auth) window.dispatchEvent(new CustomEvent('devassist:unauthorized'));
  if (!res.ok) throw new Error(body.message || body.error || `${failureLabel} (${res.status}).`);
  return body;
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
  if (body.id == null) throw new Error('The backend response has no repository id.');
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
