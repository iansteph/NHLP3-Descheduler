package iansteph.nhlp3.descheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.model.event.SnsDeschedulerEvent;
import iansteph.nhlp3.descheduler.model.event.SnsRecord;
import iansteph.nhlp3.descheduler.model.event.SqsDeschedulerEvent;
import iansteph.nhlp3.descheduler.model.event.SqsRecord;
import iansteph.nhlp3.descheduler.model.event.snsrecord.Sns;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

public class UnitTestBase {

    public static SnsDeschedulerEvent SnsDeschedulerEvent = createSnsDeschedulerEventWithPlayEvent(getPlayEventAsStringFromTestResource());
    public static String SnsDeschedulerEventAsString = getTestResourceAsString("sns-descheduler-event-with-play-event.txt");
    public static String SqsDeschedulerEventAsString = getTestResourceAsString("sqs-descheduler-event-with-play-event.txt");
    public static SqsDeschedulerEvent SqsDeschedulerEvent = createSqsDeschedulerEventWithPlayEvent(getPlayEventAsStringFromTestResource());

    private static String getPlayEventAsStringFromTestResource() {

        return getTestResourceAsString("play-event.txt");
    }

    private static String getTestResourceAsString(final String filename) {

        try {

            final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            final File playEventTestResourceFile = new File(format("src/test/resources/%s", filename));
            final String playEventAsString = objectMapper.readValue(playEventTestResourceFile, String.class);
            return playEventAsString;
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

    private static SnsDeschedulerEvent createSnsDeschedulerEventWithPlayEvent(final String playEventString) {

        final Sns sns = new Sns();
        sns.setMessage(playEventString);
        final SnsRecord snsRecord = new SnsRecord();
        snsRecord.setSns(sns);
        final List<SnsRecord> records = Collections.singletonList(snsRecord);
        final SnsDeschedulerEvent snsDeschedulerEvent = new SnsDeschedulerEvent();
        snsDeschedulerEvent.setRecords(records);
        return snsDeschedulerEvent;
    }

    private static SqsDeschedulerEvent createSqsDeschedulerEventWithPlayEvent(final String playEventString) {

        final SqsRecord sqsRecord = new SqsRecord();
        sqsRecord.setBody(playEventString);
        final List<SqsRecord> records = Collections.singletonList(sqsRecord);
        final SqsDeschedulerEvent sqsDeschedulerEvent = new SqsDeschedulerEvent();
        sqsDeschedulerEvent.setRecords(records);
        return sqsDeschedulerEvent;
    }
}
