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
 * NewsAndAnnouncementsServiceImpl.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 18.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.messaging.news_announcements.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.FollowedEntity;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.user.UserState;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.TreatmentReviewUtil;
import com.mobileman.kuravis.core.services.messaging.mail.MailService;
import com.mobileman.kuravis.core.services.messaging.news_announcements.NewsAndAnnouncementsService;
import com.mobileman.kuravis.core.services.user.UserService;

/**
 * @author MobileMan GmbH
 * 
 */
@Service
public class NewsAndAnnouncementsServiceImpl extends QuartzJobBean implements NewsAndAnnouncementsService {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private UserService userService;

	private static Calendar[] getLastWeekDates() {
		Calendar c = Calendar.getInstance();
		int cdow = c.get(Calendar.DAY_OF_WEEK);
		Calendar lastMon = (Calendar) c.clone();
		lastMon.add(Calendar.DATE, -7 - (cdow - Calendar.MONDAY));
		Calendar lastSun = (Calendar) lastMon.clone();
		lastSun.add(Calendar.DATE, 6);
		return new Calendar[] { lastMon, lastSun };
	}

	/**
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.messaging.news_announcements.NewsAndAnnouncementsService#process()
	 */
	@Override
	public void process() {

		Calendar[] timeWindow = getLastWeekDates();
		Date monday = timeWindow[0].getTime();
		//Date sunday = timeWindow[1].getTime();
		 
		Set<String> followedEntitiesId = new HashSet<String>();
		/////////////////////////////////////////////////////////////////////////////
		// Find reviews
		Query newestReviewsQuery = Query.query(Criteria.where(EntityUtils.CREATED_ON).gte(monday));
		newestReviewsQuery.fields()
			.include(TreatmentReview.DISEASE)
			.include(TreatmentReview.TREATMENT)
			.include(TreatmentReview.AUTHOR)
			.include(TreatmentReview.TEXT)
			.include(TreatmentReview.RATING);
		List<TreatmentReview> newestReviews = mongoTemplate.find(newestReviewsQuery, TreatmentReview.class);
		Map<String, List<TreatmentReview>> reviewsBySummaryId = new HashMap<String, List<TreatmentReview>>();
		for (TreatmentReview review : newestReviews) {
			String summaryId = EntityUtils.createTreatmentReviewSummaryId(review.getDisease().get_id(), review.getTreatment().get_id());
			followedEntitiesId.add(review.getDisease().get_id());
			List<TreatmentReview> reviews = reviewsBySummaryId.get(summaryId);
			if (reviews == null) {
				reviews = new ArrayList<TreatmentReview>();
				reviewsBySummaryId.put(summaryId, reviews);
			}
			
			review.setRating(TreatmentReviewUtil.convertRating(review.getRating()));
			reviews.add(review);
		}
		
		/////////////////////////////////////////////////////////////////////////////
		// Find summaries
		Query newestSummariesQuery = Query.query(Criteria.where(EntityUtils.CREATED_ON).gte(monday)
						.orOperator(
								Criteria.where(TreatmentReviewSummary.SUGGESTION).is(Boolean.FALSE), 
								Criteria.where(TreatmentReviewSummary.SUGGESTION).is(null)));
		newestSummariesQuery.fields()
			.include(TreatmentReviewSummary.DISEASE)
			.include(TreatmentReviewSummary.TREATMENT);
		
		List<TreatmentReviewSummary> newestSummaries = mongoTemplate.find(newestSummariesQuery, TreatmentReviewSummary.class);
		Map<String, List<TreatmentReviewSummary>> summariesByDisease = new HashMap<String, List<TreatmentReviewSummary>>();
		for (TreatmentReviewSummary summary : newestSummaries) {
			List<TreatmentReviewSummary> summaries = summariesByDisease.get(summary.getDisease().get_id());
			if (summaries == null) {
				summaries = new ArrayList<TreatmentReviewSummary>();
				summariesByDisease.put(summary.getDisease().get_id(), summaries);
			}
			
			summaries.add(summary);
			followedEntitiesId.add(summary.get_id());
		}
		
		////////////////////////////////////////////////////////////////////
		// Find users ID whose are following DISEASE or SUMMARY
		final Query usersFollowingQuery = Query.query(Criteria.where(FollowedEntity.ENTITY_ID).in(followedEntitiesId));
		List<FollowedEntity> followedEntities = mongoTemplate.find(usersFollowingQuery, FollowedEntity.class);
		Map<String, List<FollowedEntity>> followedEntitiesByUser = new HashMap<String, List<FollowedEntity>>();
		for (FollowedEntity followedEntity : followedEntities) {
			List<FollowedEntity> entities = followedEntitiesByUser.get(followedEntity.getUser().get_id());
			if (entities == null) {
				entities = new ArrayList<FollowedEntity>();
				followedEntitiesByUser.put(followedEntity.getUser().get_id(), entities);
			}
			
			entities.add(followedEntity);
		}
		
		// active user
		// subcsi
		Query usersQuery = Query.query(Criteria.where(User.STATE).is(UserState.ACTIVE.getValue())
				.and(User.ID).in(followedEntitiesByUser.keySet())
				.and("settings.privacySettings.emailNotification.news_announcements").is(Boolean.TRUE));
		usersQuery.fields()
			.include(User.NAME)
			.include(User.EMAIL);
		
		List<User> users = mongoTemplate.find(usersQuery, User.class);
		for (User user : users) {
			user.setFollowedEntities(followedEntitiesByUser.get(user.get_id()));
			this.mailService.sendNewsAndAnnouncements(user, reviewsBySummaryId, summariesByDisease);
		}
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
