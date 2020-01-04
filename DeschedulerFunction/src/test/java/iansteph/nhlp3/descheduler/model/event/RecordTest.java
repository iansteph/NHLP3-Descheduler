package iansteph.nhlp3.descheduler.model.event;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iansteph.nhlp3.descheduler.UnitTestBase;
import iansteph.nhlp3.descheduler.model.event.record.SnsRecord;
import iansteph.nhlp3.descheduler.model.event.record.SqsRecord;
import iansteph.nhlp3.descheduler.model.event.record.sns.Sns;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class RecordTest extends UnitTestBase {

    @Test
    public void test_SnsRecord_getPlayEventAsString_is_successful() {

        final Sns sns = new Sns();
        sns.setMessage("someMessage");
        final SnsRecord snsRecord = new SnsRecord();
        snsRecord.setSns(sns);
        final Record record = snsRecord;

        final String playEventAsString = record.getPlayEventAsString();

        assertThat(playEventAsString, is(notNullValue()));
    }

    @Test(expected = NullPointerException.class)
    public void test_SnsRecord_getPlayEventAsString_throws_NullPointerException_when_Sns_is_null() {

        final Record record = new SnsRecord();

        record.getPlayEventAsString();
    }

    @Test
    public void test_SnsRecord_getPlayEventAsString_returns_null_play_event_string_when_sns_message_is_null() {

        final SnsRecord snsRecord = new SnsRecord();
        snsRecord.setSns(new Sns());
        final Record record = snsRecord;

        final String playEventAsString = record.getPlayEventAsString();

        assertThat(playEventAsString, is(nullValue()));
    }

    @Test
    public void test_SqsRecord_getPlayEventAsString_is_successful() {

        final SqsRecord sqsRecord = new SqsRecord();
        sqsRecord.setBody("someBody");
        final Record record = sqsRecord;

        final String playEventAsString = record.getPlayEventAsString();

        assertThat(playEventAsString, is(notNullValue()));
    }

    @Test
    public void test_SqsRecord_getPlayEventAsString_returns_null_play_events_string_when_sqs_body_is_null() {

        final Record record = new SqsRecord();

        final String playEventAsString = record.getPlayEventAsString();

        assertThat(playEventAsString, is(nullValue()));
    }

    @Test
    public void test_DeschedulerEvent_with_SqsRecord_in_record_list_successfully_deserializes() throws IOException {

        final SafeObjectMapper objectMapper = new SafeObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        final String playEventAsString = DeschedulerEventWithSqsRecord.getRecords().get(0).getPlayEventAsString();
        final PlayEvent playEvent = objectMapper.readValue(playEventAsString, PlayEvent.class);

        assertThat(playEvent, isA(PlayEvent.class));
        assertThat(playEvent, is(notNullValue()));
    }

    @Test
    public void test_DeschedulerEvent_with_SnsRecord_in_record_list_successfully_deserializes() throws IOException {

        final SafeObjectMapper objectMapper = new SafeObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        final String playEventAsString = DeschedulerEventWithSnsRecord.getRecords().get(0).getPlayEventAsString();
        final PlayEvent playEvent = objectMapper.readValue(playEventAsString, PlayEvent.class);

        assertThat(playEvent, isA(PlayEvent.class));
        assertThat(playEvent, is(notNullValue()));
    }
}
