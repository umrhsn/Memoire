# Token Efficiency

- Start responses with the result or action; avoid conversational filler.
- Inspect only files relevant to the requested change; prefer targeted searches.
- Modify existing files with focused diffs; do not rewrite unrelated code.
- Execute requested work only. Do not add dependencies, refactors, or unrelated features without confirmation.
- On a failed command, make at most one targeted retry. If it still fails, report the blocker.
- For completion audits, inspect the relevant status/plan, changed files, version-control diff, and required verification only.
