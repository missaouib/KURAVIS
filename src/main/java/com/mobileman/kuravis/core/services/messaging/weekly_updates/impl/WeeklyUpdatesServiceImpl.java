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
 * WeeklyUpdatesServiceImpl.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 18.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.messaging.weekly_updates.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewEvent;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.user.UserState;
import com.mobileman.kuravis.core.domain.util.CollectionUtil;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.messaging.mail.MailService;
import com.mobileman.kuravis.core.services.messaging.weekly_updates.WeeklyUpdatesService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class WeeklyUpdatesServiceImpl extends QuartzJobBean implements WeeklyUpdatesService {
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private MailService mailService;

	private static Date[] getLastWeekDates() {
		Calendar c = Calendar.getInstance();
		int cdow = c.get(Calendar.DAY_OF_WEEK);
		Calendar lastMon = (Calendar) c.clone();
		lastMon.add(Calendar.DATE, -7 - (cdow - Calendar.MONDAY));
		Calendar lastSun = (Calendar) lastMon.clone();
		lastSun.add(Calendar.DATE, 6);
		return new Date[] { lastMon.getTime(), lastSun.getTime() };
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.messaging.weekly_updates.WeeklyUpdatesService#process()
	 */
	@Override
	public Map<String, DBObject> process() {
		
		// Get all treatment reviews with either new vote or new comment during last week
		Date[] timeWindow = getLastWeekDates();
		Date monday = timeWindow[0];
		//Date sunday = timeWindow[1].getTime(); 	
		
		AggregationOperation matchOp = Aggregation.match(Criteria.where(EntityUtils.CREATED_ON).gte(monday));
		AggregationOperation groupOp = Aggregation.group(TreatmentReviewEvent.TREATMENT_REVIEW_AUTHOR_ID, TreatmentReviewEvent.TYPE).count().as("typeCount");
		AggregationOperation projectOp = Aggregation.project().andInclude(TreatmentReviewEvent.TREATMENT_REVIEW_AUTHOR_ID, TreatmentReviewEvent.TYPE, "typeCount");
		
		AggregationResults<BasicDBObject> aggregationResults = mongoTemplate.aggregate(
				Aggregation.newAggregation(matchOp, projectOp, groupOp), TreatmentReviewEvent.ENTITY_NAME, BasicDBObject.class);
		List<BasicDBObject> mappedResults = aggregationResults.getMappedResults();
		Map<String, DBObject> usersData = new HashMap<>();
		List<String> usersId = new ArrayList<String>();
		for (BasicDBObject result : mappedResults) {
			String authorId = (String) result.get(TreatmentReviewEvent.TREATMENT_REVIEW_AUTHOR_ID);
			usersId.add(authorId);
			String type = (String) result.get(TreatmentReviewEvent.TYPE);
			Number count = (Number) result.get("typeCount");
			if (usersData.containsKey(authorId)) {
				usersData.get(authorId).put(type, count);
			} else {
				result.put(type, count);
				usersData.put(authorId, result);
			}
		}
		
		Map<String, DBObject> result = new HashMap<String, DBObject>();
		for (List<String> splitedUsersId : CollectionUtil.split(usersId, 1000)) {
			Query query = Query.query(Criteria
					.where(EntityUtils.ID).in(splitedUsersId)
					.and("settings.privacySettings.emailNotification.weeklyUpdatesCommentsAndVotes").is(Boolean.TRUE)
					.and(User.STATE).is(UserState.ACTIVE.getValue())
					);
			query.fields().include(User.EMAIL);
			List<User> users = mongoTemplate.find(query, User.class);
			for (User user : users) {
				result.put(user.get_id(), usersData.get(user.get_id()));
				this.mailService.sendWeeklyUpdates(user, usersData.get(user.get_id()));
			}
		}
				
		return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
	 */
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		process();
	}


}
