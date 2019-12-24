package iansteph.nhlp3.descheduler.proxy;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;
import org.junit.Test;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SqsProxyTest {

    private final SqsClient mockSqsClient = mock(SqsClient.class);
    private final SafeObjectMapper mockObjectMapper = mock(SafeObjectMapper.class);
    private final SqsProxy sqsProxy = new SqsProxy(mockSqsClient, mockObjectMapper);

    @Test
    public void test_that_sendMessage_is_successful() {

        when(mockObjectMapper.writeValueAsString(any(PlayEvent.class))).thenReturn("somePlayEvent");

        sqsProxy.sendMessage(new PlayEvent());

        verify(mockObjectMapper, times(1)).writeValueAsString(any(PlayEvent.class));
        verify(mockSqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test(expected = RuntimeException.class)
    public void test_that_sendMessage_throws_JsonProcessingException_when_serialization_fails() {

        when(mockObjectMapper.writeValueAsString(any(PlayEvent.class))).thenThrow(new JsonParseException(null, ""));

        sqsProxy.sendMessage(new PlayEvent());

        verify(mockObjectMapper, times(1)).writeValueAsString(any(PlayEvent.class));
        verify(mockSqsClient, never()).sendMessage(any(SendMessageRequest.class));
    }

    @Test(expected = SdkException.class)
    public void test_that_sendMessage_throws_SdkException_when_call_to_sqs_fails() {

        when(mockObjectMapper.writeValueAsString(any(PlayEvent.class))).thenReturn("somePlayEvent");
        when(mockSqsClient.sendMessage(any(SendMessageRequest.class))).thenThrow(SdkException.builder().build());

        sqsProxy.sendMessage(new PlayEvent());

        verify(mockObjectMapper, times(1)).writeValueAsString(any(PlayEvent.class));
        verify(mockSqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    // Create this class that extends ObjectMapper, because ObjectMapper.writeValueAsString() throws unchecked exception & does not compile
    private class SafeObjectMapper extends ObjectMapper {

        @Override
        public String writeValueAsString(final Object value) {
            return "Hello, World!";
        }
    }
}
