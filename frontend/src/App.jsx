import { useEffect, useState } from 'react';
import AuthScreen from './components/AuthScreen.jsx';
import { clearToken, getToken, logout as apiLogout } from './lib/api.js';
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

function clearLocalUserData() {
  localStorage.removeItem('devassist.repos');
  localStorage.removeItem('devassist.active');
}

export default function App() {
  const [authenticated, setAuthenticated] = useState(Boolean(getToken()));

  useEffect(() => {
    const unauthorized = () => {
      clearToken(); clearLocalUserData(); setAuthenticated(false);
    };
    window.addEventListener('devassist:unauthorized', unauthorized);
    return () => window.removeEventListener('devassist:unauthorized', unauthorized);
  }, []);

  if (!authenticated) {
    return <AuthScreen onAuthenticated={() => window.location.reload()} />;
  }

  return <AuthenticatedApp onLogout={() => setAuthenticated(false)} />;
}

function AuthenticatedApp({ onLogout }) {
  const { repos, repo, activeId, select, connect, remove } = useRepositories();
  const chat = useChat(repo);
  const [dialogOpen, setDialogOpen] = useState(false);

  const openDialog = () => setDialogOpen(true);
  const closeDialog = () => setDialogOpen(false);

  async function connectRepository(url, name) {
    await connect(url, name);
    chat.clearError();
  }

  async function removeRepository(id) {
    try { await remove(id); chat.discard(id); }
    catch (error) { window.alert(error.message || 'Could not remove repository.'); }
  }

  async function handleLogout() {
    try { await apiLogout(); } catch { clearToken(); }
    clearLocalUserData();
    onLogout();
  }

  return (
    <div className={cx(
      'grid h-dvh grid-cols-1',
      repo
        ? 'md:max-[1099px]:grid-cols-[260px_minmax(0,1fr)] min-[1100px]:grid-cols-[260px_minmax(0,1fr)_280px]'
        : 'md:grid-cols-[260px_minmax(0,1fr)]',
    )}>
      <Sidebar repos={repos} activeId={activeId} onSelect={select} onRemove={removeRepository} onAdd={openDialog} />
      <main className="flex h-dvh min-w-0 flex-col">
        <TopBar repos={repos} repo={repo} activeId={activeId} onSelect={select} onAdd={openDialog}
          canClear={Boolean(repo) && chat.messages.length > 0} onClear={chat.clear} onLogout={handleLogout} />
        {repo ? <Conversation repo={repo} chat={chat} /> : <Welcome onConnect={openDialog} />}
      </main>
      {repo && <ContextPanel repo={repo} messageCount={chat.messages.length} files={extractFilePaths(chat.messages)} />}
      {dialogOpen && <ConnectRepoDialog onClose={closeDialog} onConnect={connectRepository} />}
    </div>
  );
}
