package com.skipjaq.awspricing.crawlers;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by mirek on 04.01.17.
 */
public class HtmlUnitCrawler {
    public static int getTotalPrice(String calcUrl) {
        //System.getProperties().put("org.apache.commons.logging.simplelog.defaultlog", "trace");
        final int DELAY = 60000;
        WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.setHTMLParserListener(HTMLParserListener.LOG_REPORTER);
        webClient.waitForBackgroundJavaScript(DELAY);
        Optional<HtmlPage> currentPage = Optional.empty();
        try {
            currentPage = Optional.ofNullable(webClient.getPage(calcUrl));
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPage.ifPresent(x -> System.out.println(x.asText()));

        return 1;
    }
}
