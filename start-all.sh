#!/usr/bin/env bash
# Start the whole Banking-Microservices stack (Eureka first, then services).
# Requires: JDK 25, MongoDB running on 27017.
set -u
cd "$(dirname "$0")"
export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home}"
LOGDIR=/tmp
SERVICES=(auth-service customer-service account-service transaction-service wallet-service api-gateway)

wait_port() { # port name timeout
  local port=$1 name=$2 deadline=$((SECONDS + ${3:-150}))
  while (( SECONDS < deadline )); do
    if nc -z 127.0.0.1 "$port" 2>/dev/null; then echo "  $name UP on $port"; return 0; fi
    sleep 3
  done
  echo "  $name did NOT come up on $port (see $LOGDIR/run_*.log)"; return 1
}

echo "Starting eureka-server..."
nohup mvn -q -f eureka-server/pom.xml -DskipTests spring-boot:run >"$LOGDIR/run_eureka.log" 2>&1 &
wait_port 8761 eureka 150

echo "Starting services..."
for s in "${SERVICES[@]}"; do
  nohup mvn -q -f "$s/pom.xml" -DskipTests spring-boot:run >"$LOGDIR/run_$s.log" 2>&1 &
  echo "  launched $s"
done

echo "Waiting for services to bind..."
wait_port 8086 auth 200
wait_port 8081 customer 200
wait_port 9001 account 200
wait_port 8083 transaction 200
wait_port 8084 wallet 200
wait_port 8080 gateway 200

echo
echo "Swagger UI: http://localhost:8080/swagger-ui.html"
echo "Ports -> gateway:8080 eureka:8761 customer:8081 transaction:8083 wallet:8084 auth:8086 account:9001"
