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
 * ReviewCreation2Facebook.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 8.11.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.social.facebook;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.integration.Message;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.stereotype.Component;

import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mongodb.DBObject;

import facebook4j.Facebook;
import facebook4j.FacebookFactory;

/**
 * @author MobileMan GmbH
 *
 */
@Component
public class ReviewCreation2Facebook {

	@Autowired
	private ReloadableResourceBundleMessageSource messageSource;
	
	private String serverDNSName;
	
	@Autowired
	private FacebookConnectionFactory facebookConnectionFactory;
	
	@Autowired
	private ConnectionFactoryRegistry connectionFactoryLocator;
	
	/**
	 *
	 * @param serverDNSName serverDNSName
	 */
	public void setServerDNSName(String serverDNSName) {
		this.serverDNSName = serverDNSName;
	}
	
	/**
	 *
	 * @return serverDNSName
	 */
	public String getServerDNSName() {
		return this.serverDNSName;
	}
	
	/**
	 * @param message
	 */
	public void message(Message<DBObject> message) {  
		
		DBObject treatment = (DBObject) message.getPayload().get("treatment");
		DBObject disease = (DBObject) message.getPayload().get("disease");
		
		String textMessage = messageSource.getMessage("facebook.review.created.fmt", new Object[]{ 
				disease.get(EntityUtils.NAME),
				treatment.get(EntityUtils.NAME),
				getServerDNSName(),
				treatment.get(EntityUtils.ID),
		}, Locale.GERMANY);
		
		Facebook facebook = new FacebookFactory().getInstance();
		
		//facebookTemplate.commentOperations().addComment(textMessage, textMessage);
		
   }
}
