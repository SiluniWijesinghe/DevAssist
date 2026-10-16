import { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Check, Copy } from 'lucide-react';

function CodeBlock({ lang, text }) {
  const [copied, setCopied] = useState(false);
  async function copy() {
    try { await navigator.clipboard.writeText(text); setCopied(true); setTimeout(() => setCopied(false), 1500); } catch { /* clipboard blocked */ }
  }
  return (
    <div className="my-3.5 overflow-hidden rounded-[9px] border border-line bg-surface">
      <div className="flex items-center justify-between bg-soft py-1.5 pr-2 pl-3 font-mono text-xs text-muted">
        <span>{lang || 'code'}</span>
        <button type="button" onClick={copy} className="inline-flex items-center gap-1.5 rounded-[5px] px-1.5 py-0.5 hover:bg-line hover:text-ink">
          {copied ? <><Check size={12} /> Copied</> : <><Copy size={12} /> Copy</>}
        </button>
      </div>
      <pre className="overflow-x-auto px-3.5 py-3"><code className="font-mono text-[12.5px] leading-[1.7] whitespace-pre">{text}</code></pre>
    </div>
  );
}

const heading = 'mt-5.5 mb-2 leading-snug font-semibold tracking-tight';

// Every markdown element gets its Tailwind classes here, so no global prose styles are needed.
const components = {
  p: ({ children }) => <p className="my-3">{children}</p>,
  h1: ({ children }) => <h1 className={`${heading} text-[19px]`}>{children}</h1>,
  h2: ({ children }) => <h2 className={`${heading} text-[17px]`}>{children}</h2>,
  h3: ({ children }) => <h3 className={`${heading} text-[15px]`}>{children}</h3>,
  h4: ({ children }) => <h4 className={`${heading} text-[15px]`}>{children}</h4>,
  ul: ({ children }) => <ul className="my-3 list-disc space-y-1 pl-5.5">{children}</ul>,
  ol: ({ children }) => <ol className="my-3 list-decimal space-y-1 pl-5.5">{children}</ol>,
  blockquote: ({ children }) => <blockquote className="my-3 border-l-[3px] border-line px-3.5 py-0.5 text-muted">{children}</blockquote>,
  hr: () => <hr className="my-4.5 border-line" />,
  a: ({ href, children }) => <a href={href} target="_blank" rel="noreferrer" className="text-accent underline-offset-2 hover:underline">{children}</a>,
  table: ({ children }) => <div className="my-3.5 overflow-x-auto"><table className="border-collapse text-[13px]">{children}</table></div>,
  th: ({ children }) => <th className="border border-line bg-soft px-2.5 py-1.5 text-left font-semibold">{children}</th>,
  td: ({ children }) => <td className="border border-line px-2.5 py-1.5 text-left">{children}</td>,
  code({ className, children }) {
    const text = String(children);
    const lang = /language-([\w-]+)/.exec(className || '')?.[1];
    if (!lang && !text.includes('\n')) return <code className="rounded bg-soft px-1.5 py-px font-mono text-[12.5px]">{children}</code>;
    return <CodeBlock lang={lang} text={text.replace(/\n$/, '')} />;
  },
  pre: ({ children }) => <>{children}</>,
};

export default function Markdown({ children }) {
  return (
    <div className="[overflow-wrap:anywhere] [&>:first-child]:mt-0 [&>:last-child]:mb-0">
      <ReactMarkdown remarkPlugins={[remarkGfm]} components={components}>{children}</ReactMarkdown>
    </div>
  );
}
