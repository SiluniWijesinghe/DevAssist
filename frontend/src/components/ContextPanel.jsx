import { FileCode2 } from 'lucide-react';

function Row({ label, children }) {
  return (
    <p className="mb-2 flex justify-between gap-2.5 text-[13px]">
      <span className="text-muted">{label}</span>
      {children}
    </p>
  );
}

const heading = 'mb-2.5 text-xs font-semibold text-muted';

export default function ContextPanel({ repo, messageCount, files, onSync, syncing }) {
  return (
    <aside className="hidden overflow-y-auto border-l border-line bg-surface px-4 py-5 min-[1100px]:block">
      <h3 className={heading}>Repository</h3>
      <Row label="Name">{repo.name}</Row>
      <Row label="Source">
        <a href={repo.url} target="_blank" rel="noreferrer" className="text-accent">Open on GitHub</a>
      </Row>
      <Row label="Messages">{messageCount}</Row>
      <Row label="Indexed">{repo.indexedAt ? new Date(repo.indexedAt).toLocaleString() : '—'}</Row>
      <button type="button" disabled={syncing} onClick={onSync}
        className="mb-2 w-full rounded-md border border-line px-2 py-1.5 text-[13px] hover:bg-soft disabled:opacity-60">
        {syncing ? 'Re-syncing…' : 'Re-sync from GitHub'}
      </button>

      <h3 className={`${heading} mt-6.5`}>Files referenced</h3>
      {files.length === 0 ? (
        <p className="text-[13px] text-muted">Files the agent cites will be listed here. Click one to copy its path.</p>
      ) : (
        <ul>
          {files.map(f => (
            <li key={f}>
              <button
                type="button"
                title="Copy path"
                onClick={() => navigator.clipboard?.writeText(f)}
                className="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left font-mono text-xs hover:bg-soft"
              >
                <FileCode2 size={14} className="shrink-0 text-accent" />
                <span className="[overflow-wrap:anywhere]">{f}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </aside>
  );
}
