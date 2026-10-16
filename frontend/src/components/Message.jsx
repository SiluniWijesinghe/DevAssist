import { LogoMark } from './Logo.jsx';
import Markdown from './Markdown.jsx';
import Spinner from './ui/Spinner.jsx';

export function UserMessage({ children }) {
  return (
    <div className="mb-7 flex justify-end">
      <div className="max-w-[85%] rounded-[12px_12px_3px_12px] bg-soft px-3.5 py-2.5 break-words whitespace-pre-wrap">{children}</div>
    </div>
  );
}

function AssistantFrame({ children }) {
  return (
    <div className="mb-7">
      <div className="mb-2 flex items-center gap-2 text-[13px] font-semibold">
        <LogoMark size={20} /> DevAssist
      </div>
      {children}
    </div>
  );
}

export function AssistantMessage({ children }) {
  return (
    <AssistantFrame>
      <Markdown>{children}</Markdown>
    </AssistantFrame>
  );
}

export function WorkingIndicator() {
  return (
    <AssistantFrame>
      <p className="flex items-center gap-2 text-muted" role="status">
        <Spinner size={14} /> Searching the repository…
      </p>
    </AssistantFrame>
  );
}
