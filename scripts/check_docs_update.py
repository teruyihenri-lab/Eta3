#!/usr/bin/env python3
"""Kontrolli i dokumentacionit të Eta3. Vetëm biblioteka standarde."""
from pathlib import Path
import argparse
import subprocess
import sys

ROOT = Path("docs/project")
REQUIRED = ["AI_ACTION_LOG.md","AI_CHANGES.md","AI_CONTEXT.md","AI_DECISIONS.md","AI_GENERATED_CODE.md","AI_HANDOFF.md","AI_INSTRUCTIONS.md","AI_MEMORY.md","AI_PROMPTS.md","AI_RESPONSES.md","AI_SESSION_LOG.md","API_DOCUMENTATION.md","API_ENDPOINTS.md","AUTHENTICATION.md","AUTHORIZATION.md","BACKUP_RECOVERY.md","BUG_TRACKER.md","BUSINESS_LOGIC.md","CHANGELOG.md","CI_CD.md","CODING_STANDARDS.md","COMPLETED_TASKS.md","COMPONENTS.md","CONFIGURATION.md","CONTINUATION_GUIDE.md","CURRENT_STATUS.md","DATABASE_SCHEMA.md","DATA_MODELS.md","DECISIONS.md","DEPENDENCIES.md","DEPLOYMENT.md","DESIGN_PATTERNS.md","DEVELOPMENT_GUIDE.md","DEVELOPMENT_HISTORY.md","DOCUMENTATION_INDEX.md","ENVIRONMENT_VARIABLES.md","ERROR_HANDLING.md","FEATURES.md","FILE_STRUCTURE.md","IMPLEMENTATION_LOG.md","INFRASTRUCTURE.md","INSTALLATION.md","INTEGRATIONS.md","KNOWN_ISSUES.md","LESSONS_LEARNED.md","LOGGING.md","MAINTENANCE.md","MIGRATIONS.md","MILESTONES.md","MONITORING.md","NEXT_STEPS.md","OPTIMIZATION.md","PENDING_TASKS.md","PERFORMANCE.md","PROGRESS.md","PROJECT_ARCHITECTURE.md","PROJECT_OVERVIEW.md","PROJECT_REQUIREMENTS.md","PROJECT_SCOPE.md","PROJECT_STATE.md","PROJECT_STRUCTURE.md","README.md","RECOVERY_CONTEXT.md","REFACTORING.md","ROADMAP.md","SECURITY.md","SESSION_HISTORY.md","SETUP.md","SYSTEM_DESIGN.md","TASK_HISTORY.md","TECHNICAL_DEBT.md","TECH_STACK.md","TESTING.md","TEST_RESULTS.md","TODO.md","TROUBLESHOOTING.md","UI_UX_DESIGN.md","VERSION_HISTORY.md","WORKFLOWS.md"]
NEEDED = {"docs/project/AI_CHANGES.md", "docs/project/CURRENT_STATUS.md", "docs/project/NEXT_STEPS.md"}
SOURCE_PREFIX = ("app/", "scripts/", "gradle/", ".github/workflows/")
SOURCE_ROOT = {"build.gradle.kts", "settings.gradle.kts", "gradle.properties"}
EXCLUDED = {"scripts/check_docs_update.py", ".github/workflows/docs-guard.yml"}

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--check-files", action="store_true")
    parser.add_argument("--base")
    parser.add_argument("--head", default="HEAD")
    args=parser.parse_args()
    missing = [str(ROOT/n) for n in REQUIRED if not (ROOT/n).is_file()]
    if not Path("AGENTS.md").is_file(): missing.append("AGENTS.md")
    if missing:
        print("Mungojnë dokumentet: " + ", ".join(missing), file=sys.stderr)
        return 2
    print(f"OK: {len(REQUIRED)} dokumente dhe AGENTS.md")
    if args.base:
        try:
            changed = set(subprocess.check_output(["git", "diff", "--name-only", args.base, args.head], text=True).splitlines())
        except (OSError, subprocess.CalledProcessError) as e:
            print(f"Git diff dështoi: {e}", file=sys.stderr)
            return 3
        code = {p for p in changed if p not in EXCLUDED and (p.startswith(SOURCE_PREFIX) or p in SOURCE_ROOT)}
        if code and not NEEDED.issubset(changed):
            print("Ndryshim kodi pa AI_CHANGES, CURRENT_STATUS dhe NEXT_STEPS në të njëjtin ndryshim.", file=sys.stderr)
            return 4
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
