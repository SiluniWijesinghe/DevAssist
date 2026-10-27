import { useEffect, useState } from 'react';
import { useStored } from './useStored.js';
import { fetchRepositories, importRepository, deleteRepository } from '../lib/api.js';

// The server is the source of truth for the repo list; only the active selection is kept locally.
export function useRepositories() {
  const [repos, setRepos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeId, setActiveId] = useStored('devassist.active', null);
  const repo = repos.find(r => r.id === activeId) || null;

  useEffect(() => {
    let cancelled = false;
    fetchRepositories()
      .then(list => {
        if (cancelled) return;
        setRepos(list.map(r => ({ id: r.id, name: r.name, url: r.url })));
        // Drop a stale selection that doesn't belong to this user
        setActiveId(id => (list.some(r => r.id === id) ? id : null));
      })
      .catch(() => { /* 401 is handled globally by the devassist:unauthorized event */ })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  async function connect(url, name) {
    const next = await importRepository(url, name);
    setRepos(list => [next, ...list.filter(r => r.id !== next.id)]);
    setActiveId(next.id);
    return next;
  }

  async function remove(id) {
    await deleteRepository(id); // throws on failure; App.jsx already shows an alert
    setRepos(list => list.filter(r => r.id !== id));
    if (activeId === id) setActiveId(null);
  }

  return { repos, repo, activeId, loading, select: setActiveId, connect, remove };
}