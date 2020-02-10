package iansteph.nhlp3.descheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.model.event.SnsMessageLambdaTriggerEvent;

import java.io.File;

import static java.lang.String.format;

public class UnitTestBase {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static SnsMessageLambdaTriggerEvent getTestResourceAsSnsMessageLambdaTriggerEvent(final String filename) {

        try {

            final File testResourceFile = getTestResourceAsFile(filename);
            return objectMapper.readValue(testResourceFile, SnsMessageLambdaTriggerEvent.class);
        }
        catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    public static String getTestResourceAsString(final String filename) {

        try {

            final File testResourceFile = getTestResourceAsFile(filename);
            return objectMapper.readValue(testResourceFile, String.class);
        }
        catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    private static File getTestResourceAsFile(final String filename) {

        return new File(format("src/test/resources/%s", filename));
    }
}
