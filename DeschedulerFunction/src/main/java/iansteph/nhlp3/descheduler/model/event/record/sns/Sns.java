package iansteph.nhlp3.descheduler.model.event.record.sns;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sns {

    private String signatureVersion;
    private String timestamp;
    private String signature;
    private String signingCertUrl;
    private String messageId;
    private String message;
    private Map<String, MessageAttribute> messageAttributes;
    private String type;
    private String topicArn;
    private String subject;

    public String getSignatureVersion() {

        return signatureVersion;
    }

    public void setSignatureVersion(final String signatureVersion) {

        this.signatureVersion = signatureVersion;
    }

    public String getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(final String timestamp) {

        this.timestamp = timestamp;
    }

    public String getSignature() {

        return signature;
    }

    public void setSignature(final String signature) {

        this.signature = signature;
    }

    public String getSigningCertUrl() {

        return signingCertUrl;
    }

    public void setSigningCertUrl(final String signingCertUrl) {

        this.signingCertUrl = signingCertUrl;
    }

    public String getMessageId() {

        return messageId;
    }

    public void setMessageId(final String messageId) {

        this.messageId = messageId;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(final String message) {

        this.message = message;
    }

    public Map<String, MessageAttribute> getMessageAttributes() {

        return messageAttributes;
    }

    public void setMessageAttributes(final Map<String, MessageAttribute> messageAttributes) {

        this.messageAttributes = messageAttributes;
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

        return "Sns{" +
                "signatureVersion='" + signatureVersion + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", signature='" + signature + '\'' +
                ", signingCertUrl='" + signingCertUrl + '\'' +
                ", messageId='" + messageId + '\'' +
                ", message='" + message + '\'' +
                ", messageAttributes=" + messageAttributes +
                ", type='" + type + '\'' +
                ", topicArn='" + topicArn + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sns sns = (Sns) o;
        return Objects.equals(signatureVersion, sns.signatureVersion) &&
                Objects.equals(timestamp, sns.timestamp) &&
                Objects.equals(signature, sns.signature) &&
                Objects.equals(signingCertUrl, sns.signingCertUrl) &&
                Objects.equals(messageId, sns.messageId) &&
                Objects.equals(message, sns.message) &&
                Objects.equals(messageAttributes, sns.messageAttributes) &&
                Objects.equals(type, sns.type) &&
                Objects.equals(topicArn, sns.topicArn) &&
                Objects.equals(subject, sns.subject);
    }

    @Override
    public int hashCode() {

        return Objects.hash(signatureVersion, timestamp, signature, signingCertUrl, messageId, message, messageAttributes, type, topicArn,
                subject);
    }
}
