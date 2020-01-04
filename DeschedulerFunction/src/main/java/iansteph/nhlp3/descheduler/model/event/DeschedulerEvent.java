package iansteph.nhlp3.descheduler.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeschedulerEvent {

    private List<Record> records;

    public List<Record> getRecords() {

        return records;
    }

    public void setRecords(final List<Record> records) {

        this.records = records;
    }

    @Override
    public String toString() {

        return "DeschedulerEvent{" +
                "records=" + records +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeschedulerEvent that = (DeschedulerEvent) o;
        return Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {

        return Objects.hash(records);
    }
}
