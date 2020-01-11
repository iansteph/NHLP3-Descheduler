package iansteph.nhlp3.descheduler.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnsDeschedulerEvent implements DeschedulerEvent {

    private List<SnsRecord> records;

    @Override
    public String getPlayEventString() {

        // Sns only returns one record per lambda invocation
        return records.get(0).getPlayEventString();
    }

    @Override
    public List<? extends Record> getRecords() {

        return records;
    }

    public void setRecords(final List<SnsRecord> records) {

        this.records = records;
    }

    @Override
    public String toString() {

        return "SnsDeschedulerEvent{" +
                "records=" + records +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnsDeschedulerEvent that = (SnsDeschedulerEvent) o;
        return Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {

        return Objects.hash(records);
    }
}
