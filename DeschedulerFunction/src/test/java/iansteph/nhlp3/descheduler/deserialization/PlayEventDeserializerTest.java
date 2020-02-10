package iansteph.nhlp3.descheduler.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import iansteph.nhlp3.descheduler.UnitTestBase;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlayEventDeserializerTest extends UnitTestBase {

    private final PlayEventDeserializer playEventDeserializer = new PlayEventDeserializer();

    @Test
    public void test_deserialize_successfully_deserializes_play_event_from_escaped_json() throws IOException {

        final JsonParser mockJsonParser = mock(JsonParser.class);
        final String playEventString = getTestResourceAsString("play-event.txt");
        when(mockJsonParser.getText()).thenReturn(playEventString);

        final PlayEvent result = playEventDeserializer.deserialize(mockJsonParser, null);

        assertThat(result, is(notNullValue()));
        assertThat(result.getGamePk(), is(notNullValue()));
    }

    @Test(expected = JsonProcessingException.class)
    public void test_deserialize_throws_JsonProcessingException_when_deserializing_unescaped_json() throws IOException {

        final JsonParser mockJsonParser = mock(JsonParser.class);
        final String playEventString = getTestResourceAsString("unescaped-play-event.txt");
        when(mockJsonParser.getText()).thenReturn(playEventString);

        playEventDeserializer.deserialize(mockJsonParser, null);
    }

    @Test(expected = NullPointerException.class)
    public void test_derserialize_throws_NullPointerException_when_deserializing_null() throws IOException {

        playEventDeserializer.deserialize(null, null);
    }
}
