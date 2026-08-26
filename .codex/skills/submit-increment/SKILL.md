---
name: submit-increment
description: Safely submit a branch-based assignment increment with explicit approval before every git step, including branch creation, commit, merge, tagging, and push.
---

# Submit increment

Use this skill when the user wants to submit the next assignment increment in a Git workflow.

Follow these rules exactly:

- Always ask for explicit permission before running any repository-changing command.
- Before each command, show the exact command(s) you want to run and briefly explain what they do.
- Wait for the user to approve before executing any command.
- If the tag name is not clearly specified, ask before creating or using a tag.
- For this assignment, the usual tag is `Level-7` unless the user specifies a different tag name.
- Only do a merge commit with `--no-ff`.
- Do not delete the merged feature branch after merging.
- Push both the merged feature branch and the updated `master` branch to the fork.

## Recommended workflow

1. Check the repository state and current branch:

   ```powershell
   git status --short --branch
   ```

   Ask before running it.

2. Create or switch to the feature branch, if needed:

   ```powershell
   git switch -c branch-Level-7
   ```

   If the branch already exists, use:

   ```powershell
   git switch branch-Level-7
   ```

   Ask before running either command.

3. Review the work and create a commit only after approval:

   ```powershell
   git add .
   git commit -m "<clear commit message>"
   ```

4. Return to `master` and merge with a merge commit:

   ```powershell
   git switch master
   git merge --no-ff branch-Level-7 -m "Merge branch-Level-7 into master"
   ```

5. Add the tag only after the user confirms the tag name. If no tag is specified, ask whether to use `Level-7`:

   ```powershell
   git tag Level-7
   ```

   If the user gives a different tag name, replace it as requested.

6. Push the branch, the updated `master` branch, and the tag to the fork:

   ```powershell
   git push origin master
   git push origin branch-Level-7
   git push origin Level-7
   ```

   Ask before running this step.

7. Confirm the final state:

   ```powershell
   git status --short --branch
   git tag --list
   ```

## Default decision for tag naming

If the user is unsure which tag name to use, ask a clarifying question instead of guessing. For this Level 7 task, the default tag name is `Level-7` only after the user confirms it.

## Important note

This workflow is intentionally conservative: do not run any Git step until the user explicitly approves the exact command and the command's purpose is clear.
