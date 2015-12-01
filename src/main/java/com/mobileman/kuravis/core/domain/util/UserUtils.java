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
 * UserUtils.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 6.9.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.util;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.mobileman.kuravis.core.domain.user.Gender;
import com.mobileman.kuravis.core.domain.user.User;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public abstract class UserUtils {
	
	private static final String[] AVATAR_COLORS =  {"#FDB3AD", "#F79C88", "#FC8376", "#DD6C7D", "#C95B60", "#C1C9A3", "#96A16C", "#768347", "#799181", "#558581",
		"#898FA7", "#A5C4DB", "#7CAACB", "#5F8DAA", "#33627C", "#DAAF94", "#B8A093", "#967B66", "#7C4E46", "#685245"};
	
	private static final java.util.Random AVATAR_RND = new java.util.Random();

	/**
	 * @return random avatar color
	 */
	public static String getAvatarColor() {
		String result = AVATAR_COLORS[AVATAR_RND.nextInt(AVATAR_COLORS.length)];
		return result;
	}

	/**
	 * @param user
	 * @return emailNotification
	 */
	public static DBObject getEmailNotificationSettings(DBObject user) {
		DBObject emailNotification = null;
		DBObject privacySettings = getPrivacySettings(user);
		if (privacySettings != null && privacySettings.get("emailNotification") != null) {
			emailNotification = (DBObject) privacySettings.get("emailNotification");
			return emailNotification;
		}
		
		return null;
	}
	
	/**
	 * @param user
	 * @return emailNotification
	 */
	public static DBObject getPrivacySettings(DBObject user) {
		DBObject settings = (DBObject) user.get("settings");
		if (settings != null && settings.get("privacySettings") != null) {
			DBObject privacySettings = (DBObject) settings.get("privacySettings");
			return privacySettings;
		}
		
		return null;
	}

	/**
	 * @param user
	 * @return profile of giben user
	 */
	public static DBObject getProfileSettings(DBObject user) {
		DBObject settings = (DBObject) user.get("settings");
		if (settings != null && settings.get("profile") != null) {
			DBObject profile = (DBObject) settings.get("profile");
			return profile;
		}
		
		return null;
	}

	/**
	 * @param source
	 * @param target
	 */
	public static void copyUser(DBObject source, DBObject target) {
		for (String property : new String[]{ "email", "name", "gender", "yearOfBirth", "state", "settings", EntityUtils.ID }) {
			if (source.containsField(property)) {
				if (Map.class.isInstance(source.get(property))) {
					target.put(property, new BasicDBObject(Map.class.cast(source.get(property))));
				} else {
					target.put(property, source.get(property));
				}
				
			}
		}
	}

	/**
	 * @param user
	 * @return gender if exists, else UNKNOWN 
	 */
	public static String getGender(DBObject user) {
		if (user != null) {
			if (!user.containsField(User.ATTR_GENDER) || user.get(User.ATTR_GENDER) == null) {
				return Gender.UNKNOWN.getValue();
			}
			
			return (String) user.get(User.ATTR_GENDER);
		}
		
		return null;
	}

	/**
	 * @return
	 */
	public static DBObject getLoggedUser() {
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser != null && currentUser.getPrincipal() != null) {
			DBObject user = (DBObject) currentUser.getPrincipal();
			return user;
		}
		return null;
	}

	public static String getLoggedUserId() {
		return EntityUtils.getEntityId(getLoggedUser());
	}

	/**
	 * @param userObject
	 * @return base user - ID + name
	 */
	public static User createBaseUser(Object userObject) {
		if (userObject == null || !DBObject.class.isInstance(userObject)) {
			return null;
		}
		
		DBObject user = (DBObject)userObject;
		
		User result = new User();
		result.set_id((String) user.get(EntityUtils.ID));
		result.setName((String) user.get(EntityUtils.NAME));		
		return result;
	}
}
