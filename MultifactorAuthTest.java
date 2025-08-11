package com.example.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.URI;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MultifactorAuthTest {

    private WebDriver driver;
    private SeleniumExecutor exec;

    @BeforeAll
    public void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setup() throws Exception {
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        // load local index.html from resources/__files
        URI uri = getClass().getResource("/__files/index.html").toURI();
        driver.get(uri.toString());

        exec = new SeleniumExecutor(driver);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testMultiFactorLoginHappyPath() {
        String email = "alice@example.com";
        String password = "P@ssw0rd"; // this must match masked.html's expected password

        // Step 1: set login and click next
        exec.SetLoginAndClickNext(email);

        // Step 2: open code provider page (new tab), extract code
        String code = exec.OpenCodePageAndReturnCode();

        // Step 3: set code and click next
        exec.SetCodeAndClickNext(code);

        // Step 4: fill masked password and login
        exec.FillMaskedPasswordAndClickLogin(password);

        // Step 5: get logged in text and assert
        String welcome = exec.GetLoggedInText();
        assertEquals("Welcome, Alice!", welcome);
    }
}
