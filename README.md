# MySQL Database Schema Crawler

A Spring Boot application that crawls MySQL schemas and provides REST APIs for database metadata.

## Core Idea & Logic

1. **Schema Discovery**
   - Queries MySQL's INFORMATION_SCHEMA to extract database structure
   - Identifies tables, columns, relationships, and indexes
   - Maps database metadata to Java objects

2. **Performance Optimization**
   - Uses HikariCP for efficient connection pooling
   - Implements two-level caching (Spring Cache + ConcurrentHashMap)
   - Optimizes SQL queries for metadata retrieval

3. **Data Organization**
   - Models database structure as Java objects (Table, Column, ForeignKey, Index)
   - Maintains relationships between database entities
   - Provides structured JSON responses

4. **API Design**
   - RESTful endpoints for schema access
   - Hierarchical URL structure (/tables/{tableName}/columns)
   - Proper HTTP status codes and error handling

5. **Error Management**
   - Custom exception handling for database operations
   - Transaction management for data consistency
   - Detailed error messages for debugging

## Quick Start

```bash
mvn clean package
java -jar target/db-schema-crawler-1.0.0.jar
```

## API Map

| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/schema/tables | GET | All tables with metadata |
| /api/schema/tables/{tableName} | GET | Specific table details |
| /api/schema/tables/{tableName}/columns | GET | Table columns |

## Features Map

| Category | Features |
|----------|----------|
| Schema Analysis | Tables, Columns, Keys, Indexes |
| Performance | HikariCP, Spring Cache, ConcurrentHashMap |
| Error Handling | Custom exceptions, HTTP codes, Rollbacks |
| Documentation | Swagger/OpenAPI |

## Project Map

```
src/main/java/com/vistora/
├── config/      # DatabaseConfig
├── controller/  # SchemaController
├── model/       # Table, Column, ForeignKey, Index
├── service/     # SchemaCrawlerService
└── Application
```

## Example Response

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

## Requirements

- Java 17+
- Maven 3.6+
- MySQL 5.7+
- Spring Boot 3.2.3

## License

MIT License 