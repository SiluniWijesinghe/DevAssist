import { useState } from 'react';
import Sidebar from './components/Sidebar.jsx';
import TopBar from './components/TopBar.jsx';
import Welcome from './components/Welcome.jsx';
import Conversation from './components/Conversation.jsx';
import ContextPanel from './components/ContextPanel.jsx';
import ConnectRepoDialog from './components/ConnectRepoDialog.jsx';
import { useRepositories } from './hooks/useRepositories.js';
import { useChat } from './hooks/useChat.js';
import { extractFilePaths } from './lib/files.js';
import { cx } from './lib/cx.js';

export default function App() {
  const { repos, repo, activeId, select, connect, remove } = useRepositories();
  const chat = useChat(repo);
  const [dialogOpen, setDialogOpen] = useState(false);

  const openDialog = () => setDialogOpen(true);
  const closeDialog = () => setDialogOpen(false);

  async function connectRepository(url, name) {
    await connect(url, name);
    chat.clearError();
  }

  function removeRepository(id) {
    remove(id);
    chat.discard(id);
  }

  return (
    <div
      // The two-column and three-column ranges must not overlap, or the later CSS rule wins at both widths.
      className={cx(
        'grid h-dvh grid-cols-1',
        repo
          ? 'md:max-[1099px]:grid-cols-[260px_minmax(0,1fr)] min-[1100px]:grid-cols-[260px_minmax(0,1fr)_280px]'
          : 'md:grid-cols-[260px_minmax(0,1fr)]',
      )}
    >
      <Sidebar repos={repos} activeId={activeId} onSelect={select} onRemove={removeRepository} onAdd={openDialog} />

      <main className="flex h-dvh min-w-0 flex-col">
        <TopBar
          repos={repos}
          repo={repo}
          activeId={activeId}
          onSelect={select}
          onAdd={openDialog}
          canClear={Boolean(repo) && chat.messages.length > 0}
          onClear={chat.clear}
        />
        {repo ? <Conversation repo={repo} chat={chat} /> : <Welcome onConnect={openDialog} />}
      </main>

      {repo && <ContextPanel repo={repo} messageCount={chat.messages.length} files={extractFilePaths(chat.messages)} />}

      {dialogOpen && <ConnectRepoDialog onClose={closeDialog} onConnect={connectRepository} />}
    </div>
  );
}
