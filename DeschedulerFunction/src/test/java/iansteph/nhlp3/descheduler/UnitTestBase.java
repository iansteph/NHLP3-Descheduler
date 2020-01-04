package iansteph.nhlp3.descheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.model.event.DeschedulerEvent;
import iansteph.nhlp3.descheduler.model.event.Record;
import iansteph.nhlp3.descheduler.model.event.record.SnsRecord;
import iansteph.nhlp3.descheduler.model.event.record.SqsRecord;
import iansteph.nhlp3.descheduler.model.event.record.sns.Sns;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class UnitTestBase {

    public static DeschedulerEvent DeschedulerEventWithSnsRecord =
            createDeschedulerEventWithRecords(Collections.singletonList(createSnsRecord(getPlayEventAsStringFromTestResource())));
    public static DeschedulerEvent DeschedulerEventWithSqsRecord =
            createDeschedulerEventWithRecords(Collections.singletonList(createSqsRecord(getPlayEventAsStringFromTestResource())));

    public static String getPlayEventAsStringFromTestResource() {

        try {

            final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            final File playEventTestResourceFile = new File("src/test/resources/play-event.txt");
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

    private static DeschedulerEvent createDeschedulerEventWithRecords(final List<Record> records) {

        final DeschedulerEvent deschedulerEvent = new DeschedulerEvent();
        deschedulerEvent.setRecords(records);
        return deschedulerEvent;
    }

    private static Record createSnsRecord(final String message) {

        final Sns sns = new Sns();
        sns.setMessage(message);
        final SnsRecord snsRecord = new SnsRecord();
        snsRecord.setSns(sns);
        return snsRecord;
    }

    private static Record createSqsRecord(final String body) {

        final SqsRecord sqsRecord = new SqsRecord();
        sqsRecord.setBody(body);
        return sqsRecord;
    }
}
