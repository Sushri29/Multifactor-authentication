package com.example.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

public class SeleniumExecutor {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public SeleniumExecutor(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Pass a variable to the login field and click the Next button.
     */
    public void SetLoginAndClickNext(String login) {
        WebElement emailInput = wait.until(d -> d.findElement(By.id("email")));
        emailInput.clear();
        emailInput.sendKeys(login);

        WebElement nextBtn = driver.findElement(By.id("next-btn"));
        nextBtn.click();
    }

    /**
     * Open another page using the 'open page' link. Get the code from the opened page and return it.
     */
    public String OpenCodePageAndReturnCode() {
        String originalHandle = driver.getWindowHandle();

        WebElement openLink = wait.until(d -> d.findElement(By.id("open-code-link")));
        // This link opens a new tab/window (target="_blank")
        openLink.click();

        // wait until new window appears
        wait.until((ExpectedCondition<Boolean>) d -> d.getWindowHandles().size() > 1);

        Set<String> handles = driver.getWindowHandles();
        String otherHandle = null;
        for (String h : handles) {
            if (!h.equals(originalHandle)) {
                otherHandle = h;
                break;
            }
        }

        if (otherHandle == null) {
            throw new RuntimeException("No new window found after clicking open page link");
        }

        // switch to new window and read the code
        driver.switchTo().window(otherHandle);
        WebElement codeElem = wait.until(d -> d.findElement(By.id("code-value")));
        String code = codeElem.getText().trim();

        // close the code provider page and switch back
        driver.close();
        driver.switchTo().window(originalHandle);

        return code;
    }

    /**
     * Pass the taken code to the code field and click the Next button.
     */
    public void SetCodeAndClickNext(String code) {
        WebElement codeInput = wait.until(d -> d.findElement(By.id("code")));
        codeInput.clear();
        codeInput.sendKeys(code);

        WebElement codeNextBtn = driver.findElement(By.id("code-next-btn"));
        codeNextBtn.click();
    }

    /**
     * Fill in the masked password fields and click the ‘Log in’ button.
     *
     * The page uses multiple small inputs each with attribute data-pos (1-based position in the password).
     * Some inputs are disabled (masked) and should not be filled. Fill only enabled inputs with characters
     * from the given password according to their data-pos.
     */
    public void FillMaskedPasswordAndClickLogin(String password) {
        // find all inputs representing individual password positions
        // inputs have class "masked-input" and an attribute data-pos (1-based)
        var inputs = wait.until(d -> d.findElements(By.cssSelector("input.masked-input")));
        for (WebElement inp : inputs) {
            boolean enabled = inp.isEnabled();
            if (!enabled) {
                // masked — do not send
                continue;
            }
            String posAttr = inp.getAttribute("data-pos");
            if (posAttr == null) continue;
            int pos = Integer.parseInt(posAttr); // 1-based
            if (pos <= 0 || pos > password.length()) {
                throw new RuntimeException("Position attribute out of password bounds: " + pos);
            }
            char ch = password.charAt(pos - 1);
            inp.clear();
            inp.sendKeys(Character.toString(ch));
        }

        WebElement loginBtn = driver.findElement(By.id("login-btn"));
        loginBtn.click();
    }

    /**
     * Get the text displayed upon positive logging in.
     */
    public String GetLoggedInText() {
        WebElement welcome = wait.until(d -> d.findElement(By.id("welcome")));
        return welcome.getText().trim();
    }
}
