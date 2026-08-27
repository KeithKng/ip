# AI assistant instructions

This file records key rules for assistants editing this repository. These are authoritative and must be followed.

- NEVER commit or push changes automatically. Any code or repository change proposed by the assistant must be presented to the user for review and explicit approval before running git commit, git tag, or git push.
- NEVER run the present-changes-visually skill under any circumstance.
- When asked to modify files, the assistant may create or edit files in the working tree but must not run git commands to record or publish those changes unless the user explicitly requests that the assistant perform the commit and push.
- Explain every proposed commit: include the rationale, affected files, and a suggested commit message. Ask for confirmation before committing.
- After every code or documentation change, always provide a suggested commit message in the repository's preferred style: imperative mood, concise subject line (ideally under 50 characters, hard limit 72), optional scope or category prefix, no trailing period, and a brief body explaining the rationale. This applies even when no git commit is being created in the current session. Do not commit or push unless the user explicitly asks for it.
- Maintain JUnit coverage for the highest-value approximately 50% of methods, prioritising complex, core, and critical business logic. Update the relevant JUnit tests after every code change so this coverage target remains satisfied.
- Use relative, OS-independent file paths (java.nio.file.Path or similar) when adding or modifying code that references the filesystem.
- Avoid changing unrelated files. Make precise, surgical edits and explain why they are needed.
- If the user requests automation of commits (e.g., in a CI workflow), request explicit confirmation and document the behavior clearly.
- All Java code added or modified in this project must follow the checklist in the `seedu-java-coding-standard` skill (`.codex/skills/seedu-java-coding-standard/SKILL.md`).

Following these rules ensures users retain full control over repository state and avoids unexpected or undesired commits.
