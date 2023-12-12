package com.pgvector;

import java.sql.SQLException;
import io.r2dbc.postgresql.codec.Vector;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Statement;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class R2DBCTest {
    @Test
    void example() throws SQLException {
        ConnectionFactory connectionFactory = ConnectionFactories.get(
            ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "postgresql")
                .option(ConnectionFactoryOptions.HOST, "localhost")
                .option(ConnectionFactoryOptions.USER, System.getenv("USER"))
                .option(ConnectionFactoryOptions.PASSWORD, "")
                .option(ConnectionFactoryOptions.DATABASE, "pgvector_java_test")
                .build()
        );

        Mono.from(connectionFactory.create())
            .flatMapMany(connection -> connection
                .createStatement("SELECT $1 AS embedding")
                .bind("$1", Vector.of(1, 2, 3))
                .execute())
            .flatMap(result -> result
                .map((row, rowMetadata) -> row.get("embedding", Vector.class)))
            .doOnNext(System.out::println)
            .blockLast();
    }
}
