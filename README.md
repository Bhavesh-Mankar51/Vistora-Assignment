# MySQL Database Schema Crawler

A robust Java-based Spring Boot application that crawls MySQL database schemas and provides a RESTful API for accessing database metadata.

## Features

- **Comprehensive Schema Analysis**
  - Tables and their properties (name, comment, row count)
  - Columns with detailed metadata (type, size, precision, scale, nullability, defaults)
  - Primary and foreign key relationships
  - Indexes and their properties
  - Table comments and row counts

- **Performance Optimizations**
  - Connection pooling with HikariCP
  - Caching with Spring Cache
  - Thread-safe caching with ConcurrentHashMap
  - Transaction management
  - Optimized SQL queries

- **Robust Error Handling**
  - Custom exception handling
  - Proper HTTP status codes
  - Detailed error messages
  - Transaction rollback on errors

- **API Documentation**
  - Swagger/OpenAPI documentation
  - Clear endpoint descriptions
  - Response codes and examples

## Technical Implementation

### Database Connection
- Uses HikariCP for connection pooling
- Configurable connection settings
- Automatic connection management
- Transaction support

### Schema Crawling Logic
1. **Table Discovery**
   - Queries INFORMATION_SCHEMA.TABLES
   - Filters for base tables only
   - Orders results alphabetically

2. **Column Analysis**
   - Retrieves comprehensive column metadata
   - Handles different data types
   - Captures constraints and defaults
   - Tracks auto-increment status

3. **Relationship Mapping**
   - Identifies primary keys
   - Maps foreign key relationships
   - Captures referential integrity rules
   - Maintains relationship hierarchy

4. **Index Analysis**
   - Identifies all indexes
   - Captures index type and uniqueness
   - Maps index columns
   - Orders by index name and sequence

### Caching Strategy
- Two-level caching:
  1. Spring Cache for method-level caching
  2. ConcurrentHashMap for in-memory caching
- Cache invalidation on schema changes
- Thread-safe cache operations

### API Endpoints

#### GET /api/schema/tables
- Retrieves complete database schema
- Returns all tables with their metadata
- Cached for performance
- Includes:
  - Table names and comments
  - Column definitions
  - Primary and foreign keys
  - Indexes
  - Row counts

#### GET /api/schema/tables/{tableName}
- Retrieves specific table details
- Returns comprehensive table metadata
- Cached per table
- Includes all table properties

#### GET /api/schema/tables/{tableName}/columns
- Retrieves columns for a specific table
- Returns detailed column information
- Cached with table data
- Includes column properties and constraints

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL Server 5.7 or higher
- Spring Boot 3.2.3

## Configuration

1. Database Configuration (`application.properties`):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database?createDatabaseIfNotExist=true
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Connection Pool Settings
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.max-lifetime=1800000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

2. Logging Configuration:
```properties
logging.level.org.springframework=INFO
logging.level.com.vistora=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## Building and Running

1. Build the project:
```bash
mvn clean package
```

2. Run the application:
```bash
java -jar target/db-schema-crawler-1.0.0.jar
```

## Project Structure

```
src/main/java/com/vistora/
├── config/
│   └── DatabaseConfig.java      # Database and connection pool configuration
├── controller/
│   └── SchemaController.java    # REST API endpoints
├── model/
│   ├── Column.java             # Column metadata model
│   ├── ForeignKey.java         # Foreign key relationship model
│   ├── Index.java              # Index metadata model
│   └── Table.java              # Table metadata model
├── service/
│   └── SchemaCrawlerService.java # Schema crawling logic
└── DbSchemaCrawlerApplication.java
```

## Error Handling

The application implements comprehensive error handling:

1. **Database Errors**
   - Connection failures
   - Query execution errors
   - Transaction rollbacks

2. **API Errors**
   - 404 for not found resources
   - 500 for server errors
   - Detailed error messages

3. **Validation**
   - Input validation
   - Data integrity checks
   - Constraint validation

## Performance Considerations

1. **Connection Pooling**
   - Configurable pool size
   - Connection timeouts
   - Idle connection management

2. **Caching**
   - Method-level caching
   - In-memory caching
   - Cache invalidation

3. **Query Optimization**
   - Efficient SQL queries
   - Proper indexing
   - Batch operations

## Security

- Secure database credentials
- Input validation
- SQL injection prevention
- Proper error handling

## Monitoring

- Spring Boot Actuator endpoints
- Detailed logging
- Performance metrics
- Health checks

## Example API Response

```json
{
  "name": "users",
  "comment": "Stores user information",
  "rowCount": 1000,
  "columns": [
    {
      "name": "id",
      "type": "INT",
      "size": 11,
      "precision": 0,
      "scale": 0,
      "nullable": false,
      "primaryKey": true,
      "autoIncrement": true,
      "defaultValue": null,
      "comment": "Primary key"
    }
  ],
  "primaryKeys": ["id"],
  "foreignKeys": [],
  "indexes": [
    {
      "name": "PRIMARY",
      "tableName": "users",
      "columnNames": ["id"],
      "unique": true,
      "type": "BTREE"
    }
  ]
}
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 