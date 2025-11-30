package CoreTest;
import BaseTest.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import CoreFramework.pages.LoginPage;


@Epic("Authentication")
@Feature("Login")
public class LoginTest extends BaseTest {

    private final static String validMAil = "salwajobseeker@gmail.com";
    private final static String emptyMail = "";
    private final static String invalidEmail = "salwaj@gmail";
    private final static  String validPassword = "salwajobseeker@123";
    private final static  String invalidPassword = "salwajookk23";
    private final static String emptyPassword = "";




//    @Test(groups = {"smoke"})
//    @Story("Valid Login")
//    @Severity(SeverityLevel.CRITICAL)
//    @Description("Verify that the user can login with valid credentials")
//    public void LoginWithValidCredential() throws InterruptedException {
//        LoginPage loginPage = new LoginPage();
//        SoftAssert softAssert = new SoftAssert();
//        loginPage.navigateToLoginPage();
//        loginPage.loginToWebsite(validMAil,validPassword);
//        softAssert.assertTrue(true);
//        softAssert.assertAll();
//    }
//
//    @Test(groups = {"smoke"})
//    @Story("Invalid Login")
//    @Severity(SeverityLevel.CRITICAL)
//    @Description("Verify that the user can't login with invalid password")
//    public void LoginWithInValidPassword() throws InterruptedException {
//        String errorMessage="Incorrect email or password";
//        LoginPage loginPage = new LoginPage();
//        SoftAssert softAssert = new SoftAssert();
//        loginPage.navigateToLoginPage();
//        loginPage.loginToWebsite(validPassword,invalidPassword);
//        softAssert.assertEquals(errorMessage,loginPage.getEmailOrPasswordErrorMessage(),"Unexpected error message");
//        softAssert.assertAll();
//
//    }
//
//    @Test(groups = {"smoke"})
//    @Story("Invalid email")
//    @Severity(SeverityLevel.CRITICAL)
//    @Description("Verify that the user can't login with invalid email")
//    public void LoginWithInValidEmail() throws InterruptedException {
//        String errorMessage="Please enter a valid email address";
//        LoginPage loginPage = new LoginPage();
//        SoftAssert softAssert = new SoftAssert();
//        loginPage.navigateToLoginPage();
//        loginPage.loginToWebsite(invalidEmail,validPassword);
//        softAssert.assertEquals(loginPage.getEmailFieldErrorMessage(),errorMessage,"Unexpected error message");
//        softAssert.assertAll();
//    }
//
//
//    @Test(groups = {"smoke"})
//    @Story("Invalid Login")
//    @Severity(SeverityLevel.CRITICAL)
//    @Description("Verify that the user can't login with empty mail")
//    public void LoginWithEmptyEmail() throws InterruptedException {
//        String errorMessage="Please enter your email address";
//        LoginPage loginPage = new LoginPage();
//        SoftAssert softAssert = new SoftAssert();
//        loginPage.navigateToLoginPage();
//        loginPage.loginToWebsite(emptyMail,validPassword);
//        softAssert.assertEquals(loginPage.getEmailFieldErrorMessage(),errorMessage,"Unexpected error message");
//        softAssert.assertAll();
//
//    }

    @Test(groups = {"smoke"})
    @Story("Invalid Login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that the user can't login with empty password")
    public void LoginWithEmptyPassword() throws InterruptedException {
        String errorMessage="Please enkkkkkkkkkkter your password.";
        LoginPage loginPage = new LoginPage();
        SoftAssert softAssert = new SoftAssert();
        loginPage.navigateToLoginPage();
        loginPage.loginToWebsite(validMAil,emptyPassword);
        softAssert.assertEquals(loginPage.getPasswordFieldErrorMessage(),errorMessage,"Unexpected error message");
        softAssert.assertAll();


    }
}
