package iansteph.nhlp3.descheduler.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.UnitTestBase;
import iansteph.nhlp3.descheduler.model.event.SnsMessageLambdaTriggerEvent;
import iansteph.nhlp3.descheduler.proxy.CloudWatchEventsProxy;
import org.junit.Test;
import software.amazon.awssdk.core.exception.SdkException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DeschedulerHandlerTest extends UnitTestBase {

    private final CloudWatchEventsProxy mockCloudWatchEventsProxy = mock(CloudWatchEventsProxy.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private class Sleeper implements Sleep { @Override public void sleep(int numberOfMillisecondsToSleep) {} }
    private final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, objectMapper, new Sleeper());
    private final SnsMessageLambdaTriggerEvent inputEvent =
            getTestResourceAsSnsMessageLambdaTriggerEvent("sns-descheduler-event-with-play-event.json");

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_play_event_is_null() {

        final SnsMessageLambdaTriggerEvent event = inputEvent;
        event.getRecords().get(0).getSns().setMessage(null);

        deschedulerHandler.handleRequest(event, null);

        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_play_event_as_null_game_pk() {

        final SnsMessageLambdaTriggerEvent event = inputEvent;
        event.getRecords().get(0).getSns().setMessage(null);

        deschedulerHandler.handleRequest(event, null);

        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_remove_targets_call_to_cloudwatch_fails() {

        doThrow(SdkException.builder().build()).when(mockCloudWatchEventsProxy).removeTargets(anyString());

        deschedulerHandler.handleRequest(inputEvent, null);

        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_delete_rule_call_to_cloudwatch_fails() {

        doThrow(SdkException.builder().build()).when(mockCloudWatchEventsProxy).removeTargets(anyString());

        deschedulerHandler.handleRequest(inputEvent, null);

        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
    }

    @Test
    public void test_handleRequest_successfully_handles_play_event() {

        deschedulerHandler.handleRequest(inputEvent, null);

        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
    }
}