package iansteph.nhlp3.descheduler.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iansteph.nhlp3.descheduler.model.event.sns.Sns;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Record {

    @JsonProperty(value = "Sns")
    private Sns sns;

    public Sns getSns() {

        return sns;
    }

    public void setSns(final Sns sns) {

        this.sns = sns;
    }

    @Override
    public String toString() {

        return "Record{" +
                "sns=" + sns +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return Objects.equals(sns, record.sns);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sns);
    }
}
