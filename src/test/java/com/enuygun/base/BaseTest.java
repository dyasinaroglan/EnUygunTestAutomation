package com.enuygun.base;

import com.enuygun.model.ElementInfo;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class BaseTest {

    protected static WebDriver driver;
    protected static WebDriverWait wait;


    protected static Actions action;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    DesiredCapabilities capabilities;
    ChromeOptions chromeOptions;
    FirefoxOptions firefoxOptions;
    EdgeOptions edgeOptions;

    String browserName = "chrome";
    String selectPlatform = "windows";

    String testUrl = "https://www.enuygun.com/";

    ConcurrentMap<String, Object> elementMapList = new ConcurrentHashMap<>();

    public ChromeOptions chromeOptions() {
        chromeOptions = new ChromeOptions();
        capabilities = DesiredCapabilities.chrome();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        chromeOptions.setExperimentalOption("prefs", prefs);
        chromeOptions.addArguments("--ignore-certificate-errors");
        chromeOptions.addArguments("--disable-notifications");
        chromeOptions.addArguments("--disable-notifications");
        System.setProperty("webdriver.chrome.driver", "web_driver/chromedriver.exe");
        chromeOptions.merge(capabilities);
        return chromeOptions;
    }

    public FirefoxOptions firefoxOptions() {
        firefoxOptions = new FirefoxOptions();
        capabilities = DesiredCapabilities.firefox();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        chromeOptions.setExperimentalOption("prefs", prefs);
        chromeOptions.addArguments("--ignore-certificate-errors");
        chromeOptions.addArguments("--kiosk");
        chromeOptions.addArguments("--disable-notifications");
        chromeOptions.addArguments("--start-fullscreen");
        System.setProperty("webdriver.firefox.driver", "web_driver/firefoxdriver.exe");
        firefoxOptions.merge(capabilities);
        return firefoxOptions;
    }

    private void initializeDriver() {
        switch (browserName.toLowerCase()) {
            case "chrome":
                driver = new ChromeDriver(chromeOptions());
                break;
            case "firefox":
                driver = new FirefoxDriver(firefoxOptions());
                break;
            default:
                throw new IllegalStateException("Desteklenmeyen tarayıcı");
        }
    }

    private void setupBrowser() {
        if ("mac".equalsIgnoreCase(selectPlatform) || "windows".equalsIgnoreCase(selectPlatform)) {
            initializeDriver();
            driver.manage().timeouts().pageLoadTimeout(45, TimeUnit.SECONDS);
            driver.get(testUrl);
            logger.info(testUrl + " adresi açılıyor.");

            if ("windows".equalsIgnoreCase(selectPlatform)) {
                action = new Actions(driver);
            }
        }
    }
    @BeforeScenario
    public void startSetup() {
        try {
            logger.info("Cihazda " + selectPlatform + " ortamında " + browserName + " browserinda test ayağa kalkacak");
            setupBrowser();

        } catch (Exception e) {
            logger.info("Driver başlatılırken hata oluştu " + e.getMessage());
        }
    }
    @AfterScenario
    public void quitDriver() {
        driver.quit();
    }
    public void initMap(List<File> fileList) throws FileNotFoundException {
        elementMapList = new ConcurrentHashMap<>();
        Type elementType = new TypeToken<List<ElementInfo>>() {
        }.getType();
        Gson gson = new Gson();
        List<ElementInfo> elementInfoList = null;
        for (File file : fileList) {
            try {
                FileReader filez = new FileReader(file);
                elementInfoList = gson.fromJson(new FileReader(file), elementType);
                elementInfoList.parallelStream().forEach(elementInfo -> elementMapList.put(elementInfo.getKey(), elementInfo));
            } catch (FileNotFoundException e) {

            }
        }
    }

    public static List<File> getFileList(String directoryName) throws IOException { //Json dosyalarını bulup liste haline döndüren metod, kütüphane
        List<File> dirList = new ArrayList<>(); //File nesnelerini saklayacak bir list oluşturulur
        try (Stream<Path> walkStream = Files.walk(Paths.get(directoryName))) { // tüm dosya ve klasörleri gezer, kütüphanede rafları gezmesi örneği
            walkStream.filter(p -> p.toFile().isFile()).forEach(f -> { // sadece dosyaları filtreler, kütüphanede yer alan kitapları sadece (gazete ve dergi değil)
                if (f.toString().endsWith(".json")) { // dosya json ile bitiyorsa dirList değişkenine ekler, kütüphanede sadece romanları aramak gibi
                    dirList.add(f.toFile());
                }
            });
        }
        return dirList;
    }
    public ElementInfo findElementInfoByKey(String key) {
        return (ElementInfo) elementMapList.get(key);
    }

    public void saveValue(String key, String value) {
        elementMapList.put(key, value);
    }

    public String getValue(String key) {
        return elementMapList.get(key).toString();

    }
}
