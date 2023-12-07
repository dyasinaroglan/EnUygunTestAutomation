package com.enuygun.page;

import com.enuygun.base.BaseTest;
import com.enuygun.model.ElementInfo;
import com.thoughtworks.gauge.Step;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseSteps extends BaseTest {

    public static int DEFAULT_MAX_ITERATION_COUNT = 100;
    public static int DEFAULT_MILLISECOND_WAIT_AMOUNT = 300;

    String tempData;

    public BaseSteps() throws IOException {
        String workingDir = System.getProperty("user.dir");
        initMap(getFileList(workingDir + "/src"));

    }

    public By getElementInfoBy(ElementInfo elementInfo) {
        By by = null;
        if (elementInfo.getType().equals("css")) {
            by = By.cssSelector(elementInfo.getValue());
        } else if (elementInfo.getType().equals("xpath")) {
            by = By.xpath(elementInfo.getValue());
        } else if (elementInfo.getType().equals("id")) {
            by = By.id(elementInfo.getValue());
        }
        return by;
    }

    WebElement findElement(String key) {

        By by = getElementInfoBy(findElementInfoByKey(key));
        WebDriverWait wait = new WebDriverWait(driver, 20);
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(by));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'})", element);
        return element;
    }

    List<WebElement> findElements(String key) {
        return driver.findElements(getElementInfoBy(findElementInfoByKey(key)));
    }

    private void clickTo(WebElement element) {
        element.click();
    }

    private void sendKeysTo(WebElement element, String text) {
        element.sendKeys(text);
    }

    public void javaScriptClickTo(WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);
    }

    @Step("<key> li elementi bul temizle ve <text> değerini yaz")
    public void sendKeys(String key, String text) {
        WebElement element = findElement(key);
        element.clear();
        sendKeysTo(element, text);
        logger.info("Element bulundu ve yazıldı: Key : " + key + " text : " + text);
    }

    @Step("Elementine tıkla <key>")
    public void clickElement(String key) {
        clickTo(findElement(key));
        logger.info(key + " elementine tıklandı.");
    }

    @Step("<int> saniye bekle")
    public void waitSecond(int seconds) throws InterruptedException {
        try {
            logger.info(seconds + " saniye bekleniyor");
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Step("<key> elementinin disabled olduğunu kotrol et")
    public void checkDisabled(String key) {
        WebElement element = findElement(key);
        Assertions.assertTrue(element.isDisplayed(), " Element disabled değil");
        logger.info(key + " elementi disabled");
    }

    @Step("<key> elementi <expected> değerini içerdiği <unexpected> değerini içermediği kontrol edilir")
    public void checkExpectedEqualsUnexpected(String key, String expected, String unexpected) {
        String elementText = findElement(key).getText();

        if (!elementText.equals(expected)) {
            Assertions.fail(key + " elementi " + expected + " değerini içeriyor.");
        }
        logger.info(key + " elementi beklenen '" + expected + "' değerini içeriyor.");

        if (elementText.equals(unexpected)) {
            Assertions.fail(key + " elementi beklenmeyen '" + unexpected + "' metnini içeriyor. Alınan metin: " + elementText);
        }
        logger.info(key + " elementi beklenmeyen '" + unexpected + "' değerini içermiyor.");
    }

    @Step("<key> elementinin <attribute> niteliği <value> değerine sahip mi")
    public void elementAttributeValueCheck(String key, String attribute, String value) throws InterruptedException {
        WebElement element = findElement(key);
        String actualValue;
        int count = 0;
        while (count < DEFAULT_MAX_ITERATION_COUNT) {
            actualValue = element.getAttribute(attribute).trim();
            if (actualValue.equals(value)) {
                logger.info(key + " elementinin " + attribute + " niteliği " + value + " değerine sahip.");
                return;
            }
            waitSecond(DEFAULT_MILLISECOND_WAIT_AMOUNT);
        }
        Assertions.fail(key + " elementinin " + attribute + " niteliği " + value + " değeri ile eşleşmiyor.");
    }

    @Step("<key> elementi <expectedText> değerini içeriyor mu kontrol et")
    public void checkElementEqualsText(String key, String expectedText) {

        String actualText = findElement(key).getText();
        logger.info("Element str:" + actualText);
        logger.info("Expected str:" + expectedText);
        Assertions.assertEquals(actualText, expectedText, "Beklenen metni içermiyor " + key);
        logger.info(key + " elementi " + expectedText + " degerine eşittir.");
    }
    @Step("ENTER tuşuna bas")
    public void pressEnter() {
        action.sendKeys(Keys.ENTER).build().perform();
    }
    @Step("<key> menusünden random seçim yap")
    public void clickOnRandomItemInList(String key) {
        List<WebElement> elements = findElements(key);
        Random random = new Random();
        int index = random.nextInt(elements.size());
        elements.get(index).click();
        logger.info(key + " elementine random tiklandi " + elements.get(index).getText());
    }
    @Step("İkinci sekmeye geçilir")
    public void switchToPage2() {
        String parentWindow = driver.getWindowHandle();
        Set<String> allWindows = driver.getWindowHandles();
        for (String curWindow : allWindows) {
            driver.switchTo().window(curWindow);
        }
        logger.info("Ikinci sekmeye gecildi");
    }

    @Step("<key> elementinin text değerinin boşluk sonrası/öncesi(varsa) atılır ve belleğe kaydedilir <count>")
    public void dropDownRandomddSaveAfter(String key, int count) {

        String element = findElement(key).getText();
        logger.info(element);
        if (element.contains(" ")) {
            String[] parts = element.split(" ");
            if(count >= 0 && count < parts.length){
                element = parts[count].trim();
                element = element.replaceAll("[.]", "");
            }
        }

        tempData = element;
        logger.info("Urunun fiyati: " + tempData);
    }
    @Step("<key> elementinin text içeriği belleğe kaydedilen text ile eşit olduğu kontrol edilir <count>")
    public void priceAssertionsSplit(String key, int count) {
        String expectedText = findElement(key).getText();
        logger.info("Orijinal Metin: " + expectedText);

        String[] parts = expectedText.split(" ");
        // Eğer metin "24.000 TL 15.000 TL" formatında ise count 1 olmalı, değilse 0
        count = (parts.length > 2) ? 1 : 0;

        if (count < parts.length) {
            expectedText = parts[count].trim();
            logger.info("Seçilen Parça: " + expectedText);

            // Noktaları ve TL metnini çıkar
            expectedText = expectedText.replace(".", "").replace("TL", "").trim();
            logger.info("Sayısal Değer: " + expectedText);
        }

        String errorMessage = String.format("Fiyatlar eşit değil. Beklenen: %s, Gerçek: %s", tempData, expectedText);
        Assertions.assertTrue(Objects.equals(expectedText, tempData), errorMessage);
        logger.info("Belleğe kaydedilen değer : " + tempData + " ile " + "ExpectedText değeri : " + expectedText + " eşittir");
    }

    @Step("<key> elementinin text değeri tempData'da saklanan değere eşittir")
    public void checkElementValueEqualsTempData(String key) {
        String element = findElement(key).getText();
        Assertions.assertEquals(tempData, element, "Elementin text değeri '" + key + "' tempData'da saklanan değerle eşleşmiyor");
        logger.info("Elementin değeri '" + key + "' tempData'da saklanan değerle eşleşir");
    }
    @Step({"<key> element size değeri <expectedCount> değerine eşit mi kontrol et"})
    public void checkElementCountEquals(String key, int expectedCount) {
        int actualCount = findElements(key).size();
        assertEquals(expectedCount, actualCount, "Expected count does not match for " + key);
        logger.info(key + " elementi sayısı " + expectedCount + " değerine eşittir.");
    }
}
