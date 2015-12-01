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
 * TempTreatmentReviewServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 18.11.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.treatment_review.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.event.EventService;
import com.mobileman.kuravis.core.services.treatment_review.TempTreatmentReviewService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class TempTreatmentReviewServiceImpl extends AbstractEntityServiceImpl implements TempTreatmentReviewService {
	@Autowired
	private EventService eventService;
	
	@Autowired
	private UserService userService;

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return EntityUtils.TEMP_TREATMENT_REVIEW;
	}

	/**
	 * @param userId  
	 */
	public void deleteAllTempTreatmentReviewsOfUser(String userId) {
		DBObject user = userService.findById(userId);
		String userEmail = "";
		if (user != null) {
			userEmail = (String) user.get("email");
		}
		
		deleteAllTempTreatmentReviewsOfUser(userId, userEmail);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.treatment_review.TempTreatmentReviewService#deleteAllTempTreatmentReviewsOfUser(java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteAllTempTreatmentReviewsOfUser(String userId, String email) {
		if (userId == null) {
			userId = "";
		}
		
		if (email == null) {
			email = "";
		}
		
		QueryBuilder query = QueryBuilder.start().or(new BasicDBObject("subscription.email", email), new BasicDBObject("author." + EntityUtils.ID, userId));
		getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).remove(query.get());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DBObject> findUserReviewsGoupByDisease(String userId) {
		Query query = Query.query(Criteria.where(TreatmentReview.AUTHOR + "." + TreatmentReview.ID).is(userId));
		Sort sort = new Sort(TreatmentReview.CURED, TreatmentReview.DISEASE, TreatmentReview.TREATMENT);
		query.with(sort);
		List<DBObject> result = new ArrayList<>();
		Map<String, List<DBObject>> diseaseIdReviewsMap = new LinkedHashMap<>(); 
		List<DBObject> reviews = findAllByQuery(query.getQueryObject(), new PageRequest(0, 200, sort));
		for (DBObject review : reviews) {
			DBObject disease = (DBObject) review.get(TreatmentReview.DISEASE);
			String disaeseId = EntityUtils.getEntityId(disease);
			boolean curen = EntityUtils.getBoolean(TreatmentReview.CURED, review);
			String key = curen + "|" + disaeseId;
			List<DBObject> trs = diseaseIdReviewsMap.get(key);
			if (trs == null) {
				trs = new ArrayList<>();
				diseaseIdReviewsMap.put(key, trs);
				
				// put diseaseItem
				DBObject diseaseItem = new BasicDBObject(Disease.NAME, disease.get(Disease.NAME));
				diseaseItem.put(TreatmentReview.ENTITY_NAME + "s", trs);
				diseaseItem.put(TreatmentReview.CURED, curen);
				result.add(diseaseItem);
			}
			TreatmentEvent te = eventService.findLastTreatmentEvent(userId, disaeseId, EntityUtils.getEntityId(review.get(TreatmentReview.TREATMENT)));
			if (te != null) {
				review.put(TreatmentEvent.ENTITY_NAME, te);
			}
			trs.add(review);
		}
		return result;
	}
	
}
