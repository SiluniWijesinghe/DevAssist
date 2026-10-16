import { RotateCw } from 'lucide-react';

export default function ErrorBanner({ message, onRetry }) {
  return (
    <div role="alert" className="flex items-center justify-between gap-3 rounded-lg bg-danger-soft px-3 py-2.5 text-[13px] text-danger">
      <span>{message}</span>
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="inline-flex shrink-0 items-center gap-1.5 rounded-md border border-current px-2.5 py-1 text-xs"
        >
          <RotateCw size={12} /> Retry
        </button>
      )}
    </div>
  );
}
