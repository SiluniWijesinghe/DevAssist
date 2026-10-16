import { useId } from 'react';

export default function TextField({ label, ...inputProps }) {
  const id = useId();
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-[13px] font-medium">{label}</label>
      <input
        id={id}
        className="rounded-lg border border-line bg-bg px-3 py-2.5 outline-none focus:border-accent disabled:opacity-60"
        {...inputProps}
      />
    </div>
  );
}
