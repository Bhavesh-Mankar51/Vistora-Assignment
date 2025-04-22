# MySQL Database Schema Crawler

A Java-based Spring Boot application that crawls MySQL database schemas and generates model representations.

## Features

- Connects to MySQL databases using JDBC
- Extracts comprehensive database metadata including:
  - Tables and their properties
  - Columns with data types and constraints
  - Primary and foreign key relationships
  - Indexes and their properties
- Generates REST APIs for accessing schema information
- Configurable through JSON configuration file

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL Server 5.7 or higher

## Configuration

1. Update the `config/database.json` file with your database connection details:
```json
{
    "url": "jdbc:mysql://localhost:3306/your_database",
    "username": "your_username",
    "password": "your_password",
    "database": "your_database"
}
```

2. Configure additional settings in `src/main/resources/application.properties` if needed.

## Building and Running

1. Build the project:
```bash
mvn clean package
```

2. Run the application:
```bash
java -jar target/db-schema-crawler-1.0.0.jar
```

## API Endpoints

- `GET /api/schema/tables` - Retrieves the complete database schema including tables, columns, relationships, and indexes.

## Project Structure

```
src/main/java/com/vistora/
├── config/
│   └── DatabaseConfig.java
├── controller/
│   └── SchemaController.java
├── model/
│   ├── Column.java
│   ├── ForeignKey.java
│   ├── Index.java
│   └── Table.java
├── service/
│   └── SchemaCrawlerService.java
└── DbSchemaCrawlerApplication.java
```

## Example Response

The `/api/schema/tables` endpoint returns a JSON array of tables with their complete metadata. Here's an example response:

```json
[
  {
    "name": "users",
    "comment": "User information table",
    "columns": [
      {
        "name": "id",
        "type": "INT",
        "size": 11,
        "nullable": false,
        "primaryKey": true,
        "defaultValue": null,
        "comment": "Primary key"
      },
      {
        "name": "username",
        "type": "VARCHAR",
        "size": 50,
        "nullable": false,
        "primaryKey": false,
        "defaultValue": null,
        "comment": "User login name"
      }
    ],
    "foreignKeys": [
      {
        "name": "fk_user_address",
        "sourceTable": "users",
        "sourceColumn": "address_id",
        "targetTable": "addresses",
        "targetColumn": "id",
        "updateRule": "CASCADE",
        "deleteRule": "SET NULL"
      }
    ],
    "primaryKeys": ["id"],
    "indexes": [
      {
        "name": "idx_username",
        "tableName": "users",
        "columnNames": ["username"],
        "unique": true,
        "type": "BTREE"
      }
    ]
  }
]
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 