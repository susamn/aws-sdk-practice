package com.susamn.awssdk.practive.sqs;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @author Supratim Samanta
 * @date 5/3/21 at 11:35
 */
@Component
public class SQSListenerBean {
    public static final Logger LOGGER = LoggerFactory.getLogger(SQSListenerBean.class);
    private final SqsAsyncClient sqsAsyncClient;
    private final String queueUrl;
    @Value("AWS_QUEUE_NAME")
    private String queueName;

    public SQSListenerBean(SqsAsyncClient sqsAsyncClient) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.queueName = Objects.requireNonNull(queueName);
        try {
            this.queueUrl = this.sqsAsyncClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(this.queueName).build()).get().queueUrl();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void continuousListener() {
        Mono<ReceiveMessageResponse> receiveMessageResponseMono = Mono.fromFuture(() ->
                    sqsAsyncClient.receiveMessage(
                            ReceiveMessageRequest.builder()
                                    .maxNumberOfMessages(5)
                                    .queueUrl(queueUrl)
                                    .waitTimeSeconds(10)
                                    .visibilityTimeout(30).build()
            )
        );

        receiveMessageResponseMono
                .repeat()
                .retry()
                .map(ReceiveMessageResponse::messages)
                .map(Flux::fromIterable)
                .flatMap(messageFlux -> messageFlux)
                .subscribe(message -> {
                    LOGGER.info("message body: " + message.body());
                    sqsAsyncClient.deleteMessage(DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(message.receiptHandle()).build())
                            .thenAccept(deleteMessageResponse -> {
                                LOGGER.info("deleted message with handle " + message.receiptHandle());
                            });
                });
    }
}
