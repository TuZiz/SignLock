# Summary 03-01: Repair Runtime Resources and Operator Feedback

## What Changed

- refreshed the runtime-facing lock usage hint to match the current `/bl add` and `/bl remove` workflow
- made plugin startup, shutdown, and reload logs clearer for Chinese operators
- kept the shipped `config.yml` as a readable UTF-8 Chinese default with lightweight operator comments

## Key Files

- `src/main/java/ym/signLock/SignLock.java`
- `src/main/java/ym/signLock/config/SignLockConfig.java`
- `src/main/resources/config.yml`

## Verification

- `./gradlew.bat test --tests "ym.signLock.command.*" --tests "ym.signLock.service.*"`
- Result: `BUILD SUCCESSFUL`

## Notes

- `plugin.yml` already had the required metadata and Chinese wording, so this plan did not rewrite it unnecessarily.
- Existing config keys and lock semantics were preserved.

---

*Plan: 03-01*
