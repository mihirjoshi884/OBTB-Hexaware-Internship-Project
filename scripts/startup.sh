#!/bin/bash

# 1. PATH CONFIGURATION
BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$BASE_DIR/logs/startup_logs"
BACKEND_ROOT="$BASE_DIR/backend/OBTB-HEXAWARE"
FRONTEND_ROOT="$BASE_DIR/frontend/obtb-app"
TIMESTAMP=$(date "+%Y-%m-%d %H:%M:%S")

# 2. MAIN CLASS DEFINITIONS
CONFIG_MAIN="org.hexaware.configserver.ConfigServerApplication"
OAUTH_MAIN="org.hexaware.oauthservice.OauthServiceApplication"
GATEWAY_MAIN="org.hexaware.apigateway.ApiGatewayApplication"
USER_MAIN="org.hexaware.userservice.UserServiceApplication"
NOTIF_MAIN="org.hexaware.notificationservice.NotificationServiceApplication"

start_service() {
    local service_folder=$1
    local main_class=$2
    local service_name=$3
    # FIX: Using 'tr' instead of ',,' for macOS compatibility
    local lower_name=$(echo "$service_name" | tr '[:upper:]' '[:lower:]')
    local log_file="$LOG_DIR/${lower_name}.log"
    
    echo "------------------------------------------" >> "$log_file"
    echo "🚀 SESSION START: $TIMESTAMP" >> "$log_file"
    echo "------------------------------------------" >> "$log_file"
    
    cd "$BACKEND_ROOT/$service_folder" && ../mvnw spring-boot:run -Dspring-boot.run.mainClass="$main_class" -Dmaven.test.skip=true >> "$log_file" 2>&1 &
}

wait_for_port() {
    local port=$1
    local name=$2
    echo "⏳ Waiting for $name (port $port)..."
    while ! lsof -i :$port > /dev/null; do 
        sleep 3
    done
    echo "✅ $name is HEALTHY!"
}

# 3. EXECUTION ORDER
echo "🚀 Starting OBTB Infrastructure..."

# Step A: Config Server
start_service "Config-Server" "$CONFIG_MAIN" "config-server"
wait_for_port 8083 "Config-Server"

# Step B: OAuth Service
start_service "OauthService" "$OAUTH_MAIN" "oauthservice"
wait_for_port 8081 "OauthService"

# Step C: API Gateway
start_service "API-GATEWAY" "$GATEWAY_MAIN" "api-gateway"
wait_for_port 9090 "API-GATEWAY"

# Step D: Parallel Services
echo "👥 Starting User and Notification Services..."
start_service "USER-SERVICE" "$USER_MAIN" "user-service"
start_service "NotificationService" "$NOTIF_MAIN" "notificationservice"

# Step E: Frontend
wait_for_port 8082 "USER-SERVICE"
echo "💻 Starting Angular Frontend..."
cd "$FRONTEND_ROOT" && npm start >> "$LOG_DIR/angular-app.log" 2>&1 &
wait_for_port 4200 "Frontend"

echo "---------------------------------------------------"
echo "✨ ALL SYSTEMS READY! Launching Control Room..."
echo "---------------------------------------------------"
sleep 2

# 4. MULTITAIL CONTROL ROOM
multitail -s 2 -sn 3,3 \
    -ci green  "$LOG_DIR/config-server.log" \
    -ci yellow "$LOG_DIR/oauthservice.log" \
    -ci blue   "$LOG_DIR/api-gateway.log" \
    -ci cyan   "$LOG_DIR/user-service.log" \
    -ci white  "$LOG_DIR/notificationservice.log" \
    -ci red    "$LOG_DIR/angular-app.log"
