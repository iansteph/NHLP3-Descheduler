package iansteph.nhlp3.descheduler.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.model.DeleteRuleRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.DeleteRuleResponse;
import software.amazon.awssdk.services.cloudwatchevents.model.RemoveTargetsRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.RemoveTargetsResponse;

import static java.lang.String.format;

public class CloudWatchEventsProxy {

    private final CloudWatchEventsClient cloudWatchEventsClient;

    private static final String TARGET_ID = "Event-Publisher-Lambda-Function";

    private static final Logger logger = LogManager.getLogger(CloudWatchEventsProxy.class);

    public CloudWatchEventsProxy(final CloudWatchEventsClient cloudWatchEventsClient) {

        this.cloudWatchEventsClient = cloudWatchEventsClient;
    }

    public void deleteRule(final String ruleName) {

        try {

            final DeleteRuleRequest deleteRuleRequest = DeleteRuleRequest.builder()
                    .name(ruleName)
                    .build();
            logger.info(format("DeleteRule request: %s", deleteRuleRequest));
            final DeleteRuleResponse deleteRuleResponse = cloudWatchEventsClient.deleteRule(deleteRuleRequest);
            logger.info(format("DeleteRule response: %s", deleteRuleResponse));
        }
        catch (SdkException e) {

            logger.error(e);
            throw e;
        }
    }

    public void removeTargets(final String ruleName) {

        try {

            final RemoveTargetsRequest removeTargetsRequest = RemoveTargetsRequest.builder()
                    .rule(ruleName)
                    .ids(TARGET_ID)
                    .build();
            logger.info(format("RemoveTargets request: %s", removeTargetsRequest));
            final RemoveTargetsResponse removeTargetsResponse = cloudWatchEventsClient.removeTargets(removeTargetsRequest);
            logger.info(format("RemoveTargets response: %s", removeTargetsResponse));
        }
        catch (SdkException e) {

            logger.error(e);
            throw e;
        }
    }
}
