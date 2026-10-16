import { Trash2 } from 'lucide-react';
import { LogoMark } from './Logo.jsx';
import Button from './ui/Button.jsx';

export default function TopBar({ repos, repo, activeId, onSelect, onAdd, canClear, onClear }) {
  // The select doubles as the repository switcher on small screens, where the sidebar is hidden.
  const handleSelect = e => (e.target.value === '+' ? onAdd() : onSelect(Number(e.target.value) || e.target.value));

  return (
    <header className="flex h-14 flex-none items-center gap-3.5 border-b border-line bg-surface px-3.5 md:px-7">
      <LogoMark size={26} className="shrink-0 md:hidden" />

      <select
        value={activeId ?? ''}
        onChange={handleSelect}
        aria-label="Repository"
        className="min-w-0 flex-1 rounded-md border border-line bg-bg px-2 py-1.5 md:hidden"
      >
        {!repo && <option value="">Select repository</option>}
        {repos.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
        <option value="+">Add repository…</option>
      </select>

      <div className="hidden min-w-0 flex-1 items-baseline gap-3 md:flex">
        {repo ? (
          <>
            <strong className="font-semibold">{repo.name}</strong>
            <a
              href={repo.url}
              target="_blank"
              rel="noreferrer"
              className="hidden truncate font-mono text-xs text-muted hover:text-accent md:block"
            >
              {repo.url.replace('https://', '')}
            </a>
          </>
        ) : (
          <strong className="font-semibold">Get started</strong>
        )}
      </div>

      {canClear && (
        <Button variant="ghost" onClick={onClear} aria-label="Clear conversation">
          <Trash2 size={14} />
          <span className="hidden md:inline">Clear conversation</span>
        </Button>
      )}
    </header>
  );
}
