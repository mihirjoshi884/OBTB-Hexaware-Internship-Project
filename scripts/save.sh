#!/bin/bash

# 1. SETUP
BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$BASE_DIR/logs/save_logs"
TIMESTAMP=$(date "+%Y-%m-%d %H:%M:%S")
HISTORY_LOG="$LOG_DIR/git_history.log"

cd "$BASE_DIR"

echo "------------------------------------------" >> "$HISTORY_LOG"
echo "💾 SAVE SESSION: $TIMESTAMP" >> "$HISTORY_LOG"
echo "------------------------------------------" >> "$HISTORY_LOG"

# 2. SHOW STATUS
echo "🔍 Current changes detected:"
git status -s
echo ""

# 3. COMMIT MESSAGE
echo "📝 Enter commit message (or press Enter for default):"
read msg
if [ -z "$msg" ]; then
    msg="OBTB Backup: $TIMESTAMP (Automated)"
fi

# 4. STAGING
echo "📦 Staging all files (including scripts and logs)..."
# We add .gitkeep files explicitly to ensure folder structure is preserved
find logs -name ".gitkeep" | xargs git add
git add .

# 5. COMMIT
echo "💾 Committing changes..."
git commit -m "$msg" | tee -a "$HISTORY_LOG"

# 6. PUSH OPTIONS
echo ""
echo "🚀 Where would you like to push?"
echo "1) Local Repository only"
echo "2) GitHub (Remote)"
echo "3) Both (Recommended)"
read -p "Selection [1-3]: " choice

case $choice in
    2|3)
        CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
        echo "☁️  Pushing to GitHub branch: $CURRENT_BRANCH..."
        git push origin "$CURRENT_BRANCH" 2>&1 | tee -a "$HISTORY_LOG"
        echo "✅ Push completed!"
        ;;
    *)
        echo "✅ Changes saved to local git history."
        ;;
esac

echo "📝 Log details saved to: logs/save_logs/git_history.log"
