package iansteph.nhlp3.descheduler.model.event.sqsrecord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attributes {

    private String approximateReceiveCount;
    private String sentTimestamp;
    private String sequenceNumber;
    private String messageGroupNumber;
    private String senderId;
    private String messageDeduplicationId;
    private String approximateFirstReceiveTimestamp;

    public String getApproximateReceiveCount() {

        return approximateReceiveCount;
    }

    public void setApproximateReceiveCount(final String approximateReceiveCount) {

        this.approximateReceiveCount = approximateReceiveCount;
    }

    public String getSentTimestamp() {

        return sentTimestamp;
    }

    public void setSentTimestamp(final String sentTimestamp) {

        this.sentTimestamp = sentTimestamp;
    }

    public String getSequenceNumber() {

        return sequenceNumber;
    }

    public void setSequenceNumber(final String sequenceNumber) {

        this.sequenceNumber = sequenceNumber;
    }

    public String getMessageGroupNumber() {

        return messageGroupNumber;
    }

    public void setMessageGroupNumber(final String messageGroupNumber) {

        this.messageGroupNumber = messageGroupNumber;
    }

    public String getSenderId() {

        return senderId;
    }

    public void setSenderId(final String senderId) {

        this.senderId = senderId;
    }

    public String getMessageDeduplicationId() {

        return messageDeduplicationId;
    }

    public void setMessageDeduplicationId(final String messageDeduplicationId) {

        this.messageDeduplicationId = messageDeduplicationId;
    }

    public String getApproximateFirstReceiveTimestamp() {

        return approximateFirstReceiveTimestamp;
    }

    public void setApproximateFirstReceiveTimestamp(final String approximateFirstReceiveTimestamp) {

        this.approximateFirstReceiveTimestamp = approximateFirstReceiveTimestamp;
    }

    @Override
    public String toString() {

        return "Attributes{" +
                "approximateReceiveCount='" + approximateReceiveCount + '\'' +
                ", sentTimestamp='" + sentTimestamp + '\'' +
                ", sequenceNumber='" + sequenceNumber + '\'' +
                ", messageGroupNumber='" + messageGroupNumber + '\'' +
                ", senderId='" + senderId + '\'' +
                ", messageDeduplicationId='" + messageDeduplicationId + '\'' +
                ", approximateFirstReceiveTimestamp='" + approximateFirstReceiveTimestamp + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attributes that = (Attributes) o;
        return Objects.equals(approximateReceiveCount, that.approximateReceiveCount) &&
                Objects.equals(sentTimestamp, that.sentTimestamp) &&
                Objects.equals(sequenceNumber, that.sequenceNumber) &&
                Objects.equals(messageGroupNumber, that.messageGroupNumber) &&
                Objects.equals(senderId, that.senderId) &&
                Objects.equals(messageDeduplicationId, that.messageDeduplicationId) &&
                Objects.equals(approximateFirstReceiveTimestamp, that.approximateFirstReceiveTimestamp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(approximateReceiveCount, sentTimestamp, sequenceNumber, messageGroupNumber, senderId, messageDeduplicationId,
                approximateFirstReceiveTimestamp);
    }
}
