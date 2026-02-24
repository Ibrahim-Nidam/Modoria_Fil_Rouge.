# Modoria E-Commerce Backend

A seasonal e-commerce platform backend built with Spring Boot 3.x and Java 25.

## Features

- **Seasonal Themes**: Auto-detection of seasons with dynamic theming
- **JWT Authentication**: Secure authentication with access and refresh tokens
- **Role-Based Access Control**: Admin, Customer, and Support roles
- **Product Management**: Full CRUD with categories, images, and seasonal products
- **Shopping Cart**: Persistent cart with real-time updates
- **Order Management**: Complete order lifecycle with status tracking
- **Reviews & Ratings**: Customer reviews with moderation
- **Real-time Chat**: WebSocket-based customer support
- **AI Integration**: Ollama-powered recommendations and chatbot
- **Payment Processing**: Stripe integration for payments
- **Redis Caching**: High-performance caching layer
- **HashiCorp Vault**: Secure secrets management

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 25 LTS |
| Framework | Spring Boot 3.4.1 |
| Database | PostgreSQL / H2 (dev) |
| Cache | Redis |
| Security | Spring Security + JWT |
| API | REST + GraphQL |
| Real-time | WebSocket (STOMP) |
| ORM | Spring Data JPA |
| Mapping | MapStruct |
| Migrations | Liquibase |
| Secrets | HashiCorp Vault |
| AI | Ollama |
| Payments | Stripe |
| Build | Maven |
| Container | Docker |

## Quick Start

### Prerequisites

- Java 25+
- Maven 3.9+
- Docker & Docker Compose

### Development Mode

1. Start backing services:
```bash
docker-compose -f docker-compose.dev.yml up -d
```

2. Run the application:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

3. Access the API at `http://localhost:8080`

### Production Mode

1. Build the application:
```bash
mvn clean package -DskipTests
```

2. Start all services:
```bash
docker-compose up -d
```

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev` |
| `JWT_SECRET` | JWT signing key | (generated) |
| `DB_HOST` | Database host | `localhost` |
| `DB_USERNAME` | Database user | `modoria` |
| `DB_PASSWORD` | Database password | `modoria` |
| `REDIS_HOST` | Redis host | `localhost` |
| `STRIPE_SECRET_KEY` | Stripe API key | - |
| `OLLAMA_BASE_URL` | Ollama API URL | `http://localhost:11434` |

### Application Profiles

- **dev**: H2 in-memory database, no Vault
- **qa**: PostgreSQL, Vault enabled
- **prod**: Full production configuration

## Project Structure

```
src/main/java/com/modoria/
├── config/          # Configuration classes
├── controller/      # REST & GraphQL controllers
├── domain/          # Entity classes
├── dto/             # Request/Response DTOs
├── exception/       # Custom exceptions
├── mapper/          # MapStruct mappers
├── repository/      # Spring Data repositories
├── security/        # JWT & Spring Security
├── service/         # Business logic
└── util/            # Utility classes
```

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/refresh` - Refresh token

### Products
- `GET /api/v1/products` - List products
- `GET /api/v1/products/{id}` - Get product
- `GET /api/v1/products/search?q=` - Search products
- `GET /api/v1/products/featured` - Featured products
- `POST /api/v1/products` - Create product (Admin)
- `PUT /api/v1/products/{id}` - Update product (Admin)

### Seasons
- `GET /api/v1/seasons/current` - Get current season
- `GET /api/v1/seasons` - List all seasons

### Cart
- `GET /api/v1/cart` - Get user cart
- `POST /api/v1/cart/items` - Add item
- `PUT /api/v1/cart/items/{productId}` - Update item
- `DELETE /api/v1/cart/items/{productId}` - Remove item

### Orders
- `POST /api/v1/orders` - Create order
- `GET /api/v1/orders` - List user orders
- `GET /api/v1/orders/{id}` - Get order details

## Database Schema

The application uses Liquibase for database migrations. Migrations are in:
`src/main/resources/db/changelog/migrations/`

## Docker

### Development
```bash
docker-compose -f docker-compose.dev.yml up -d
```

### Production
```bash
docker-compose up -d --build
```

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## License

MIT License
