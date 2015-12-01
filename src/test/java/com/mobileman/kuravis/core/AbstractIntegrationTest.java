/*******************************************************************************
 * Copyright 2015 MobileMan GmbH
 * www.mobileman.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.mobileman.kuravis.core;

import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import com.mobileman.kuravis.core.domain.util.RoleUtils;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.services.user.UserServiceTest;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;


/**
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractIntegrationTest extends AbstractJUnit4SpringContextTests {
	
	@Autowired
	protected UserService userService;

	@Autowired
	protected MongoOperations operations;
	
	@Autowired
	protected MongoTemplate mongoTemplate;
	
	protected static Wiser wiser;
	
	/**
	 * @return MongoTemplate
	 */
	public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}
	
	/**
	 * @return this collection name
	 */
	public String getCollectionName() {
		return null;
	}

	/**
	 * 
	 */
	@Before
	public void setUp() {		
		if (wiser == null) {
			wiser = new Wiser();
			wiser.setPort(2525);
			wiser.start();
			
			Map<String, JavaMailSenderImpl> ofType = applicationContext.getBeansOfType(org.springframework.mail.javamail.JavaMailSenderImpl.class);
			  for (Entry<String, JavaMailSenderImpl> bean : ofType.entrySet()) {
				JavaMailSenderImpl mailSender = bean.getValue();
				mailSender.setPort(2525);
				mailSender.setHost("localhost");
				mailSender.setUsername(null);
				mailSender.setPassword(null);
				Properties props = new Properties();
				props.put("mail.transport.protocol", "smtp");
				props.put("mail.smtp.host", "localhost");
				props.put("mail.smtp.auth", "false");
				mailSender.setJavaMailProperties(props);
			  }
		}
		
		if (wiser.getMessages() != null) {
			wiser.getMessages().clear();
		}
	}

	/**
	 * @param collections
	 */
	public void setUp(String... collections) {
		for (String collection : collections) {
			operations.dropCollection(collection);
		}
	}
	
	protected static List<WiserMessage> getEmailMessages() {
		return wiser.getMessages();
	}
	
	/**
	 * 
	 */
	protected void loginTestUserAdmin() {
		userService.signout();
		assertFalse(ErrorUtils.isError(userService.signin("peter.novak2@test.com", "peter.novak", "", false)));
	}
	
	protected void loginAdminIfNeeded() {
		boolean logged = false;
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser != null && currentUser.isAuthenticated()) {
			DBObject user = (DBObject) currentUser.getPrincipal();
			if (RoleUtils.isAdminAccount((DBObject) user.get("account"))) {
				logged = true;
			}
		}
		if (!logged) {
			loginTestUserAdmin();
		}
	}

	
	/**
	 * 
	 */
	protected void signout() {
		userService.signout();
	}

	/**
	 * 
	 */
	protected void loginUnathorizedUser() {
		userService.signout();
		assertFalse(ErrorUtils.isError(userService.signin("peter.novak21@test.com", "peter.novak", "", false)));
	}

	protected void loginAuthorizedUser() {
		userService.signout();
		DBObject user = UserServiceTest.getUsers().get(0);
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "peter.novak", "", false)));
	}
	
	protected void doLogin(String user, String pwd) {
		userService.signout();
		assertFalse(ErrorUtils.isError(userService.signin(user, pwd, "", false)));
	}

	protected int countByIds(String entityName, String idName, List<String> ids) {
		return new Long(getMongoTemplate().getCollection(entityName).count(QueryBuilder.start().put(idName).in(ids).get())).intValue();
	}

}
