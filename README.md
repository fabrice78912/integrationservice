# Integration Service

## Overview

The **Integration Service** is a **Spring Boot** application using **Apache Camel** to stream messages from a **single on-prem Kafka broker** to **AWS SQS**.

It ensures **manual commit of Kafka offsets** after successful delivery to SQS, supporting reliable migration from on-prem systems to the cloud.

---

## Features

* Stream messages from a **single Kafka broker** to AWS SQS
* Manual Kafka offset commit after successful SQS delivery
* Handles JSON messages
* Logging and polling configurable

---

## Prerequisites

* Java 17+
* Maven 3.8+
* Apache Camel 4.6.0
* Kafka broker running on-premise
* AWS account with SQS permissions
* AWS credentials configured (default credentials provider works)

---

## Configuration

`application.yaml` example:

```yaml
server:
  port: 8087

spring:
  application:
    name: integrationservice

kafka:
  brokers: localhost:9092
  topic: NOTIFICATION_TOPIC
  group-id: camel-group
  auto-offset-reset: earliest

aws:
  region: ca-central-1
  sqs:
    queue-name: myQueue

camel:
  springboot:
    name: kafka-to-sqs-camel
    main-run-controller: true

  component:
    kafka:
      brokers: ${kafka.brokers}
      groupId: ${kafka.group-id}
      autoOffsetReset: ${kafka.auto-offset-reset}

    aws2-sqs:
      region: ${aws.region}
      useDefaultCredentialsProvider: true

logging:
  level:
    root: INFO
    org.apache.camel: INFO
    org.apache.camel.component.kafka: DEBUG
    org.apache.kafka.clients.consumer: DEBUG
    org.apache.kafka: WARN
    software.amazon.awssdk: WARN
```

---

## Route Design

Single Kafka topic route example:

```java
from("kafka:{{kafka.topic}}"
        + "?brokers={{kafka.brokers}}"
        + "&groupId={{kafka.group-id}}"
        + "&autoOffsetReset=earliest"
        + "&consumersCount=1"
        + "&pollTimeoutMs=5000"
        + "&maxPollRecords=10"
        + "&allowManualCommit=true")
    .routeId("kafka-to-sqs")
    .log("Received from Kafka: ${body}")
    .to("aws2-sqs:{{aws.sqs.queue-name}}")
    .process(exchange -> {
        KafkaManualCommit manual = exchange.getIn().getHeader(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);
        if (manual != null) manual.commit();
    })
    .log("Message sent to SQS and commit performed");
```

* Manual commit ensures **messages are marked processed only after SQS delivery succeeds**.

---

## Running the Service

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/integrationservice-0.0.1-SNAPSHOT.jar
```

## Notes

* Only **one Kafka broker** needed
* Polling options (`pollTimeoutMs`, `maxPollRecords`) can be tuned for throughput
* Manual commit prevents message loss if SQS fails

---

## License

MIT License
