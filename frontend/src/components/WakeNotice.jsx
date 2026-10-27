import { useEffect, useState } from 'react';
import Spinner from './ui/Spinner.jsx';
import { useWakeState } from '../lib/wake.js';

// Shown while the free-tier backend is waking up. Requests wait and continue on their own.
export default function WakeNotice() {
  const { waking, since } = useWakeState();
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    if (!waking) return undefined;
    const tick = () => setSeconds(Math.floor((Date.now() - since) / 1000));
    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, [waking, since]);

  if (!waking) return null;

  return (
    <div
      role="status"
      aria-live="polite"
      className="fixed inset-x-0 top-3 z-20 mx-auto flex w-[min(92vw,460px)] items-start gap-3 rounded-xl border border-line bg-surface px-4 py-3 text-[13px] shadow-[0_12px_40px_#0005]"
    >
      <span className="mt-0.5 text-accent"><Spinner size={16} /></span>
      <div>
        <p className="font-semibold">Waking up the server…</p>
        <p className="mt-0.5 text-muted">
          Free hosting pauses when idle, so this can take up to a minute. You don&apos;t need to refresh;
          your request will continue automatically. ({seconds}s)
        </p>
      </div>
    </div>
  );
}
