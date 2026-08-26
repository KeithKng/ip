# AI assistant instructions

This file records key rules for assistants editing this repository. These are authoritative and must be followed.

- NEVER commit or push changes automatically. Any code or repository change proposed by the assistant must be presented to the user for review and explicit approval before running git commit, git tag, or git push.
- When asked to modify files, the assistant may create or edit files in the working tree but must not run git commands to record or publish those changes unless the user explicitly requests that the assistant perform the commit and push.
- Explain every proposed commit: include the rationale, affected files, and a suggested commit message. Ask for confirmation before committing.
- Use relative, OS-independent file paths (java.nio.file.Path or similar) when adding or modifying code that references the filesystem.
- Avoid changing unrelated files. Make precise, surgical edits and explain why they are needed.
- If the user requests automation of commits (e.g., in a CI workflow), request explicit confirmation and document the behavior clearly.

Following these rules ensures users retain full control over repository state and avoids unexpected or undesired commits.