package iansteph.nhlp3.descheduler.model.event.sns;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import iansteph.nhlp3.descheduler.deserialization.PlayEventDeserializer;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sns {

    private String Message;

    public String getMessage() {

        return Message;
    }

    public void setMessage(final String message) {

        this.Message = message;
    }

    @Override
    public String toString() {

        return "Sns{" +
                "Message=" + Message +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sns sns = (Sns) o;
        return Objects.equals(Message, sns.Message);
    }

    @Override
    public int hashCode() {

        return Objects.hash(Message);
    }
}
