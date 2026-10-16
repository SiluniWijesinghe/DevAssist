export const GITHUB_URL_RE = /^https:\/\/github\.com\/[^/]+\/[^/]+(?:\.git)?$/i;

export const normalizeUrl = url => url.trim().replace(/\/$/, '');

// https://github.com/owner/my-repo.git -> my-repo
export const inferName = url => url.trim().replace(/\/$/, '').match(/github\.com\/[^/]+\/([^/]+?)(?:\.git)?$/i)?.[1] || '';
