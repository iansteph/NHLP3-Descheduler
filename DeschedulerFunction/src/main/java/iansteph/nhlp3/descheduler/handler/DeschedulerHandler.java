package iansteph.nhlp3.descheduler.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.model.event.DeschedulerEvent;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;
import iansteph.nhlp3.descheduler.model.event.Record;
import iansteph.nhlp3.descheduler.model.event.SnsDeschedulerEvent;
import iansteph.nhlp3.descheduler.model.event.SqsDeschedulerEvent;
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
public class DeschedulerHandler implements RequestHandler<String, Object> {

    private final CloudWatchEventsProxy cloudWatchEventsProxy;
    private final SqsProxy sqsProxy;
    private final ObjectMapper objectMapper;

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

    public PlayEvent handleRequest(final String deschedulerEventAsString, final Context context) {

        final DeschedulerEvent deschedulerEvent = preProcessDeschedulerEvent(deschedulerEventAsString);
        final String playEventString = deschedulerEvent.getPlayEventString();
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

    private DeschedulerEvent preProcessDeschedulerEvent(final String deschedulerEventAsString) {

        logger.info(format("DeschedulerEventAsString: %s", deschedulerEventAsString));
        final DeschedulerEvent deschedulerEvent = deserializeDeschedulerEvent(deschedulerEventAsString);
        validateDeschedulerEvent(deschedulerEvent);
        return deschedulerEvent;
    }

    private DeschedulerEvent deserializeDeschedulerEvent(final String deschedulerEventAsString) {

        try {

            checkNotNull(deschedulerEventAsString, "DeschedulerEventAsString cannot be null");
            final Class<? extends DeschedulerEvent> deserializationTargetClass =
                    identifyDeschedulerEventImplementation(deschedulerEventAsString);
            final DeschedulerEvent deschedulerEvent = objectMapper.readValue(deschedulerEventAsString, deserializationTargetClass);
            logger.info(format("Deserialized DeschedulerEventString: %s", deschedulerEvent));
            return deschedulerEvent;
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

    private Class<? extends DeschedulerEvent> identifyDeschedulerEventImplementation(final String deschedulerEventAsString) {

        final String normalizedDeschedulerEventAsString = deschedulerEventAsString.replace(" ", "").toLowerCase();
        if (normalizedDeschedulerEventAsString.contains("\"eventsource\":\"aws:sns\"")) {

            logger.info("DeschedulerEvent implementation target class identified as SnsDeschedulerEvent");
            return SnsDeschedulerEvent.class;
        }
        else if(normalizedDeschedulerEventAsString.contains("\"eventsource\":\"aws:sqs\"")) {

            logger.info("DeschedulerEvent implementation target class identified as SqsDeschedulerEvent");
            return SqsDeschedulerEvent.class;
        }
        else {

            final RuntimeException runtimeException = new RuntimeException(format("Cannot identify implementation to deserialize to for " +
                    "the given DeschedulerEventAsString: %s", deschedulerEventAsString));
            logger.error(runtimeException);
            throw runtimeException;
        }
    }

    private void validateDeschedulerEvent(final DeschedulerEvent deschedulerEvent) {

        try {

            checkNotNull(deschedulerEvent, "DeschedulerEvent cannot be null");
            final List<? extends Record> records = deschedulerEvent.getRecords();
            checkNotNull(records, "List of Records in DeschedulerEvent cannot be null");
            records.forEach(record -> checkNotNull(record, "Record in DeschedulerEvent cannot be null"));
            // SnsRecords always have a batch size of 1, and the SQS event source trigger has the batch size configured to 1
            checkArgument(records.size() == 1, "DeschedulerEvent's Records' list size should be (and is " +
                    "configured) to be 1");
        }
        catch (NullPointerException | IllegalArgumentException e) {

            logger.error(e);
            throw e;
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
