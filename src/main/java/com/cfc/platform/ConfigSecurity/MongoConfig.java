package com.cfc.platform.ConfigSecurity;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import jakarta.annotation.PostConstruct;

@Configuration
public class MongoConfig {

    private final MappingMongoConverter mappingMongoConverter;

    public MongoConfig(MappingMongoConverter mappingMongoConverter) {
        this.mappingMongoConverter = mappingMongoConverter;
    }

    @PostConstruct
    public void setUpConverter() {
        // Tells Spring to replace dots in map keys with "__dot__" during serialization
        mappingMongoConverter.setMapKeyDotReplacement("__dot__");
    }
}