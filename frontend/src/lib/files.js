// Matches `path/to/File.java`, optionally followed by :12 or :12-30, inside backticks.
const FILE_RE = /`([\w./-]+\.[A-Za-z0-9]{1,6})(?::\d+(?:-\d+)?)?`/g;

// Unique file paths cited in the assistant's messages, in order of first appearance.
export function extractFilePaths(messages) {
  const found = new Set();
  messages
    .filter(m => m.role === 'assistant')
    .forEach(m => { for (const match of m.content.matchAll(FILE_RE)) found.add(match[1]); });
  return [...found];
}
