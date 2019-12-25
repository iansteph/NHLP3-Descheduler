package iansteph.nhlp3.descheduler.handler;

import iansteph.nhlp3.descheduler.model.event.PlayEvent;
import iansteph.nhlp3.descheduler.proxy.CloudWatchEventsProxy;
import iansteph.nhlp3.descheduler.proxy.SqsProxy;
import org.junit.Test;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.cloudwatchevents.model.Target;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeschedulerHandlerTest {

    private final CloudWatchEventsProxy mockCloudWatchEventsProxy = mock(CloudWatchEventsProxy.class);
    private final SqsProxy mockSqsProxy = mock(SqsProxy.class);
    private final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockSqsProxy);

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_because_input_play_event_is_null() {

        deschedulerHandler.handleRequest(null, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_because_listTargetsByRule_call_to_CloudWatchEventsProxy_fails() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenThrow(SdkException.builder().build());

        deschedulerHandler.handleRequest(new PlayEvent(), null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test
    public void test_handleRequest_successfully_handles_request_without_targets() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.emptyList());

        deschedulerHandler.handleRequest(new PlayEvent(), null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_handling_request_without_targets_because_deleteRule_call_to_CloudWatchEventsProxy_fails() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.emptyList());
        doThrow(SdkException.builder().build()).when(mockCloudWatchEventsProxy).deleteRule(anyString());

        deschedulerHandler.handleRequest(new PlayEvent(), null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
    }

    @Test
    public void test_handleRequest_successfully_handles_request_with_remaining_targets() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));

        deschedulerHandler.handleRequest(new PlayEvent(), null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_handling_request_with_remaining_targets_because_removeTargets_call_to_CloudWatchEventsProxy_fails() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        doThrow(SdkException.builder().build()).when(mockCloudWatchEventsProxy).removeTargets(anyString());

        deschedulerHandler.handleRequest(new PlayEvent(), null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));

    }

    @Test(expected = RuntimeException.class)
    public void test_handleRequest_throws_RuntimeException_when_handling_request_with_remaining_targets_because_sendMessage_call_to_SqsProxy_fails() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        doThrow(new RuntimeException()).when(mockSqsProxy).sendMessage(any(PlayEvent.class));

        deschedulerHandler.handleRequest(new PlayEvent(), null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_handling_request_with_remaining_targets_because_sendMessage_call_to_SqsProxy_fails() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        doThrow(SdkException.builder().build()).when(mockSqsProxy).sendMessage(any(PlayEvent.class));

        deschedulerHandler.handleRequest(new PlayEvent(), null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }
}
