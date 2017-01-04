package com.skipjaq.awspricing.pricing;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.cloudformation.AmazonCloudFormationAsyncClient;
import com.amazonaws.services.cloudformation.model.EstimateTemplateCostRequest;
import com.amazonaws.services.cloudformation.model.EstimateTemplateCostResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mirek on 04.01.17.
 */
@Service
public class AwsClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsClient.class);

    private AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

    public URL getAwsCalcURL(String cloudFormationTemplateJson) throws Exception {
        AmazonCloudFormationAsyncClient amazonCloudFormationAsyncClient = new AmazonCloudFormationAsyncClient(
                credentialsProvider.getCredentials());
        EstimateTemplateCostRequest request = new EstimateTemplateCostRequest().withTemplateBody(
                cloudFormationTemplateJson);
        EstimateTemplateCostResult estimateTemplateCostResult = amazonCloudFormationAsyncClient.estimateTemplateCost(
                request);
        URL calcUrl;
        try {
            LOGGER.info("AWS calculator url {}", estimateTemplateCostResult.getUrl());
            return new URL(estimateTemplateCostResult.getUrl());
        } catch (MalformedURLException e) {
            throw new Exception(e);
        }
    }

    public String getRegionLocationDescription(String region) {
        return "US West (N. California)";
    }
}
