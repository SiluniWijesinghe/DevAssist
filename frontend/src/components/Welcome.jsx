import { FileCode2, GitBranch, Github, Search } from 'lucide-react';
import { LogoMark } from './Logo.jsx';
import Button from './ui/Button.jsx';

// What the agent can actually do. These mirror its three backend tools.
const abilities = [
  [Search, 'Search code', 'Finds the files that match your question.'],
  [FileCode2, 'Read files', 'Opens them with line numbers, so answers cite real lines.'],
  [GitBranch, 'Inspect Git history', 'Looks up commits when you ask what changed and why.'],
];

export default function Welcome({ onConnect }) {
  return (
    <section className="m-auto w-full max-w-xl px-7 py-10">
      <LogoMark size={48} />
      <h1 className="mt-6 mb-3.5 text-[28px] leading-[1.15] font-semibold tracking-tight md:text-[34px]">
        Ask questions about any codebase.
      </h1>
      <p className="mb-6 text-[15px] text-muted">
        DevAssist clones a public GitHub repository, indexes its files and Git history, then investigates the code before it
        answers, so every response points to real files.
      </p>
      <Button onClick={onConnect}>
        <Github size={16} /> Connect a repository
      </Button>

      <ul className="mt-10 divide-y divide-line border-y border-line">
        {abilities.map(([Icon, title, text]) => (
          <li key={title} className="flex items-start gap-3 py-3.5">
            <Icon size={16} className="mt-0.5 shrink-0 text-accent" aria-hidden="true" />
            <div>
              <p className="font-medium">{title}</p>
              <p className="text-[13px] text-muted">{text}</p>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
