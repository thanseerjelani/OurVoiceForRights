#!/bin/bash

echo "🚀 MGNREGA Dashboard - Backend Setup"
echo "===================================="

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check prerequisites
echo -e "\n${YELLOW}Checking prerequisites...${NC}"

command -v java >/dev/null 2>&1 || { echo -e "${RED}Java 17+ is required but not installed.${NC}" >&2; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo -e "${RED}Maven is required but not installed.${NC}" >&2; exit 1; }
command -v docker >/dev/null 2>&1 || { echo -e "${RED}Docker is required but not installed.${NC}" >&2; exit 1; }

echo -e "${GREEN}✓ All prerequisites found${NC}"

# Create project structure
echo -e "\n${YELLOW}Creating project structure...${NC}"

mkdir -p src/main/java/com/mgnrega/dashboard/{config,controller,dto,entity,repository,service,scheduler,exception}
mkdir -p src/main/resources
mkdir -p src/test/java

echo -e "${GREEN}✓ Project structure created${NC}"

# Ask for database credentials
echo -e "\n${YELLOW}Database Configuration${NC}"
read -p "PostgreSQL username (default: postgres): " DB_USER
DB_USER=${DB_USER:-postgres}

read -sp "PostgreSQL password (default: postgres): " DB_PASS
DB_PASS=${DB_PASS:-postgres}
echo

read -p "Database name (default: mgnrega_db): " DB_NAME
DB_NAME=${DB_NAME:-mgnrega_db}

# Ask for MGNREGA API details
echo -e "\n${YELLOW}MGNREGA API Configuration${NC}"
echo "Get your API key from: https://data.gov.in"
read -p "API Key (press Enter to use default): " API_KEY
API_KEY=${API_KEY:-"579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b"}

read -p "Resource ID (press Enter to use default): " RESOURCE_ID
RESOURCE_ID=${RESOURCE_ID:-"mgnrega-data"}

# Update application.yml
echo -e "\n${YELLOW}Updating configuration...${NC}"

cat > src/main/resources/application.yml <<EOF
spring:
  application:
    name: mgnrega-dashboard

  datasource:
    url: jdbc:postgresql://localhost:5432/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASS}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  cache:
    type: simple
    cache-names: states, districts, performance

mgnrega:
  api:
    base-url: https://api.data.gov.in/resource
    resource-id: ${RESOURCE_ID}
    api-key: ${API_KEY}
    retry:
      max-attempts: 3
      delay: 2000

scheduler:
  data-refresh:
    cron: "0 0 2 * * ?"

server:
  port: 8080
  error:
    include-message: always

logging:
  level:
    root: INFO
    com.mgnrega.dashboard: DEBUG
EOF

echo -e "${GREEN}✓ Configuration updated${NC}"

# Setup database
echo -e "\n${YELLOW}Setting up database...${NC}"

if command -v psql >/dev/null 2>&1; then
    echo "Creating database: ${DB_NAME}"
    PGPASSWORD=${DB_PASS} psql -U ${DB_USER} -h localhost -c "CREATE DATABASE ${DB_NAME};" 2>/dev/null || echo "Database might already exist"
    echo -e "${GREEN}✓ Database setup complete${NC}"
else
    echo -e "${YELLOW}⚠ psql not found. Using Docker for PostgreSQL${NC}"
fi

# Choose setup method
echo -e "\n${YELLOW}Choose setup method:${NC}"
echo "1) Docker Compose (Recommended - includes PostgreSQL)"
echo "2) Local Maven (requires local PostgreSQL)"
read -p "Enter choice (1 or 2): " SETUP_CHOICE

if [ "$SETUP_CHOICE" = "1" ]; then
    echo -e "\n${YELLOW}Starting with Docker Compose...${NC}"
    docker-compose up -d
    echo -e "${GREEN}✓ Services started${NC}"
    echo -e "\n${GREEN}Backend is running at: http://localhost:8080${NC}"
    echo -e "Check logs with: docker-compose logs -f backend"
else
    echo -e "\n${YELLOW}Building with Maven...${NC}"
    mvn clean install -DskipTests
    echo -e "${GREEN}✓ Build complete${NC}"

    echo -e "\n${YELLOW}Starting application...${NC}"
    mvn spring-boot:run &

    echo -e "\n${GREEN}✓ Application started${NC}"
    echo -e "${GREEN}Backend is running at: http://localhost:8080${NC}"
fi

# Test the API
echo -e "\n${YELLOW}Waiting for API to be ready...${NC}"
sleep 10

if curl -s http://localhost:8080/api/health > /dev/null; then
    echo -e "${GREEN}✓ API is healthy!${NC}"
    echo -e "\n${GREEN}Setup Complete! 🎉${NC}"
    echo -e "\nTest the API:"
    echo -e "  curl http://localhost:8080/api/health"
    echo -e "  curl http://localhost:8080/api/states"
else
    echo -e "${YELLOW}⚠ API not responding yet. Check logs for details.${NC}"
fi

echo -e "\n${YELLOW}Next Steps:${NC}"
echo "1. Test the API endpoints"
echo "2. Trigger data sync: curl -X POST http://localhost:8080/api/sync"
echo "3. Proceed to frontend setup"
echo -e "\nFor detailed instructions, see README.md"