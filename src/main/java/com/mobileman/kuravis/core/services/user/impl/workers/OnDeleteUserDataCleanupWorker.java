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
 * OnDeleteUserDataCleanupWorker.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 21.11.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.user.impl.workers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewEventType;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.event.EventService;
import com.mobileman.kuravis.core.services.fraud_report.FraudReportService;
import com.mobileman.kuravis.core.services.treatment_review.TempTreatmentReviewService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class OnDeleteUserDataCleanupWorker {

	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;
	
	@Autowired
	private FraudReportService fraudReportService;
	
	@Autowired
	private TempTreatmentReviewService tempTreatmentReviewService;
	
	@Autowired
	private EventService eventService;
	
	protected DBCollection getCollection(String collection) {
		return this.mongoTemplate.getDb().getCollection(collection);
	}
	
	/**
	 * @param context 
	 * 
	 */
	public void process(Map<String, Object> context) {
		String userId = (String) context.get("userId");
		String email = (String) context.get("email");
		this.treatmentReviewService.deleteAllTreatmentReviews(userId);
		
		DBCursor votes = getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).find(new BasicDBObject("user." + EntityUtils.ID, userId).append("type", TreatmentReviewEventType.VOTE.getValue()), 
				new BasicDBObject("treatmentReviewId", 1));
		Map<String, Void> marker = new HashMap<String, Void>();
		for (DBObject vote : votes) {
			String treatmentReviewId = (String) vote.get("treatmentReviewId");
			if (!marker.containsKey(treatmentReviewId)) {
				getCollection(TreatmentReview.ENTITY_NAME).update(new BasicDBObject(EntityUtils.ID, treatmentReviewId), new BasicDBObject("$inc", new BasicDBObject("votesCount", -1)));
				marker.put(treatmentReviewId, null);
			}
		}
		
		getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).remove(new BasicDBObject("user." + EntityUtils.ID, userId));
		
		this.tempTreatmentReviewService.deleteAllTempTreatmentReviewsOfUser(userId, email);
		
		this.fraudReportService.deleteFraudReportsOfUser(userId);
		
		this.eventService.deleteAllUserEvents(userId);
	}
}
