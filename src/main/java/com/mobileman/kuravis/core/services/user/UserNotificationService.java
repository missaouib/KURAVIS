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
 * UserNotificationService.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 19.10.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.user;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.mobileman.kuravis.core.domain.user.notification.UserNotification;
import com.mobileman.kuravis.core.services.entity.EntityService;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public interface UserNotificationService extends EntityService<UserNotification> {

	/**
	 * @param userToNotify 
	 * @param treatmetReviewComment
	 */
	void createNotificationForTreatmentReviewComment(DBObject userToNotify, DBObject treatmetReviewComment);
	
	/**
	 * @param userToNotify 
	 * @param treatmentReviewVote
	 */
	void createNotificationForTreatmentReviewVote(DBObject userToNotify, DBObject treatmentReviewVote);

	/**
	 * @return count of unread notifications
	 */
	DBObject getUnreadNotificationsCount();

	/**
	 * @param page
	 * @return all notifications
	 */
	List<DBObject> getNotifications(Pageable page);
	
	/**
	 * @param page
	 * @return all notifications
	 */
	List<DBObject> getUnreadNotifications(Pageable page);

	/**
	 * Deletes all user notifications associated to given treatment review
	 * @param treatmentReview
	 */
	void deleteAllUserNotificationsForTreatmentReview(DBObject treatmentReview);

}
