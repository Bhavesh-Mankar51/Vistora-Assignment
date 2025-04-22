package com.vistora.controller;

import com.vistora.model.Table;
import com.vistora.model.Column;
import com.vistora.service.SchemaCrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schema")
@Tag(name = "Database Schema", description = "API for accessing database schema information")
@Slf4j
public class SchemaController {

    private final SchemaCrawlerService schemaCrawlerService;

    public SchemaController(SchemaCrawlerService schemaCrawlerService) {
        this.schemaCrawlerService = schemaCrawlerService;
    }

    @GetMapping("/tables")
    @Operation(summary = "Get all tables", description = "Retrieves the complete database schema including all tables")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all tables")
    public ResponseEntity<List<Table>> getAllTables() {
        log.info("Request to get all tables");
        try {
            List<Table> tables = schemaCrawlerService.crawlSchema();
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            log.error("Error retrieving tables", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tables/{tableName}")
    @Operation(summary = "Get table by name", description = "Retrieves detailed information about a specific table")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved table information")
    @ApiResponse(responseCode = "404", description = "Table not found")
    public ResponseEntity<Table> getTable(
            @Parameter(description = "Name of the table to retrieve") 
            @PathVariable String tableName) {
        log.info("Request to get table: {}", tableName);
        try {
            Table table = schemaCrawlerService.crawlTable(tableName);
            return ResponseEntity.ok(table);
        } catch (SchemaCrawlerService.SchemaCrawlerException e) {
            log.error("Error retrieving table: {}", tableName, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error retrieving table: {}", tableName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tables/{tableName}/columns")
    @Operation(summary = "Get table columns", description = "Retrieves all columns for a specific table")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved table columns")
    @ApiResponse(responseCode = "404", description = "Table not found")
    public ResponseEntity<List<Column>> getTableColumns(
            @Parameter(description = "Name of the table") 
            @PathVariable String tableName) {
        log.info("Request to get columns for table: {}", tableName);
        try {
            Table table = schemaCrawlerService.crawlTable(tableName);
            return ResponseEntity.ok(table.getColumns());
        } catch (SchemaCrawlerService.SchemaCrawlerException e) {
            log.error("Error retrieving columns for table: {}", tableName, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error retrieving columns for table: {}", tableName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 