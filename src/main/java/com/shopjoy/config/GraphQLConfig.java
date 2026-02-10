package com.shopjoy.config;

import graphql.schema.*;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class GraphQLConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(GraphQLScalarType.newScalar()
                        .name("DateTime")
                        .description("Java LocalDateTime as scalar")
                        .coercing(new Coercing<LocalDateTime, String>() {
                            @Override
                            public String serialize(@NonNull Object dataFetcherResult) {
                                if (dataFetcherResult instanceof LocalDateTime) {
                                    return ((LocalDateTime) dataFetcherResult).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                }
                                return null;
                            }

                            @Override
                            public LocalDateTime parseValue(@NonNull Object input) {
                                if (input instanceof String) {
                                    return LocalDateTime.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                }
                                return null;
                            }

                            @Override
                            public LocalDateTime parseLiteral(@NonNull Object input) {
                                if (input instanceof String) {
                                    return LocalDateTime.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                }
                                return null;
                            }
                        })
                        .build());
    }
}

