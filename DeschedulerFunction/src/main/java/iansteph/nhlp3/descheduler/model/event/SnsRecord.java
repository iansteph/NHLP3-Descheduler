package iansteph.nhlp3.descheduler.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import iansteph.nhlp3.descheduler.model.event.snsrecord.Sns;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnsRecord implements Record {

    private static final Logger logger = LogManager.getLogger(SnsRecord.class);

    private String eventVersion;
    private String eventSubscriptionArn;
    private String eventSource;
    private Sns sns;

    @Override
    public String getPlayEventString() {

        try {

            checkNotNull(sns, "Sns in SnsRecord cannot be null");
            return sns.getMessage();
        }
        catch (NullPointerException e) {

            logger.error(e);
            throw e;
        }
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

    @Override
    public String toString() {

        return "SnsRecord{" +
                "eventVersion='" + eventVersion + '\'' +
                ", eventSubscriptionArn='" + eventSubscriptionArn + '\'' +
                ", eventSource='" + eventSource + '\'' +
                ", sns=" + sns +
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
                Objects.equals(sns, snsRecord.sns);
    }

    @Override
    public int hashCode() {

        return Objects.hash(eventVersion, eventSubscriptionArn, eventSource, sns);
    }
}
