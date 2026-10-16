import { useEffect, useState } from 'react';

// useState that persists to localStorage (safe if storage is blocked or full).
export function useStored(key, initial) {
  const [value, setValue] = useState(() => {
    try { const raw = localStorage.getItem(key); return raw ? JSON.parse(raw) : initial; } catch { return initial; }
  });
  useEffect(() => { try { localStorage.setItem(key, JSON.stringify(value)); } catch { /* ignore */ } }, [key, value]);
  return [value, setValue];
}
