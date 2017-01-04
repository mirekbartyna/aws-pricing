package com.skipjaq.awspricing.crawlers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by mirek on 04.01.17.
 */
@Service
public class SeleniumCrawler {
    private WebDriver driver;
    private DesiredCapabilities caps;

    private static final String CHROME_DRIVER_PATH = "/home/mirek/bin/chromedriver";
    private static final String PHANTOMJS_DRIVER_PATH = "/home/mirek/bin/phantomjs-2.1.1-linux-x86_64/bin/phantomjs";


    public double getTotalPrice(String calcUrl) {
        try {
            configurePhantomJS();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        driver.get(calcUrl);
        WebElement myDynamicElement = (new WebDriverWait(driver, 10000))
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".billTable > tbody:last-child > tr:last-child input")
                        )
                );

        WebDriverWait wait = new WebDriverWait(driver, 10000);
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                if (myDynamicElement.getAttribute("value").equals("0.00")) {
                    return false;
                }
                return true;
            }
        });
        double price = Double.valueOf(myDynamicElement.getAttribute("value"));

        unconfigure();

        return price;
    }

    private void configureChrome() throws IOException {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
        caps = DesiredCapabilities.chrome();
        caps.setJavascriptEnabled(true);
        driver = new ChromeDriver();
    }

    private void configurePhantomJS() throws IOException {
        System.setProperty(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOMJS_DRIVER_PATH);
        caps = DesiredCapabilities.phantomjs();
        caps.setJavascriptEnabled(true);
        driver = new PhantomJSDriver();
    }


    private void unconfigure() {
        driver.quit();
    }
}
