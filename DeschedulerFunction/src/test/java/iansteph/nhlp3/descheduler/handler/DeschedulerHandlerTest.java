package iansteph.nhlp3.descheduler.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.UnitTestBase;
import iansteph.nhlp3.descheduler.model.event.DeschedulerEvent;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;
import iansteph.nhlp3.descheduler.model.event.Record;
import iansteph.nhlp3.descheduler.model.event.record.SnsRecord;
import iansteph.nhlp3.descheduler.model.event.record.SqsRecord;
import iansteph.nhlp3.descheduler.proxy.CloudWatchEventsProxy;
import iansteph.nhlp3.descheduler.proxy.SqsProxy;
import org.junit.Test;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.cloudwatchevents.model.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeschedulerHandlerTest extends UnitTestBase {

    private final CloudWatchEventsProxy mockCloudWatchEventsProxy = mock(CloudWatchEventsProxy.class);
    private final SqsProxy mockSqsProxy = mock(SqsProxy.class);
    private final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy,
            new ObjectMapper().registerModule(new JavaTimeModule()), mockSqsProxy);

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_because_input_descheduler_event_is_null() {

        deschedulerHandler.handleRequest(null, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_because_descheduler_event_has_null_records() {

        deschedulerHandler.handleRequest(new DeschedulerEvent(), null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_because_descheduler_event_has_records_with_null_record() {

        final List<Record> records = Collections.singletonList(null);
        final DeschedulerEvent deschedulerEvent = new DeschedulerEvent();
        deschedulerEvent.setRecords(records);

        deschedulerHandler.handleRequest(deschedulerEvent, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_handleRequest_throws_IllegalArgumentException_because_descheduler_event_has_records_size_is_more_than_one() {

        final List<Record> records = new ArrayList<>();
        records.add(new SnsRecord());
        records.add(new SnsRecord());
        final DeschedulerEvent deschedulerEvent = new DeschedulerEvent();
        deschedulerEvent.setRecords(records);

        deschedulerHandler.handleRequest(deschedulerEvent, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_deserializing_descheduler_event_with_null_play_event() {

        final List<Record> records = new ArrayList<>();
        final SqsRecord sqsRecord = new SqsRecord();
        sqsRecord.setBody(null);
        records.add(new SqsRecord());
        final DeschedulerEvent deschedulerEvent = new DeschedulerEvent();
        deschedulerEvent.setRecords(records);

        deschedulerHandler.handleRequest(deschedulerEvent, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void test_handleRequest_throws_RuntimeException_when_deserializing_descheduler_event_fails() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenThrow(new IOException());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(DeschedulerEventWithSnsRecord, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_because_listTargetsByRule_call_to_CloudWatchEventsProxy_fails() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenThrow(SdkException.builder().build());

        deschedulerHandler.handleRequest(DeschedulerEventWithSnsRecord, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
    }

    @Test
    public void test_handleRequest_successfully_handles_sqs_request_without_targets() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.emptyList());

        deschedulerHandler.handleRequest(DeschedulerEventWithSqsRecord, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_handling_sqs_request_without_targets_because_deleteRule_call_to_CloudWatchEventsProxy_fails() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.emptyList());
        doThrow(SdkException.builder().build()).when(mockCloudWatchEventsProxy).deleteRule(anyString());

        deschedulerHandler.handleRequest(DeschedulerEventWithSqsRecord, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
    }

    @Test
    public void test_handleRequest_successfully_handles_sns_request_with_remaining_targets() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));

        deschedulerHandler.handleRequest(DeschedulerEventWithSnsRecord, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_handling_sns_request_with_remaining_targets_because_removeTargets_call_to_CloudWatchEventsProxy_fails() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        doThrow(SdkException.builder().build()).when(mockCloudWatchEventsProxy).removeTargets(anyString());

        deschedulerHandler.handleRequest(DeschedulerEventWithSnsRecord, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));

    }

    @Test(expected = RuntimeException.class)
    public void test_handleRequest_throws_RuntimeException_when_handling_sns_request_with_remaining_targets_because_sendMessage_call_to_SqsProxy_fails() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        doThrow(new RuntimeException()).when(mockSqsProxy).sendMessage(any(PlayEvent.class));

        deschedulerHandler.handleRequest(DeschedulerEventWithSnsRecord, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_handling_sns_request_with_remaining_targets_because_sendMessage_call_to_SqsProxy_fails() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        doThrow(SdkException.builder().build()).when(mockSqsProxy).sendMessage(any(PlayEvent.class));

        deschedulerHandler.handleRequest(DeschedulerEventWithSnsRecord, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }
}
