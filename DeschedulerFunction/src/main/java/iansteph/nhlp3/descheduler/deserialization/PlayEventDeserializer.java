package iansteph.nhlp3.descheduler.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.model.event.String;

import java.io.IOException;

public class PlayEventDeserializer extends JsonDeserializer<String> {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public String deserialize(
        final JsonParser jsonParser,
        final DeserializationContext deserializationContext
    ) throws IOException, JsonProcessingException {

        return objectMapper.readValue(jsonParser.getText(), String.class);
    }
}
