package iansteph.nhlp3.descheduler.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;
import iansteph.nhlp3.descheduler.proxy.CloudWatchEventsProxy;
import iansteph.nhlp3.descheduler.proxy.SqsProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.model.Target;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.utils.AttributeMap;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Handler for requests to Lambda function.
 */
public class DeschedulerHandler implements RequestHandler<ObjectNode, Object> {

    private final CloudWatchEventsProxy cloudWatchEventsProxy;
    private final SqsProxy sqsProxy;
    private final ObjectMapper objectMapper;

    private static final String SQS_EVENT_SOURCE = "aws:sqs";
    private static final String SNS_EVENT_SOURCE = "aws:sns";
    private static final Logger logger = LogManager.getLogger(DeschedulerHandler.class);

    public DeschedulerHandler() {

        // Common
        final AwsCredentialsProvider defaultAwsCredentialsProvider = DefaultCredentialsProvider.builder().build();
        final SdkHttpClient httpClient = ApacheHttpClient.builder().buildWithDefaults(AttributeMap.empty());

        // CloudWatch
        final CloudWatchEventsClient cloudWatchEventsClient = CloudWatchEventsClient.builder()
                .credentialsProvider(defaultAwsCredentialsProvider)
                .endpointOverride(URI.create("https://events.us-east-1.amazonaws.com/"))
                .httpClient(httpClient)
                .region(Region.US_EAST_1)
                .build();
        this.cloudWatchEventsProxy = new CloudWatchEventsProxy(cloudWatchEventsClient);

        // ObjectMapper
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        // SQS
        final SqsClient sqsClient = SqsClient.builder()
                .credentialsProvider(defaultAwsCredentialsProvider)
                .endpointOverride(URI.create("https://sqs.us-east-1.amazonaws.com/"))
                .httpClient(httpClient)
                .region(Region.US_EAST_1)
                .build();
        this.sqsProxy = new SqsProxy(sqsClient, objectMapper);
    }

    public DeschedulerHandler(
            final CloudWatchEventsProxy cloudWatchEventsProxy,
            final ObjectMapper objectMapper,
            final SqsProxy sqsProxy
    ) {

        this.cloudWatchEventsProxy = cloudWatchEventsProxy;
        this.objectMapper = objectMapper;
        this.sqsProxy = sqsProxy;
    }

    public PlayEvent handleRequest(final ObjectNode deschedulerEventJson, final Context context) {

        final String playEventString = getPlayEventStringFromDeschedulerEventJson(deschedulerEventJson);
        final PlayEvent playEvent = preProcessPlayEvent(playEventString);
        logger.info(format("Processing GameId %d and PlayEvent %s", playEvent.getGamePk(), playEvent));
        final String ruleName = "GameId-" + playEvent.getGamePk();
        final List<Target> targets = cloudWatchEventsProxy.listTargetsByRule(ruleName);
        if (targets.isEmpty()) {

            logger.info(format("There are no targets remaining for event rule: %s", ruleName));
            cloudWatchEventsProxy.deleteRule(ruleName);
        }
        else {

            logger.info(format("Removing existing target for event rule: %s", ruleName));
            cloudWatchEventsProxy.removeTargets(ruleName);
            logger.info(format("Target removed for event rule: %s", ruleName));
            sqsProxy.sendMessage(playEvent);
        }
        return playEvent;
    }

    private String getPlayEventStringFromDeschedulerEventJson(final JsonNode deschedulerEventJson) {

        logger.info(format("DeschedulerEventJson: %s", deschedulerEventJson));
        final String eventSource = validateDeschedulerEventJson(deschedulerEventJson);
        final String playEventString = retrievePlayEventString(deschedulerEventJson, eventSource);
        return playEventString;
    }

    private String validateDeschedulerEventJson(final JsonNode deschedulerEventJson) {

        try {

            checkNotNull(deschedulerEventJson, "DeschedulerEventJson cannot be null");
            final JsonNode recordsNode = deschedulerEventJson.get("Records");
            checkNotNull(recordsNode, "List of Records in DeschedulerEventJson cannot be missing");
            checkArgument(!recordsNode.isNull(), "List of Records in DeschedulerEventJson cannot be null JSON literal");
            // SnsRecords always have a batch size of 1, and the SQS event source trigger has the batch size configured to 1
            checkArgument(recordsNode.size() == 1, "DeschedulerEventJson's Records' list size should be 1");
            final JsonNode record = recordsNode.get(0);
            checkArgument(!record.isNull(), "Record in DeschedulerEventJson cannot be null JSON literal");
            final JsonNode eventSourceNode = getEventSourceFromRecord(record);
            checkNotNull(eventSourceNode, "EventSource in DeschedulerEventJson cannot be null");
            checkArgument(!eventSourceNode.isNull(), "EventSource in DeschedulerEventJson cannot be null JSON literal");
            return eventSourceNode.asText();
        }
        catch (NullPointerException | IllegalArgumentException e) {

            logger.error(e);
            throw e;
        }
    }

    private JsonNode getEventSourceFromRecord(final JsonNode record) {

        if (record.has("EventSource")) {

            return record.get("EventSource");
        }
        else if (record.has("eventSource")) {

            return record.get("eventSource");
        }
        else {

            throw new RuntimeException(format("No event source key could be found for Record: %s", record));
        }
    }

    private String retrievePlayEventString(final JsonNode deschedulerEventJson, final String eventSource) {

        // SNS only has one record per invocation when configured as event source for lambda and SQS is configured to have batch size of 1
        final JsonNode record = deschedulerEventJson.get("Records").get(0);
        if (eventSource.equals(SNS_EVENT_SOURCE)) {

            logger.info("Handling as SNS event");
            final String playEventString = record.get("Sns").get("Message").asText();
            return playEventString;
        }
        else if(eventSource.equals(SQS_EVENT_SOURCE)) {

            logger.info("Handling as SQS event");
            final String playEventString = record.get("body").asText();
            return playEventString;
        }
        else {

            final RuntimeException runtimeException = new RuntimeException(format("Cannot identify event source type for invocation with" +
                    " DeschedulerEventJson: %s", deschedulerEventJson));
            logger.error(runtimeException);
            throw runtimeException;
        }
    }

    private PlayEvent preProcessPlayEvent(final String playEventString) {

        final PlayEvent playEvent = deserializePlayEvent(playEventString);
        validatePlayEvent(playEvent);
        return playEvent;
    }

    private PlayEvent deserializePlayEvent(final String playEventString) {

        try {

            checkNotNull(playEventString, "PlayEventString cannot be null");
            final PlayEvent playEvent = objectMapper.readValue(playEventString, PlayEvent.class);
            logger.info(format("Deserialized PlayEventString: %s", playEvent));
            return playEvent;
        }
        catch (NullPointerException e) {

            logger.error(e);
            throw e;
        }
        catch (IOException e) {

            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    private void validatePlayEvent(final PlayEvent playEvent) {

        try {

            checkNotNull(playEvent, "Deserialized PlayEvent cannot be null");
            final Integer gamePk = playEvent.getGamePk();
            checkNotNull(gamePk, "PlayEvent's gamePk cannot be null");
        }
        catch (NullPointerException e) {

            logger.error(e);
            throw e;
        }
    }
}
