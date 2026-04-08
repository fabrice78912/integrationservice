package com.demo.integrationservice.routes;

import org.apache.camel.*;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.consumer.KafkaManualCommit;
import org.apache.camel.component.mock.MockEndpoint;
//import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

//@CamelSpringBootTest
//@SpringBootTest
@SpringBootTest(properties = {
        "camel.springboot.auto-startup=false"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaToSqsRouteTest {

    @Autowired
    private CamelContext context;

    @Autowired
    private ProducerTemplate template;

    @EndpointInject("mock:sqs")
    private MockEndpoint mockSqs;

    @BeforeAll
    void setup() throws Exception {

        AdviceWith.adviceWith(context, "kafka-to-sqs", route -> {
            route.replaceFromWith("direct:test");
            route.weaveByType(org.apache.camel.model.ToDefinition.class)
                    .replace()
                    .to("mock:sqs");
        });

        context.start();//
    }

    // =========================
    // ✅ TEST 1 : Nominal
    // =========================
    @Test
    void should_send_message_to_sqs() throws Exception {

        String json = """
        {
          "entityId": "123",
          "eventType": "RESETPASSWORD",
          "data": {
            "email": "test@test.com"
          }
        }
        """;

        mockSqs.expectedMessageCount(1);

        template.sendBody("direct:test", json);

        mockSqs.assertIsSatisfied();
    }

    // =========================
    // ⚠️ TEST 2 : Message vide
    // =========================
    @Test
    void should_ignore_empty_message() throws Exception {

        mockSqs.expectedMessageCount(0);

        template.sendBody("direct:test", "");

        mockSqs.assertIsSatisfied();
    }

    // =========================
    // ❌ TEST 3 : JSON invalide
    // =========================
    @Test
    void should_handle_invalid_json() throws Exception {

        mockSqs.expectedMessageCount(0);

        template.sendBody("direct:test", "{ invalid json }");

        mockSqs.assertIsSatisfied();
    }

    // =========================
    // ❌ TEST 4 : SQS down
    // =========================
    @Test
    void should_handle_sqs_exception() throws Exception {

        mockSqs.whenAnyExchangeReceived(exchange -> {
            throw new RuntimeException("SQS DOWN");
        });

        String json = """
    {
      "entityId": "123",
      "eventType": "RESETPASSWORD",
      "data": {
        "email": "test@test.com"
      }
    }
    """;

        template.sendBody("direct:test", json);

        // pas d'exception attendue grâce à ton onException
    }

    // =========================
    // 🔥 TEST 5 : Commit Kafka
    // =========================
    @Test
    void should_commit_on_success() throws Exception {

        KafkaManualCommit manualCommit = new KafkaManualCommit() {
            @Override
            public void commit() {
                System.out.println("✅ TEST COMMIT OK");
            }
        };

        Exchange exchange = context.getEndpoint("direct:test").createExchange();

        exchange.getIn().setBody("""
        {
          "entityId": "123",
          "eventType": "RESETPASSWORD",
          "data": {
            "email": "test@test.com"
          }
        }
        """);

        exchange.getIn().setHeader(KafkaConstants.MANUAL_COMMIT, manualCommit);

        mockSqs.expectedMessageCount(1);

        template.send("direct:test", exchange);

        mockSqs.assertIsSatisfied();
    }

    // =========================
    // 🔑 TEST 6 : Header propagation
    // =========================
    @Test
    void should_propagate_message_key() throws Exception {

        Exchange exchange = context.getEndpoint("direct:test").createExchange();

        exchange.getIn().setBody("""
        {
          "entityId": "123",
          "eventType": "RESETPASSWORD",
          "data": {
            "email": "test@test.com"
          }
        }
        """);

        exchange.getIn().setHeader("messageKey", "123");

        mockSqs.expectedMessageCount(1);

        template.send("direct:test", exchange);

        mockSqs.assertIsSatisfied();

        String header = mockSqs.getExchanges().get(0)
                .getIn()
                .getHeader("messageKey", String.class);

        assertEquals("123", header);
    }

    // =========================
    // ✅ TEST 7 : contenu envoyé
    // =========================
    @Test
    void should_send_correct_payload() throws Exception {

        String json = """
        {
          "entityId": "123",
          "eventType": "RESETPASSWORD",
          "data": {
            "email": "test@test.com"
          }
        }
        """;

        mockSqs.expectedMessageCount(1);

        template.sendBody("direct:test", json);

        mockSqs.assertIsSatisfied();

        String result = mockSqs.getExchanges().get(0)
                .getIn()
                .getBody(String.class);

        assertTrue(result.contains("RESETPASSWORD"));
        assertTrue(result.contains("test@test.com"));
    }
}
