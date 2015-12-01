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
 * SocialConfig.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 13.2.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

import com.mobileman.kuravis.core.social.facebook.ReviewCreation2Facebook;
import com.mobileman.kuravis.core.social.twitter.ReviewCreation2TwitterTransformer;

/**
 * @author MobileMan GmbH
 *
 */
@Configuration
@PropertySource({"classpath:/properties/social.properties", "classpath:/properties/mail.properties"})
public class SocialConfig {
	
	@Value("${facebook.clientId}") 
	private String facebookClientId;
	
	@Value("${facebook.clientSecret}") 
	private String facebookClientSecret;
	
	@Value("${twitter.consumerKey}") 
	private String twitterConsumerKey;
	
	@Value("${twitter.consumerSecret}") 
	private String twitterConsumerSecret;
	
	@Value("${twitter.accessToken}") 
	private String twitterAccessToken;
	
	@Value("${twitter.accessTokenSecret}") 
	private String twitterAccessTokenSecret;
	
	@Value("${mail.serverDNSName}") 
	private String serverDNSName;
	
	/**
	 * @return FacebookConnectionFactory
	 */
	@Bean
	public FacebookConnectionFactory facebookConnectionFactory() {
		FacebookConnectionFactory factory = new FacebookConnectionFactory(facebookClientId, facebookClientSecret);
		return factory;
	}
	
	/**
	 * @return FacebookConnectionFactory
	 */
	@Bean
	public TwitterConnectionFactory twitterConnectionFactory() {
		TwitterConnectionFactory factory = new TwitterConnectionFactory(twitterConsumerKey, twitterConsumerSecret);
		return factory;
	}
	
	/**
	 * @return ConnectionFactoryRegistry
	 */
	@Bean
	public ConnectionFactoryRegistry connectionFactoryLocator() {
		ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
		registry.setConnectionFactories(Arrays.<ConnectionFactory<?>>asList(twitterConnectionFactory(), facebookConnectionFactory()));
		return registry;
	}
	
	/**
	 * @return TwitterTemplate
	 */
	@Bean
	public TwitterTemplate twitterTemplate() {
		TwitterTemplate  template = new TwitterTemplate(twitterConsumerKey, twitterConsumerSecret, twitterAccessToken, twitterAccessTokenSecret);
		return template;
	}
	
	/**
	 * @return ReviewCreation2TwitterTransformer
	 */
	@Bean
	public ReviewCreation2TwitterTransformer reviewCreatedTwitterTransformer() {
		ReviewCreation2TwitterTransformer  template = new ReviewCreation2TwitterTransformer();
		template.setServerDNSName(serverDNSName);
		return template;
	}
	
	/**
	 * @return MessageChannel
	 */
	@Bean
	public ReviewCreation2Facebook reviewCreation2FacebookChannel() {
		ReviewCreation2Facebook result = new ReviewCreation2Facebook();
		result.setServerDNSName(serverDNSName);
		return result;
	}

}
