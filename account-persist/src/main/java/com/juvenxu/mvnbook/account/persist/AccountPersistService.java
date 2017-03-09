package com.juvenxu.mvnbook.account.persist;

public interface AccountPersistService {
	Account createAccount(Account account) throws AccountPersistException;// 创建账户
	Account readAccount(String id) throws AccountPersistException;// 读取账户
	Account updateAccount(Account account) throws AccountPersistException;// 更新账户
	void deleteAccount(String id) throws AccountPersistException;// 删除账户
}
