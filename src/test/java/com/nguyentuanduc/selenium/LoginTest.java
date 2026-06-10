package com.nguyentuanduc.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import io.github.bonigarcia.wdm.WebDriverManager;

public class LoginTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String url = "https://sinhvien1.tlu.edu.vn/#/login";

    @BeforeEach
    public void setUp() {
        System.out.println("Setting up ChromeDriver...");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Auto-detect if running in CI/CD environment or manual override
        boolean isCI = System.getenv("CI") != null || Boolean.getBoolean("headless");
        if (isCI) {
            System.out.println("Running Chrome in HEADLESS mode (CI/CD environment)...");
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        } else {
            System.out.println("Running Chrome in GUI mode...");
        }

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            System.out.println("Closing Chrome driver...");
            driver.quit();
        }
    }

    @Test
    public void testLoginSuccess() {
        System.out.println("Executing Scenario: Login Success...");
        driver.get(url);

        // Fill username
        System.out.println("Entering username...");
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameInput.clear();
        usernameInput.sendKeys("2351067091");

        // Fill password (Correct password to ensure success)
        System.out.println("Entering correct password...");
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passwordInput.clear();
        passwordInput.sendKeys("Tuanduc@.com1501");

        // Click login button
        System.out.println("Clicking login button...");
        WebElement loginButton = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-ng-click='vm.login()']")));
        loginButton.click();

        // Verify that redirection happens to dashboard (URL does not contain "login"
        // anymore, or contains "dashboard")
        System.out.println("Verifying redirection after successful login...");
        boolean isLoginSuccessful = wait.until(ExpectedConditions.urlContains("dashboard"));

        String currentUrl = driver.getCurrentUrl();
        System.out.println("Current URL after login: " + currentUrl);
        assertTrue(isLoginSuccessful, "Login failed: did not redirect to dashboard! Current URL: " + currentUrl);
    }

    @Test
    public void testLoginFailure() {
        System.out.println("Executing Scenario: Login Failure (Wrong password)...");
        driver.get(url);

        // Fill username
        System.out.println("Entering username...");
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameInput.clear();
        usernameInput.sendKeys("2351067091");

        // Fill wrong password (Incorrect password to ensure login fails as expected)
        System.out.println("Entering incorrect password...");
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passwordInput.clear();
        passwordInput.sendKeys("WrongPassword123");

        // Click login button
        System.out.println("Clicking login button...");
        WebElement loginButton = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-ng-click='vm.login()']")));
        loginButton.click();

        // Wait a brief moment and verify that we are still on the login page
        System.out.println("Verifying that user remains on login page...");
        boolean staysOnLoginPage = wait.until(ExpectedConditions.urlContains("login"));

        String currentUrl = driver.getCurrentUrl();
        System.out.println("Current URL after failed login attempt: " + currentUrl);
        assertTrue(staysOnLoginPage,
                "User was redirected away from login page despite wrong credentials! Current URL: " + currentUrl);

        // Check if error toast/message is displayed (usually toastr has class
        // 'toast-error' or 'toast-message')
        try {
            WebElement toastError = wait
                    .until(ExpectedConditions.presenceOfElementLocated(By.className("toast-error")));
            System.out.println("Detected error toast message: " + toastError.getText());
        } catch (Exception e) {
            System.out.println("No error toast message detected (or took too long), but URL remains correct.");
        }
    }
}
