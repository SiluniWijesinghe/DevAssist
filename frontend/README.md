# DevAssist frontend

React 18 + Vite + Tailwind CSS v4. Talks to the Spring Boot backend through `/api` (proxied to `localhost:8080` in dev, to `backend:8080` by nginx in Docker).

```bash
npm install
npm run dev      # http://localhost:5173
npm run build
```

## Structure

```
src/
  App.jsx                    composes the layout; owns no business logic
  index.css                  Tailwind import + design tokens (light/dark)
  hooks/
    useStored.js             useState persisted to localStorage
    useRepositories.js       connected repos, selected repo, connect/remove
    useChat.js               per-repo conversations, send/stop/retry
  lib/                       api calls, error text, GitHub URL + file-path helpers
  components/
    Logo.jsx                 LogoMark (the "D" git graph) and Logo (mark + wordmark)
    Sidebar.jsx  TopBar.jsx  ContextPanel.jsx
    Welcome.jsx              empty state
    Conversation.jsx         MessageList + Composer + error banner
    MessageList.jsx  Message.jsx  StarterPrompts.jsx  Composer.jsx
    ConnectRepoDialog.jsx
    Markdown.jsx             assistant answers, with copyable code blocks
    ui/                      Button, TextField, ErrorBanner, Spinner
```

## Theming

Colors are CSS variables in `src/index.css` (`--accent`, `--surface`, ...). They switch with the OS light/dark setting and are exposed to Tailwind as `bg-surface`, `text-muted`, `border-line`, `bg-accent`, and so on. Change the brand color by editing `--accent` and `--accent-soft` in both blocks, and the `fill`/`stroke` values in `public/favicon.svg`.
