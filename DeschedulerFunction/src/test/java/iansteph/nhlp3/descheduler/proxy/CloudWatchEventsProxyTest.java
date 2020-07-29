package iansteph.nhlp3.descheduler.proxy;

import org.junit.Test;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.model.DeleteRuleRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.DeleteRuleResponse;
import software.amazon.awssdk.services.cloudwatchevents.model.RemoveTargetsRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.RemoveTargetsResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CloudWatchEventsProxyTest {

    private static final String EVENT_PUBLISHER_TARGET_ID = "Event-Publisher-Lambda-Function";
    private static final String SHIFT_PUBLISHER_TARGET_ID = "Shift-Publisher-Lambda-Function";

    private final CloudWatchEventsClient mockCloudWatchEventsClient = mock(CloudWatchEventsClient.class);
    private final CloudWatchEventsProxy cloudWatchEventsProxy = new CloudWatchEventsProxy(mockCloudWatchEventsClient);

    @Test
    public void test_deleteRule_is_successful() {

        when(mockCloudWatchEventsClient.deleteRule(any(DeleteRuleRequest.class))).thenReturn(DeleteRuleResponse.builder().build());

        cloudWatchEventsProxy.deleteRule("someRuleName");

        verify(mockCloudWatchEventsClient, times(1)).deleteRule(any(DeleteRuleRequest.class));
    }

    @Test(expected = SdkException.class)
    public void test_deleteRule_throws_SdkException_when_call_to_cloud_watch_events_fails() {

        when(mockCloudWatchEventsClient.deleteRule(any(DeleteRuleRequest.class))).thenThrow(SdkException.builder().build());

        cloudWatchEventsProxy.deleteRule("someRuleName");

        verify(mockCloudWatchEventsClient, times(1)).deleteRule(any(DeleteRuleRequest.class));
    }

    @Test
    public void test_removeTargets_is_successful() {

        when(mockCloudWatchEventsClient.removeTargets(any(RemoveTargetsRequest.class))).thenReturn(RemoveTargetsResponse.builder().build());
        final String ruleName = "someRuleName";
        final RemoveTargetsRequest removeTargetsRequest = RemoveTargetsRequest.builder()
                .rule(ruleName)
                .ids(EVENT_PUBLISHER_TARGET_ID, SHIFT_PUBLISHER_TARGET_ID)
                .build();

        cloudWatchEventsProxy.removeTargets(ruleName);

        verify(mockCloudWatchEventsClient, times(1)).removeTargets(eq(removeTargetsRequest));
    }

    @Test(expected = SdkException.class)
    public void test_removeTargets_fails_if_call_to_cloud_watch_events_fails() {

        when(mockCloudWatchEventsClient.removeTargets(any(RemoveTargetsRequest.class))).thenThrow(SdkException.builder().build());

        cloudWatchEventsProxy.removeTargets("someRuleName");

        verify(mockCloudWatchEventsClient, times(1)).removeTargets(any(RemoveTargetsRequest.class));
    }
}
