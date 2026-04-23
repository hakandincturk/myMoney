package com.hakandincturk.myMoney.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import com.hakandincturk.starter.MyMoneyApplication;

@SpringBootTest(classes = MyMoneyApplication.class)
@ActiveProfiles("integration")
public abstract class BaseIntegrationTest {

  static final GenericContainer<?> POSTGRES =
      new GenericContainer<>("postgres:17-alpine")
          .withExposedPorts(5432)
          .withEnv("POSTGRES_DB", "mymoney_test")
          .withEnv("POSTGRES_USER", "test")
          .withEnv("POSTGRES_PASSWORD", "test")
          .waitingFor(Wait.forListeningPort());

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    String jdbcUrl = "jdbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getMappedPort(5432) + "/mymoney_test";
    registry.add("spring.datasource.url", () -> jdbcUrl);
    registry.add("spring.datasource.username", () -> "test");
    registry.add("spring.datasource.password", () -> "test");
  }
}
