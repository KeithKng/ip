---
name: test-ui
description: Run the command-line UI test cases defined in test/ui-test-plan.md after application changes.
---

# Test UI

Use this skill after updating the project's command-line application. The test plan at
`test/ui-test-plan.md` is the source of truth for the test cases and how to run the
program.

1. Update the plan first if the change adds or alters observable command behavior.
   Each test case must state its aim and provide one or more command/expected-output
   pairs.
2. Compile the application with Java 25, following the project instructions.
3. Run the plan from the repository root:

   ```powershell
   .\.codex\skills\test-ui\scripts\run-ui-tests.ps1
   ```

   Supply `-RunCommand` only when the plan's default command needs to be overridden.
4. Preserve the runner's console-session record in the response. On the first failed
   command it stops the process and prints both the expected and actual output; do not
   continue with later test cases.

The runner compares the response body for each command. The transcript it prints
includes the application's divider lines, so the complete console exchange remains
visible without repeating those fixed lines in every expected-output block.
