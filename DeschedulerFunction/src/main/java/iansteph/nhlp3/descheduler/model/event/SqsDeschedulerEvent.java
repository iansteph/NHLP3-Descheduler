package iansteph.nhlp3.descheduler.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SqsDeschedulerEvent implements DeschedulerEvent {

    private List<SqsRecord> records;

    @Override
    public String getPlayEventString() {

        // The SQS queue in this application is configured to only return one record per lambda invocation
        return records.get(0).getPlayEventString();
    }

    @Override
    public List<? extends Record> getRecords() {

        return records;
    }

    public void setRecords(final List<SqsRecord> records) {

        this.records = records;
    }

    @Override
    public String toString() {

        return "SqsDeschedulerEvent{" +
                "records=" + records +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqsDeschedulerEvent that = (SqsDeschedulerEvent) o;
        return Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {

        return Objects.hash(records);
    }
}
