// Joins class names, skipping falsy values: cx('a', cond && 'b') -> 'a b'
export const cx = (...parts) => parts.filter(Boolean).join(' ');
