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
 * TreatmentSideEffectServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 29.8.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.treatment_side_effect.impl;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.treatment_side_effect.TreatmentSideEffect;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.RoleUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.treatment_side_effect.TreatmentSideEffectService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class TreatmentSideEffectServiceImpl extends AbstractEntityServiceImpl<TreatmentSideEffect> implements TreatmentSideEffectService {
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return EntityUtils.TREATMENT_SIDE_EFFECT;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#delete(java.lang.String, java.lang.String)
	 */
	@Override
	public DBObject delete(String type, String id) {
		
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser == null || !currentUser.isAuthenticated()) {
			return ErrorUtils.error("Not authenticated: currentUser=" + currentUser, ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		
		DBObject user = (DBObject) currentUser.getPrincipal();
		if (!RoleUtils.isAdminAccount((DBObject) user.get("account"))) {
			return ErrorUtils.error("Unknown account: " + user, ErrorCodes.UNAUTHORIZED);
		}
		
		DBCursor cursor = getCollection(TreatmentReview.ENTITY_NAME).find(
				new BasicDBObject("sideEffects", new BasicDBObject("$elemMatch", new BasicDBObject("sideEffect._id", id))), 
				new BasicDBObject("disease." + EntityUtils.ID, 1).append("treatment." + EntityUtils.ID, 1));
		
		for (DBObject dbObject : cursor) {
			String treatmentReviewId = (String) dbObject.get(EntityUtils.ID);	
			this.treatmentReviewService.delete(treatmentReviewId);
		}
		
		return super.delete(type, id);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#update(java.lang.String, java.lang.String, com.mongodb.DBObject)
	 */
	@Override
	public DBObject update(String type, String id, DBObject sideEffect) {
		DBObject result = validateName(type, id, sideEffect);
		String dbName = EntityUtils.getEntityName(result);
		result = super.update(type, id, sideEffect);
		if (ErrorUtils.isError(result)) {
			return result;
		}
		String givenName = EntityUtils.getEntityName(sideEffect);
		if (!ObjectUtils.nullSafeEquals(givenName, dbName)) {
			getCollection(TreatmentReview.ENTITY_NAME).update(new BasicDBObject("sideEffects.sideEffect." + EntityUtils.ID, id),
					new BasicDBObject("$set", new BasicDBObject("sideEffects.$.sideEffect." + EntityUtils.NAME, givenName)), false, true);

			getCollection(TreatmentReviewSummary.ENTITY_NAME).update(new BasicDBObject("sideEffects." + EntityUtils.NAME, dbName),
					new BasicDBObject("$set", new BasicDBObject("sideEffects.$." + EntityUtils.NAME, givenName)), false, true);
		}
		return result;
	}
}
