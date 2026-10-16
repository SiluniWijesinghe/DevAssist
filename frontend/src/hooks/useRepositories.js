import { useStored } from './useStored.js';
import { importRepository } from '../lib/api.js';

// Connected repositories and the one currently selected. Persisted in this browser.
export function useRepositories() {
  const [repos, setRepos] = useStored('devassist.repos', []);
  const [activeId, setActiveId] = useStored('devassist.active', null);
  const repo = repos.find(r => r.id === activeId) || null;

  // Throws if the import fails, so the dialog can show the message.
  async function connect(url, name) {
    const next = await importRepository(url, name);
    setRepos(list => [next, ...list.filter(r => r.id !== next.id)]);
    setActiveId(next.id);
    return next;
  }

  function remove(id) {
    setRepos(list => list.filter(r => r.id !== id));
    if (activeId === id) setActiveId(null);
  }

  return { repos, repo, activeId, select: setActiveId, connect, remove };
}
