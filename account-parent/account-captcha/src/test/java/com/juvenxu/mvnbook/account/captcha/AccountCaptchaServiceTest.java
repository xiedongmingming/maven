package com.juvenxu.mvnbook.account.captcha;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AccountCaptchaServiceTest {
	
    private AccountCaptchaService service;

    @Before
    public void prepare() throws Exception {

		ApplicationContext ctx = new ClassPathXmlApplicationContext("account-captcha.xml");// 通过IOC获取BEAN

        service = (AccountCaptchaService) ctx.getBean("accountCaptchaService");
    }

    @Test
    public void testGenerateCaptcha() throws Exception {
		
		String captchaKey = service.generateCaptchaKey();// 获取验证标识码
		
        assertNotNull(captchaKey);

		byte[] captchaImage = service.generateCaptchaImage(captchaKey);// 获取验证图片
		
        assertTrue(captchaImage.length > 0);

        File image = new File("target/" + captchaKey + ".jpg");

        OutputStream output = null;

        try {

            output = new FileOutputStream(image);

            output.write(captchaImage);

        } finally {
            if (output != null) {
                output.close();
            }
        }
        assertTrue(image.exists() && image.length() > 0);
    }

    @Test
    public void testValidateCaptchaCorrect() throws Exception {
		
        List<String> preDefinedTexts = new ArrayList<String>();
		
        preDefinedTexts.add("12345");
        preDefinedTexts.add("abcde");
		
        service.setPreDefinedTexts(preDefinedTexts);

        String captchaKey = service.generateCaptchaKey();
		
        service.generateCaptchaImage(captchaKey);
		
        assertTrue(service.validateCaptcha(captchaKey, "12345"));

        captchaKey = service.generateCaptchaKey();
		
        service.generateCaptchaImage(captchaKey);
		
        assertTrue(service.validateCaptcha(captchaKey, "abcde"));
    }

    @Test
    public void testValidateCaptchaIncorrect() throws Exception {
		
        List<String> preDefinedTexts = new ArrayList<String>();
		
        preDefinedTexts.add("12345");
		
        service.setPreDefinedTexts(preDefinedTexts);

        String captchaKey = service.generateCaptchaKey();
		
        service.generateCaptchaImage(captchaKey);
		
        assertFalse(service.validateCaptcha(captchaKey, "67890"));
    }
}
