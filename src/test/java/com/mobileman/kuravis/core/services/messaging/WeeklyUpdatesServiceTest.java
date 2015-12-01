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
 * WeeklyUpdatesServiceTest.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 22.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.messaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.messaging.weekly_updates.WeeklyUpdatesService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class WeeklyUpdatesServiceTest extends AbstractIntegrationTest {
	
	@Autowired
	private WeeklyUpdatesService weeklyUpdatesService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.AbstractIntegrationTest#getCollectionName()
	 */
	@Override
	public String getCollectionName() {
		return null;
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testProcess_NewComment() throws Exception {
		DBObject user = userService.findDBUserByEmail("peter.novak19@test.com");
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(new BasicDBObject("author._id", user.get(EntityUtils.ID)));
		assertNotNull(review);
		
		this.userService.signin("peter.novak14@test.com", "peter.novak", "", false);
		
		DBObject comment = new BasicDBObject("text", "testProcess_NewComment");
		DBObject result = this.treatmentReviewService.commentTreatmentReview((String)review.get(EntityUtils.ID), comment);
		assertFalse(ErrorUtils.isError(result));
		
		getMongoTemplate().getCollection(EntityUtils.USER).update(
				new BasicDBObject("email", user.get("email")), 
				new BasicDBObject("$set", new BasicDBObject("settings.privacySettings.emailNotification.weeklyUpdatesCommentsAndVotes", false)));
		
		Map<String, DBObject> data = weeklyUpdatesService.process();
		
		getMongoTemplate().getCollection(EntityUtils.USER).update(
				new BasicDBObject("email", user.get("email")), 
				new BasicDBObject("$set", new BasicDBObject("settings.privacySettings.emailNotification.weeklyUpdatesCommentsAndVotes", true)));
		
		Map<String, DBObject> data2 = weeklyUpdatesService.process();
		assertTrue(data2.size() > data.size());
		assertTrue(data2.containsKey(user.get(EntityUtils.ID)));
		
		getMongoTemplate().getCollection(EntityUtils.USER).update(
				new BasicDBObject("email", user.get("email")), 
				new BasicDBObject("$set", new BasicDBObject("settings.privacySettings.emailNotification.weeklyUpdatesCommentsAndVotes", false)));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testProcess_NewVoteAndComment() throws Exception {
		DBObject user = userService.findDBUserByEmail("peter.novak18@test.com");
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(new BasicDBObject("author._id", user.get(EntityUtils.ID)));
		assertNotNull(review);
		
		this.userService.signin("peter.novak14@test.com", "peter.novak", "", false);
		
		getMongoTemplate().getCollection(EntityUtils.USER).update(
				new BasicDBObject("email", user.get("email")), 
				new BasicDBObject("$set", new BasicDBObject("settings.privacySettings.emailNotification.weeklyUpdatesCommentsAndVotes", false)));
		
		DBObject result = this.treatmentReviewService.voteForTreatmentReview((String)review.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
		result = this.treatmentReviewService.commentTreatmentReview((String)review.get(EntityUtils.ID), new BasicDBObject("text", "testProcess_NewVoteAndComment"));
		assertFalse(ErrorUtils.isError(result));
		
		Map<String, DBObject> data = weeklyUpdatesService.process();
		
		getMongoTemplate().getCollection(EntityUtils.USER).update(
				new BasicDBObject("email", user.get("email")), 
				new BasicDBObject("$set", new BasicDBObject("settings.privacySettings.emailNotification.weeklyUpdatesCommentsAndVotes", true)));
		
		Map<String, DBObject> data2 = weeklyUpdatesService.process();
		assertTrue(data2.size() > data.size());
		assertTrue(data2.containsKey(user.get(EntityUtils.ID)));
		
		getMongoTemplate().getCollection(EntityUtils.USER).update(
				new BasicDBObject("email", user.get("email")), 
				new BasicDBObject("$set", new BasicDBObject("settings.privacySettings.emailNotification.weeklyUpdatesCommentsAndVotes", false)));
	}
}
