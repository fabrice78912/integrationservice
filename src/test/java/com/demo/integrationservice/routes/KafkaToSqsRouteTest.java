package com.demo.integrationservice.routes;


import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.kafka.consumer.KafkaManualCommit;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Classe KafkaToSqsRouteTest
 *
 * @author Fabrice
 * @version 1.0
 * @since 2026-04-08
 */


@CamelSpringBootTest
@SpringBootTest
public class KafkaToSqsRouteTest {

    /*@Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate template;

    private MockEndpoint mockSqs;

    @BeforeEach
    public void setup() throws Exception {
        // Remplacer aws2-sqs:* par mock:result
        camelContext.getRouteDefinition("kafka-to-sqs")
                .adviceWith(camelContext, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        replaceFromWith("direct:start");
                        weaveByToUri("aws2-sqs:*").replace().to("mock:result");
                    }
                });

        mockSqs = camelContext.getEndpoint("mock:result", MockEndpoint.class);
        mockSqs.reset();
    }

    // 1️⃣ Message simple
    @Test
    void testMessageSimple() throws Exception {
        mockSqs.expectedMessageCount(1);
        mockSqs.expectedBodiesReceived("{\"eventType\":\"TEST\",\"data\":\"ok\"}");

        template.sendBody("direct:start", "{\"eventType\":\"TEST\",\"data\":\"ok\"}");

        mockSqs.assertIsSatisfied();
    }

    // 2️⃣ Commit manuel
    @Test
    void testCommitManuel() throws Exception {
        KafkaManualCommit manual = Mockito.mock(KafkaManualCommit.class);

        template.sendBodyAndHeader("direct:start",
                "{\"eventType\":\"TEST\",\"data\":\"ok\"}",
                "CamelKafkaManualCommit", manual);

        mockSqs.expectedMessageCount(1);
        mockSqs.assertIsSatisfied();

        Mockito.verify(manual).commit(); // Vérifie que commit a été appelé
    }

    // 3️⃣ Message mal formé
    @Test
    void testMessageMalForme() throws Exception {
        mockSqs.expectedMessageCount(0); // ne doit pas envoyer

        template.sendBody("direct:start", "MESSAGE_INVALIDE");

        mockSqs.assertIsSatisfied();
        // Vérifie log/error si besoin (optionnel)
    }

    // 4️⃣ Message vide
    @Test
    void testMessageVide() throws Exception {
        mockSqs.expectedMessageCount(0);

        template.sendBody("direct:start", "");

        mockSqs.assertIsSatisfied();
    }

    // 5️⃣ Multiple messages
    @Test
    void testMultipleMessages() throws Exception {
        int count = 5;
        mockSqs.expectedMessageCount(count);

        for (int i = 1; i <= count; i++) {
            template.sendBody("direct:start",
                    "{\"eventType\":\"TEST" + i + "\",\"data\":\"ok\"}");
        }

        mockSqs.assertIsSatisfied();
    }

    // 6️⃣ Exception SQS
    @Test
    void testExceptionSqs() throws Exception {
        // Remplacer mockSqs pour lever exception
        camelContext.getRouteDefinition("kafka-to-sqs")
                .adviceWith(camelContext, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        replaceFromWith("direct:start");
                        weaveByToUri("aws2-sqs:*").replace().throwException(new RuntimeException("SQS failed"));
                    }
                });

        KafkaManualCommit manual = Mockito.mock(KafkaManualCommit.class);

        try {
            template.sendBodyAndHeader("direct:start",
                    "{\"eventType\":\"TEST\",\"data\":\"ok\"}",
                    "CamelKafkaManualCommit", manual);
        } catch (Exception e) {
            // Exception attendue
        }

        Mockito.verify(manual, Mockito.never()).commit(); // commit ne doit pas être fait
    }

    // 7️⃣ Header absent
    @Test
    void testHeaderAbsent() throws Exception {
        mockSqs.expectedMessageCount(1);

        template.sendBody("direct:start", "{\"eventType\":\"TEST\",\"data\":\"ok\"}");

        mockSqs.assertIsSatisfied();
        // Pas de crash, même si header KafkaManualCommit absent
    }*/
}