package com.juvenxu.mvnbook.account.persist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AccountPersistServiceImpl implements AccountPersistService {

	private static final String ELEMENT_ROOT = "account-persist";
	private static final String ELEMENT_ACCOUNTS = "accounts";
	private static final String ELEMENT_ACCOUNT = "account";
	private static final String ELEMENT_ACCOUNT_ID = "id";
	private static final String ELEMENT_ACCOUNT_NAME = "name";
	private static final String ELEMENT_ACCOUNT_EMAIL = "email";
	private static final String ELEMENT_ACCOUNT_PASSWORD = "password";
	private static final String ELEMENT_ACCOUNT_ACTIVATED = "activated";
	
	private String file;// 存储文件(具体值由SPRINGFRAMEWORK注入)

    private SAXReader reader = new SAXReader();

	// *******************************************************************************************
	public String getFile() {
        return file;
    }
	public void setFile(String file) {
        this.file = file;
    }
	// *******************************************************************************************
	// 实现的接口函数
	@Override
	public Account createAccount(Account account) throws AccountPersistException {

    	Document doc = readDocument();
    	
    	Element accountsEle = doc.getRootElement().element(ELEMENT_ACCOUNTS);
    	
		accountsEle.add(buildAccountElement(account));
    	
		writeDocument(doc);
    	
    	return account;
    }

	@Override
	@SuppressWarnings("unchecked")
	public void deleteAccount(String id) throws AccountPersistException {

        Document doc = readDocument();

		Element accountsEle = doc.getRootElement().element(ELEMENT_ACCOUNTS);

		for (Element accountEle : (List<Element>) accountsEle.elements()) {

			if (accountEle.elementText(ELEMENT_ACCOUNT_ID).equals(id)) {

                accountEle.detach();

                writeDocument( doc );

                return;
            }
        }
    }

    @Override
	@SuppressWarnings("unchecked")
	public Account readAccount(String id) throws AccountPersistException {

        Document doc = readDocument();

		Element accountsEle = doc.getRootElement().element(ELEMENT_ACCOUNTS);

		for (Element accountEle : (List<Element>) accountsEle.elements()) {

			if (accountEle.elementText(ELEMENT_ACCOUNT_ID).equals(id)) {

				return buildAccount(accountEle);

            }
        }

        return null;
    }

	@Override
	public Account updateAccount(Account account) throws AccountPersistException {

		if (readAccount(account.getId()) != null) {

			deleteAccount(account.getId());
    		
			return createAccount(account);
    	}
    	
    	return null;
    }
	// *******************************************************************************************

	private Account buildAccount(Element element) {// 表示由XML创建一个账户

        Account account = new Account();

		account.setId(element.elementText(ELEMENT_ACCOUNT_ID));
		account.setName(element.elementText(ELEMENT_ACCOUNT_NAME));
		account.setEmail(element.elementText(ELEMENT_ACCOUNT_EMAIL));
		account.setPassword(element.elementText(ELEMENT_ACCOUNT_PASSWORD));
		account.setActivated(("true".equals(element.elementText(ELEMENT_ACCOUNT_ACTIVATED)) ? true : false));

        return account;
    }

	private Element buildAccountElement(Account account) {// 创建一个账户对应的XML元素

		Element element = DocumentFactory.getInstance().createElement(ELEMENT_ACCOUNT);
    	
		element.addElement(ELEMENT_ACCOUNT_ID).setText(account.getId());
		element.addElement(ELEMENT_ACCOUNT_NAME).setText(account.getName());
		element.addElement(ELEMENT_ACCOUNT_EMAIL).setText(account.getEmail());
		element.addElement(ELEMENT_ACCOUNT_PASSWORD).setText(account.getPassword());
		element.addElement(ELEMENT_ACCOUNT_ACTIVATED).setText(account.isActivated() ? "true" : "false");
    	
    	return element;
    }

	// *******************************************************************************************
	private Document readDocument() throws AccountPersistException {// 读取文档

		File dataFile = new File(file);
    	
		if (!dataFile.exists()) {// 当不存在时创建

    		dataFile.getParentFile().mkdirs();
    		
    		Document doc = DocumentFactory.getInstance().createDocument();
    		
			Element rootEle = doc.addElement(ELEMENT_ROOT);
		
			rootEle.addElement(ELEMENT_ACCOUNTS);
    		
    		writeDocument( doc );
    	}
    	
		try {
			return reader.read(new File(file));
		} catch (DocumentException e) {
			throw new AccountPersistException("unable to read persist data xml", e);
        }
    }
	private void writeDocument(Document doc) throws AccountPersistException {

    	Writer out = null;
    	
		try {

			out = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
            
			XMLWriter writer = new XMLWriter(out, OutputFormat.createPrettyPrint());// 第二个参数用来创建一个带缩进及换行的友好格式
            
			writer.write(doc);

		} catch (IOException e) {
			throw new AccountPersistException("unable to write persist data xml", e);
		} finally {
			try {
				if (out != null) {
        			out.close();
        		}
			} catch (IOException e) {
				throw new AccountPersistException("unable to close persist data xml writer", e);
        	}
        }
    }
	// *******************************************************************************************
	public static void main(String[] args) throws AccountPersistException {

		File persistDataFile = new File("persist-data.xml");

		if (persistDataFile.exists()) {
			persistDataFile.delete();
		}

		@SuppressWarnings("resource")
		ApplicationContext ctx = new ClassPathXmlApplicationContext("account-persist.xml");

		AccountPersistService service = (AccountPersistService) ctx.getBean("accountPersistService");

		Account account = new Account();
		account.setId("mingxie");
		account.setName("Xie Ming");
		account.setEmail("mingxie@yosemitecloud.com");
		account.setPassword("xxxxx");
		account.setActivated(true);

		service.createAccount(account);
	}
}
