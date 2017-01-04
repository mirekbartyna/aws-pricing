package com.skipjaq;

import com.skipjaq.awspricing.pricing.AwsClient;
import com.skipjaq.awspricing.pricing.AwsPricing;
import com.skipjaq.awspricing.pricing.CloudFormationTemplatePriceScanner;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class AwsPricingApplication {
	public static void main(String[] args) {
		SpringApplication.run(AwsPricingApplication.class, args);

		String cfTemplate = null;
		try {
			cfTemplate = new String(Files.readAllBytes(Paths.get("/home/mirek/skipjaq/CF_templates/incglobal.json")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		AwsClient awsClient = new AwsClient();
//        URL awsCalcUrl = null;
//        try {
//            awsCalcUrl = awsClient.getAwsCalcURL(cfTemplate);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
		String region = "eu-west-1";
		String regionLocationDesc = awsClient.getRegionLocationDescription(region);
//        SeleniumCrawler seleniumCrawler = new SeleniumCrawler();
//        System.out.println("Crawler price = " + seleniumCrawler.getTotalPrice(awsCalcUrl.toString()));

		List<String> ec2InstanceTypes = null;
		CloudFormationTemplatePriceScanner cfTemplatePriceScanner = CloudFormationTemplatePriceScanner.builder()
				.withCFTemplate(cfTemplate).build();
		try {
			ec2InstanceTypes = cfTemplatePriceScanner.scanCFTemplate();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		AwsPricing awsPricing = new AwsPricing();
		double price = awsPricing.getTotalPrice(regionLocationDesc, ec2InstanceTypes);
		System.out.println("JSON price per hour = " + price + "$");
	}
}
