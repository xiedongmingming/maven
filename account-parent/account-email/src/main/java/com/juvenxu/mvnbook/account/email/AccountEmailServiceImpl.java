package com.juvenxu.mvnbook.account.email;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class AccountEmailServiceImpl implements AccountEmailService {

	// 使用SPRING的JAVAMAILSENDER发送邮件: spring-context-support.jar

	// 这段JAVA代码中没有邮件服务器配置信息(这得益于SPRINGFRAMEWORK的依赖注入),这些配置都通过外部的配置注入到了JAVAMAILSENDER对象中
	
	private JavaMailSender javaMailSender;// 帮助简化邮件发送的工具类库

    private String systemEmail;

	// ****************************************************************************************
	@Override
	public void sendMail(String to, String subject, String htmlText) throws AccountEmailException {
		try {

			MimeMessage msg = javaMailSender.createMimeMessage();// 该对象对应了将要发送的邮件

			MimeMessageHelper msgHelper = new MimeMessageHelper(msg);// 帮助设置该邮件的发送地址、收件地址、主题以及内容

			msgHelper.setFrom(systemEmail);
			msgHelper.setTo(to);
			msgHelper.setSubject(subject);
			msgHelper.setText(htmlText, true);// 第二个参数表示邮件的内容为HTML格式

			javaMailSender.send(msg);// 发送邮件

		} catch (MessagingException e) {
			throw new AccountEmailException("faild to send mail.", e);
        }
    }
	// ****************************************************************************************
	public JavaMailSender getJavaMailSender() {
        return javaMailSender;
    }
	public void setJavaMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
	public String getSystemEmail() {
        return systemEmail;
    }
	public void setSystemEmail(String systemEmail) {
        this.systemEmail = systemEmail;
    }
	// ****************************************************************************************
	public static void main(String[] args) throws AccountEmailException, InterruptedException, IOException, MessagingException {

		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);// 根据指定协议创建测试服务器

		greenMail.setUser("test@juvenxu.com", "123456");

		greenMail.start();

		@SuppressWarnings("resource")
		ApplicationContext ctx = new ClassPathXmlApplicationContext("account-email.xml");

		AccountEmailService accountEmailService = (AccountEmailService) ctx.getBean("accountEmailService");

		String subject = "Test Subject";
		String htmlText = "<h3>Test</h3>";

		accountEmailService.sendMail("test2@juvenxu.com", subject, htmlText);

		greenMail.waitForIncomingEmail(2000, 1);// 表示接收一封邮件最多等待2秒

		Message[] msgs = greenMail.getReceivedMessages();
		for (Message msg : msgs) {
			System.out.println(msg.getContent());
			System.out.println(msg.getSubject());
			System.out.println(msg.getContentType());
		}

		greenMail.stop();
	}
	// ****************************************************************************************
}
