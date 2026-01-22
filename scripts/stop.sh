#!/bin/bash

# 1. Setup
BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
STOP_LOG="$BASE_DIR/logs/stop_logs/stop.log"
TIMESTAMP=$(date "+%Y-%m-%d %H:%M:%S")

echo "------------------------------------------" >> "$STOP_LOG"
echo "🛑 SHUTDOWN START: $TIMESTAMP" >> "$STOP_LOG"
echo "------------------------------------------" >> "$STOP_LOG"

# 2. Define OBTB Ports
# 8083:Config, 8081:OAuth, 9090:Gateway, 8082:User, 8084:Notif, 4200:Angular
PORTS=(8083 8081 9090 8082 8084 4200)

echo "🛑 Stopping OBTB Infrastructure..."

# 3. Kill by Port
for PORT in "${PORTS[@]}"
do
    PID=$(lsof -t -i:$PORT)
    if [ ! -z "$PID" ]; then
        echo "🧹 Port $PORT: Found PID $PID. Killing..." | tee -a "$STOP_LOG"
        kill -9 $PID 2>/dev/null
    else
        echo "✅ Port $PORT: Already clear." >> "$STOP_LOG"
    fi
done

# 4. Cleanup any lingering Maven processes (the 'parents' of the Java apps)
# This prevents the "bad substitution" or "process already running" errors later
STRAY_MVN=$(pgrep -f "mvnw")
if [ ! -z "$STRAY_MVN" ]; then
    echo "🧹 Cleaning up lingering Maven processes..." | tee -a "$STOP_LOG"
    pkill -f "mvnw"
fi

echo "✨ All services stopped and ports cleared."
echo "📝 Log saved to: logs/stop_logs/stop.log"
