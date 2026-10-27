import { useSyncExternalStore } from 'react';

// Tiny shared store: is the backend currently waking up? (free hosting sleeps when idle)
let state = { waking: false, since: 0 };
const listeners = new Set();

export function setWaking(waking) {
  if (state.waking === waking) return;
  state = { waking, since: waking ? Date.now() : 0 };
  listeners.forEach(listener => listener());
}

const subscribe = listener => {
  listeners.add(listener);
  return () => listeners.delete(listener);
};

export const useWakeState = () => useSyncExternalStore(subscribe, () => state);
