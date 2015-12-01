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
package com.mobileman.kuravis.core.services.user.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import nl.captcha.Captcha;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.event.Event;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.user.User.DiseaseState;
import com.mobileman.kuravis.core.domain.user.UserAccount;
import com.mobileman.kuravis.core.domain.user.UserState;
import com.mobileman.kuravis.core.domain.user.notification.UserNotification;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.RoleUtils;
import com.mobileman.kuravis.core.domain.util.UserUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.services.configuration.ConfigurationService;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.messaging.mail.MailService;
import com.mobileman.kuravis.core.services.treatment_review.TempTreatmentReviewService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.user.InvitationService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.services.user.impl.workers.OnDeleteUserDataCleanupWorker;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.util.security.CaptchaUtil;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * @author MobileMan GmbH
 *
 */
@Service("userService")
public class UserServiceImpl extends AbstractEntityServiceImpl<User> implements UserService {
	
	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
	
	
	@Autowired
	private OnDeleteUserDataCleanupWorker onDeleteUserDataCleanupWorker;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;
	
	@Autowired
	private DiseaseService diseaseService;
		
	@Autowired
	private SchedulerFactoryBean schedulerFactoryBean;
		
	@Autowired
	private InvitationService invitationService;
	
	@Autowired
	private TempTreatmentReviewService tempTreatmentReviewService;
	
	private Map<String, Integer> captchaData = new ConcurrentHashMap<String, Integer>();
	
	private DBObject getDBUserAccountById(String accountId) {
		DBObject dbUserAccount = getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject(EntityUtils.ID, accountId));
		if (dbUserAccount == null) {
			return null;
		}
		
