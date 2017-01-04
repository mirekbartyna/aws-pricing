package com.skipjaq.awspricing.pricing;

import com.skipjaq.awspricing.pricing.model.AwsOffer;
import com.skipjaq.awspricing.pricing.model.PricingInfo;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mirek on 04.01.17.
 */
@Service
public class AwsPricing {
    private final static String AWS_OFFERS_URL = "https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/index.json";
    //    private final static String AWS_PRICES_URL = "https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/{offer_code}/current/index.json";
    private final static String AWS_PRICES_URL = "http://localhost:8091/pricingInfo.json";
    private final static String AWS_AMAZON_EC2 = "AmazonEC2";

    private AwsOffer awsOffer;
    private PricingInfo pricingInfo;

    private AwsOffer getAwsOffer(String awsOffersUrl) {
        RestTemplate restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(
                Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM}));
        restTemplate.setMessageConverters(Arrays.asList(converter, new FormHttpMessageConverter()));
        try {
            return restTemplate.getForObject(awsOffersUrl, AwsOffer.class);
        } catch (RestClientException e) {
            e.printStackTrace();
            return null;
        }
    }

    private PricingInfo getAwsPricing(String awsTemplatesUrl, String offerCode) {
        RestTemplate restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(
                Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM}));
        restTemplate.setMessageConverters(Arrays.asList(converter, new FormHttpMessageConverter()));
        return restTemplate.getForObject(awsTemplatesUrl, PricingInfo.class, offerCode);
    }

    private List<PricingInfo.Product> getProductsInLocation(String regionLocationDesc) {
        return pricingInfo.getProducts().values().stream()
                .filter(v -> v.getAttributes().get("servicecode").equals("AmazonEC2") &&
                        v.getAttributes().get("locationType").equals("AWS Region") &&
                        v.getAttributes().get("location").equals(regionLocationDesc))
                .collect(Collectors.toList());
    }

    private List<PricingInfo.Product> getProducts(List<PricingInfo.Product> products, List<String> instanceTypes) {
        return instanceTypes.stream()
                .flatMap(i -> products.stream()
                        // TODO (mirek) warning in logs if more SKU than ONE, take most expensive one
                        .filter(p -> i.equals(p.getAttributes().get("instanceType")) &&
                                "Shared".equals(p.getAttributes().get("tenancy")) &&
                                "Linux".equals(p.getAttributes().get("operatingSystem")) &&
                                "No License required".equals(p.getAttributes().get("licenseModel")) &&
                                "NA".equals(p.getAttributes().get("preInstalledSw"))
                        ))
                .collect(Collectors.toList());
    }

    private List<String> getSkus(List<PricingInfo.Product> products) {
        return products.stream()
                .map(p -> p.getSku())
                .collect(Collectors.toList());
    }

    // i.e. "2016-12-01T00:00:00Z"
    private static Date getDate(String dateTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            Date date = formatter.parse(dateTime);
            return date;
        } catch (ParseException e) {
            Date currentDate = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(currentDate);
            c.roll(Calendar.DAY_OF_MONTH, 1);
            Date yesterday = c.getTime();
            System.out.println("Parse date time went wrong, getting yesterday " + yesterday);
            return yesterday;
        }
    }

    private List<PricingInfo.Term> getTerms(List<String> skus) {
        return skus.stream()
                .map(s -> pricingInfo.getTerms().get("OnDemand").get(s).values())
                .flatMap(v -> v.stream()
                        // TODO(mirek) check if term is only one for product
                        .filter(t -> Date.from(Instant.now()).after(getDate(t.getEffectiveDate())))
                )
                .collect(Collectors.toList());
    }

    private double getPriceFor(List<PricingInfo.Term> terms) {
        List<String> prices = terms.stream()
                .map(t -> t.getPriceDimensions().values().stream()
                        .filter(p -> "Hrs".equals(p.getUnit()))
                        .map(p -> p.getPricePerUnit().get("USD"))
                )
                .flatMap(s -> s)
                .collect(Collectors.toList());
        return prices.stream().map(s -> Double.valueOf(s)).mapToDouble(d -> d).sum();
    }

    public double getTotalPrice(String regionLocationDesc, List<String> instanceTypes) {
        awsOffer = getAwsOffer(AWS_OFFERS_URL);
        pricingInfo = getAwsPricing(AWS_PRICES_URL, awsOffer.getOfferCodeFor(AWS_AMAZON_EC2));
        List<PricingInfo.Product> productsInLocation = getProductsInLocation(regionLocationDesc);
        List<PricingInfo.Product> products = getProducts(productsInLocation, instanceTypes);
        List<String> skus = getSkus(products);
        List<PricingInfo.Term> terms = getTerms(skus);

        return getPriceFor(terms);
    }
}