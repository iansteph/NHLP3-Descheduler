package iansteph.nhlp3.descheduler.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import iansteph.nhlp3.descheduler.model.event.sns.Sns;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Record {

    private Sns Sns;

    public Sns getSns() {

        return Sns;
    }

    public void setSns(final Sns sns) {

        this.Sns = sns;
    }

    @Override
    public String toString() {

        return "Record{" +
                "Sns=" + Sns +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return Objects.equals(Sns, record.Sns);
    }

    @Override
    public int hashCode() {

        return Objects.hash(Sns);
    }
}
