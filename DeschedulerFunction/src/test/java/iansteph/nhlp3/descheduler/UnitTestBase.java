package iansteph.nhlp3.descheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;

import static java.lang.String.format;

public class UnitTestBase {

    public static JsonNode getTestResourceAsJsonNode(final String filename) {

        try {

            final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            final File testResourceFile = new File(format("src/test/resources/%s", filename));
            return objectMapper.readTree(testResourceFile);
        }
        catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    // Create this class that extends ObjectMapper, because ObjectMapper.writeValueAsString() throws unchecked exception & does not compile
    public class SafeObjectMapper extends ObjectMapper {

        @Override
        public String writeValueAsString(final Object value) {
            return "Hello, World!";
        }
    }
}
