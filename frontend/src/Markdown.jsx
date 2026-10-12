import { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Check, Copy } from 'lucide-react';

function CodeBlock({ lang, text }) {
  const [copied, setCopied] = useState(false);
  async function copy() {
    try { await navigator.clipboard.writeText(text); setCopied(true); setTimeout(() => setCopied(false), 1500); } catch { /* clipboard blocked */ }
  }
  return <div className="code-block">
    <div className="code-head"><span>{lang || 'code'}</span><button type="button" onClick={copy}>{copied ? <><Check size={12} /> Copied</> : <><Copy size={12} /> Copy</>}</button></div>
    <pre><code>{text}</code></pre>
  </div>;
}

const components = {
  code({ className, children }) {
    const text = String(children);
    const lang = /language-([\w-]+)/.exec(className || '')?.[1];
    if (!lang && !text.includes('\n')) return <code className="inline-code">{children}</code>;
    return <CodeBlock lang={lang} text={text.replace(/\n$/, '')} />;
  },
  pre: ({ children }) => <>{children}</>,
  a: ({ href, children }) => <a href={href} target="_blank" rel="noreferrer">{children}</a>,
  table: ({ children }) => <div className="table-wrap"><table>{children}</table></div>,
};

export default function Markdown({ children }) {
  return <div className="md"><ReactMarkdown remarkPlugins={[remarkGfm]} components={components}>{children}</ReactMarkdown></div>;
}
