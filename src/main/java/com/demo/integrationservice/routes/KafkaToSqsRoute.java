package com.demo.integrationservice.routes;

import com.demo.integrationservice.payload.Event;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.consumer.KafkaManualCommit;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

/**
 * Classe KafkaToSqsRoute
 *
 * @author Fabrice
 * @version 1.0
 * @since 2026-04-08
 */
@Component
public class KafkaToSqsRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        //Gestion erreur SQS
        onException(software.amazon.awssdk.services.sqs.model.SqsException.class)
                .handled(true)
                .log("Erreur SQS: ${exception.message}")
                .log("Message en erreur: ${body}")
                .process(exchange -> {
                    System.out.println("Commit NON effectué (échec SQS)");
                });

        //Gestion erreur JSON (parsing)
        onException(Exception.class)
                .handled(true)
                .log("Erreur parsing JSON: ${exception.message}")
                .log("Message invalide: ${body}");

        from("kafka:{{kafka.topic}}"
                + "?brokers={{kafka.brokers}}"
                + "&groupId={{kafka.group-id}}"
                + "&autoOffsetReset=earliest"
                + "&consumersCount=1"
                + "&pollTimeoutMs=5000"
                + "&maxPollRecords=10"
                + "&allowManualCommit=true")
                .routeId("kafka-to-sqs")
                .removeHeaders("*")

                //Vérification message vide
                .choice()
                .when(simple("${body} == null || ${body} == ''"))
                .log("Message vide ignoré")

                .otherwise()

                //Message brut
                .log("📥 Message reçu de Kafka: ${body}")

                // mapping JSON → Event
                .unmarshal().json(JsonLibrary.Jackson, Event.class)

                //Vérification mapping avant envoi sur SQS
                .process(exchange -> {
                    Event event = exchange.getIn().getBody(Event.class);

                    System.out.println("Event reçu: " + event);
                    System.out.println("Data: " + event.getData());
                    System.out.println("EVENT TYPE: " + event.getEventType());
                    System.out.println("EntityId: " + event.getEntityId());
                })

                //Envoi vers SQS (reconverti en JSON automatiquement)
                .marshal().json(JsonLibrary.Jackson)
                .to("aws2-sqs:{{aws.sqs.queue-name}}")

                //Commit seulement si succès
                .process(exchange -> {
                    KafkaManualCommit manual = exchange.getIn()
                            .getHeader(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);

                    if (manual != null) {
                        manual.commit();
                        System.out.println("Commit effectué");
                    }
                })
                .log("Message envoyé à SQS avec succès")
                .end();
/*
        from("kafka:{{kafka.topic}}"
                + "?brokers={{kafka.brokers}}"
                + "&groupId={{kafka.group-id}}"
                + "&autoOffsetReset=earliest"
                + "&consumersCount=1"
                + "&pollTimeoutMs=5000"
                + "&maxPollRecords=10"
                + "&allowManualCommit=true")
                .routeId("kafka-to-sqs")

                // ✅ garder la KEY AVANT nettoyage
                .process(exchange -> {
                    String key = exchange.getIn().getHeader(KafkaConstants.KEY, String.class);
                    exchange.getIn().setHeader("messageKey", key);
                })

                // ❌ supprimer seulement kafka headers (PAS TOUT)
                .removeHeaders("kafka.*")

                // ❌ check message vide
                .choice()
                .when(simple("${body} == null || ${body} == ''"))
                .log("Message vide ignoré")
                .otherwise()

                .log("📥 Kafka message reçu: ${body}")

                // ✅ conversion JSON → Event
                .unmarshal().json(JsonLibrary.Jackson, Event.class)

                // ✅ log debug
                .process(exchange -> {
                    Event event = exchange.getIn().getBody(Event.class);

                    System.out.println("EventType=" + event.getEventType());
                    System.out.println("EntityId=" + event.getEntityId());
                })

                // 🚀 envoi SQS DIRECT (Camel convertit automatiquement)
                .to("aws2-sqs:{{aws.sqs.queue-name}}")

                // ✅ commit seulement si succès SQS
                .process(exchange -> {
                    KafkaManualCommit manual = exchange.getIn()
                            .getHeader(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);

                    if (manual != null) {
                        manual.commit();
                        System.out.println("✅ Kafka commit effectué");
                    }
                })

                .log("✅ Message envoyé vers SQS avec succès")
                .end();
*/
    }
}