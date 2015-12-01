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
 * RoleUtils.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 17.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.util;

import java.util.List;

import com.mobileman.kuravis.core.domain.user.Roles;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public class RoleUtils {

	/**
	 * @param account
	 * @return true if user account has role {@link Roles#ADMIN}
	 */
	@SuppressWarnings("unchecked")
	public static boolean isAdminAccount(DBObject account) {
		if (account == null || account.get("roles") == null) {
			return false;
		}
		
		List<String> roles = (List<String>) account.get("roles");
		if (roles.contains(Roles.ADMIN)) {
			return true;
		}
		
		return false;
	}

	/**
	 * @param user
	 * @return true if user has role {@link Roles#ADMIN}
	 */
	public static boolean isAdminUser(DBObject user) {
		if (user == null) {
			return false;
		}
		
		return isAdminAccount((DBObject) user.get("account"));
	}
	
	/**
	 * @param user
	 * @return true if user has role {@link Roles#NONVERIFIED_USER}
	 */
	public static boolean isNonverifiedUser(DBObject user) {
		if (user == null) {
			return false;
		}
		
		return isNonverifiedAccount((DBObject) user.get("account"));
	}
	
	/**
	 * @param account
	 * @return true if user account has role {@link Roles#NONVERIFIED_USER}
	 */
	@SuppressWarnings("unchecked")
	public static boolean isNonverifiedAccount(DBObject account) {
		if (account == null || account.get("roles") == null) {
			return false;
		}
		
		List<String> roles = (List<String>) account.get("roles");
		if (roles.contains(Roles.NONVERIFIED_USER)) {
			return true;
		}
		
		return false;
	}
}
