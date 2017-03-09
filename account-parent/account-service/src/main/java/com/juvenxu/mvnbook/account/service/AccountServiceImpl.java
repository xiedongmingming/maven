package com.juvenxu.mvnbook.account.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.juvenxu.mvnbook.account.captcha.AccountCaptchaException;
import com.juvenxu.mvnbook.account.captcha.AccountCaptchaService;
import com.juvenxu.mvnbook.account.captcha.RandomGenerator;
import com.juvenxu.mvnbook.account.email.AccountEmailException;
import com.juvenxu.mvnbook.account.email.AccountEmailService;
import com.juvenxu.mvnbook.account.persist.Account;
import com.juvenxu.mvnbook.account.persist.AccountPersistException;
import com.juvenxu.mvnbook.account.persist.AccountPersistService;

public class AccountServiceImpl implements AccountService {

	private Map<String, String> activationMap = new HashMap<String, String>();// 存放所有待激活的用户:激活码-->账号ID

	// *******************************************************************************************
    private AccountPersistService accountPersistService;
    private AccountEmailService accountEmailService;
    private AccountCaptchaService accountCaptchaService;

	public AccountPersistService getAccountPersistService() {
        return accountPersistService;
    }
	public void setAccountPersistService(AccountPersistService accountPersistService) {
        this.accountPersistService = accountPersistService;
    }
	public AccountEmailService getAccountEmailService() {
        return accountEmailService;
    }
	public void setAccountEmailService(AccountEmailService accountEmailService) {
        this.accountEmailService = accountEmailService;
    }
	public AccountCaptchaService getAccountCaptchaService() {
        return accountCaptchaService;
    }
	public void setAccountCaptchaService(AccountCaptchaService accountCaptchaService) {
        this.accountCaptchaService = accountCaptchaService;
    }
	// *******************************************************************************************
	@Override
	public byte[] generateCaptchaImage(String captchaKey) throws AccountServiceException {
		try {
            return accountCaptchaService.generateCaptchaImage( captchaKey );
		} catch (AccountCaptchaException e) {
			throw new AccountServiceException("unable to generate captcha image.", e);
        }
    }
	@Override
	public String generateCaptchaKey() throws AccountServiceException {
		try {
            return accountCaptchaService.generateCaptchaKey();
		} catch (AccountCaptchaException e) {
			throw new AccountServiceException("unable to generate captcha key.", e);
        }
    }
	@Override
	public void signUp(SignUpRequest signUpRequest) throws AccountServiceException {// 注册账号

		try {
			if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
				throw new AccountServiceException("2 passwords do not match.");
            }

			if (!accountCaptchaService.validateCaptcha(signUpRequest.getCaptchaKey(), signUpRequest.getCaptchaValue())) {
				throw new AccountServiceException("incorrect captcha.");
            }

            Account account = new Account();

			account.setId(signUpRequest.getId());
			account.setEmail(signUpRequest.getEmail());
			account.setName(signUpRequest.getName());
			account.setPassword(signUpRequest.getPassword());
			account.setActivated(false);

			accountPersistService.createAccount(account);

            String activationId = RandomGenerator.getRandomString();

			activationMap.put(activationId, account.getId());

			// 生成激活链接
			String link = signUpRequest.getActivateServiceUrl().endsWith("/") ? signUpRequest.getActivateServiceUrl() + activationId : signUpRequest.getActivateServiceUrl() + "?key=" + activationId;

			accountEmailService.sendMail(account.getEmail(), "please activate your account", link);

		} catch (AccountCaptchaException e) {
			throw new AccountServiceException("unable to validate captcha.", e);
		} catch (AccountPersistException e) {
			throw new AccountServiceException("unable to create account.", e);
		} catch (AccountEmailException e) {
			throw new AccountServiceException("unable to send actiavtion mail.", e);
        }
    }
	@Override
	public void activate(String activationId) throws AccountServiceException {// 执行激活操作

		String accountId = activationMap.get(activationId);

		if (accountId == null) {
			throw new AccountServiceException("invalid account activation id.");
        }

		try {

			Account account = accountPersistService.readAccount(accountId);

			account.setActivated(true);

			accountPersistService.updateAccount(account);

		} catch (AccountPersistException e) {
			throw new AccountServiceException("unable to activate account.");
        }

    }
	@Override
	public void login(String id, String password) throws AccountServiceException {// 执行登录操作

		try {

			Account account = accountPersistService.readAccount(id);// 读取用于的记录

			if (account == null) {// 表示用户不存在
				throw new AccountServiceException("account does not exist.");
            }

			if (!account.isActivated()) {// 表示该用户还未激活
				throw new AccountServiceException("account is disabled.");
            }

			if (!account.getPassword().equals(password)) {// 该用户的密码不对
				throw new AccountServiceException("incorrect password.");
            }

		} catch (AccountPersistException e) {
			throw new AccountServiceException("unable to log in.", e);
        }
    }
	// *******************************************************************************************
	public static void main(String[] args) throws AccountPersistException, AccountServiceException, InterruptedException, MessagingException {

		String[] springConfigFiles = { "account-email.xml", "account-persist.xml", "account-captcha.xml", "account-service.xml" };// 各个模块的SPRING-BEAN配置文件

		@SuppressWarnings("resource")
		ApplicationContext ctx = new ClassPathXmlApplicationContext(springConfigFiles);

		AccountCaptchaService accountCaptchaService = (AccountCaptchaService) ctx.getBean("accountCaptchaService");

		List<String> preDefinedTexts = new ArrayList<String>();
		preDefinedTexts.add("12345");
		preDefinedTexts.add("abcde");
		accountCaptchaService.setPreDefinedTexts(preDefinedTexts);

		AccountService accountService = (AccountService) ctx.getBean("accountService");

		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
		greenMail.setUser("test@juvenxu.com", "123456");
		greenMail.start();

		File persistDataFile = new File("D:\\persist-data.xml");
		if (persistDataFile.exists()) {
			persistDataFile.delete();
		}

		String captchaKey = accountService.generateCaptchaKey();

		accountService.generateCaptchaImage(captchaKey);

		String captchaValue = "12345";

		SignUpRequest signUpRequest = new SignUpRequest();
		signUpRequest.setCaptchaKey(captchaKey);
		signUpRequest.setCaptchaValue(captchaValue);
		signUpRequest.setId("juven");
		signUpRequest.setEmail("test@juvenxu.com");
		signUpRequest.setName("Juven Xu");
		signUpRequest.setPassword("admin123");
		signUpRequest.setConfirmPassword("admin123");
		signUpRequest.setActivateServiceUrl("http://localhost:8080/account/activate");

		accountService.signUp(signUpRequest);

		greenMail.waitForIncomingEmail(2000, 1);
		Message[] msgs = greenMail.getReceivedMessages();
		System.out.println("请激活链接: ");
		String activationLink = GreenMailUtil.getBody(msgs[0]).trim();

		try {
			accountService.login("juven", "admin123");
		} catch (AccountServiceException e) {
			System.out.println("登录失败!!!!!!");
		}

		String activationCode = activationLink.substring(activationLink.lastIndexOf("=") + 1);

		accountService.activate(activationCode);
		accountService.login("juven", "admin123");

		try {
			accountService.login("juven", "admin456");
		} catch (AccountServiceException e) {
			System.out.println("登录失败!!!!!!");
		}

		greenMail.stop();
	}
}
