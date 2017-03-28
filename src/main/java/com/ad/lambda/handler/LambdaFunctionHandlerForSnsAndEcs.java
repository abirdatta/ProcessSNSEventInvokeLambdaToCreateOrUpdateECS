package com.ad.lambda.handler;

import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import com.ad.lambda.model.ECSServiceRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.LogType;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author abirdatta
 *
 */
public class LambdaFunctionHandlerForSnsAndEcs implements RequestHandler<SNSEvent, String> {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    public String handleRequest(SNSEvent request, Context context) {
        AWSCredentials awsCreds = new EnvironmentVariableCredentialsProvider().getCredentials();

        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCreds);

        AWSLambdaClient awsLambdaClient = (AWSLambdaClient) AWSLambdaClientBuilder.standard()
                .withCredentials(awsCredentialsProvider).withRegion("us-east-1").build();

        InvokeRequest invokeRequest = new InvokeRequest().withFunctionName("LambdaFunctionToCreateOrUpdateECS").withLogType(LogType.Tail);
        for (SNSRecord snsRecord : request.getRecords()) {
            logger.info("Record Direct: " + snsRecord.getSNS().getMessage());
            invokeRequest.setPayload(snsRecord.getSNS().getMessage());
        }
        InvokeResult invokeResult = awsLambdaClient.invoke(invokeRequest);
        logger.info(StringUtils.newStringUtf8(Base64.decodeBase64(invokeResult.getLogResult())));
        logger.info(invokeResult.getStatusCode()==200 ? "Successfully updated ECS" : "Error in updating ECS");
        return invokeResult.getStatusCode()==200 ? "Successfully updated ECS" : "Error in updating ECS";
    }

    private ECSServiceRequest getTransformedEcsRequestObject(SNSEvent request, ECSServiceRequest ecsServiceRequest) {
        for (SNSRecord snsRecord : request.getRecords()) {
            System.out.println("Record Direct: " + snsRecord.getSNS().getMessage());
            ObjectMapper mapper = new ObjectMapper();
            try {
                ecsServiceRequest = mapper.readValue(snsRecord.getSNS().getMessage(), ECSServiceRequest.class);
            } catch (Exception e) {
                System.out.println("Invalid json provided for update");
                e.printStackTrace();
            }
            logger.info("Parsing object createorupdate field from json -- >" + ecsServiceRequest.getUpdateOrCreate());
        }
        return ecsServiceRequest;
    }

}
