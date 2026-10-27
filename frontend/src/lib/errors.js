export const SESSION_NOTICE_KEY = 'devassist.notice';

export const MESSAGES = {
  startTimeout: 'The server is taking longer than usual to start. Please wait a minute and try again.',
  interrupted:
    'The connection was interrupted. Please try again. If you were importing a large repository, it may still be processing, so check your repository list in a minute.',
  sessionExpired: 'Your session has expired. Please sign in again.',
  generic: 'Something went wrong on our side. Please try again in a moment.',
};

// Thrown by lib/api.js. `message` is always safe to show to the user.
export class ApiError extends Error {
  constructor(message, { status = 0, kind = 'http' } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status; // HTTP status, or 0 when there was no response
    this.kind = kind; // 'http' | 'network' | 'timeout'
  }
}

const GENERIC_SERVER_TEXT = 'Something went wrong';

// Turns an HTTP failure into a plain-language message.
export function messageForStatus(status, { serverMessage = '', path = '', failureLabel = 'Request failed' } = {}) {
  const server = serverMessage && serverMessage !== GENERIC_SERVER_TEXT ? serverMessage : '';
  const onLogin = path.startsWith('/api/auth/login');

  switch (status) {
    case 400:
    case 422:
      return server || 'Please check what you entered and try again.';
    case 401:
      return onLogin ? server || 'Incorrect email or password.' : MESSAGES.sessionExpired;
    case 403:
      return "You don't have permission to do that.";
    case 404:
      return 'That item could not be found. It may have been removed.';
    case 409:
      return server || 'That already exists.';
    case 413:
      return 'That is too large to send.';
    case 429:
      return 'Too many requests. Please wait a moment and try again.';
    case 502:
      return /^clone failed/i.test(server)
        ? "Couldn't download that repository. Check that the URL is correct and the repository is public, then try again."
        : "The server couldn't complete that request. Please try again in a moment.";
    case 503:
    case 504:
      return 'The server is busy or took too long to respond. Please try again in a minute. If you were importing a large repository, it may still be processing.';
    default:
      return status >= 500 ? MESSAGES.generic : `${failureLabel}. Please try again.`;
  }
}

export const friendlyError = (e, fallback) => {
  if (e instanceof ApiError) return e.message;
  if (e instanceof TypeError) return MESSAGES.interrupted; // fetch() rejected without a response
  return e?.message || fallback;
};
