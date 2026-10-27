import { useEffect, useRef } from 'react';
import StarterPrompts from './StarterPrompts.jsx';
import { AssistantMessage, UserMessage, WorkingIndicator } from './Message.jsx';

export default function MessageList({ repoName, indexedAt, messages, sending, onPickStarter }) {
  const indexed = indexedAt ? Date.parse(indexedAt) : null;
  const endRef = useRef(null);
  useEffect(() => { endRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages.length, sending]);

  return (
    <div className="flex-1 overflow-y-auto px-3.5 pt-7 pb-2 md:px-7">
      <div className="mx-auto w-full max-w-[740px]">
        {messages.length === 0 && <StarterPrompts repoName={repoName} disabled={sending} onPick={onPickStarter} />}
        {messages.map((m, i) =>
          m.role === 'user'
            ? <UserMessage key={i}>{m.content}</UserMessage>
            : <AssistantMessage key={i}
              stale={Boolean(indexed && m.createdAt && Date.parse(m.createdAt) < indexed)}>{m.content}</AssistantMessage>
        )}
        {sending && <WorkingIndicator />}
        <div ref={endRef} />
      </div>
    </div>
  );
}
