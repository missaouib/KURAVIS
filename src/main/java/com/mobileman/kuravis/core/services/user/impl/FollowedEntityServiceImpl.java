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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.FollowedEntity;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.UserUtils;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.messaging.mail.MailService;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.services.user.FollowedEntityService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Service
public class FollowedEntityServiceImpl extends AbstractEntityServiceImpl<FollowedEntity> implements FollowedEntityService {
	@Autowired
	private MailService mailService;

	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;

	@Autowired
	private DiseaseService diseaseService;

	private void loadText(FollowedEntity e) {
		String text = null;
		if (Disease.ENTITY_NAME.equalsIgnoreCase(e.getEntityType())) {
			text = diseaseService.getById(e.getEntityId()).getName();
		} else if (TreatmentReviewSummary.ENTITY_NAME.equalsIgnoreCase(e.getEntityType())) {
			TreatmentReviewSummary trs = treatmentReviewSummaryService.getById(e.getEntityId());
			text = trs.getDisease().getName() + " / " + trs.getTreatment().getName();
		}
		e.setText(text);
	}

	@Override
	public String follow(FollowedEntity entityToFollow) {
		EntityUtils.setBaseProperties(entityToFollow);
		String id = create(entityToFollow);
		loadText(entityToFollow);
		DBObject loggedUser = UserUtils.getLoggedUser();
		entityToFollow.getUser().setEmail((String) loggedUser.get(User.EMAIL));
		mailService.sendFollowNotification(entityToFollow);
		return id;
	}

	@Override
	public void unfollow(String id) {
		Query queryById = new Query().addCriteria(Criteria.where(FollowedEntity.ID).is(id));
		long count = getMongoTemplate().count(queryById, entityClass);
		if (count == 0) {
			throw new EmptyResultDataAccessException(1);
		}
		getMongoTemplate().remove(queryById, entityClass);
	}

	@Override
	public void unfollowAll(String userId) {
		getMongoTemplate().remove(new Query().addCriteria(Criteria.where(FollowedEntity.USER_ID).is(userId)), entityClass);
	}

	@Override
	public List<FollowedEntity> findByUserId(String userId) {
		Query query = new Query().addCriteria(Criteria.where(FollowedEntity.USER_ID).is(userId));
		List<FollowedEntity> result = getMongoTemplate().find(query, entityClass);
		if (!CollectionUtils.isEmpty(result)) {
			Map<String, FollowedEntity> entityMap = new HashMap<>();
			List<String> diseaseIds = new ArrayList<>();
			List<String> treatmentRevSumIds = new ArrayList<>();
			for (FollowedEntity e : result) {
				entityMap.put(e.getEntityId(), e);
				if (Disease.ENTITY_NAME.equalsIgnoreCase(e.getEntityType())) {
					diseaseIds.add(e.getEntityId());
				} else if (TreatmentReviewSummary.ENTITY_NAME.equalsIgnoreCase(e.getEntityType())) {
					treatmentRevSumIds.add(e.getEntityId());
				}
			}
			if (!treatmentRevSumIds.isEmpty()) {
				query = Query.query(Criteria.where(TreatmentReviewSummary.ID).in(treatmentRevSumIds));
				List<DBObject> summaries = treatmentReviewSummaryService.findAllByQuery(TreatmentReviewSummary.ENTITY_NAME, query.getQueryObject(), new BasicDBObject(TreatmentReviewSummary.ID, 1)
						.append(TreatmentReviewSummary.DISEASE, 1).append(TreatmentReviewSummary.TREATMENT, 1));
				for (DBObject e : summaries) {
					String id = EntityUtils.getEntityId(e);
					entityMap.get(id).setText(
							EntityUtils.getEntityName((DBObject) e.get(TreatmentReviewSummary.DISEASE)) + " / " + EntityUtils.getEntityName((DBObject) e.get(TreatmentReviewSummary.TREATMENT)));
				}
			}
			if (!diseaseIds.isEmpty()) {
				query = Query.query(Criteria.where(Disease.ID).in(diseaseIds));
				List<DBObject> items = diseaseService.findAllByQuery(Disease.ENTITY_NAME, query.getQueryObject(), new BasicDBObject(Disease.ID, 1).append(Disease.NAME, 1));
				for (DBObject e : items) {
					String id = EntityUtils.getEntityId(e);
					entityMap.get(id).setText((String) e.get(Disease.NAME));
				}
			}
		}
		return result;
	}

	@Override
	public FollowedEntity getByEntity(String userId, String entityType, String entityId) {
		Query query = new Query().addCriteria(Criteria.where(FollowedEntity.USER_ID).is(userId).and(FollowedEntity.ENTITY_TYPE).is(entityType).and(FollowedEntity.ENTITY_ID).is(entityId));
		return getMongoTemplate().findOne(query, entityClass);
	}
}
