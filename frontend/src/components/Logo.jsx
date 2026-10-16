// The DevAssist mark: a "D" drawn as a Git graph (a trunk, a branch, three commits).
// It reads as a letter at a glance and as a repository history up close.
export function LogoMark({ size = 28, className }) {
  return (
    <svg viewBox="0 0 32 32" width={size} height={size} role="img" aria-label="DevAssist" className={className}>
      <rect width="32" height="32" rx="8" className="fill-accent" />
      <path
        d="M11 8v16M11 8h4a8 8 0 0 1 0 16h-4"
        fill="none"
        strokeWidth="2.4"
        strokeLinecap="round"
        strokeLinejoin="round"
        className="stroke-accent-ink"
      />
      <g strokeWidth="2.2" className="fill-accent stroke-accent-ink">
        <circle cx="11" cy="8" r="2.6" />
        <circle cx="11" cy="24" r="2.6" />
        <circle cx="23" cy="16" r="2.6" />
      </g>
    </svg>
  );
}

export function Logo({ size = 28 }) {
  return (
    <div className="flex items-center gap-2.5">
      <LogoMark size={size} />
      <span className="text-base font-semibold tracking-tight">DevAssist</span>
    </div>
  );
}
