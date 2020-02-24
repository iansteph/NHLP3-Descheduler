package iansteph.nhlp3.descheduler;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UnitTestBase {

    public SNSEvent createSnsEvent() {

        final String message = "{\"gamePk\":2019020553,\"play\":{\"about\":{\"dateTime\":\"2019-10-28T01:38:15Z\",\"eventId\":54,\"eventI" +
                "dx\":9,\"goals\":{\"away\":0,\"home\":0},\"ordinalNum\":\"1st\",\"period\":1,\"periodTime\":\"01:12\",\"periodTimeRemain" +
                "ing\":\"18:48\",\"periodType\":\"REGULAR\"},\"coordinates\":{\"x\":52,\"y\":-24},\"players\":[{\"player\":{\"fullName\":" +
                "\"Brock Nelson\",\"id\":8475754,\"link\":\"/api/v1/people/8475754\"},\"playerType\":\"Shooter\"},{\"player\":{\"fullName" +
                "\":\"John Gibson\",\"id\":8476434,\"link\":\"/api/v1/people/8476434\"},\"playerType\":\"Goalie\"}],\"result\":{\"descrip" +
                "tion\":\"Brock Nelson Wrist Shot saved by John Gibson\",\"event\":\"Shot\",\"eventCode\":\"NYI54\",\"eventTypeId\":\"SHO" +
                "T\"},\"team\":{\"id\":2,\"link\":\"/api/v1/teams/2\",\"name\":\"New York Islanders\",\"triCode\":\"NYI\"}}}";
        final Map<String, SNSEvent.MessageAttribute> messageAttributeMap = new HashMap<>();
        messageAttributeMap.put("Test", new SNSEvent.MessageAttribute().withValue("TestString").withType("String"));
        messageAttributeMap.put("TestBinary", new SNSEvent.MessageAttribute().withValue("TestBinary").withType("Binary"));
        final SNSEvent.SNS sns = new SNSEvent.SNS()
                .withMessage(message)
                .withMessageAttributes(messageAttributeMap)
                .withMessageId("95df01b4-ee98-5cb9-9903-4c221d41eb5e")
                .withSignature("tcc6faL2yUC6dgZdmrwh1Y4cGa/ebXEkAi6RibDsvpi+tE/1+82j...65r==")
                .withSignatureVersion("1")
                .withSigningCertUrl("https://sns.us-east-2.amazonaws.com/SimpleNotificationService-ac565b8b1a6c5d002d285f9598aa1d9b.pem")
                .withSubject("TestInvoke")
                .withTimestamp(DateTime.now())
                .withTopicArn("arn:aws:sns:us-east-2:123456789012:sns-lambda")
                .withType("Notification")
                .withUnsubscribeUrl("https://sns.us-east-2.amazonaws.com/?Action=Unsubscribe&amp;SubscriptionArn=arn:aws:sns:us-east-2:12" +
                        "3456789012:test-lambda:21be56ed-a058-49f5-8c98-aedd2564c486");
        final SNSEvent.SNSRecord snsRecord = new SNSEvent.SNSRecord()
                .withSns(sns)
                .withEventSource("aws:sns")
                .withEventSubscriptionArn("arn:aws:sns:us-east-2:123456789012:sns-lambda:21be56ed-a058-49f5-8c98-aedd2564c486")
                .withEventVersion("1.0");
        return new SNSEvent()
                .withRecords(Collections.singletonList(snsRecord));
    }
}
