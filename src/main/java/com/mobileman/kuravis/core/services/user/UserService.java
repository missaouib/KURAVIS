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
package com.mobileman.kuravis.core.services.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.services.entity.EntityService;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public interface UserService extends EntityService<User> {
	
	/**
	 * @param query
	 * @param page
	 * @return users by Disease and/or Treatment
	 */
	List<DBObject> findUsersByDiseaseAndTreatment(DBObject query, Pageable page);

	/**
	 * @param email
	 * @return user data
	 */
	User findUserByEmail(String email);
	
	/**
	 * @param accounId
	 * @return user roles names
	 */
	List<String> getUserRoles(String accounId);
	
	/**
	 * @param email
	 * @return user data
	 */
	DBObject findDBUserByEmail(String email);
	
	/**
	 * @param email
	 * @return user data
	 */
	DBObject findDBUserAccountByEmail(String email);

	/**
	 * UC12 Sign-in
	 * @param email
	 * @param password
	 * @param captchaAnswer 
	 * @param rememberMe
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject signin(String email, String password, String captchaAnswer, boolean rememberMe);
	
	/**
	 * UC13 Sign-out
	 * @return error message in case of error
	 */
	String signout();
	
	/**
	 * Check session
	 * @return error message in case of error, current user roles in case of success
	 */
	DBObject checkSession();

	/**
	 * UC17 Forgot password - reset
	 * @param email
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject resetCredentials(String email);

	/**
	 * UC18 Change Password
	 * @param password
	 * @param password2
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject changePassword(String password, String password2);
	
	/**
	 * UC18 Change Password
	 * @param resetPasswordUuid 
	 * @param password
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject changeResetedPassword(String resetPasswordUuid, String password);

	/**
	 * UC19 Account deletion
	 * Deletes user account
	 * @param userId id of an user whom account has to be deleted
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject deleteUserAccount(String userId);
	
	/**
	 * UC230 User admin - User deletion
	 * Deletes user
	 * @param userId id of an user to delete
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject delete(String userId);

	/**
	 * UC10 Sign-up
	 * @param body
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject signup(DBObject body);

	/**
	 * @param activationUuid
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject activateAccount(String activationUuid);

	/**
	 * Updates diseases of the user
	 * @param data
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject updateDiseases(List<? extends DBObject> data);

	/**
	 * @param userId
	 * @param data
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject updateUser(String userId, DBObject data);

	/**
	 * UC1020 Email - update email
	 * @param email
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject updateEmail(String email);

	/**
	 * UC1100 Privacy Settings - update Privacy Settings
	 * @param privacySettings
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject updatePrivacySettings(Map<String, Object> privacySettings);

	/**
	 * @param email
	 * @return DBdata - data with captcha image
	 */
	byte[] generateCaptcha(String email);

	/**
	 * @param userId
	 * @return settings
	 */
	DBObject findUserSettings(String userId);
	
	/**
	 * @param usersId
	 * @param properties 
	 * @return settings
	 */
	Map<String, DBObject> findUsersData(Collection<String> usersId, String... properties);

	/**
	 * @param email
	 * @return true if user with given email exists
	 */
	boolean userExistsWithEmail(String email);
	
	/**
	 * Feedback from the user
	 * @param comment 
	 * @param email 
	 * @return error message in case of error
	 */
	String userFeedback(String comment, String email);
}
