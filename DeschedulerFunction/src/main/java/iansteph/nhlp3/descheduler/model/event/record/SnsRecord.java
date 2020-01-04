package iansteph.nhlp3.descheduler.model.event.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import iansteph.nhlp3.descheduler.model.event.Record;
import iansteph.nhlp3.descheduler.model.event.record.sns.Sns;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnsRecord extends Record {

    private String eventVersion;
    private String eventSubscriptionArn;
    private String eventSource;
    private Sns sns;
    private String type;
    private String topicArn;
    private String subject;

    @Override
    public String getPlayEventAsString() {

        checkNotNull(sns, "SnsRecord cannot contain null Sns object");
        return sns.getMessage();
    }

    public String getEventVersion() {

        return eventVersion;
    }

    public void setEventVersion(final String eventVersion) {

        this.eventVersion = eventVersion;
    }

    public String getEventSubscriptionArn() {

        return eventSubscriptionArn;
    }

    public void setEventSubscriptionArn(final String eventSubscriptionArn) {

        this.eventSubscriptionArn = eventSubscriptionArn;
    }

    public String getEventSource() {

        return eventSource;
    }

    public void setEventSource(final String eventSource) {

        this.eventSource = eventSource;
    }

    public Sns getSns() {

        return sns;
    }

    public void setSns(final Sns sns) {

        this.sns = sns;
    }

    public String getType() {

        return type;
    }

    public void setType(final String type) {

        this.type = type;
    }

    public String getTopicArn() {

        return topicArn;
    }

    public void setTopicArn(final String topicArn) {

        this.topicArn = topicArn;
    }

    public String getSubject() {

        return subject;
    }

    public void setSubject(final String subject) {

        this.subject = subject;
    }

    @Override
    public String toString() {

        return "SnsRecord{" +
                "eventVersion='" + eventVersion + '\'' +
                ", eventSubscriptionArn='" + eventSubscriptionArn + '\'' +
                ", eventSource='" + eventSource + '\'' +
                ", sns=" + sns +
                ", type='" + type + '\'' +
                ", topicArn='" + topicArn + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnsRecord snsRecord = (SnsRecord) o;
        return Objects.equals(eventVersion, snsRecord.eventVersion) &&
                Objects.equals(eventSubscriptionArn, snsRecord.eventSubscriptionArn) &&
                Objects.equals(eventSource, snsRecord.eventSource) &&
                Objects.equals(sns, snsRecord.sns) &&
                Objects.equals(type, snsRecord.type) &&
                Objects.equals(topicArn, snsRecord.topicArn) &&
                Objects.equals(subject, snsRecord.subject);
    }

    @Override
    public int hashCode() {

        return Objects.hash(eventVersion, eventSubscriptionArn, eventSource, sns, type, topicArn, subject);
    }
}
