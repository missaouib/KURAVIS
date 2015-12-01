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
 * MailManager.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 12.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.messaging.mail;

import java.util.List;
import java.util.Map;

import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.FollowedEntity;
import com.mobileman.kuravis.core.domain.user.User;
import com.mongodb.DBObject;


/**
 * @author MobileMan GmbH
 *
 */
public interface MailService {

	/**
	 * @param user 
	 * @param resetPasswordUuid 
	 */
	void sendResetCredientialsEmail(final DBObject user, String resetPasswordUuid);

	/**
	 * @param user
	 * @param activationUuid 
	 */
	void sendActivationEmail(DBObject user, String activationUuid);
	
	/**
	 * @param user
	 */
	void sendWelcomeEmail(DBObject user);
	
	/**
	 * @param user
	 * @param event 
	 */
	void sendTreatmentEventRemminder(User user, TreatmentEvent event);
	
	/**
	 * @param user 
	 * @param diseasesById
	 * @param summariesByDisease 
	 */
	void sendNewsAndAnnouncements(User user, final Map<String, List<TreatmentReview>> diseasesById, Map<String, List<TreatmentReviewSummary>> summariesByDisease);

	/**
	 * @param user
	 * @param data 
	 */
	void sendWeeklyUpdates(final User user, DBObject data);

	/**
	 * @param sender
	 * @param email
	 */
	void sendInvitationEmail(DBObject sender, String email);

	/**
	 * @param subscription
	 */
	void sendUserSubscribedEmail(DBObject subscription);
	
	/**
	 * @param userFeedback
	 * @param email 
	 */
	void sendUserFeedbackMail(String userFeedback, String email);
	
	void sendFollowNotification(final FollowedEntity entity);
	
}
