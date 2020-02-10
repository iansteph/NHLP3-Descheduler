package iansteph.nhlp3.descheduler.model.event.sns;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageAttribute {

    private String type;
    private String value;

    public String getType() {

        return type;
    }

    public void setType(final String type) {

        this.type = type;
    }

    public String getValue() {

        return value;
    }

    public void setValue(final String value) {

        this.value = value;
    }

    @Override
    public String toString() {

        return "MessageAttribute{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageAttribute that = (MessageAttribute) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, value);
    }
}
