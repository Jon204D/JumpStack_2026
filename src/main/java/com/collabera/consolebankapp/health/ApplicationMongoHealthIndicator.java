package com.collabera.consolebankapp.health;

import org.bson.Document;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component("applicationMongo")
public class ApplicationMongoHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;

    public ApplicationMongoHealthIndicator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Health health() {
        try {
            Document response = mongoTemplate.executeCommand(new Document("ping", 1));
            Number ok = response.get("ok", Number.class);

            if (ok != null && ok.doubleValue() == 1.0) {
                return Health.up()
                        .withDetail("database", mongoTemplate.getDb().getName())
                        .build();
            }

            return Health.down()
                    .withDetail("database", mongoTemplate.getDb().getName())
                    .withDetail("response", response)
                    .build();
        } catch (Exception exception) {
            return Health.down(exception)
                    .withDetail("database", mongoTemplate.getDb().getName())
                    .build();
        }
    }
}
