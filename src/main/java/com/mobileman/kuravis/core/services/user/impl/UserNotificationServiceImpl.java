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
 * UserNotificationServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 19.10.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.user.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.user.UserNotificationType;
import com.mobileman.kuravis.core.domain.user.notification.UserNotification;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.user.UserNotificationService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class UserNotificationServiceImpl extends AbstractEntityServiceImpl<UserNotification> implements UserNotificationService {
	
	@Autowired
	private UserService userService;
	
	private final String lastUserNotificationsReadTimestamp = "lastUserNotificationsReadTimestamp";

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return UserNotification.ENTITY_NAME;
	}
	
	private UserNotification createBaseNotificationObject(DBObject userToNotify, UserNotificationType type) {		
		UserNotification userNotification = new UserNotification();
		userNotification.setUserNotificationType(type);
		userNotification.setUserId((String)userToNotify.get(EntityUtils.ID));
		return userNotification;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void createNotificationForTreatmentReviewComment(DBObject userToNotify, DBObject treatmetReviewComment) {
		
		if (treatmetReviewComment == null) {
			throw ErrorUtils.exception("Undefine comment", ErrorCodes.INCORRECT_PARAMETER);
		}
				
		UserNotification notification = createBaseNotificationObject(userToNotify, UserNotificationType.TREATMENT_REVIEW_COMMENT);
		notification.setTreatmentReviewId((String) treatmetReviewComment.get(UserNotification.TREATMENT_REVIEW_ID));
		notification.setTreatmentReviewCommentId((String) treatmetReviewComment.get(EntityUtils.ID));
		notification.setText((String) treatmetReviewComment.get(UserNotification.TEXT));
		create(notification);
	}

	/** 
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void createNotificationForTreatmentReviewVote(DBObject userToNotify, DBObject treatmentReviewVote) {
		
		if (treatmentReviewVote == null) {
			throw ErrorUtils.exception("Undefine vote", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		UserNotification notification = createBaseNotificationObject(userToNotify, UserNotificationType.TREATMENT_REVIEW_VOTE);
		notification.setTreatmentReviewId((String) treatmentReviewVote.get(UserNotification.TREATMENT_REVIEW_ID));
		notification.setTreatmentReviewVoteId((String) treatmentReviewVote.get(EntityUtils.ID));
		create(notification);
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserNotificationService#getUnreadNotificationsCount()
	 */
	@Override
	public DBObject getUnreadNotificationsCount() {
		
		DBObject user = (DBObject) SecurityUtils.getSubject().getPrincipal();
		
		Date timestamp = (Date) user.get(lastUserNotificationsReadTimestamp);
		long count = 0L;
		if (timestamp == null) {
			count = getCollection().count(new BasicDBObject(UserNotification.USER_ID, user.get(EntityUtils.ID)));
		} else {
			count = getCollection().count(new BasicDBObject(UserNotification.USER_ID, user.get(EntityUtils.ID)).append(EntityUtils.CREATED_ON, new BasicDBObject("$gte", timestamp)));
		}
		
		DBObject result = new BasicDBObject("count", count);
		return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserNotificationService#getNotifications(org.springframework.data.domain.Pageable)
	 */
	@Override
	public List<DBObject> getNotifications(Pageable page) {
		
		DBObject user = (DBObject) SecurityUtils.getSubject().getPrincipal();
		
		Date timestamp = (Date) user.get(lastUserNotificationsReadTimestamp);
		
		if (page.getPageNumber() == 0) {
			user.put(lastUserNotificationsReadTimestamp, new Date());
			getMongoTemplate().getCollection(EntityUtils.USER).update(
					new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), 
					new BasicDBObject("$set", new BasicDBObject(lastUserNotificationsReadTimestamp, user.get(lastUserNotificationsReadTimestamp))));
		}
		
		List<DBObject> result = findAllByQuery(new BasicDBObject(UserNotification.USER_ID, user.get(EntityUtils.ID)), page);
		Set<String> usersId = new HashSet<String>();
		for (DBObject notification : result) {
			String userId = EntityUtils.getEntityId(notification.get("user"));
			usersId.add(userId);
		}
		
		Map<String, DBObject> settingsMap = userService.findUsersData(usersId, "settings", User.ATTR_GENDER, User.ATTR_YEAR_OF_BIRTH);
		
		for (DBObject notification : result) {
			
			DBObject notifUser = (DBObject) notification.get("user");
			String userId = EntityUtils.getEntityId(notifUser);
			notifUser.put("settings", settingsMap.get(userId).get("settings"));
			notifUser.put(User.ATTR_GENDER, settingsMap.get(userId).get(User.ATTR_GENDER));
			notifUser.put(User.ATTR_YEAR_OF_BIRTH, settingsMap.get(userId).get(User.ATTR_YEAR_OF_BIRTH));
			
			Date createdOn = (Date) notification.get(EntityUtils.CREATED_ON);
			if (timestamp == null || timestamp.compareTo(createdOn) <= 0) {
				notification.put("unread", Boolean.TRUE);
			}
		}
		
		return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserNotificationService#getUnreadNotifications(org.springframework.data.domain.Pageable)
	 */
	@Override
	public List<DBObject> getUnreadNotifications(Pageable page) {
		
		DBObject user = (DBObject) SecurityUtils.getSubject().getPrincipal();
		
		Date timestamp = (Date) user.get(lastUserNotificationsReadTimestamp);
		
		if (page.getPageNumber() == 0) {
			user.put(lastUserNotificationsReadTimestamp, new Date());
			getMongoTemplate().getCollection(EntityUtils.USER).update(
					new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), 
					new BasicDBObject("$set", new BasicDBObject(lastUserNotificationsReadTimestamp, user.get(lastUserNotificationsReadTimestamp))));
		}
		
		DBObject filter = new BasicDBObject(UserNotification.USER_ID, user.get(EntityUtils.ID));
		if (timestamp != null) {
			filter.put(EntityUtils.CREATED_ON, new BasicDBObject("$gte", timestamp));
		}
		
		List<DBObject> result = findAllByQuery(filter, page);
		Set<String> usersId = new HashSet<String>();
		for (DBObject notification : result) {
			String userId = EntityUtils.getEntityId(notification.get("user"));
			usersId.add(userId);
		}
		
		Map<String, DBObject> settingsMap = userService.findUsersData(usersId, "settings", User.ATTR_GENDER, User.ATTR_YEAR_OF_BIRTH);
		
		for (DBObject notification : result) {
			
			DBObject notifUser = (DBObject) notification.get("user");
			String userId = EntityUtils.getEntityId(notifUser);
			notifUser.put("settings", settingsMap.get(userId).get("settings"));
			notifUser.put(User.ATTR_GENDER, settingsMap.get(userId).get(User.ATTR_GENDER));
			notifUser.put(User.ATTR_YEAR_OF_BIRTH, settingsMap.get(userId).get(User.ATTR_YEAR_OF_BIRTH));
			
			Date createdOn = (Date) notification.get(EntityUtils.CREATED_ON);
			if (timestamp == null || timestamp.compareTo(createdOn) <= 0) {
				notification.put("unread", Boolean.TRUE);
			}
		}
		
		return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.UserNotificationService#deleteAllUserNotificationsForTreatmentReview(com.mongodb.DBObject)
	 */
	@Override
	public void deleteAllUserNotificationsForTreatmentReview(DBObject treatmentReview) {
		getCollection().remove(new BasicDBObject(UserNotification.TREATMENT_REVIEW_ID, treatmentReview.get(EntityUtils.ID)));
	}

	

}
