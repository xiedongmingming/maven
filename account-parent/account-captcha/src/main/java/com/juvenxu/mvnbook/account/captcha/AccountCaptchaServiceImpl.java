package com.juvenxu.mvnbook.account.captcha;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

public class AccountCaptchaServiceImpl implements AccountCaptchaService, InitializingBean {

    private DefaultKaptcha producer;

    private Map<String, String> captchaMap = new HashMap<String, String>();

    private List<String> preDefinedTexts;

    private int textCount = 0;

	// ****************************************************************************************
	@Override
	public void afterPropertiesSet() throws Exception {
        producer = new DefaultKaptcha();
		producer.setConfig(new Config(new Properties()));
    }

	@Override
	public String generateCaptchaKey() {
        String key = RandomGenerator.getRandomString();
        String value = getCaptchaText();
		captchaMap.put(key, value);
        return key;
    }

	@Override
	public List<String> getPreDefinedTexts() {
        return preDefinedTexts;
    }

	@Override
	public void setPreDefinedTexts(List<String> preDefinedTexts) {
        this.preDefinedTexts = preDefinedTexts;
    }

	@Override
	public byte[] generateCaptchaImage(String captchaKey) throws AccountCaptchaException {
		String text = captchaMap.get(captchaKey);
		if (text == null) {
			throw new AccountCaptchaException("captch key '" + captchaKey + "' not found!");
        }
		BufferedImage image = producer.createImage(text);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "jpg", out);
		} catch (IOException e) {
			throw new AccountCaptchaException("failed to write captcha stream!", e);
        }
        return out.toByteArray();
    }

	@Override
	public boolean validateCaptcha(String captchaKey, String captchaValue) throws AccountCaptchaException {
		String text = captchaMap.get(captchaKey);
		if (text == null) {
			throw new AccountCaptchaException("captch key '" + captchaKey + "' not found!");
        }
		if (text.equals(captchaValue)) {
			captchaMap.remove(captchaKey);
            return true;
		} else {
            return false;
        }
    }

	// ****************************************************************************************
	private String getCaptchaText() {
		if (preDefinedTexts != null && !preDefinedTexts.isEmpty()) {
			String text = preDefinedTexts.get(textCount);
			textCount = (textCount + 1) % preDefinedTexts.size();
			return text;
		} else {
			return producer.createText();
		}
	}
	// ****************************************************************************************

	public static void main(String[] args) throws AccountCaptchaException, IOException {

		ApplicationContext ctx = new ClassPathXmlApplicationContext("account-captcha.xml");

		AccountCaptchaService service = (AccountCaptchaService) ctx.getBean("accountCaptchaService");

		String captchaKey = service.generateCaptchaKey();// 生成验证码标识

		byte[] captchaImage = service.generateCaptchaImage(captchaKey);// 根据验证码标识生成图片

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
	}
}
