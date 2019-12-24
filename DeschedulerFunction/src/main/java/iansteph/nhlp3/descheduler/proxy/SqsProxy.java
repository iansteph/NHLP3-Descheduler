package iansteph.nhlp3.descheduler.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import static java.lang.String.format;

public class SqsProxy {

    private final ObjectMapper objectMapper;
    private final SqsClient sqsClient;

    private static final int DESCHEDULING_SQS_MESSAGE_DELAY = 15 * 60;
    private static final String DESCHEDULING_SQS_QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/627812672245/NHLP3-Descheduling";

    private static final Logger logger = LogManager.getLogger(SqsProxy.class);

    public SqsProxy(final SqsClient sqsClient, final ObjectMapper objectMapper) {

        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(final PlayEvent playEvent) {

        try {

            final String serializedPlayEvent = objectMapper.writeValueAsString(playEvent);
            final SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .messageBody(serializedPlayEvent)
                    .delaySeconds(DESCHEDULING_SQS_MESSAGE_DELAY)
                    .queueUrl(DESCHEDULING_SQS_QUEUE_URL)
                    .build();
            logger.info(format("SendMessage request: %s", sendMessageRequest));
            final SendMessageResponse sendMessageResponse = sqsClient.sendMessage(sendMessageRequest);
            logger.info(format("SendMessage response: %s", sendMessageResponse));
        }
        catch (JsonProcessingException e) {

            logger.error(e);
            throw new RuntimeException(e);
        }
        catch (SdkException e) {

            logger.error(e);
            throw e;
        }
    }
}
