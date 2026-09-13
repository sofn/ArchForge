#!/bin/bash
# Install ArchForge Project Standard skill
SKILL_DIR="$HOME/.config/devin/skills/archforge-project-standard"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

mkdir -p "$SKILL_DIR"
ln -sf "$SCRIPT_DIR/SKILL.md" "$SKILL_DIR/SKILL.md"
ln -sf "$SCRIPT_DIR/standard.md" "$SKILL_DIR/standard.md"
echo "Installed archforge-project-standard skill to $SKILL_DIR"
