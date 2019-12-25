package iansteph.nhlp3.descheduler.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import iansteph.nhlp3.descheduler.model.event.PlayEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handler for requests to Lambda function.
 */
public class DeschedulerHandler implements RequestHandler<PlayEvent, Object> {

    private static final Logger logger = LogManager.getLogger(DeschedulerHandler.class);

    public Object handleRequest(final PlayEvent playEvent, final Context context) {
        // List targets
        // If there are targets:
        //     Remove the target
        //     Put event in SQS queue with delay
        //     Complete
        // Else:
        //     Delete the Rule
        return null;
    }
}
