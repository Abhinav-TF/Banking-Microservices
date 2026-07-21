#!/usr/bin/env bash
# Stop the whole Banking-Microservices stack (services + Eureka). Leaves MongoDB alone.
for port in 8080 8081 8083 8084 8086 9001 8761; do
  pids=$(lsof -tiTCP:$port -sTCP:LISTEN 2>/dev/null)
  [ -n "$pids" ] && { echo "stopping :$port -> $pids"; kill $pids 2>/dev/null; }
done
pkill -f "spring-boot:run" 2>/dev/null
sleep 2
echo "done."
