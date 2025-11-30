package CoreFramework.pages;

import CoreFramework.actions.UIActions;
import CoreFramework.utils.reporting.StepLogger;
import io.qameta.allure.Step;

public class LoginPage {

    String emailFieldSelector ="email";
    String passwordFieldSelector="password";
    String loginButtonSelector="button[class *= 'css-1l6prjt']";
    String loginPageURL="https://testing.wuzzuf.basharsys.com/login";
    String emailOrPasswordErrorMessageSelector= ".css-3ens4a.e8gzw8v1";
    String emailFieldErrorMessage="//span[@class='css-y0dsrb etjvxgw0'][contains(. ,'email')]";
    String passwordFieldErrorMessage="//span[@class='css-y0dsrb etjvxgw0'][contains(. ,'password')]";






    UIActions uiActions;
    public LoginPage(){uiActions=new UIActions();}

    @Step("Go to the login Page")
    public void navigateToLoginPage(){
        StepLogger.logStep("Go to the login Page");
        uiActions.navigateToPage(loginPageURL);

    }

    @Step("Enter the user mail: {email}")
    public void enterEmail(String email) throws InterruptedException {
        StepLogger.logStep("Enter the user mail: " + email);
        uiActions.setText(UIActions.SelectorType.name, emailFieldSelector,email);
    }

    @Step("Enter the user password: {password}")
    public void enterPassword(String password) throws InterruptedException {
        StepLogger.logStep("Enter the user password: "+password);
        uiActions.clearText(UIActions.SelectorType.name,passwordFieldSelector);
        uiActions.setText(UIActions.SelectorType.name,passwordFieldSelector,password);
    }

    @Step("Click on login button")
    public void clickLoginButton() throws InterruptedException {
        StepLogger.logStep("Click on login button");
        uiActions.click(UIActions.SelectorType.cssSelector,loginButtonSelector);
    }

    public void loginToWebsite(String email,String password) throws InterruptedException {
        enterEmail(email);
        enterPassword(password);
        clickLoginButton();
    }
    public String getEmailOrPasswordErrorMessage() throws InterruptedException {
        return uiActions.getText(UIActions.SelectorType.cssSelector,emailOrPasswordErrorMessageSelector);
    }
    public String getEmailFieldErrorMessage() throws InterruptedException {
        return uiActions.getText(UIActions.SelectorType.xpath,emailFieldErrorMessage);
    }
    public String getPasswordFieldErrorMessage() throws InterruptedException {
        return uiActions.getText(UIActions.SelectorType.xpath,passwordFieldErrorMessage);
    }

}
