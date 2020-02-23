package iansteph.nhlp3.descheduler.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;
import iansteph.nhlp3.descheduler.model.event.SnsMessageLambdaTriggerEvent;
import iansteph.nhlp3.descheduler.proxy.CloudWatchEventsProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.utils.AttributeMap;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Handler for requests to Lambda function.
 */
public class DeschedulerHandler implements RequestHandler<SNSEvent, Object> {

    private final CloudWatchEventsProxy cloudWatchEventsProxy;
    private final ObjectMapper objectMapper;
    private final Sleep sleeper;

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

        // Sleeper
        this.sleeper = new Sleeper();
    }

    public DeschedulerHandler(
        final CloudWatchEventsProxy cloudWatchEventsProxy,
        final ObjectMapper objectMapper,
        final Sleep sleeper
    ) {

        this.cloudWatchEventsProxy = cloudWatchEventsProxy;
        this.objectMapper = objectMapper;
        this.sleeper = sleeper;
    }

    public PlayEvent handleRequest(final SNSEvent snsEvent, final Context context) {

        logger.info(format("Handling event: %s", snsEvent));

//        // SNS only has one record per invocation when configured as event source for lambda
//        final PlayEvent playEvent = snsMessageLambdaTriggerEvent.getRecords().get(0).getSns().getMessage();
//        validatePlayEvent(playEvent);
//        logger.info(format("Processing GameId %d and PlayEvent %s", playEvent.getGamePk(), playEvent));
//        final String ruleName = "GameId-" + playEvent.getGamePk();
//
//        // Remove Targets for Rule so it can be deleted
//        logger.info(format("Removing existing target for event rule: %s", ruleName));
//        cloudWatchEventsProxy.removeTargets(ruleName);
//        logger.info(format("Targets removed for event rule: %s", ruleName));
//
//        // Wait for Target removal to propagate through CloudWatch
//        final int numberOfMillisecondsToWait = 1000 * 60 * 3;
//        logger.info(format("Waiting for %s milliseconds before deleting rule to allow target removal to propagate",
//                numberOfMillisecondsToWait));
//        sleeper.sleep(numberOfMillisecondsToWait);
//
//        // Delete Rule
//        logger.info(format("Deleting Rule %s", ruleName));
//        cloudWatchEventsProxy.deleteRule(ruleName);
//        logger.info(format("%s deleted successfully", ruleName));
//
//        return playEvent;
        return null;
    }

    private void validatePlayEvent(final PlayEvent playEvent) {

        try {

            checkNotNull(playEvent, "PlayEvent cannot be null");
            final Integer gamePk = playEvent.getGamePk();
            checkNotNull(gamePk, "PlayEvent's gamePk cannot be null");
        }
        catch (NullPointerException e) {

            logger.error(e);
            throw e;
        }
    }
}