		return dbUserAccount;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return EntityUtils.USER;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#findDBUserAccountByEmail(java.lang.String)
	 */
	@Override
	public DBObject findDBUserAccountByEmail(String email) {
		DBObject dbUserAccount = getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject("email", email));
		return dbUserAccount;
	}
	
	/**
	 * @param accounId
	 * @return user roles names
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getUserRoles(String accounId) {
		DBObject account = getDBUserAccountById(accounId);
		if (account == null) {
			return Collections.emptyList();
		}
		
		List<String> roles = (List<String>) account.get("roles");
		return roles == null ? Collections.<String>emptyList() : roles;
	}

	@Override
	public User findUserByEmail(String email) {
		
		DBObject dbUserAccount = getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject("email", email));
		if (dbUserAccount == null) {
			return null;
		}
		
		DBObject dbUser = findDBUserByEmail(email);
		if (dbUser == null) {
			return null;
		}
		
		User user = new User();
		user.setAccount(new UserAccount(email, (String) dbUserAccount.get("password")));
		user.setName((String) dbUser.get(EntityUtils.NAME));
		user.set_id((String) dbUser.get(EntityUtils.ID));
		
		return user;
	}
	
	@Override
	public DBObject findDBUserByEmail(String email) {
		DBObject dbUserAccount = getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject("email", email));
		if (dbUserAccount == null) {
			return null;
		}
		
		DBObject dbUser = getCollection().findOne(new BasicDBObject(EntityUtils.ID, dbUserAccount.get("userId")));
		if (dbUser == null) {
			return null;
		}
		
		return dbUser;
	}

	@Override
	public DBObject signin(String email, String password, String captchaAnswer, boolean rememberMe) {
		if (log.isTraceEnabled()) {
			log.trace("signin(" + email + "," + password + ","  + captchaAnswer + ","  + rememberMe + "," + ")");
		}
		if (email == null) {
			email = "";
		}
		if (password == null) {
			password = "";
		}
		DBObject result = null;
		DBObject account = findDBUserAccountByEmail(email);
		if (account == null) {
			result = ErrorUtils.error("Unauthorized", ErrorCodes.UNAUTHORIZED);
			if (captchaData.size() > 1000) {
				captchaData.clear();
			}
			
			Integer unsuccessful_login_count = captchaData.get(email);
			if (unsuccessful_login_count == null) {
				unsuccessful_login_count = 1;
			} else {
				unsuccessful_login_count = unsuccessful_login_count.intValue() + 1;
				if (unsuccessful_login_count.intValue() >= this.configurationService.getMaxUnsuccessfulLoginsCount()) {
					result.put("show_captcha", true);
				}
			}
			
			captchaData.put(email, unsuccessful_login_count);
			return result;
		}
		
		Integer unsuccessful_login_count = (Integer) account.get("unsuccessful_login_count");
		if (unsuccessful_login_count == null) {
			unsuccessful_login_count = 0;
		}
		
		UsernamePasswordToken token = new UsernamePasswordToken(email, password, rememberMe);
		Subject currentUser = SecurityUtils.getSubject();
		DBObject user = null;
		
		try {
			
		    currentUser.login(token);
		    user = (DBObject) currentUser.getPrincipal();
		    
		} catch ( UnknownAccountException e ) {
			log.error("signin(...)", e);
			result = ErrorUtils.error("Unknown email", ErrorCodes.UNKNOWN_EMAIL);
		} catch ( IncorrectCredentialsException e ) {
			log.error("signin(...)", e);
			result = handleCaptchaError(account);
						
		} catch ( LockedAccountException e ) {
			log.error("signin(...)", e);
			result = ErrorUtils.error(e.getMessage(), ErrorCodes.ACCOUNT_LOCKED);
		} catch ( ExcessiveAttemptsException e ) {
			log.error("signin(...)", e);
			result = handleCaptchaError(account);
		} catch ( AuthenticationException e ) {
			log.error("signin(...)", e);
			result = handleCaptchaError(account);
		}
		
		if (user != null) {
			
			if (unsuccessful_login_count.intValue() >= this.configurationService.getMaxUnsuccessfulLoginsCount()) {
				// max usuccesssful logins coun reached - check answer
				if (!ObjectUtils.nullSafeEquals(captchaAnswer, account.get("captcha_answer"))) {
					result = ErrorUtils.error("Unauthorized", ErrorCodes.UNAUTHORIZED);
					result.put("show_captcha", true);
					return result;
				}				
			}
			
			Date lastLoginDate = new Date();
			user.put("lastLoginDate", lastLoginDate);
			user.put("unsuccessful_login_count", 0);
			
			getCollection().update(
					new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), 
					new BasicDBObject("$set", new BasicDBObject("lastLoginDate", user.get("lastLoginDate"))
							.append("unsuccessful_login_count", 0)));
			
			getCollection(EntityUtils.USERACCOUNT).update(
					new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)), 
					new BasicDBObject("$set", new BasicDBObject("unsuccessful_login_count", 0)
						.append("captcha_answer", "")));
			
			result = ErrorUtils.success();
			result.put("roles", getUserRoles());
			result.put("email", user.get("email"));
			result.put("name", user.get("name"));
			result.put("gender", user.get("gender"));
			result.put("yearOfBirth", user.get("yearOfBirth"));
			result.put("state", user.get("state"));
			result.put("unsuccessful_login_count", user.get("unsuccessful_login_count"));
			result.put("lastLoginDate", user.get("lastLoginDate"));
			result.put(EntityUtils.ID, user.get(EntityUtils.ID));
			result.put("settings", user.get("settings"));
			result.put("state", user.get("state"));
		}
		return result;
	}
	
	private DBObject handleCaptchaError(DBObject account) {
		DBObject result = ErrorUtils.error("UNAUTHORIZED", ErrorCodes.UNAUTHORIZED);
		getCollection(EntityUtils.USERACCOUNT).update(
				new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)), 
				new BasicDBObject("$inc", new BasicDBObject("unsuccessful_login_count", 1)));
		
		account = getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)));
		if (account == null) {
			return ErrorUtils.error("Unknown email", ErrorCodes.UNAUTHORIZED);
		}
		
		Integer unsuccessful_login_count = (Integer) account.get("unsuccessful_login_count");
		if (unsuccessful_login_count == null) {
			unsuccessful_login_count = 0;
		}
		
		if (unsuccessful_login_count.intValue() >= this.configurationService.getMaxUnsuccessfulLoginsCount()) {
			result.put("show_captcha", true);
		}
		
		return result;
	}

	/**
	 * @return current user roles
	 */
	@SuppressWarnings("unchecked")
	private List<String> getUserRoles() {
		Subject subject = SecurityUtils.getSubject();
		DBObject user = (DBObject) subject.getPrincipal();
		DBObject account = getDBUserAccountById((String) user.get("accountId"));
		return (List<String>) account.get("roles");
	}

	@Override
	public String signout() {
		log.info("signout() - start");
		
		String result = null;
		try {
			Subject currentUser = SecurityUtils.getSubject();
			currentUser.logout();
		} catch (Exception e) {
			log.error("signin(...)", e);
			result = e.getMessage();
		}

		log.info("signout() - end: " + result);
		return result;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#checkSession()
	 */
	@Override
	public DBObject checkSession() {
		DBObject result = null;
		try {
			Subject subject = SecurityUtils.getSubject();
			if (subject != null && subject.isAuthenticated()) {
				DBObject user = (DBObject) subject.getPrincipal();
				result = ErrorUtils.success();
				UserUtils.copyUser(user, result);
				result.put("roles", getUserRoles());
				
			} else {
				result = ErrorUtils.error("User is not signed in", ErrorCodes.USER_NOT_AUTHENTICATED);
			}
			
		} catch (Exception e) {
			log.error("signin(...)", e);
			result = ErrorUtils.createErrorResult(e);
		}
		return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#resetCredentials(java.lang.String)
	 */
	@Override
	public DBObject resetCredentials(String email) {
		log.info("resetCredentials(" + email + ") - start");
		
		if (StringUtils.isEmpty(email)) {
			 return ErrorUtils.error("Email unknown", ErrorCodes.UNKNOWN_EMAIL);
		}
		
		final DBObject dbUserAccount = getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject("email", email));
		if (dbUserAccount == null) {
			 return ErrorUtils.error("Email unknown", ErrorCodes.UNKNOWN_EMAIL);
		}
		
		final DBObject dbUser = getCollection().findOne(new BasicDBObject(EntityUtils.ID, dbUserAccount.get("userId")));
		if (dbUser == null) {
			return null;
		}
		
		final String resetPasswordUuid = UUID.randomUUID().toString();
		getCollection(EntityUtils.USERACCOUNT).update(new BasicDBObject(EntityUtils.ID, dbUserAccount.get(EntityUtils.ID)), 
				new BasicDBObject("$set", new BasicDBObject("resetPasswordUuid", resetPasswordUuid)));
		
		mailService.sendResetCredientialsEmail(dbUser, resetPasswordUuid);
		
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, configurationService.getResetPasswordLifetime());
			
			JobDetail jobDetail = JobBuilder.newJob(PasswordResetRollbackJob.class)
					.withIdentity("reset-password-" + resetPasswordUuid, PasswordResetRollbackJob.GROUP)
					.storeDurably(false)
					.requestRecovery(true)
					.usingJobData("resetPasswordUuid", resetPasswordUuid)
					.usingJobData("accountId", (String)dbUserAccount.get(EntityUtils.ID))
					.build();

			Trigger trigger = TriggerBuilder.newTrigger()
					.forJob(jobDetail)
					.withDescription("Reset password trigger - " + resetPasswordUuid)
					.startAt(calendar.getTime())
					.build();
			
			this.schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
			
		} catch (SchedulerException e) {
			return ErrorUtils.createErrorResult(e);
		}
		
		
		log.info("resetCredentials(" + email + ") - end");
		return ErrorUtils.success();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public DBObject changeResetedPassword(String resetPasswordUuid, String password) {
		log.info("changePassword(" + resetPasswordUuid + ", ..., ...) - start");
		
		if (StringUtils.isEmpty(resetPasswordUuid)) {
			 return ErrorUtils.error("Endefined account", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject account = getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject("resetPasswordUuid", resetPasswordUuid));
		if (account == null) {
			 return ErrorUtils.error("Undefined account", ErrorCodes.UNAUTHORIZED);
		}
		
		String hash = null;
		try {
			hash = com.mobileman.kuravis.core.util.security.SecurityUtils.getSaltedHash(password);
			WriteResult result = getCollection(EntityUtils.USERACCOUNT).update(
					new BasicDBObject("resetPasswordUuid", resetPasswordUuid), 
					new BasicDBObject("$set", new BasicDBObject("password", hash))
					.append("$unset", new BasicDBObject("resetPasswordUuid", "")), false, false, WriteConcern.SAFE);
			if (!StringUtils.isEmpty(result.getError())) {
				
			}
			
		} catch (Exception e) {
			return ErrorUtils.error(e.getMessage(), ErrorCodes.INTERNAL_ERROR);
		}
		
		
		log.info("changePassword(" + resetPasswordUuid + ", ..., ...) - end");
		return ErrorUtils.success();
	}
	
	private DBObject checkPassword(String password) {
		if (StringUtils.isEmpty(password)) {
			 return ErrorUtils.error("Password not provided", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		if (password.length() < configurationService.getMinPasswordLength()) {
			return ErrorUtils.error("Password too short", ErrorCodes.PASSWORD_TOO_SHORT);
		}
		
		if (password.length() > configurationService.getMaxPasswordLength()) {
			return ErrorUtils.error("Password too long", ErrorCodes.PASSWORD_TOO_LONG);
		}
		
		return null;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#changePassword(java.lang.String, java.lang.String)
	 */
	@Override
	public DBObject changePassword(String password, String password2) {
		log.info("changePassword(..., ...) - start");
		
		DBObject user = UserUtils.getLoggedUser();
		if (!ObjectUtils.nullSafeEquals(password, password2)) {
			 return ErrorUtils.error("Password not same", ErrorCodes.PASSWORD_NOT_SAME);
		}
		
		DBObject checkResult = checkPassword(password);
		if (checkResult != null) {
			return checkResult;
		}
		
		String hash = null;
		try {
			hash = com.mobileman.kuravis.core.util.security.SecurityUtils.getSaltedHash(password2);
			WriteResult result = getCollection(EntityUtils.USERACCOUNT).update(
					new BasicDBObject(EntityUtils.ID, user.get(User.ATTR_ACCOUNT_ID)), 
					new BasicDBObject("$set", new BasicDBObject("password", hash)), false, false, WriteConcern.SAFE);
			if (!StringUtils.isEmpty(result.getError())) {
				ErrorUtils.error(result.getError(), ErrorCodes.INTERNAL_ERROR);
			}
			
		} catch (Exception e) {
			return ErrorUtils.error(e.getMessage(), ErrorCodes.INTERNAL_ERROR);
		}
		
		
		log.info("changePassword(..., ...) - end");
		return ErrorUtils.success();
	}


	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#deleteUserAccount(String)
	 */
	@Override
	public DBObject deleteUserAccount(String userId) {
		log.info("deleteAccount(" + userId + ") - start");
		
		DBObject user = UserUtils.getLoggedUser();
		if (user == null) {
			return ErrorUtils.error("Not authenticated: currentUser", ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		
		if (StringUtils.isEmpty(userId)) {
			return ErrorUtils.error("Unknown account: " + user, ErrorCodes.UNKNOWN_ACCOUNT);
		}
		
		if (!userId.equals(user.get(EntityUtils.ID))) {
			if (!RoleUtils.isAdminAccount(user)) {
				return ErrorUtils.error("Not authorized: " + user, ErrorCodes.UNAUTHORIZED);
			}
		}
		
		signout();
		
		DBObject deleteduser = getCollection().findOne(new BasicDBObject(EntityUtils.ID, userId));
		if (deleteduser != null) {
			String email = (String) deleteduser.get("email");
			this.onDeleteUserDataCleanupWorker.process(new BasicDBObject("userId", userId).append("email", email));
			getCollection().remove(new BasicDBObject(EntityUtils.ID, userId));
		}
		
		DBObject dbUserAccount = getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject(EntityUtils.ID, user.get(User.ATTR_ACCOUNT_ID)));
		if (dbUserAccount != null) {
			getCollection(EntityUtils.USERACCOUNT).remove(new BasicDBObject(EntityUtils.ID, dbUserAccount.get(EntityUtils.ID)));
		}
				
		log.info("deleteAccount() - end");
		return ErrorUtils.success();
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#delete(java.lang.String, java.lang.String)
	 */
	@Override
	public DBObject delete(String type, String userId) {
		log.info("delete(" + userId + ") - start");
		
		DBObject loggedUser = UserUtils.getLoggedUser();
		if (!RoleUtils.isAdminUser(loggedUser)) {
			return ErrorUtils.error("Not an admin account: " + loggedUser, ErrorCodes.UNAUTHORIZED);
		}
		
		DBObject user = findById(userId);
		if (user == null) {
			return ErrorUtils.success();
		}
		
		DBObject account = getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject(EntityUtils.ID, user.get(User.ATTR_ACCOUNT_ID)));
				
		String email = (String) user.get("email");
		this.onDeleteUserDataCleanupWorker.process(new BasicDBObject("userId", userId).append("email", email));
		
		if (account != null) {
			getCollection(EntityUtils.USERACCOUNT).remove(new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)));
		}
		
		getCollection().remove(new BasicDBObject(EntityUtils.ID, userId));
			
		log.info("delete() - end");
		return ErrorUtils.success();
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#updateUser(java.lang.String, DBObject)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DBObject updateUser(String userId, DBObject newData) {
		log.info("update("  + userId + ", "  + newData + ") - start");
				
		DBObject authUser = UserUtils.getLoggedUser();
		if (!ObjectUtils.nullSafeEquals(userId, authUser.get(EntityUtils.ID))) {
			return ErrorUtils.error("Is not authenticated user =" + userId, ErrorCodes.UNAUTHORIZED);
		}
		
		DBObject oldUserData = findById(userId);
		BasicDBObject command = new BasicDBObject();
		UserUtils.copyUser(newData, authUser);
		
		this.treatmentReviewService.updateTretmentReviewStatistics(newData, oldUserData);
		updateProfileSettings(userId, newData, oldUserData);
		
		addPropertyUpdate("email", newData, oldUserData, command);
		
		if (addPropertyUpdate("name", newData, oldUserData, command)) {
			// update all relevant entities
			applyUserNameUpdate(userId, (String) newData.get("name"));
		}
		
		addPropertyUpdate(User.ATTR_GENDER, newData, oldUserData, command);
		addPropertyUpdate(User.ATTR_YEAR_OF_BIRTH, newData, oldUserData, command);
		addPropertyUpdate("aboutMe", newData, oldUserData, command);
		addPropertyUpdate("location", newData, oldUserData, command);
		
		List<Map<String, Object>> inUserDiseases = (List<Map<String, Object>>) newData.get("diseases");
		List<DBObject> newUserDiseases = new ArrayList<DBObject>();
		if (!CollectionUtils.isEmpty(inUserDiseases)) {
			for (Map<String, Object> userDisease : inUserDiseases) {

				Map<String, Object> disease = (Map<String, Object>) userDisease.get("disease");
				if (disease == null) {
					continue;
				}
				
				if (!disease.containsKey(EntityUtils.ID)) {
					if (StringUtils.isEmpty(disease.get(EntityUtils.NAME))) {
						continue;
					}
					
					disease = diseaseService.findOrInsertByProperty(EntityUtils.NAME, new BasicDBObject(EntityUtils.NAME, disease.get(EntityUtils.NAME))).toMap();
				}
				
				disease = new BasicDBObject(EntityUtils.ID, disease.get(EntityUtils.ID)).append(EntityUtils.NAME, disease.get(EntityUtils.NAME));
				DBObject newUserDisease = new BasicDBObject(EntityUtils.CREATED_ON, new Date())
					.append(EntityUtils.MODIFIED_ON, new Date())
					.append("state", userDisease.get("state"))
					.append("treatmentHeardFrom", userDisease.get("treatmentHeardFrom"))
					.append("disease", disease);
				
				newUserDiseases.add(newUserDisease);
			}
			
			if (!newUserDiseases.isEmpty()) {
				getCollection().update(new BasicDBObject(EntityUtils.ID, userId), new BasicDBObject("$set", new BasicDBObject("diseases", Collections.emptyList())));
				getCollection().update(new BasicDBObject(EntityUtils.ID, userId), new BasicDBObject("$set", new BasicDBObject("diseases", newUserDiseases)));
			}
		} else {
			getCollection().update(new BasicDBObject(EntityUtils.ID, userId), new BasicDBObject("$set", new BasicDBObject("diseases", Collections.emptyList())));
		}
				
		if (!command.isEmpty()) {
			WriteResult result = getCollection().update(new BasicDBObject("_id", userId), new BasicDBObject("$set", command));
			if (result.getError() != null) {
				return ErrorUtils.error(result.getError());
			}
		}
		
		return ErrorUtils.success();
	}
	
	/**
	 * @param userId
	 * @param newUserData
	 * @param oldUserData
	 */
	@SuppressWarnings("unchecked")
	private DBObject updateProfileSettings(String userId, DBObject newUserData, DBObject oldUserData) {
		log.info("updateProfileSettings(" + userId + ", " + newUserData + "," + oldUserData + ") - start");
		
		DBObject authUser = UserUtils.getLoggedUser();
		if (authUser == null) {
			return ErrorUtils.error("Has to be authenticated: currentUser=" + SecurityUtils.getSubject(), ErrorCodes.UNAUTHORIZED);
		}
		
		DBObject settings = (DBObject) authUser.get("settings");
		if (settings == null) {
			settings = new BasicDBObject();
			authUser.put("settings", settings);
		}
		
		Map<String, Object> newSettings = (Map<String, Object>) newUserData.get("settings");
		if (newSettings == null) {
			newSettings = new HashMap<>();
		}
		
		if (!newSettings.containsKey("profile")) {
			return ErrorUtils.success();
		}
		
		settings.put("profile", newSettings.get("profile"));
		
		WriteResult result = getCollection().update(new BasicDBObject(EntityUtils.ID, userId), 
				new BasicDBObject("$set", new BasicDBObject("settings.profile", newSettings.get("profile"))));
		if (result.getError() != null) {
			throw ErrorUtils.exception(result.getError());
		}
				
		log.info("updateProfileSettings(..., ...) - end");
		return ErrorUtils.success();
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#signup(com.mongodb.DBObject)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DBObject signup(DBObject data) {
		log.trace("signup(" + data + ") - start");
				
		String email = (String) data.get("email");
		if (StringUtils.isEmpty(email)) {
			return ErrorUtils.error("email not defined", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		String password = (String) data.get("password");
		if (StringUtils.isEmpty(password)) {
			return ErrorUtils.error("password not defined", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		String userName = (String) data.get("name");
		if (StringUtils.isEmpty(userName)) {
			return ErrorUtils.error("user name not defined", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject validationError = validateEmail(email);
		if (validationError != null) {
			return validationError;
		}
		
		List<Map<String, Object>> diseases = (List<Map<String, Object>>) data.get("diseases");
		if (diseases != null) {
			for (Map<String, Object> disease : diseases) {
				disease.put("createdOn", new Date());
			}
		}
		
		DBObject newUser = EntityUtils.newDBObjectId();
		newUser.put("name", userName);
		newUser.put("aboutMe", data.get("aboutMe"));
		newUser.put(User.ATTR_GENDER, data.get(User.ATTR_GENDER));
		newUser.put("location", data.get("location"));
		newUser.put(User.ATTR_YEAR_OF_BIRTH, data.get(User.ATTR_YEAR_OF_BIRTH));
		newUser.put("registrationDate", new Date());
		newUser.put("diseases", diseases);
		newUser.put("email", email);
		newUser.put("state", UserState.UNVERIFIED.getValue());
		newUser.put("invitationCount", 0);
				
		createSettings(newUser);
		
		String activationUuid = EntityUtils.newId();
		DBObject account = EntityUtils.newDBObjectId();
		account.put("email", email);
		account.put("activationUuid", activationUuid);
		account.put("roles", Arrays.asList(Roles.NONVERIFIED_USER));
		account.put("userId", newUser.get(EntityUtils.ID));
		
		try {
			account.put("password", com.mobileman.kuravis.core.util.security.SecurityUtils.getSaltedHash(password));
		} catch (Exception e) {
			return ErrorUtils.createErrorResult(e);
		}
		
		newUser.put("accountId", account.get(EntityUtils.ID));
		
		WriteResult saveResult = getCollection().save(newUser, WriteConcern.SAFE);
		if (!StringUtils.isEmpty(saveResult.getError())) {
			return ErrorUtils.error(saveResult.getError());
		}
		
		saveResult = getCollection(EntityUtils.USERACCOUNT).save(account, WriteConcern.SAFE);
		if (!StringUtils.isEmpty(saveResult.getError())) {
			return ErrorUtils.error(saveResult.getError());
		}
		
		mailService.sendActivationEmail(newUser, activationUuid);
		
		DBObject result = ErrorUtils.success();
		return result;
	}

	/**
	 * @param newUser
	 */
	private void createSettings(DBObject newUser) {
		DBObject settings = new BasicDBObject();
		DBObject privacySettings = new BasicDBObject();
		DBObject emailNotification = new BasicDBObject();
		newUser.put("settings", settings);
		settings.put("privacySettings", privacySettings);
		privacySettings.put("emailNotification", emailNotification);
		
		emailNotification.put("weeklyUpdatesCommentsAndVotes", Boolean.TRUE);
		emailNotification.put("news_announcements", Boolean.TRUE);
		
		DBObject profile = new BasicDBObject();
		settings.put("profile", profile);
		profile.put("avatarColor", UserUtils.getAvatarColor());
	}

	/**
	 * @param email
	 */
	private DBObject validateEmail(String email) {
		DBObject existingUser = findDBUserAccountByEmail(email);
		if (existingUser != null) {
			return ErrorUtils.error("E-Mail has been already registered with another user", ErrorCodes.EMAIL_ALREADY_REGISTERED);
		}
		
		if (isEmailBanned(email)) {
			return ErrorUtils.error("E-Mail is banned", ErrorCodes.EMAIL_IS_BANNED);
		}
		
		return null;
	}


	/**
	 * @param email
	 * @return true if email address is banned
	 */
	private boolean isEmailBanned(String email) {
		return false;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#activateAccount(java.lang.String)
	 */
	@Override
	public DBObject activateAccount(String activationUuid) {
		log.info("activateAccount(" + activationUuid + ") - start");
		
		if (StringUtils.isEmpty(activationUuid)) {
			return ErrorUtils.error("Activation uuid is missing", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject account = getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject("activationUuid", activationUuid));
		if (account == null) {
			return ErrorUtils.error("Unknown account", ErrorCodes.UNKNOWN_ACCOUNT);
		}
		
		DBObject user = getCollection().findOne(new BasicDBObject(User.ATTR_ACCOUNT_ID, account.get(EntityUtils.ID)));
		UserState state = user.get("state") == null ? null : UserState.valueOf(String.class.cast(user.get("state")).toUpperCase());
		String userId = (String) user.get(EntityUtils.ID);
		String userEmail = (String) user.get("email");
	    if (state.equals(UserState.ACTIVE)) {
	    	return ErrorUtils.error("User is already verified", ErrorCodes.ALREADY_VERIFIED);
		}
	    
	    if (state.equals(UserState.INACTIVE)) {
	    	// inactive, deleted user
	    	return ErrorUtils.error("User is inactive", ErrorCodes.UNKNOWN_ACCOUNT);
		}
		
		getCollection().update(new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), 
				new BasicDBObject("$set", new BasicDBObject("state", UserState.ACTIVE.getValue())));
		
		// can not push & pull in same update operation on same key, see https://jira.mongodb.org/browse/SERVER-1050
		getCollection(EntityUtils.USERACCOUNT).update(new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)), 
				new BasicDBObject("$push", new BasicDBObject("roles", Roles.USER))
						  .append("$unset", new BasicDBObject("activationUuid", "")));
		
		getCollection(EntityUtils.USERACCOUNT).update(new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)), 
				new BasicDBObject("$pull", new BasicDBObject("roles", Roles.NONVERIFIED_USER)));
		
		Subject subject = SecurityUtils.getSubject();
		if (subject != null && subject.getPrincipal() != null) {
			DBObject principal = (DBObject) subject.getPrincipal();
			principal.put("state", UserState.ACTIVE.getValue());
			DBObject principalAccount = (DBObject) principal.get("account");
			principalAccount.put("roles", Arrays.asList(Roles.USER));
		}
		
		DBCursor tmpReviewsCursor = getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).find(new BasicDBObject("author." + EntityUtils.ID, user.get(EntityUtils.ID)));
		for (DBObject tmpReview : tmpReviewsCursor) {
			this.treatmentReviewService.createTreatmentReview(tmpReview);
		}
		
		this.tempTreatmentReviewService.deleteAllTempTreatmentReviewsOfUser(userId, userEmail);
				
		try {
			mailService.sendWelcomeEmail(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		DBObject result = ErrorUtils.success();
		log.info("activateAccount(" + activationUuid + ") - end: " + result);
		return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#updateDiseases(List)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DBObject updateDiseases(List<? extends DBObject> newDiseases) {
		log.info("updateDiseases(" + newDiseases + ") - start");
				
		if (newDiseases == null) {
			newDiseases = new ArrayList<DBObject>();
		}
		
		DBObject user = UserUtils.getLoggedUser();
		if (user == null) {
			return ErrorUtils.error("Has to be authenticated: currentUser=" + SecurityUtils.getSubject(), ErrorCodes.UNAUTHORIZED);
		}
		
		Map<String, DBObject> newDiseasesMap = new HashMap<String, DBObject>();
		for (DBObject disease : newDiseases) {
			String id = (String)disease.get(EntityUtils.ID);
			if (StringUtils.isEmpty(id)) {
				return ErrorUtils.error("Disease uuid is missing", ErrorCodes.INCORRECT_PARAMETER);
			}
			
			String name = (String)disease.get(EntityUtils.NAME);
			if (StringUtils.isEmpty(name)) {
				DBObject dbDisease = diseaseService.findById(id);
				if (dbDisease == null) {
					return ErrorUtils.error("Disease does not exists: " + id, ErrorCodes.INCORRECT_PARAMETER);
				}
				
				name = (String) dbDisease.get(EntityUtils.NAME);
			}
			
			newDiseasesMap.put(id, new BasicDBObject(EntityUtils.ID, id).append(EntityUtils.NAME, name));
		}
		
		List<DBObject> userDiseases = user.get("diseases") == null ? Collections.<DBObject>emptyList() : (List<DBObject>) user.get("diseases");
		Map<String, DBObject> userDiseasesMap = new HashMap<String, DBObject>();
		for (DBObject userDiseaseData : userDiseases) {
			DBObject disease = (DBObject) userDiseaseData.get("disease");
			userDiseasesMap.put((String) disease.get(EntityUtils.ID), disease);
			
			if (!newDiseasesMap.containsKey(disease.get(EntityUtils.ID))) {
				getCollection().update(new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), 
						new BasicDBObject("$pull", new BasicDBObject("diseases", new BasicDBObject("disease." + EntityUtils.ID, disease.get(EntityUtils.ID)))));
			}
		}
		
		for (DBObject newDisease : newDiseases) {
			if (!userDiseasesMap.containsKey(newDisease.get(EntityUtils.ID))) {
				DBObject newDiseaseData = new BasicDBObject()
					.append("createdOn", new Date())
					.append("state", DiseaseState.IN_THERAPY.getValue())
					.append("disease", newDisease);
				
				getCollection().update(new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), 
						new BasicDBObject("$push", new BasicDBObject("diseases", newDiseaseData)));
			}
		}
		
		user.put("diseases", findById((String) user.get(EntityUtils.ID)).get("diseases"));
		
		DBObject result = ErrorUtils.success();
		log.info("updateDiseases(" + newDiseases + ") - end: " + result);
		return result;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#update(java.lang.String, java.lang.String, DBObject)
	 */
	@Override
	public DBObject update(String type, String userId, DBObject newData) {
		DBObject result = updateUser(userId, newData);
		return result;
	}

	/**
	 * @param userId
	 * @param newUserName
	 */
	private void applyUserNameUpdate(String userId, String newUserName) {
		
		String property = "author.name";
		
		DBObject filterUser = QueryBuilder.start().put("author." + EntityUtils.ID).is(userId).get();
		getCollection(TreatmentReview.ENTITY_NAME).update(filterUser, new BasicDBObject("$set", new BasicDBObject(property, newUserName)));
		getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).update(filterUser, new BasicDBObject("$set", new BasicDBObject(property, newUserName)));
		
		for (String entityName : new String[] { 
				TreatmentReview.ENTITY_NAME, 
				EntityUtils.TEMP_TREATMENT_REVIEW,
				Event.ENTITY_NAME
				}) {
			getCollection(entityName).update(filterUser, new BasicDBObject("$set", new BasicDBObject(property, newUserName)));
		}
		
		filterUser = QueryBuilder.start().put("user." + EntityUtils.ID).is(userId).get();
		for (String entityName : new String[] { 
				EntityUtils.TREATMENT_REVIEW_EVENT, 
				UserNotification.ENTITY_NAME, 
				Disease.ENTITY_NAME,
				EntityUtils.TREATMENT, 
				EntityUtils.FRAUD_REPORT, 
				EntityUtils.FRAUD_REPORT_ITEM,
				Event.ENTITY_NAME
				}) {
			getCollection(entityName).update(filterUser, new BasicDBObject("$set", new BasicDBObject("user.name", newUserName)));
		}
	}

	/**
	 * @param newObject
	 * @param oldObject
	 * @param command
	 * @return true if property has to be changed
	 */
	private boolean addPropertyUpdate(String property, DBObject newObject, DBObject oldObject, DBObject command) {
		if (newObject.containsField(property)) {
			if (!ObjectUtils.nullSafeEquals(oldObject.get(property), newObject.get(property))) {
				if (newObject.get(property) == null) {
					command.put(property, "");
					return true;
				} else {
					command.put(property, newObject.get(property));
					return true;
				}			
			}
		}
		
		return false;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#findUsersByDiseaseAndTreatment(com.mongodb.DBObject, Pageable)
	 */
	@Override
	public List<DBObject> findUsersByDiseaseAndTreatment(DBObject query, Pageable page) {
		
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		
		if (query != null) {
			if (query.containsField("diseaseId")) {
				builder.add("disease." + EntityUtils.ID, query.get("diseaseId"));
			}
			
			if (query.containsField("treatmentId")) {
				builder.add("treatment." + EntityUtils.ID, query.get("treatmentId"));
			}
		}
		
		AggregationOutput out = getCollection(TreatmentReview.ENTITY_NAME).aggregate(
				new BasicDBObject("$match", builder.get()),
				new BasicDBObject("$group", new BasicDBObject("_id", new BasicDBObject("_id", "$author._id"))),
				new BasicDBObject("$project", 
						new BasicDBObject(EntityUtils.ID, "$_id._id")),
				new BasicDBObject("$skip", page.getOffset()),
				new BasicDBObject("$limit", page.getPageSize())
				
				);
				
		List<DBObject> result = new ArrayList<>();
		Set<String> userIds = new HashSet<String>();
		for (DBObject dbObject : out.results()) {
			String userId = EntityUtils.getEntityId(dbObject);
			userIds.add(userId);
			result.add(dbObject);
		}
		
		Map<String, DBObject> usersData = findUsersData(userIds, "name", "gender", "settings.profile");
		for (DBObject user : result) {
			DBObject data = usersData.get(EntityUtils.getEntityId(user));
			if (data != null) {
				user.put("name", data.get("name"));
				user.put("gender", data.get("gender"));
				user.put("settings", data.get("settings"));
			}
		}
		
		return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#updateEmail(java.lang.String)
	 */
	@Override
	public DBObject updateEmail(String email) {
		log.info("updateEmail(" + email + ") - start");
		
		DBObject user = UserUtils.getLoggedUser();
		if (user == null) {
			return ErrorUtils.error("Not authenticated: currentUser=" + SecurityUtils.getSubject(), ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		
		DBObject validationError = validateEmail(email);
		if (validationError != null) {
			return validationError;
		}
		
		BasicDBObject command = new BasicDBObject();
		command.put("state", UserState.UNVERIFIED.getValue());
		command.put("email", email);
		
		DBObject account = findDBUserAccountByEmail((String) user.get("email"));
		String activationUuid = EntityUtils.newId();
		getCollection(EntityUtils.USERACCOUNT).update(new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)), 
				new BasicDBObject("$set", new BasicDBObject("email", email).append("activationUuid", activationUuid))
				.append("$pullAll", new BasicDBObject("roles", Roles.ALL_ROLES.toArray())));
		
		getCollection(EntityUtils.USERACCOUNT).update(new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)), 
				new BasicDBObject("$push", new BasicDBObject("roles", Roles.NONVERIFIED_USER)));
		
		if (!command.isEmpty()) {
			WriteResult result = getCollection().update(new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), new BasicDBObject("$set", command));
			if (result.getError() != null) {
				return ErrorUtils.error(result.getError());
			}
		}
		
		user.put("email", email);
		DBObject sessionAccount = (DBObject) user.get("account");
		sessionAccount.put("email", email);
		sessionAccount.put("roles", Arrays.asList(Roles.NONVERIFIED_USER));
		
		mailService.sendActivationEmail(user, activationUuid);
		
		log.info("updateEmail(..., ...) - end");
		return ErrorUtils.success();
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#updatePrivacySettings(java.util.Map)
	 */
	@Override
	public DBObject updatePrivacySettings(Map<String, Object> privacySettings) {
		log.info("updatePrivacySettings(" + privacySettings + ") - start");
		
		DBObject user = UserUtils.getLoggedUser();
		if (user == null) {
			throw ErrorUtils.exception("Not authenticated: currentUser=" + SecurityUtils.getSubject(), ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		
		DBObject settings = (DBObject) user.get("settings");
		if (settings == null) {
			settings = new BasicDBObject();
			user.put("settings", settings);
		}
		
		settings.put("privacySettings", privacySettings);
		
		WriteResult result = getCollection().update(new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), 
				new BasicDBObject("$set", new BasicDBObject("settings.privacySettings", privacySettings)));
		if (result.getError() != null) {
			throw ErrorUtils.exception(result.getError());
		}
				
		log.info("updatePrivacySettings(..., ...) - end");
		return ErrorUtils.success();
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#generateCaptcha(java.lang.String)
	 */
	@Override
	public byte[] generateCaptcha(String email) {
		log.info("generateCaptcha(" + email + ") - start");
				
		Captcha captcha = CaptchaUtil.generateCaptcha();
		byte[] imageData = CaptchaUtil.getCaptchaImageData(captcha);
		
		DBObject account = findDBUserAccountByEmail(email);
		if (account != null) {
			getCollection(EntityUtils.USERACCOUNT).update(
					new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)), 
					new BasicDBObject("$set", new BasicDBObject("captcha_answer", captcha.getAnswer())));
		}
		
		log.info("generateCaptcha(" + email + ") - end");
		
		return imageData;
	}
	
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#findUserSettings(java.lang.String)
	 */
	@Override
	public DBObject findUserSettings(String userId) {
		DBObject filter = QueryBuilder.start().put(EntityUtils.ID).is(userId).get();
		DBObject userData = getCollection().findOne(filter, new BasicDBObject("settings", 1));
		return userData != null ? (DBObject)userData.get("settings") : null;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, DBObject> findUsersData(Collection<String> usersId, String... properties) {
		if (usersId.isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<String, DBObject> result = new HashMap<String, DBObject>();
		DBObject filter = QueryBuilder.start().put(EntityUtils.ID).in(usersId).get();
		DBObject props = new BasicDBObject();
		for (String property : properties) {
			props.put(property, 1);
		}
		DBCursor cursor = getCollection().find(filter, props);
		while (cursor.hasNext()) {
			DBObject user = cursor.next();
			result.put(EntityUtils.getEntityId(user), user);
		}
		 
		return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserService#userExistsWithEmail(java.lang.String)
	 */
	@Override
	public boolean userExistsWithEmail(String email) {
		long count = getCollection(EntityUtils.USERACCOUNT).count(new BasicDBObject("email", email));
		return count > 0;
	}
	
	@Override
	public String userFeedback(String comment, String email) {
		log.info("userFeedback() - start");
		
		String result = null;
		mailService.sendUserFeedbackMail(comment, email);
		
		log.info("userFeedback() - end: " + result);
		return result;
	}
}
