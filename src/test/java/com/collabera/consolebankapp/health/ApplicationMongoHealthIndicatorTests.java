package com.collabera.consolebankapp.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoDatabase;

class ApplicationMongoHealthIndicatorTests {

    @Test
    void reportsUpWhenApplicationDatabaseResponds() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        MongoDatabase mongoDatabase = mock(MongoDatabase.class);
        when(mongoTemplate.executeCommand(any(Document.class)))
                .thenReturn(new Document("ok", 1.0));
        when(mongoTemplate.getDb()).thenReturn(mongoDatabase);
        when(mongoDatabase.getName()).thenReturn("console_bank_app");

        Health health = new ApplicationMongoHealthIndicator(mongoTemplate).health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("console_bank_app", health.getDetails().get("database"));
    }

    @Test
    void reportsDownWhenApplicationDatabaseFails() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        MongoDatabase mongoDatabase = mock(MongoDatabase.class);
        when(mongoTemplate.executeCommand(any(Document.class)))
                .thenThrow(new IllegalStateException("database unavailable"));
        when(mongoTemplate.getDb()).thenReturn(mongoDatabase);
        when(mongoDatabase.getName()).thenReturn("console_bank_app");

        Health health = new ApplicationMongoHealthIndicator(mongoTemplate).health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("console_bank_app", health.getDetails().get("database"));
    }
}
