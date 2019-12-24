package iansteph.nhlp3.descheduler.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.net.URI;
import java.util.List;

import static java.lang.String.format;

/**
 * Handler for requests to Lambda function.
 */
public class DeschedulerHandler implements RequestHandler<PlayEvent, Object> {

    private final CloudWatchEventsProxy cloudWatchEventsProxy;
    private final SqsProxy sqsProxy;

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
        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
            final SqsProxy sqsProxy
    ) {

        this.cloudWatchEventsProxy = cloudWatchEventsProxy;
        this.sqsProxy = sqsProxy;
    }

    public Object handleRequest(final PlayEvent playEvent, final Context context) {

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
}
