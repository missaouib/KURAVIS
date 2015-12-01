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
 * UserNotification.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 23.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.user.notification;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mobileman.kuravis.core.domain.Entity;
import com.mobileman.kuravis.core.domain.user.UserNotificationType;


/**
 * @author MobileMan GmbH
 *
 */
@JsonSerialize(include=Inclusion.NON_NULL)
@Document(collection = UserNotification.ENTITY_NAME)
public class UserNotification extends Entity implements UserNotificationAttributes {
	
	private UserNotificationType userNotificationType;
	
	private String treatmentReviewId;
	
	private String treatmentReviewCommentId;
	
	private String treatmentReviewVoteId;

	private String userId;

	private String text;
	
	/**
	 *
	 * @return userId
	 */
	public String getUserId() {
		return this.userId;
	}

	/**
	 *
	 * @param userId userId
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	/**
	 *
	 * @return userNotificationType
	 */
	public UserNotificationType getUserNotificationType() {
		return this.userNotificationType;
	}

	/**
	 *
	 * @param userNotificationType userNotificationType
	 */
	public void setUserNotificationType(UserNotificationType userNotificationType) {
		this.userNotificationType = userNotificationType;
	}

	/**
	 *
	 * @return treatmentReviewId
	 */
	public String getTreatmentReviewId() {
		return this.treatmentReviewId;
	}

	/**
	 *
	 * @param treatmentReviewId treatmentReviewId
	 */
	public void setTreatmentReviewId(String treatmentReviewId) {
		this.treatmentReviewId = treatmentReviewId;
	}

	/**
	 *
	 * @return treatmentReviewCommentId
	 */
	public String getTreatmentReviewCommentId() {
		return this.treatmentReviewCommentId;
	}

	/**
	 *
	 * @param treatmentReviewCommentId treatmentReviewCommentId
	 */
	public void setTreatmentReviewCommentId(String treatmentReviewCommentId) {
		this.treatmentReviewCommentId = treatmentReviewCommentId;
	}

	/**
	 *
	 * @return text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 *
	 * @param text text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 *
	 * @return treatmentReviewVoteId
	 */
	public String getTreatmentReviewVoteId() {
		return this.treatmentReviewVoteId;
	}

	/**
	 *
	 * @param treatmentReviewVoteId treatmentReviewVoteId
	 */
	public void setTreatmentReviewVoteId(String treatmentReviewVoteId) {
		this.treatmentReviewVoteId = treatmentReviewVoteId;
	}
	
	
}
