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
 * NewsAndAnnouncementsServiceTest.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 19.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.messaging;

import static com.mobileman.kuravis.core.domain.Attributes.ID;
import static com.mobileman.kuravis.core.domain.util.EntityUtils.getEntityId;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.FollowedEntity;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.messaging.news_announcements.NewsAndAnnouncementsService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.services.user.FollowedEntityService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class NewsAndAnnouncementsServiceTest extends AbstractIntegrationTest {
	
	@Autowired
	private NewsAndAnnouncementsService newsAndAnnouncementsService;
	
	@Autowired
	private DiseaseService diseaseService;
	
	@Autowired
	private TreatmentService treatmentService;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;
	
	@Autowired
	private FollowedEntityService followedEntityService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.AbstractIntegrationTest#getCollectionName()
	 */
	@Override
	public String getCollectionName() {
		return "";
	}
	
	@Override
	@Before
	public void setUp() {
		super.setUp();
		//Data.setUp(mongoTemplate, userService);
		assertFalse(ErrorUtils.isError(userService.signin("peter.novak1@test.com", "peter.novak", "", false)));
	}
	
	/**
	 * 
	 */
	private String createReview(DBObject disease, DBObject treatment) {
		Subject currentUser = SecurityUtils.getSubject();
		DBObject user = (DBObject) currentUser.getPrincipal();
				
		List<DBObject> reviews = treatmentReviewService.findAllByQuery(
				Query.query(Criteria.where(TreatmentReview.DISEASE + "." + ID).is(getEntityId(disease))
						.and(TreatmentReview.TREATMENT + "." + ID).is(getEntityId(treatment))
						.and(TreatmentReview.AUTHOR + "." + ID).is(getEntityId(user))).getQueryObject());
		if (!reviews.isEmpty()) {
			return getEntityId(reviews.get(0));
		}
		
		DBObject review = new BasicDBObject();
		review.put(TreatmentReview.DISEASE, disease.toMap());
		review.put(TreatmentReview.TREATMENT, treatment.toMap());
		review.put(TreatmentReview.RATING, 0.6d);
		review.put(TreatmentReview.TEXT, "This is really cool!");
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		return getEntityId(result);
	}
	
	/**
	 * @param summariesId
	 * @param diseases
	 */
	private void setupFollowing(List<String> summariesId, List<DBObject> diseases) {
		
		Subject currentUser = SecurityUtils.getSubject();
		DBObject user = (DBObject) currentUser.getPrincipal();
		
		for (DBObject disease : diseases) {
			long count = followedEntityService.count(
					Query.query(Criteria.where(FollowedEntity.ENTITY_ID).is(getEntityId(disease))
							.and(FollowedEntity.USER + "." + ID).is(getEntityId(user))).getQueryObject());
			if (count == 0) {
				FollowedEntity entity = new FollowedEntity();
				entity.setEntityType(Disease.ENTITY_NAME);
				entity.setEntityId(getEntityId(disease));
				followedEntityService.follow(entity);
			}
		}
		
		for (String summaryId : summariesId) {
			long count = followedEntityService.count(
					Query.query(Criteria.where(FollowedEntity.ENTITY_ID).is(summaryId)
							.and(FollowedEntity.USER + "." + ID).is(getEntityId(user))).getQueryObject());
			if (count == 0) {
				FollowedEntity entity = new FollowedEntity();
				entity.setEntityType(TreatmentReviewSummary.ENTITY_NAME);
				entity.setEntityId(summaryId);
				followedEntityService.follow(entity);
			}
		}
	}
	
	/**
	 * 
	 */
	private String findReviewSummaryId(DBObject disease, DBObject treatment) {
		List<DBObject> summaries = treatmentReviewSummaryService.findAllByQuery(
				Query.query(Criteria.where(TreatmentReviewSummary.DISEASE + "." + ID).is(getEntityId(disease))
						.and(TreatmentReviewSummary.TREATMENT + "." + ID).is(getEntityId(treatment))).getQueryObject());
		if (summaries.isEmpty()) {
			return null;
		}
		
		return getEntityId(summaries.get(0));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testProcess() throws Exception {
		Subject currentUser = SecurityUtils.getSubject();
		DBObject user = (DBObject) currentUser.getPrincipal();
		
		List<DBObject> diseases = new ArrayList<DBObject>();
		DBObject headache = diseaseService.findByProperty(Disease.NAME, "Headache");
		DBObject malaria = diseaseService.findByProperty(Disease.NAME, "Malaria");
		diseases.add(headache);
		diseases.add(malaria);
		
		DBObject paralen = treatmentService.findByProperty(Treatment.NAME, "Paralen");
		DBObject doloangin = treatmentService.findByProperty(Treatment.NAME, "Dolo-Angin");
		DBObject vitaminC = treatmentService.findByProperty(Treatment.NAME, "Vitamin C");
		
		List<String> reviewsId = new ArrayList<String>();
		reviewsId.add(createReview(headache, paralen));
		reviewsId.add(createReview(malaria, doloangin));
		
		reviewsId.add(createReview(headache, vitaminC));
		reviewsId.add(createReview(malaria, paralen));
		
		List<String> summariesId = new ArrayList<String>();		
		summariesId.add(findReviewSummaryId(headache, paralen));
		summariesId.add(findReviewSummaryId(malaria, doloangin));
		summariesId.add(findReviewSummaryId(headache, vitaminC));
		summariesId.add(findReviewSummaryId(malaria, paralen));
		
		setupFollowing(summariesId, diseases);
				
		try {
			
			newsAndAnnouncementsService.process();
			
		} finally {
			followedEntityService.unfollowAll(getEntityId(user));
			for (String reviewId : reviewsId) {
				treatmentReviewService.delete(getEntityId(reviewId));
			}
		}
		
	}

	

	
}
