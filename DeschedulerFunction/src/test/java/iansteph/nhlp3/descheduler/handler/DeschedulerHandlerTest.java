package iansteph.nhlp3.descheduler.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.UnitTestBase;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;
import iansteph.nhlp3.descheduler.proxy.CloudWatchEventsProxy;
import iansteph.nhlp3.descheduler.proxy.SqsProxy;
import org.junit.Test;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.cloudwatchevents.model.Target;

import java.io.IOException;
import java.util.Collections;

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
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, objectMapper, mockSqsProxy);

    private static final String DefaultSqsDeschedulerEventString = "{\"Records\":[{\"eventSource\":\"aws:sqs\",\"body\":\"someBody\"}]}";
    public static final JsonNode SnsDeschedulerEventJson = getTestResourceAsJsonNode("sns-descheduler-event-with-play-event.json");
    public static final JsonNode SqsDeschedulerEventJson = getTestResourceAsJsonNode("sqs-descheduler-event-with-play-event.json");

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_descheduler_event_json_is_null() {

        deschedulerHandler.handleRequest(null, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_descheduler_event_json_does_not_have_list_of_records() throws IOException {

        final String inputJsonString = "{\"someKey\":\"someValue\"}";
        final JsonNode inputJson = objectMapper.readTree(inputJsonString);

        deschedulerHandler.handleRequest(inputJson, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_handleRequest_throws_NullPointerException_when_descheduler_event_json_has_null_list_of_records() throws IOException {

        final String inputJsonString = "{\"Records\":null}";
        final JsonNode inputJson = objectMapper.readTree(inputJsonString);

        deschedulerHandler.handleRequest(inputJson, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_handleRequest_throws_IllegalArgumentException_when_descheduler_event_json_list_of_records_has_size_not_equal_to_one() throws IOException {

        final String inputJsonString = "{\"Records\":[{\"key\":\"value\"},{\"key\":\"value\"}]}";
        final JsonNode inputJson = objectMapper.readTree(inputJsonString);

        deschedulerHandler.handleRequest(inputJson, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_handleRequest_throws_NullPointerException_when_descheduler_event_json_has_null_record_in_list_of_records() throws IOException {

        final String inputJsonString = "{\"Records\":[null]}";
        final JsonNode inputJson = objectMapper.readTree(inputJsonString);

         deschedulerHandler.handleRequest(inputJson, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = RuntimeException.class)
    public void test_handleRequest_throws_RuntimeException_when_descheduler_event_json_does_not_contain_event_source() throws IOException {

        final String inputJsonString = "{\"Records\":[{\"key\":\"value\"}]}";
        final JsonNode inputJson = objectMapper.readTree(inputJsonString);

        deschedulerHandler.handleRequest(inputJson, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_handleRequest_throws_RuntimeException_when_descheduler_event_json_contains_null_event_source() throws IOException {

        final String inputJsonString = "{\"Records\":[{\"EventSource\":null}]}";
        final JsonNode inputJson = objectMapper.readTree(inputJsonString);

        deschedulerHandler.handleRequest(inputJson, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = RuntimeException.class)
    public void test_handleRequest_throws_RuntimeException_when_event_source_does_not_match_sns_or_sqs() throws IOException {

        final String inputJsonString = "{\"Records\":[{\"EventSource\":\"notSnsOrSqs\"}]}";
        final JsonNode inputJson = objectMapper.readTree(inputJsonString);

        deschedulerHandler.handleRequest(inputJson, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_play_event_string_is_null() throws IOException {

        final String inputJsonString = "{\"Records\":[{\"EventSource\":\"aws:sqs\",\"Body\":null}]}";
        final JsonNode inputJson = objectMapper.readTree(inputJsonString);

        deschedulerHandler.handleRequest(inputJson, null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = RuntimeException.class)
    public void test_handleRequest_catches_IOException_when_play_event_deserialization_fails_and_throws_RuntimeException() throws IOException {

        deschedulerHandler.handleRequest(objectMapper.readTree(DefaultSqsDeschedulerEventString), null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_deserialized_play_event_is_null() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(objectMapper.readTree(DefaultSqsDeschedulerEventString), null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = NullPointerException.class)
    public void test_handleRequest_throws_NullPointerException_when_gamePk_is_null() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(new PlayEvent());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(objectMapper.readTree(DefaultSqsDeschedulerEventString), null);

        verify(mockCloudWatchEventsProxy, never()).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_listTargetsByRule_call_to_CloudWatchEventsProxy_fails() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(new PlayEvent(1));
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenThrow(SdkException.builder().build());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(objectMapper.readTree(DefaultSqsDeschedulerEventString), null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_deleteRule_call_to_CloudWatchEventsProxy_fails() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(new PlayEvent(1));
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.emptyList());
        doThrow(SdkException.builder().build()).when(mockCloudWatchEventsProxy).deleteRule(anyString());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(objectMapper.readTree(DefaultSqsDeschedulerEventString), null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_removeTargets_call_to_CloudWatchEventsProxy_fails() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(new PlayEvent(1));
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        doThrow(SdkException.builder().build()).when(mockCloudWatchEventsProxy).removeTargets(anyString());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(objectMapper.readTree(DefaultSqsDeschedulerEventString), null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test(expected = SdkException.class)
    public void test_handleRequest_throws_SdkException_when_sendMessage_call_to_SqsProxy_fails() throws IOException {

        final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.readValue(anyString(), eq(PlayEvent.class))).thenReturn(new PlayEvent(1));
        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        doThrow(SdkException.builder().build()).when(mockSqsProxy).sendMessage(any(PlayEvent.class));
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, mockObjectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(objectMapper.readTree(DefaultSqsDeschedulerEventString), null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }

    @Test
    public void test_handleRequest_successfully_handles_sns_descheduler_event_with_no_remaining_target() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.emptyList());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, objectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(SnsDeschedulerEventJson, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test
    public void test_handleRequest_successfully_handles_sns_descheduler_event_with_remaining_target() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, objectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(SnsDeschedulerEventJson, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }

    @Test
    public void test_handleRequest_successfully_handles_sqs_descheduler_event_with_no_remaining_target() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.emptyList());
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, objectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(SqsDeschedulerEventJson, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).removeTargets(anyString());
        verify(mockSqsProxy, never()).sendMessage(any(PlayEvent.class));
    }

    @Test
    public void test_handleRequest_successfully_handles_sqs_descheduler_event_with_remaining_target() {

        when(mockCloudWatchEventsProxy.listTargetsByRule(anyString())).thenReturn(Collections.singletonList(Target.builder().build()));
        final DeschedulerHandler deschedulerHandler = new DeschedulerHandler(mockCloudWatchEventsProxy, objectMapper, mockSqsProxy);

        deschedulerHandler.handleRequest(SqsDeschedulerEventJson, null);

        verify(mockCloudWatchEventsProxy, times(1)).listTargetsByRule(anyString());
        verify(mockCloudWatchEventsProxy, never()).deleteRule(anyString());
        verify(mockCloudWatchEventsProxy, times(1)).removeTargets(anyString());
        verify(mockSqsProxy, times(1)).sendMessage(any(PlayEvent.class));
    }
}
