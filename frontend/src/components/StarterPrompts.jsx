const starters = [
  ['Explain the architecture', 'Give me a high-level overview of this repository: main modules, entry points, and how they fit together.'],
  ['Trace a feature end to end', 'Trace one important user-facing feature from its entry point through each layer. Cite file paths.'],
  ['Review for risks', 'Review the repository for likely bugs, fragile areas, and maintainability risks. Ground each finding in specific files.'],
];

export default function StarterPrompts({ repoName, disabled, onPick }) {
  return (
    <div>
      <h2 className="mt-[6vh] mb-4 text-[22px] font-semibold tracking-tight">What do you want to know about {repoName}?</h2>
      <div className="divide-y divide-line border-y border-line">
        {starters.map(([title, prompt]) => (
          <button
            key={title}
            type="button"
            disabled={disabled}
            onClick={() => onPick(prompt)}
            className="flex w-full flex-col gap-0.5 px-1 py-3.5 text-left enabled:hover:bg-soft disabled:cursor-not-allowed disabled:opacity-50"
          >
            <strong className="font-semibold">{title}</strong>
            <span className="text-[13px] text-muted">{prompt}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
