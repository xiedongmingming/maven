package com.juvenxu.mvnbook.account.email;

import static junit.framework.Assert.assertEquals;

import javax.mail.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

public class AccountEmailServiceTest {// JAVA的MAIL技术

	private GreenMail greenMail;// 配置并启动一个测试使用的邮件服务器

    @Before
	public void startMailServer() throws Exception {

		greenMail = new GreenMail(ServerSetup.SMTP);// 根据指定协议创建测试服务器

		greenMail.setUser("test@juvenxu.com", "123456");

        greenMail.start();
    }

    @Test
	public void testSendMail() throws Exception {

		@SuppressWarnings("resource")
		ApplicationContext ctx = new ClassPathXmlApplicationContext("account-email.xml");

		AccountEmailService accountEmailService = (AccountEmailService) ctx.getBean("accountEmailService");

        String subject = "Test Subject";
        String htmlText = "<h3>Test</h3>";

		accountEmailService.sendMail("test2@juvenxu.com", subject, htmlText);

		// 邮件发送完毕之后再使用GREENMAIL进行检查
		greenMail.waitForIncomingEmail(2000, 1);// 表示接收一封邮件最多等待2秒

        Message[] msgs = greenMail.getReceivedMessages();

		assertEquals(1, msgs.length);
		assertEquals(subject, msgs[0].getSubject());
		assertEquals(htmlText, GreenMailUtil.getBody(msgs[0]).trim());
    }

    @After
	public void stopMailServer() throws Exception {
        greenMail.stop();
    }
}
