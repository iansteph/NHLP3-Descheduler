package iansteph.nhlp3.descheduler.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnsMessageLambdaTriggerEvent {

    private List<Record> Records;

    public List<Record> getRecords() {

        return Records;
    }

    public void setRecords(final List<Record> records) {

        this.Records = records;
    }

    @Override
    public String toString() {

        return "SnsMessageLambdaTriggerEvent{" +
                "Records=" + Records +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnsMessageLambdaTriggerEvent that = (SnsMessageLambdaTriggerEvent) o;
        return Objects.equals(Records, that.Records);
    }

    @Override
    public int hashCode() {

        return Objects.hash(Records);
    }
}
