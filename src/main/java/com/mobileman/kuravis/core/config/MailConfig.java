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
/**
 * MailConfig.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 20.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import com.mobileman.kuravis.core.services.messaging.mail.MailService;
import com.mobileman.kuravis.core.services.messaging.mail.impl.MailServiceImpl;

/**
 * @author MobileMan GmbH
 *
 */
@Configuration
@PropertySource({"classpath:/properties/mail.properties"})
public class MailConfig {
	
	@Value("${mail.host}") 
	private String mailHost;
	
	@Value("${mail.port}") 
	private int mailPort = 25;
	
	@Value("${mail.systemAdminEmail}") 
	private String systemAdminEmail;
	
	@Value("${mail.memberEmail}") 
	private String memberEmail;
	
	@Value("${mail.supportEmail}") 
	private String supportEmail;
	
	@Value("${mail.spamEmail}") 
	private String spamEmail;
	
	@Value("${mail.contactEmail}") 
	private String contactEmail;
	
	@Value("${mail.serverDNSName}") 
	private String serverDNSName;
	
	/**
	 * @return velocityEngine
	 */
	@Bean
	public VelocityEngineFactoryBean velocityEngine() {
		VelocityEngineFactoryBean bean = new VelocityEngineFactoryBean();
		bean.setResourceLoaderPath("classpath:/velocity");
		bean.setPreferFileSystemAccess(false);
		return bean;
	}
	
	/**
	 * @return mailSender
	 */
	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl bean = new JavaMailSenderImpl();
		bean.setHost(mailHost);
		bean.setPort(mailPort);
		Properties javaMailProperties = new Properties();
		javaMailProperties.setProperty("mail.smtp.auth", "false");
		javaMailProperties.setProperty("mail.smtp.starttls.enable", "false");
		javaMailProperties.setProperty("mail.mime.charset", "UTF8");
		bean.setJavaMailProperties(javaMailProperties);
		return bean;
	}
	
	/**
	 * @return mailService
	 */
	@Bean
	public MailService mailService() {
		MailServiceImpl bean = new MailServiceImpl();
		bean.setMailSender(mailSender());
		bean.setSystemAdminEmail(systemAdminEmail);
		bean.setMemberEmail(memberEmail);
		bean.setSupportEmail(supportEmail);
		bean.setSpamEmail(spamEmail);
		bean.setKontaktEmail(contactEmail);
		bean.setServerDNSName(serverDNSName);
		return bean;
	}
}
