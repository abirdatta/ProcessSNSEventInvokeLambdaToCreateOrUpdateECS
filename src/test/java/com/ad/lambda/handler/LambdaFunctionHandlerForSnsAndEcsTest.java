package com.ad.lambda.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class LambdaFunctionHandlerForSnsAndEcsTest {

    private static SNSEvent snsevent;

    @BeforeClass
    public static void createInput() throws IOException {
        // TODO: set up your sample input object here.
        snsevent = new SNSEvent();
        File jsonFile = new File("update-payload.json");
        String jsonString = FileUtils.readFileToString(jsonFile);
        
        SNSRecord snsrecord = new SNSRecord();
        SNS sns = new SNS();
        sns.setMessage(jsonString);
        sns.setSubject("Test Message - Testing from Junit");
        sns.setTopicArn("arn:aws:sns:us-east-1:649407818518:code-build-message");
        
        snsrecord.setSns(sns);
        List<SNSRecord> snsrecords = new ArrayList<SNSRecord>();
        snsrecords.add(snsrecord);
        
        snsevent.setRecords(snsrecords);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("FunctionEcsSns");

        return ctx;
    }

    @Test
    public void testLambdaFunctionHandlerForEcs() {
        LambdaFunctionHandlerForSnsAndEcs handler = new LambdaFunctionHandlerForSnsAndEcs();
        Context ctx = createContext();

        String output = handler.handleRequest(snsevent, ctx);

        // TODO: validate output here if needed.
        if (output != null) {
            System.out.println(output.toString());
        }
    }
}
