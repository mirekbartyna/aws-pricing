package com.skipjaq.awspricing.pricing;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mirek on 04.01.17.
 */
public class CloudFormationTemplatePriceScanner {
    private List<String> ec2InstanceTypes = new ArrayList<>();
    private String cloudFormationTemplate;

    private CloudFormationTemplatePriceScanner(String cloudFormationTemplate) {
        this.cloudFormationTemplate = cloudFormationTemplate;
    }

    @SuppressWarnings("unchecked")
    public List<String> scanCFTemplate() throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(cloudFormationTemplate);

        // TODO(mirek) get collection instance-number, not instance1, instance2, ...
        findJsonObject(jsonObject, "InstanceType");

        System.out.println(ec2InstanceTypes);

        return ec2InstanceTypes;
    }

    private void findJsonObject(JSONObject jsonObj, String searchedKey) {
        for (Object key : jsonObj.keySet()) {
            String keyStr = (String) key;
            Object keyValue = jsonObj.get(keyStr);

            if (keyStr.equals(searchedKey)) {
                ec2InstanceTypes.add(keyValue.toString());
            }

            if (keyValue instanceof JSONObject)
                findJsonObject((JSONObject) keyValue, searchedKey);
        }
    }

    public static CloudFormationTemplatePriceScannerBuilder builder() {
        return new CloudFormationTemplatePriceScannerBuilder();
    }

    public static class CloudFormationTemplatePriceScannerBuilder {
        private String cloudFormationTemplate;

        public CloudFormationTemplatePriceScannerBuilder withCFTemplate(String cfTemplate) {
            this.cloudFormationTemplate = cfTemplate;
            return this;
        }

        public CloudFormationTemplatePriceScanner build() {
            return new CloudFormationTemplatePriceScanner(cloudFormationTemplate);
        }
    }
}
