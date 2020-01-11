package iansteph.nhlp3.descheduler.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.UnitTestBase;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;
import iansteph.nhlp3.descheduler.model.event.SnsDeschedulerEvent;
import iansteph.nhlp3.descheduler.model.event.SnsRecord;
import iansteph.nhlp3.descheduler.model.event.SqsDeschedulerEvent;
import iansteph.nhlp3.descheduler.model.event.SqsRecord;
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
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = RuntimeException.class)
    public void test_handleRequest_throws_NullPointerException_when_input_does_not_contain_event_source_key() {

        deschedulerHandler.handleRequest("someInput", null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = RuntimeException.class)
    public void test_handleRequest_catches_IOException_when_deserialization_fails_and_throws_RunTimeException() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(SnsDeschedulerEvent.class))).thenThrow(new IOException());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sns\"", null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_deserialized_descheduler_event_is_null() {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sns\"", null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_because_descheduler_event_has_null_list_of_records() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(SnsDeschedulerEvent.class))).thenReturn(new SnsDeschedulerEvent());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sns\"", null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_because_descheduler_event_has_null_record_in_list_of_records() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final SnsDeschedulerEvent snsDeschedulerEvent = new SnsDeschedulerEvent();
        snsDeschedulerEvent.setRecords(Collections.singletonList(null));
        when(mockObjectMapper.readValue(anyString(), eq(SnsDeschedulerEvent.class))).thenReturn(snsDeschedulerEvent);
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sns\"", null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_handleRequest_throws_IllegalArgumentException_when_record_list_in_descheduler_event_has_size_not_equal_to_one() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final SnsDeschedulerEvent snsDeschedulerEvent = new SnsDeschedulerEvent();
        final List<SnsRecord> records = new ArrayList<>();
        records.add(new SnsRecord());
        records.add(new SnsRecord());
        snsDeschedulerEvent.setRecords(records);
        when(mockObjectMapper.readValue(anyString(), eq(SnsDeschedulerEvent.class))).thenReturn(snsDeschedulerEvent);
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sns\"", null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_play_event_string_is_null() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final SqsDeschedulerEvent sqsDeschedulerEvent = new SqsDeschedulerEvent();
        final List<SqsRecord> records = new ArrayList<>();
        records.add(new SqsRecord());
        sqsDeschedulerEvent.setRecords(records);
        when(mockObjectMapper.readValue(anyString(), eq(SqsDeschedulerEvent.class))).thenReturn(sqsDeschedulerEvent);
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sqs\"", null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = RuntimeException.class)
    public void test_handleRequest_catches_IOException_when_play_event_deserialization_fails_and_throws_RuntimeException() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final SqsDeschedulerEvent sqsDeschedulerEvent = new SqsDeschedulerEvent();
        final List<SqsRecord> records = new ArrayList<>();
        final SqsRecord sqsRecord = new SqsRecord();
        sqsRecord.setBody("someBody");
        records.add(sqsRecord);
        sqsDeschedulerEvent.setRecords(records);
        when(mockObjectMapper.readValue(anyString(), eq(SqsDeschedulerEvent.class))).thenReturn(sqsDeschedulerEvent);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenThrow(new IOException());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sqs\"", null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_deserialized_play_event_is_null() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final SqsDeschedulerEvent sqsDeschedulerEvent = new SqsDeschedulerEvent();
        final List<SqsRecord> records = new ArrayList<>();
        final SqsRecord sqsRecord = new SqsRecord();
        sqsRecord.setBody("someBody");
        records.add(sqsRecord);
        sqsDeschedulerEvent.setRecords(records);
        when(mockObjectMapper.readValue(anyString(), eq(SqsDeschedulerEvent.class))).thenReturn(sqsDeschedulerEvent);
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sqs\"", null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_gamePk_is_null() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final SqsDeschedulerEvent sqsDeschedulerEvent = new SqsDeschedulerEvent();
        final List<SqsRecord> records = new ArrayList<>();
        final SqsRecord sqsRecord = new SqsRecord();
        sqsRecord.setBody("someBody");
        records.add(sqsRecord);
        sqsDeschedulerEvent.setRecords(records);
        when(mockObjectMapper.readValue(anyString(), eq(SqsDeschedulerEvent.class))).thenReturn(sqsDeschedulerEvent);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(new PlayEvent());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sqs\"", null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_listTargetsByRule_call_to_CloudWatchEventsProxy_fails() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final SqsDeschedulerEvent sqsDeschedulerEvent = new SqsDeschedulerEvent();
        final List<SqsRecord> records = new ArrayList<>();
        final SqsRecord sqsRecord = new SqsRecord();
        sqsRecord.setBody("someBody");
        records.add(sqsRecord);
        sqsDeschedulerEvent.setRecords(records);
        when(mockObjectMapper.readValue(anyString(), eq(SqsDeschedulerEvent.class))).thenReturn(sqsDeschedulerEvent);
        final PlayEvent playEvent = new PlayEvent();
        playEvent.setGamePk(1);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(playEvent);
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenThrow(SdkException.builder().build());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sqs\"", null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_deleteRule_call_to_CloudWatchEventsProxy_fails() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final SqsDeschedulerEvent sqsDeschedulerEvent = new SqsDeschedulerEvent();
        final List<SqsRecord> records = new ArrayList<>();
        final SqsRecord sqsRecord = new SqsRecord();
        sqsRecord.setBody("someBody");
        records.add(sqsRecord);
        sqsDeschedulerEvent.setRecords(records);
        when(mockObjectMapper.readValue(anyString(), eq(SqsDeschedulerEvent.class))).thenReturn(sqsDeschedulerEvent);
        final PlayEvent playEvent = new PlayEvent();
        playEvent.setGamePk(1);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(playEvent);
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.emptyList());
        doThrow(SdkException.builder().build()).when(mockCloudWatchEventsProxy).deleteRule(anyString());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sqs\"", null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_removeTargets_call_to_CloudWatchEventsProxy_fails() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final SqsDeschedulerEvent sqsDeschedulerEvent = new SqsDeschedulerEvent();
        final List<SqsRecord> records = new ArrayList<>();
        final SqsRecord sqsRecord = new SqsRecord();
        sqsRecord.setBody("someBody");
        records.add(sqsRecord);
        sqsDeschedulerEvent.setRecords(records);
        when(mockObjectMapper.readValue(anyString(), eq(SqsDeschedulerEvent.class))).thenReturn(sqsDeschedulerEvent);
        final PlayEvent playEvent = new PlayEvent();
        playEvent.setGamePk(1);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(playEvent);
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        doThrow(SdkException.builder().build()).when(mockCloudWatchEventsProxy).removeTargets(anyString());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sqs\"", null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_sendMessage_call_to_SqsProxy_fails() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final SqsDeschedulerEvent sqsDeschedulerEvent = new SqsDeschedulerEvent();
        final List<SqsRecord> records = new ArrayList<>();
        final SqsRecord sqsRecord = new SqsRecord();
        sqsRecord.setBody("someBody");
        records.add(sqsRecord);
        sqsDeschedulerEvent.setRecords(records);
        when(mockObjectMapper.readValue(anyString(), eq(SqsDeschedulerEvent.class))).thenReturn(sqsDeschedulerEvent);
        final PlayEvent playEvent = new PlayEvent();
        playEvent.setGamePk(1);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(playEvent);
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        doThrow(SdkException.builder().build()).when(mockSqsProxy).sendMessage(any(PlayEvent.class));
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest("\"EventSource\":\"aws:sqs\"", null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }

    @Test
    public void test_handleRequest_successfully_handles_SnsDeschedulerEvent_with_no_remaining_target() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(SnsDeschedulerEvent.class))).thenReturn(SnsDeschedulerEvent);
        final PlayEvent playEvent = new PlayEvent();
        playEvent.setGamePk(1);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(playEvent);
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.emptyList());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(SnsDeschedulerEventAsString, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test
    public void test_handleRequest_successfully_handles_SnsDeschedulerEvent_with_remaining_target() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(SnsDeschedulerEvent.class))).thenReturn(SnsDeschedulerEvent);
        final PlayEvent playEvent = new PlayEvent();
        playEvent.setGamePk(1);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(playEvent);
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(SnsDeschedulerEventAsString, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }

    @Test
    public void test_handleRequest_successfully_handles_SqsDeschedulerEvent_with_no_remaining_target() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(SqsDeschedulerEvent.class))).thenReturn(SqsDeschedulerEvent);
        final PlayEvent playEvent = new PlayEvent();
        playEvent.setGamePk(1);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(playEvent);
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.emptyList());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(SqsDeschedulerEventAsString, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test
    public void test_handleRequest_successfully_handles_SqsDeschedulerEvent_with_remaining_target() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(SqsDeschedulerEvent.class))).thenReturn(SqsDeschedulerEvent);
        final PlayEvent playEvent = new PlayEvent();
        playEvent.setGamePk(1);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(playEvent);
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(SqsDeschedulerEventAsString, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }
}
