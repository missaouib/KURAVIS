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
package com.mobileman.kuravis.core.services.entity.impl;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.event.Event;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.option_list.treatment_type.TreatmentType;
import com.mobileman.kuravis.core.domain.option_list.unit.Unit;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.treatment_side_effect.TreatmentSideEffect;
import com.mobileman.kuravis.core.domain.user.Role;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.user.UserAccount;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.entity.CommonEntityService;
import com.mongodb.BasicDBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class EntityServiceImpl extends AbstractEntityServiceImpl implements CommonEntityService {

	@Override
	protected String getEntityName() {
		return "";
	}
	
	/**
	 * 
	 */
	@PostConstruct
	public void startup() {
		getMongoTemplate().getDb().getCollection(EntityUtils.TREATMENT).ensureIndex(new BasicDBObject(Treatment.NAME, 1), new BasicDBObject("unique", true));
		getMongoTemplate().getDb().getCollection(EntityUtils.DISEASE).ensureIndex(new BasicDBObject(Disease.NAME, 1), new BasicDBObject("unique", true));
		getMongoTemplate().getDb().getCollection(EntityUtils.DISEASE).ensureIndex(new BasicDBObject(Disease.TREATMENT_REVIEWS_COUNT, 1), new BasicDBObject("unique", false));
		getMongoTemplate().getDb().getCollection(EntityUtils.TREATMENT_SIDE_EFFECT).ensureIndex(new BasicDBObject(TreatmentSideEffect.ATTR_NAME, 1), new BasicDBObject("unique", true));
		getMongoTemplate().getDb().getCollection(EntityUtils.ROLE).ensureIndex(new BasicDBObject(Role.ATTR_NAME, 1), new BasicDBObject("unique", true));
		getMongoTemplate().getDb().getCollection(EntityUtils.USER).ensureIndex(new BasicDBObject(User.ATTR_ACCOUNT_ID, 1), new BasicDBObject("unique", true));
		getMongoTemplate().getDb().getCollection(EntityUtils.USERACCOUNT).ensureIndex(new BasicDBObject(UserAccount.ATTR_EMAIL, 1), new BasicDBObject("unique", true));
		getMongoTemplate().getDb().getCollection(TreatmentReviewSummary.ENTITY_NAME).ensureIndex(new BasicDBObject("rating", 1), new BasicDBObject("unique", false));
		
		getMongoTemplate().getDb().getCollection(Event.ENTITY_NAME).ensureIndex(new BasicDBObject(Event.EVENT_TYPE, 1), new BasicDBObject("unique", false));
		getMongoTemplate().getDb().getCollection(Event.ENTITY_NAME).ensureIndex(new BasicDBObject("rating", 1), new BasicDBObject("unique", false));
		
		getMongoTemplate().getDb().getCollection(Unit.ENTITY_NAME).ensureIndex(new BasicDBObject(Treatment.NAME, 1), new BasicDBObject("unique", true));
		getMongoTemplate().getDb().getCollection(TreatmentType.ENTITY_NAME).ensureIndex(new BasicDBObject(Disease.NAME, 1), new BasicDBObject("unique", true));
		
		getMongoTemplate().getDb().getCollection(TreatmentEvent.ENTITY_NAME).ensureIndex(
				new BasicDBObject(TreatmentEvent.DISEASE_ID, 1).append(TreatmentEvent.TREATMENT_ID, 1), new BasicDBObject("unique", false));
	}
}
