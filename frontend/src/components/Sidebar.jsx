import { Github, Plus, X } from 'lucide-react';
import { Logo } from './Logo.jsx';
import Button from './ui/Button.jsx';
import { cx } from '../lib/cx.js';

function RepoItem({ repo, active, onSelect, onRemove }) {
  return (
    <div className={cx('group flex items-center rounded-md', active && 'bg-accent-soft')}>
      <button
        type="button"
        onClick={onSelect}
        aria-current={active ? 'true' : undefined}
        className={cx(
          'flex min-w-0 flex-1 items-center gap-2 rounded-md p-2 text-left hover:bg-soft',
          active && 'font-medium text-accent hover:bg-transparent',
        )}
      >
        <Github size={15} className="shrink-0" />
        <span className="truncate">{repo.name}</span>
      </button>
      <button
        type="button"
        onClick={onRemove}
        aria-label={`Remove ${repo.name}`}
        title="Remove from list"
        className="mr-1 grid size-6 place-items-center rounded-md text-muted opacity-0 hover:bg-soft focus-visible:opacity-100 group-hover:opacity-100"
      >
        <X size={13} />
      </button>
    </div>
  );
}

export default function Sidebar({ repos, activeId, onSelect, onRemove, onAdd }) {
  return (
    <aside className="hidden flex-col overflow-y-auto border-r border-line bg-surface px-4 py-5 md:flex">
      <div className="mb-7">
        <Logo />
      </div>

      <div className="mb-1.5 flex items-center justify-between px-2 text-xs font-medium text-muted">
        <span>Repositories</span>
        <Button variant="icon" onClick={onAdd} aria-label="Add repository" title="Add repository">
          <Plus size={15} />
        </Button>
      </div>

      <nav aria-label="Repositories">
        {repos.length === 0 && <p className="my-1.5 px-2 text-[13px] text-muted">Nothing connected yet.</p>}
        {repos.map(r => (
          <RepoItem key={r.id} repo={r} active={r.id === activeId} onSelect={() => onSelect(r.id)} onRemove={() => onRemove(r.id)} />
        ))}
      </nav>

      <p className="mt-auto border-t border-line px-2 pt-3.5 text-xs text-muted">
        Public GitHub repositories. Conversations are saved in this browser.
      </p>
    </aside>
  );
}
