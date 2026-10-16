// fetch() rejects with a TypeError when the server cannot be reached at all.
export const friendlyError = (e, fallback) =>
  e instanceof TypeError ? 'Cannot reach the backend. Check that Spring Boot is running on port 8080.' : e?.message || fallback;
