# MGNREGA Dashboard - Backend

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 13+ (or use Docker)
- Git

### Local Development Setup

1. **Clone the repository**
```bash
git clone <your-repo-url>
cd mgnrega-backend
```

2. **Configure Database**

Create a PostgreSQL database:
```sql
CREATE DATABASE mgnrega_db;
```

3. **Update Configuration**

Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mgnrega_db
    username: your_username
    password: your_password

mgnrega:
  api:
    api-key: YOUR_API_KEY_HERE
    resource-id: YOUR_RESOURCE_ID_HERE
```

4. **Build and Run**
```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080`

---

## 🐳 Docker Setup (Recommended)

1. **Build and Run with Docker Compose**
```bash
docker-compose up -d
```

This will:
- Start PostgreSQL on port 5432
- Start the backend on port 8080
- Initialize the database with sample data

2. **Check logs**
```bash
docker-compose logs -f backend
```

3. **Stop services**
```bash
docker-compose down
```

---

## 📡 API Endpoints

### Health Check
```
GET /api/health
```

### Get All States
```
GET /api/states
Response: List of states with ID and name
```

### Get Districts by State
```
GET /api/districts/{stateId}
Response: List of districts for the given state
```

### Get Performance Data
```
GET /api/performance/{districtId}
Response: Latest performance data for the district
```

### Get Comparison Data
```
GET /api/compare/{districtId}?year=2025
Response: Current vs previous month comparison
```

### Manual Data Sync
```
POST /api/sync
Response: Triggers data sync from MGNREGA API
```

---

## 🔧 Configuration

### MGNREGA API Setup

1. Get API key from [data.gov.in](https://data.gov.in)
2. Find the resource ID for MGNREGA data
3. Update in `application.yml`

### Scheduler Configuration

The data sync runs daily at 2 AM by default. Modify in `application.yml`:
```yaml
scheduler:
  data-refresh:
    cron: "0 0 2 * * ?"  # Every day at 2 AM
```

---

## 🗄️ Database Schema

### Tables
- `states`: Indian states
- `districts`: Districts within states
- `performance`: Monthly MGNREGA performance data

### Indexes
- `idx_state_id` on districts(state_id)
- `idx_district_year` on performance(district_id, year, month)
- `idx_timestamp` on performance(timestamp)

---

## 🚀 Deployment to Railway

1. **Install Railway CLI**
```bash
npm i -g @railway/cli
railway login
```

2. **Initialize Project**
```bash
railway init
```

3. **Add PostgreSQL**
```bash
railway add postgresql
```

4. **Deploy**
```bash
railway up
```

5. **Set Environment Variables**
```bash
railway variables set MGNREGA_API_KEY=your_key
railway variables set MGNREGA_RESOURCE_ID=your_id
```

6. **Get URL**
```bash
railway domain
```

---

## 📊 Testing

### Run Tests
```bash
mvn test
```

### Test Endpoints with curl
```bash
# Health check
curl http://localhost:8080/api/health

# Get states
curl http://localhost:8080/api/states

# Get districts for Bihar (assuming ID=1)
curl http://localhost:8080/api/districts/1

# Get performance for Patna (assuming ID=1)
curl http://localhost:8080/api/performance/1

# Trigger manual sync
curl -X POST http://localhost:8080/api/sync
```

---

## 🔍 Troubleshooting

### Database Connection Issues
- Check PostgreSQL is running: `pg_isready`
- Verify credentials in application.yml
- Check firewall settings

### API Not Responding
- Check logs: `docker-compose logs backend`
- Verify port 8080 is not in use
- Check CORS configuration

### Data Sync Failing
- Verify MGNREGA API key is valid
- Check internet connectivity
- Review logs for specific errors

---

## 📁 Project Structure

```
mgnrega-backend/
├── src/
│   ├── main/
│   │   ├── java/com/mgnrega/dashboard/
│   │   │   ├── MgnregaDashboardApplication.java
│   │   │   ├── config/
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/
│   │   │   │   └── MgnregaController.java
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   │   ├── State.java
│   │   │   │   ├── District.java
│   │   │   │   └── Performance.java
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   ├── scheduler/
│   │   │   └── exception/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── init.sql
└── pom.xml
```

---

## 📞 Support

For issues, please check:
- Application logs
- Database connectivity
- API key validity
- Network configuration