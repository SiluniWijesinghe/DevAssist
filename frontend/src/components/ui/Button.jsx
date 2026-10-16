import { cx } from '../../lib/cx.js';

const base = 'inline-flex items-center justify-center gap-2 transition-colors disabled:cursor-not-allowed disabled:opacity-50';

const variants = {
  primary: 'rounded-lg bg-accent px-4 py-2.5 font-semibold text-accent-ink hover:brightness-110',
  ghost: 'rounded-lg border border-line px-3 py-1.5 text-[13px] text-muted hover:bg-soft hover:text-ink',
  icon: 'size-7 rounded-md text-muted hover:bg-soft hover:text-ink',
};

export default function Button({ variant = 'primary', className, type = 'button', ...props }) {
  return <button type={type} className={cx(base, variants[variant], className)} {...props} />;
}
