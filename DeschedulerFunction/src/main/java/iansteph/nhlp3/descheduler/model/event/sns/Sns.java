package iansteph.nhlp3.descheduler.model.event.sns;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import iansteph.nhlp3.descheduler.deserialization.PlayEventDeserializer;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;

import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sns {

    @JsonProperty(value = "Timestamp")
    private String timestamp;

    @JsonProperty(value = "MessageId")
    private String messageId;

    @JsonProperty(value = "Message")
    @JsonDeserialize(using = PlayEventDeserializer.class)
    private PlayEvent message;

    private Map<String, MessageAttribute> messageAttributes;

    public String getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(final String timestamp) {

        this.timestamp = timestamp;
    }

    public String getMessageId() {

        return messageId;
    }

    public void setMessageId(final String messageId) {

        this.messageId = messageId;
    }

    public PlayEvent getMessage() {

        return message;
    }

    public void setMessage(final PlayEvent message) {

        this.message = message;
    }

    public Map<String, MessageAttribute> getMessageAttributes() {

        return messageAttributes;
    }

    public void setMessageAttributes(final Map<String, MessageAttribute> messageAttributes) {

        this.messageAttributes = messageAttributes;
    }

    @Override
    public String toString() {

        return "Sns{" +
                "timestamp='" + timestamp + '\'' +
                ", messageId='" + messageId + '\'' +
                ", message=" + message +
                ", messageAttributes=" + messageAttributes +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sns sns = (Sns) o;
        return Objects.equals(timestamp, sns.timestamp) &&
                Objects.equals(messageId, sns.messageId) &&
                Objects.equals(message, sns.message) &&
                Objects.equals(messageAttributes, sns.messageAttributes);
    }

    @Override
    public int hashCode() {

        return Objects.hash(timestamp, messageId, message, messageAttributes);
    }
}
