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
 * InvitationServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 6.11.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.user.impl;

import static com.mobileman.kuravis.core.util.ErrorUtils.exception;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.RoleUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.messaging.mail.MailService;
import com.mobileman.kuravis.core.services.treatment_review.TempTreatmentReviewService;
import com.mobileman.kuravis.core.services.user.InvitationService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class InvitationServiceImpl extends AbstractEntityServiceImpl implements InvitationService {
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TempTreatmentReviewService tempTreatmentReviewService;

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return EntityUtils.USER_INVITATION;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.InvitationService#sendInvitation(java.lang.String)
	 */
	@Override
	public void sendInvitation(String email) {
		if (StringUtils.isEmpty(email)) {
			throw exception("Email si nil", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject user = getLoggedUser();
		if (user == null) {
			throw exception("Not authenticated", ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		
		DBObject invitation = getCollection().findOne(new BasicDBObject("user." + EntityUtils.ID, user.get(EntityUtils.ID)).append("email", email));
		if (invitation == null) {
			DBObject dbuser = userService.findById((String) user.get(EntityUtils.ID));
			Number invitationCount = (Number) dbuser.get("invitationCount");
			if (invitationCount == null || invitationCount.intValue() == 0) {
				if (!RoleUtils.isAdminUser(user)) {
					throw exception(ErrorCodes.INVITATION_NOT_POSSIBLE);
				}
			}
			
			invitation = new BasicDBObject();
			EntityUtils.setBasePropertiesOnCreate(invitation);
			invitation.put("email", email);
			invitation.put("user", EntityUtils.createBaseUser(user));
			getCollection().save(invitation);
			
			if (!RoleUtils.isAdminUser(user)) {
				getCollection(EntityUtils.USER).update(new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), 
						new BasicDBObject("$inc", new BasicDBObject("invitationCount", -1)));
			}
		}
		
		this.mailService.sendInvitationEmail(user, email);
		
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public DBObject findAllForUser(String userId) {
		if (userId == null) {
			throw exception("user si null", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject user = userService.findById(userId);
		List<DBObject> invitations = super.findAllByQuery(new BasicDBObject("user." + EntityUtils.ID, userId), new PageRequest(0, Integer.MAX_VALUE, Direction.ASC, "createdOn"));
		DBObject result = new BasicDBObject("invitations", invitations).append("invitationCount", user.get("invitationCount"));
		return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.InvitationService#inviteeData(java.lang.String)
	 */
	@Override
	public DBObject inviteeData(String email) {
		boolean userExists = userService.userExistsWithEmail(email);
		boolean invitationExists = count(new BasicDBObject("email", email)) > 0;
		boolean canBeregistered = invitationExists && !userExists;
		DBObject result = new BasicDBObject();
		result.put("canberegistered", canBeregistered);
		
		DBObject tempTreatmentReview = null;
		if (canBeregistered) {
			QueryBuilder query = QueryBuilder.start().put("subscription.email").is(email);
			List<DBObject> tempReviews = tempTreatmentReviewService.findAllByQuery(query.get());
			tempTreatmentReview = tempReviews.isEmpty() ? null : tempReviews.get(0);
		}
		
		result.put("treatmentReview", tempTreatmentReview);
		return result;
	}

	
	
}
