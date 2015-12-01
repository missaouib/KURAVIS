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
package com.mobileman.kuravis.core.services.treatment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.domain.fraud_report.FraudReport;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.user.notification.UserNotification;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

@Ignore
@ContextConfiguration(locations = { "/spring/application-context.xml" })
public class TreatmentServiceAspirinTest extends AbstractIntegrationTest {

	@Autowired
	private TreatmentService treatmentService;

	@Autowired
	private TreatmentReviewService treatmentReviewService;

	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;

	@Override
	public String getCollectionName() {
		return "treatment";
	}

	@Test
	@Ignore
	public void mergeAspirin() {
		// KUR-482
		String aspirinName = "Aspirin";
		List<DBObject> aspirins = treatmentService.findAllByQuery(new BasicDBObject(EntityUtils.NAME, aspirinName));
		DBObject source = null;
		DBObject target = null;
		for (DBObject aspirin : aspirins) {
			String entityName = EntityUtils.getEntityName(aspirin);
			if (aspirinName.equals(entityName)) {
				target = aspirin;
			} else {
				source = aspirin;
			}
		}
		String sourceTreatmentId = EntityUtils.getEntityId(source);
		String targetTreatmentId = EntityUtils.getEntityId(target);
		String diseaseId = null;

		List<String> givenReviewIds = new ArrayList<String>();
		DBObject query = QueryBuilder.start().put(TreatmentReview.TREATMENT + "." + TreatmentReview.ID).in(new Object[] { sourceTreatmentId, targetTreatmentId }).get();
		List<DBObject> reviews = treatmentReviewService.findAllByQuery(query);
		for (DBObject dbObject : reviews) {
			givenReviewIds.add(EntityUtils.getEntityId(dbObject));
			diseaseId = EntityUtils.getEntityId(dbObject.get(TreatmentReview.DISEASE));
		}

		try {
			int events = countByIds(EntityUtils.TREATMENT_REVIEW_EVENT, "treatmentReviewId", givenReviewIds);
			int notifications = countByIds(UserNotification.ENTITY_NAME, "treatmentReviewId", givenReviewIds);
			int fraudReports = countByIds(EntityUtils.FRAUD_REPORT, FraudReport.ATTR_ENTITY_ID, givenReviewIds);
			int fraudReportItems = countByIds(EntityUtils.FRAUD_REPORT_ITEM, FraudReport.ATTR_ENTITY_ID, givenReviewIds);

			DBObject reviewSummary = treatmentReviewSummaryService.findById(diseaseId + sourceTreatmentId);
			DBObject genderStatistics = (DBObject) reviewSummary.get("genderStatistics");		
			int expectedFemaleCount = EntityUtils.getInt("female", genderStatistics);
			int expectedMaleCount = EntityUtils.getInt("male", genderStatistics);
			reviewSummary = treatmentReviewSummaryService.findById(diseaseId + targetTreatmentId);
			genderStatistics = (DBObject) reviewSummary.get("genderStatistics");		
			expectedFemaleCount += EntityUtils.getInt("female", genderStatistics);
			expectedMaleCount += EntityUtils.getInt("male", genderStatistics);
			
			loginTestUserAdmin();
			treatmentService.merge(sourceTreatmentId, targetTreatmentId);
			List<DBObject> actualReviews = treatmentReviewService.findAllByQuery(new BasicDBObject("treatment._id", targetTreatmentId));
			int actualReviewsCount = actualReviews.size();
			assertEquals(reviews.size(), actualReviewsCount);

			reviewSummary = treatmentReviewSummaryService.findById(diseaseId + targetTreatmentId);
			genderStatistics = (DBObject) reviewSummary.get("genderStatistics");		
			int actualFemaleCount = EntityUtils.getInt("female", genderStatistics);
			int actualMaleCount = EntityUtils.getInt("male", genderStatistics);
			assertEquals(expectedFemaleCount, actualFemaleCount);
			assertEquals(expectedMaleCount, actualMaleCount);

			DBObject filter = QueryBuilder.start().put("summaryId").is(EntityUtils.getEntityId(reviewSummary)).get();
			List<DBObject> array = getMongoTemplate().getCollection(EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS).find(filter).toArray();
			actualFemaleCount = 0;
			actualMaleCount = 0;
			for (DBObject item : array) {
				String gender = (String) item.get("gender");
				if ("male".endsWith(gender)) {
					actualMaleCount += EntityUtils.getInt("count", item);
				} else {
					actualFemaleCount += EntityUtils.getInt("count", item);
				}
			}
			assertEquals(expectedFemaleCount, actualFemaleCount);
			assertEquals(expectedMaleCount, actualMaleCount);
			
			List<String> treatmentReviewIds = new ArrayList<String>();
			for (DBObject dbObject : actualReviews) {
				treatmentReviewIds.add(EntityUtils.getEntityId(dbObject));
			}
			int actualevents = countByIds(EntityUtils.TREATMENT_REVIEW_EVENT, "treatmentReviewId", treatmentReviewIds);
			int actualnotifications = countByIds(UserNotification.ENTITY_NAME, "treatmentReviewId", treatmentReviewIds);
			int actualfraudReports = countByIds(EntityUtils.FRAUD_REPORT, FraudReport.ATTR_ENTITY_ID, treatmentReviewIds);
			int actualfraudReportItemss = countByIds(EntityUtils.FRAUD_REPORT_ITEM, FraudReport.ATTR_ENTITY_ID, treatmentReviewIds);
			assertEquals(events, actualevents);
			assertEquals(notifications, actualnotifications);
			assertEquals(fraudReports, actualfraudReports);
			assertEquals(fraudReportItems, actualfraudReportItemss);
		} finally {
		}
	}

	@Override
	protected void loginTestUserAdmin() {
		// admin@kuravis.com;kuravis2013
		userService.signout();
		assertFalse(ErrorUtils.isError(userService.signin("admin@kuravis.com", "kuravis2013", "", false)));
	}
}
