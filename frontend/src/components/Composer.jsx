import { useEffect, useRef } from 'react';
import { ArrowUp, Square } from 'lucide-react';

export default function Composer({ repoName, value, onChange, sending, onSend, onStop }) {
  const inputRef = useRef(null);
  const wasSending = useRef(false);

  // Grow with the content, up to 160px.
  useEffect(() => {
    const el = inputRef.current;
    if (el) { el.style.height = 'auto'; el.style.height = `${Math.min(el.scrollHeight, 160)}px`; }
  }, [value]);

  // Return focus to the box once an answer arrives.
  useEffect(() => {
    if (wasSending.current && !sending) inputRef.current?.focus();
    wasSending.current = sending;
  }, [sending]);

  const sendButton = 'grid size-[34px] flex-none place-items-center rounded-lg bg-accent text-accent-ink disabled:cursor-not-allowed disabled:opacity-50';

  return (
    <div className="px-3.5 pt-2 pb-[18px] md:px-7">
      <div className="mx-auto w-full max-w-[740px]">
        <form
          onSubmit={e => { e.preventDefault(); onSend(); }}
          className="flex items-end gap-2.5 rounded-xl border border-line bg-surface py-2.5 pr-2.5 pl-4 shadow-sm focus-within:border-accent focus-within:ring-[3px] focus-within:ring-accent-soft"
        >
          <textarea
            ref={inputRef}
            rows={1}
            value={value}
            disabled={sending}
            placeholder={`Ask about ${repoName}…`}
            aria-label="Message"
            onChange={e => onChange(e.target.value)}
            onKeyDown={e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); onSend(); } }}
            className="max-h-40 flex-1 resize-none bg-transparent py-1.5 leading-[1.55] outline-none focus-visible:outline-none"
          />
          {sending ? (
            <button type="button" onClick={onStop} aria-label="Stop" className={sendButton}>
              <Square size={13} fill="currentColor" />
            </button>
          ) : (
            <button type="submit" disabled={!value.trim()} aria-label="Send" className={sendButton}>
              <ArrowUp size={17} />
            </button>
          )}
        </form>
        <p className="mt-2 px-0.5 text-xs text-muted">
          Enter to send, Shift+Enter for a new line. AI can make mistakes, so verify important details.
        </p>
      </div>
    </div>
  );
}
