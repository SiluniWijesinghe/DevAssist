async function request(path, options, failureLabel) {
  const res = await fetch(path, options);
  const body = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(body.message || body.error || `${failureLabel} (${res.status}).`);
  return body;
}

export async function importRepository(url, name) {
  const body = await request(
    `/api/repositories?url=${encodeURIComponent(url)}&name=${encodeURIComponent(name)}`,
    { method: 'POST' },
    'Import failed',
  );
  if (body.id == null) throw new Error('The backend response has no repository id.');
  return { id: body.id, name: body.name || name, url: body.url || url };
}

export async function askAgent(repositoryId, message, signal) {
  const body = await request(
    '/api/agent/chat',
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      signal,
      body: JSON.stringify({ repositoryId: Number(repositoryId), message }),
    },
    'Request failed',
  );
  return body.answer || 'The agent returned an empty answer.';
}
