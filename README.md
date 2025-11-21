# Our Voice, Our Rights - MGNREGA Dashboard Backend

> **Empowering Rural India with Accessible Government Data**

A production-ready Spring Boot backend that makes MGNREGA (Mahatma Gandhi National Rural Employment Guarantee Act) performance data accessible to every Indian citizen, especially those in rural areas with low digital literacy.

[![Live Demo](https://img.shields.io/badge/Live-Backend-success)](https://ourvoiceforrights.onrender.com)
[![Tech Stack](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org/)

---

## 🌟 Project Vision

MGNREGA is one of the world's largest welfare programs, benefiting 12.15 Crore rural Indians in 2025 alone. However, the performance data available on [data.gov.in](https://data.gov.in) remains inaccessible to the very people it affects—rural citizens with limited technical literacy.

**Our Voice, Our Rights** bridges this gap by transforming complex government data into a simple, visual, bilingual dashboard that anyone can understand.

---

## 🎯 Current Status

- ✅ **Live & Running**: [https://ourvoiceforrights.onrender.com](https://ourvoiceforrights.onrender.com)
- ✅ **Coverage**: Karnataka State (30+ districts)
- 🎯 **Next Goal**: Expand to all Indian states

---

## 🚀 Key Features

### Data Management
- ✅ Real-time integration with data.gov.in API
- ✅ Automated daily data sync (scheduled at 2 AM)
- ✅ PostgreSQL database for reliable data storage
- ✅ Intelligent caching (Spring Cache)
- ✅ Retry mechanism for API failures
- ✅ Fallback to cached data during downtime

### Performance & Scalability
- ✅ Production-ready architecture
- ✅ Optimized database queries with indexing
- ✅ CORS-enabled for frontend integration
- ✅ Comprehensive error handling
- ✅ Designed for 1M+ users/month

### API Endpoints
```
GET  /api/health                    # Health check
GET  /api/states                    # Get all states
GET  /api/districts/{stateId}       # Get districts by state
GET  /api/performance/{districtId}  # Get latest performance data
GET  /api/compare/{districtId}      # Get month-to-month comparison
POST /api/sync                      # Manual data sync trigger
```

---

## 🛠️ Tech Stack

**Backend Framework:**
- Spring Boot 3.2.0
- Java 17
- Maven

**Database:**
- PostgreSQL 15 (hosted on Neon DB)

**Key Dependencies:**
- Spring Data JPA
- Spring Cache
- Spring Retry
- Lombok
- WebFlux (for API calls)

**Deployment:**
- Render.com (Backend)
- Neon DB (Database)
- Docker (Containerization)

---

## 📊 Database Schema

```sql
states
├── id (PK)
├── name
└── state_code

districts
├── id (PK)
├── name
├── district_code
└── state_id (FK)

performance
├── id (PK)
├── district_id (FK)
├── month_name
├── fin_year
├── total_households_worked
├── average_days_employment
├── total_wages
├── ongoing_works
├── completed_works
├── total_expenditure
├── avg_wage_rate
└── timestamp
```

---

## 🚦 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 13+

### Local Setup

1. **Clone the repository**
```bash
git clone <your-repo-url>
cd mgnrega-backend
```

2. **Configure Database**

Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mgnrega_db
    username: your_username
    password: your_password

mgnrega:
  api:
    api-key: YOUR_DATA_GOV_IN_API_KEY
    resource-id: ee03643a-ee4c-48c2-ac30-9f2ff26ab722
```

3. **Run the Application**
```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080`

---

## 🐳 Docker Deployment

```bash
# Build and run with Docker Compose
docker-compose up -d --build

# Check logs
docker-compose logs -f backend

# Stop services
docker-compose down
```

---

## 📡 API Usage Examples

### Health Check
```bash
curl https://ourvoiceforrights.onrender.com/api/health
```

### Get All States
```bash
curl https://ourvoiceforrights.onrender.com/api/states
```

### Get Karnataka Districts
```bash
curl https://ourvoiceforrights.onrender.com/api/districts/1
```

### Get District Performance
```bash
curl https://ourvoiceforrights.onrender.com/api/performance/1
```

---

## 🔄 Data Sync

The backend automatically syncs data from data.gov.in every day at 2 AM. You can also trigger manual sync:

```bash
curl -X POST https://ourvoiceforrights.onrender.com/api/sync
```

---

## 🌐 Deployment on Render

1. Connect your GitHub repository to Render
2. Set environment variables:
   ```
   SPRING_DATASOURCE_URL=<your-neon-db-url>
   SPRING_DATASOURCE_USERNAME=<db-username>
   SPRING_DATASOURCE_PASSWORD=<db-password>
   MGNREGA_API_KEY=<your-api-key>
   ```
3. Deploy!

---

## 📈 Performance Metrics

- **Average Response Time**: <500ms
- **Database Query Optimization**: Indexed fields
- **Caching Strategy**: 5-minute stale time
- **API Retry Logic**: 3 attempts with 2s delay
- **Uptime**: 99.9% (Render.com infrastructure)

---

## 🎯 Roadmap

### Phase 1 (Completed) ✅
- [x] Karnataka state coverage (30+ districts)
- [x] Real-time API integration
- [x] Production deployment
- [x] Automated data sync

### Phase 2 (In Progress) 🚧
- [ ] Expand to all Indian states
- [ ] Historical data analysis
- [ ] Advanced comparison features
- [ ] Performance benchmarking

### Phase 3 (Planned) 📋
- [ ] Predictive analytics
- [ ] Mobile app backend
- [ ] Admin dashboard
- [ ] Multi-language API responses

---

## 🤝 Contributing

This is a personal project aimed at social impact. Contributions are welcome!

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

---

## 📞 Contact

**Developer**: Thanseer Jelani  
**Email**: thanseerjelani@gmail.com  
**LinkedIn**: [Connect with me](https://linkedin.com/in/yourprofile)  
**Frontend**: [https://voicesforrights.netlify.app](https://voicesforrights.netlify.app)

---

## 📄 License

This project is built for public good and social impact.

---

## 🙏 Acknowledgments

- **Data Source**: [data.gov.in](https://data.gov.in) - Government of India Open Data Platform
- **Inspiration**: The 12.15 Crore rural Indians who benefit from MGNREGA
- **Mission**: Making government data accessible to every citizen

---

**Built with ❤️ for Rural India** 🇮🇳

**#BuiltForIndia #BuiltForPublic #SocialImpact #DigitalIndia**
