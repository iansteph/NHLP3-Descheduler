package iansteph.nhlp3.descheduler.model.event.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import iansteph.nhlp3.descheduler.model.event.Record;
import iansteph.nhlp3.descheduler.model.event.record.sqs.MessageAttribute;

import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SqsRecord extends Record {

    private String messageId;
    private String receiptHandle;
    private String body;
    private Map<String, String> attributes;
    private Map<String, MessageAttribute> messageAttributes;
    private String md5OfBody;
    private String eventSource;
    private String eventSourceArn;
    private String awsRegion;

    @Override
    public String getPlayEventAsString() {

        return body;
    }

    public String getMessageId() {

        return messageId;
    }

    public void setMessageId(final String messageId) {

        this.messageId = messageId;
    }

    public String getReceiptHandle() {

        return receiptHandle;
    }

    public void setReceiptHandle(final String receiptHandle) {

        this.receiptHandle = receiptHandle;
    }

    public String getBody() {

        return body;
    }

    public void setBody(final String body) {

        this.body = body;
    }

    public Map<String, String> getAttributes() {

        return attributes;
    }

    public void setAttributes(final Map<String, String> attributes) {

        this.attributes = attributes;
    }

    public Map<String, MessageAttribute> getMessageAttributes() {

        return messageAttributes;
    }

    public void setMessageAttributes(final Map<String, MessageAttribute> messageAttributes) {

        this.messageAttributes = messageAttributes;
    }

    public String getMd5OfBody() {

        return md5OfBody;
    }

    public void setMd5OfBody(final String md5OfBody) {

        this.md5OfBody = md5OfBody;
    }

    public String getEventSource() {

        return eventSource;
    }

    public void setEventSource(final String eventSource) {

        this.eventSource = eventSource;
    }

    public String getEventSourceArn() {

        return eventSourceArn;
    }

    public void setEventSourceArn(final String eventSourceArn) {

        this.eventSourceArn = eventSourceArn;
    }

    public String getAwsRegion() {

        return awsRegion;
    }

    public void setAwsRegion(final String awsRegion) {

        this.awsRegion = awsRegion;
    }

    @Override
    public String toString() {

        return "SqsRecord{" +
                "messageId='" + messageId + '\'' +
                ", receiptHandle='" + receiptHandle + '\'' +
                ", body='" + body + '\'' +
                ", attributes=" + attributes +
                ", messageAttributes=" + messageAttributes +
                ", md5OfBody='" + md5OfBody + '\'' +
                ", eventSource='" + eventSource + '\'' +
                ", eventSourceArn='" + eventSourceArn + '\'' +
                ", awsRegion='" + awsRegion + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqsRecord sqsRecord = (SqsRecord) o;
        return Objects.equals(messageId, sqsRecord.messageId) &&
                Objects.equals(receiptHandle, sqsRecord.receiptHandle) &&
                Objects.equals(body, sqsRecord.body) &&
                Objects.equals(attributes, sqsRecord.attributes) &&
                Objects.equals(messageAttributes, sqsRecord.messageAttributes) &&
                Objects.equals(md5OfBody, sqsRecord.md5OfBody) &&
                Objects.equals(eventSource, sqsRecord.eventSource) &&
                Objects.equals(eventSourceArn, sqsRecord.eventSourceArn) &&
                Objects.equals(awsRegion, sqsRecord.awsRegion);
    }

    @Override
    public int hashCode() {

        return Objects.hash(messageId, receiptHandle, body, attributes, messageAttributes, md5OfBody, eventSource, eventSourceArn,
                awsRegion);
    }
}
