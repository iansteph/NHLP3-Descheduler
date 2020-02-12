package iansteph.nhlp3.descheduler.model.event.sns;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import iansteph.nhlp3.descheduler.deserialization.PlayEventDeserializer;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sns {

    @JsonProperty(value = "Message")
    @JsonDeserialize(using = PlayEventDeserializer.class)
    private PlayEvent message;

    public PlayEvent getMessage() {

        return message;
    }

    public void setMessage(final PlayEvent message) {

        this.message = message;
    }

    @Override
    public String toString() {

        return "Sns{" +
                "message=" + message +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sns sns = (Sns) o;
        return Objects.equals(message, sns.message);
    }

    @Override
    public int hashCode() {

        return Objects.hash(message);
    }
}
