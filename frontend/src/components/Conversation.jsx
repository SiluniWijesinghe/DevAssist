import { useState } from 'react';
import MessageList from './MessageList.jsx';
import Composer from './Composer.jsx';
import ErrorBanner from './ui/ErrorBanner.jsx';

export default function Conversation({ repo, chat }) {
  const [draft, setDraft] = useState('');

  function send(text = draft) {
    if (!text.trim() || chat.sending) return;
    setDraft('');
    chat.send(text);
  }

  return (
    <>
      <MessageList repoName={repo.name} messages={chat.messages} sending={chat.sending} onPickStarter={send} />
      <div>
        {chat.error && (
          <div className="mx-auto w-full max-w-[740px] px-3.5 md:px-7">
            <ErrorBanner message={chat.error} onRetry={chat.canRetry ? chat.retry : undefined} />
          </div>
        )}
        <Composer repoName={repo.name} value={draft} onChange={setDraft} sending={chat.sending} onSend={() => send()} onStop={chat.stop} />
      </div>
    </>
  );
}
