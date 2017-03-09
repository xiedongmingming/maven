package com.juvenxu.mvnbook.account.service;

public interface AccountService {
	String generateCaptchaKey() throws AccountServiceException;// 用来生成一个验证码的唯一标识符
	byte[] generateCaptchaImage(String captchaKey) throws AccountServiceException;// 根据这个标识符生成验证图片(图片以字节流的方式返回)
	void signUp(SignUpRequest signUpRequest) throws AccountServiceException;// 注册(参数为注册信息)
	void activate(String activationNumber) throws AccountServiceException;// 传入激活码以激活账户
	void login(String id, String password) throws AccountServiceException;// 登录
}
