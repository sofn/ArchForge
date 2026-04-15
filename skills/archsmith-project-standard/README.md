# archsmith-project-standard

A Devin skill that codifies the ArchSmith project standards — tech stack, module structure, code conventions, configuration, testing, and deployment patterns.

## What's Included

| File | Purpose |
|------|---------|
| `SKILL.md` | Skill metadata — tells Devin when and how to use this skill |
| `standard.md` | The comprehensive project standard document |
| `install.sh` | One-command installer that symlinks the skill into Devin's config |
| `README.md` | This file |

## Install

```bash
cd skills/archsmith-project-standard
./install.sh
```

This creates symlinks in `~/.config/devin/skills/archsmith-project-standard/` pointing back to this directory, so edits here are immediately reflected.

## Usage

Once installed, Devin can reference this skill when:

- **Starting a new service** — ensures the module layout, build config, and Docker setup follow the standard from day one.
- **Auditing an existing project** — compare current structure against the checklist in `standard.md` Section 8.
- **Onboarding** — point new team members to `standard.md` for a single-document overview of all conventions.

### Quick Reference

```
# Check code formatting
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply

# Full build (format check + compile + test)
./gradlew build

# Run the admin server locally (dev profile, Testcontainers)
./gradlew server-admin:bootRun

# Docker production build
./gradlew :server-admin:bootJar -x test
cd docker && ./start.sh jvm
```

## Updating the Standard

Edit `standard.md` directly. Changes take effect immediately since the install uses symlinks. After significant updates, consider bumping version notes in the document header and notifying the team.
