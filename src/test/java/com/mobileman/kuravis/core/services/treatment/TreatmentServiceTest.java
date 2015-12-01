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
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.Data;
import com.mobileman.kuravis.core.domain.fraud_report.FraudReport;
import com.mobileman.kuravis.core.domain.fraud_report.FraudReportCategory;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.user.notification.UserNotification;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.services.disease.DiseaseServiceTest;
import com.mobileman.kuravis.core.services.fraud_report.FraudReportService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewServiceTest;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.services.user.UserNotificationService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class TreatmentServiceTest extends AbstractIntegrationTest {
	
	@Autowired
	private TreatmentService treatmentService;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;

	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;
	
	@Autowired
	private FraudReportService fraudReportService;
	
	@Autowired
	private UserNotificationService userNotificationService;


	private static List<DBObject> entities = new ArrayList<DBObject>();
	
	/**
	 * @return treatments test data
	 */
	public static List<DBObject> getTreatments() {
		
		if (entities.isEmpty()) {
			BufferedReader br = null;
			try {
				InputStream fis = DiseaseServiceTest.class.getResourceAsStream("/data/treatments.csv");
				br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
				String line = null;
				int i = 0;
				Set<String> names = new HashSet<>();
				while ((line = br.readLine()) != null) {
					if (names.contains(line)) {
						continue;
					}
					
					names.add(line);
					DBObject obj = new BasicDBObject();
					obj.put("_id", UUID.randomUUID().toString());
					obj.put(Treatment.NAME, line);
					obj.put("createdOn", new Date());
					entities.add(obj);
					
					if (i++ > 25) {
						break;
					}
				}
				
				br.close();
				fis.close();
								
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				// Done with the file
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {}
				}
			}
			
			Collections.sort(entities, new Comparator<DBObject>() {
				@Override
				public int compare(DBObject o1, DBObject o2) {
					String name1 = (String) o1.get(Treatment.NAME);
					String name2 = (String) o2.get(Treatment.NAME);
					return name1.compareToIgnoreCase(name2);
				}
			});
		}
		
		return entities;
	}
	
	@Override
	@Before
	public void setUp() {
		Data.setUp(mongoTemplate, userService);
	}
	
	@Override
	public String getCollectionName() {
		return "treatment";
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void emptyTest() throws Exception {
		
	}

	/**
	 * @return ProductionTreatments
	 */
	public static List<DBObject> getProductionTreatments() {
		
		List<DBObject> result = new ArrayList<DBObject>();
		BufferedReader br = null;
		try {
			InputStream fis = DiseaseServiceTest.class.getResourceAsStream("/data/prod_treatments.csv");
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			String line = null;
			while ((line = br.readLine()) != null) {
				DBObject obj = new BasicDBObject();
				obj.put(Treatment.NAME, line);
				result.add(obj);
			}
			
			br.close();
			fis.close();
						
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			// Done with the file
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {}
			}
		}
		
		return result;
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void admin_createDeleteTreatment() throws Exception {
		userService.signout();
		
		treatmentService.save(new BasicDBObject("name", "admin_createDeleteTreatment"));
		
		List<DBObject> diseases = treatmentService.findAllByQuery(new BasicDBObject("name", "admin_createDeleteTreatment"), new PageRequest(0, 1));
		assertFalse(diseases.isEmpty());
		loginAuthorizedUser();
		
		DBObject result = null;
		try {
			result = treatmentService.delete((String) diseases.get(0).get(EntityUtils.ID));
			assertTrue(false);
		} catch (UnauthorizedException | UnauthenticatedException ex) {
		} catch (HealtPlatformException e) {
			assertEquals(HttpStatus.UNAUTHORIZED, ErrorUtils.getStatus(e.getResult()));
		}
		
		assertFalse(ErrorUtils.isError(userService.signin("peter.novak1@test.com", "peter.novak", "", false)));
		
		try {
			result = treatmentService.delete((String) diseases.get(0).get(EntityUtils.ID));
			assertTrue(false);
		} catch (UnauthorizedException | UnauthenticatedException ex) {
		} catch (HealtPlatformException e) {
			assertEquals(HttpStatus.UNAUTHORIZED, ErrorUtils.getStatus(e.getResult()));
		}
		
		assertFalse(ErrorUtils.isError(userService.signin("peter.novak2@test.com", "peter.novak", "", false)));
		
		result = treatmentService.delete((String) diseases.get(0).get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void admin_updateTreatment() throws Exception {
		loginTestUserAdmin();
		
		String id = (String) treatmentService.save(new BasicDBObject("name", "admin_updateTreatment")).get(EntityUtils.ID);
		DBObject treatment = treatmentService.findById(id);
	
		treatment.put("name", "xxx");
		DBObject result = treatmentService.update((String) treatment.get(EntityUtils.ID), treatment);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject newtreatment = treatmentService.findById(id);
		assertEquals("xxx", newtreatment.get("name"));
		
		result = treatmentService.delete((String) treatment.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
	}

	@Test(expected = HealtPlatformException.class)
	public void admin_updateTreatment_newNameAlreadyExistsWithDifferentId_returnError() throws Exception {
		loginTestUserAdmin();
		DBObject result = null;
		String vitaminC = "Vitamin C";
		DBObject vitaminCObj = treatmentService.findByProperty(EntityUtils.NAME, vitaminC);
		if (vitaminCObj == null) {
			return;
		}
		String id = (String) treatmentService.save(new BasicDBObject(EntityUtils.NAME, "KuravisVitamin")).get(EntityUtils.ID);
		try {
			DBObject treatment = treatmentService.findById(id);
			treatment.put(EntityUtils.NAME, vitaminC.toLowerCase());
			result = treatmentService.update(id, treatment);
		} finally {
			result = treatmentService.delete(id);
			assertFalse(ErrorUtils.isError(result));
		}
	}

	@Test
	public void admin_updateTreatment_renameExisting_toLowerCase() throws Exception {
		loginTestUserAdmin();
		DBObject result = null;
		String name = "KuravisVitamin";
		String id = (String) treatmentService.save(new BasicDBObject(EntityUtils.NAME, name)).get(EntityUtils.ID);
		try {
			DBObject treatment = treatmentService.findById(id);
			treatment.put(EntityUtils.NAME, name.toLowerCase());
			result = treatmentService.update(id, treatment);
			assertFalse(ErrorUtils.isError(result));
			
			DBObject newtreatment = treatmentService.findById(id);
			assertEquals(name.toLowerCase(), newtreatment.get(EntityUtils.NAME));
		} finally {
			result = treatmentService.delete(id);
			assertFalse(ErrorUtils.isError(result));
		}
	}

	@Test(expected = HealtPlatformException.class)
	public void admin_create_newNameAlreadyExists_returnError() {
		loginTestUserAdmin();
		DBObject result = null;
		String entity = "KuravisVitaminX";
		DBObject entityObj = treatmentService.create(EntityUtils.TREATMENT, new BasicDBObject(EntityUtils.NAME, entity));
		try {
			treatmentService.create(EntityUtils.TREATMENT, new BasicDBObject(EntityUtils.NAME, entity));
		} finally {
			result = treatmentService.delete(EntityUtils.getEntityId(entityObj));
			assertFalse(ErrorUtils.isError(result));
		}
	}
	
	@Test
	public void mergeTreatments() {
		loginAuthorizedUser();

		DBObject disease = DiseaseServiceTest.getDiseases().get(0);
		String diseaseId = EntityUtils.getEntityId(disease);
		String name = "KuravisVitamin";
		DBObject source = treatmentService.createOrFindByName(new BasicDBObject(EntityUtils.NAME, name + "1"));
		DBObject target = treatmentService.createOrFindByName(new BasicDBObject(EntityUtils.NAME, name + "2"));
		String sourceTreatmentId = EntityUtils.getEntityId(source);
		String targetTreatmentId = EntityUtils.getEntityId(target);
		String treatmentReviewId = null;
		DBObject result = null;
		List<String> createdTreatmentReviewIds = new ArrayList<String>();
		try {
			for (DBObject treview : TreatmentReviewServiceTest.createTreatmentReviews(disease, source, 2)) {
				DBObject treatmentReview = treatmentReviewService.createTreatmentReview(treview);
				if (ErrorUtils.isError(treatmentReview)) {
					throw new HealtPlatformException(treatmentReview);
				}
				treatmentReviewId = EntityUtils.getEntityId(treatmentReview);
				createdTreatmentReviewIds.add(treatmentReviewId);
			}

			result = addTreatmentReviewComment(treatmentReviewId);
			assertFalse(ErrorUtils.isError(result));
			addFraudReport(TreatmentReview.ENTITY_NAME, treatmentReviewId);
			// addFraudReport(EntityUtils.TREATMENT_REVIEW_EVENT, EntityUtils.getEntityId(comment));

			for (DBObject treview : TreatmentReviewServiceTest.createTreatmentReviews(disease, target, 1)) {
				DBObject treatmentReview = treatmentReviewService.createTreatmentReview(treview);
				treatmentReviewId = EntityUtils.getEntityId(treatmentReview);
				createdTreatmentReviewIds.add(treatmentReviewId);
			}
			int sourceReviewsCount = treatmentReviewService.findAllTreatmentReviews(diseaseId, sourceTreatmentId, "id").count();
			int targetReviewsCount = treatmentReviewService.findAllTreatmentReviews(diseaseId, targetTreatmentId, "id").count();

			int events = countByIds(EntityUtils.TREATMENT_REVIEW_EVENT, "treatmentReviewId", createdTreatmentReviewIds);
			int notifications = countByIds(UserNotification.ENTITY_NAME, "treatmentReviewId", createdTreatmentReviewIds);
			int fraudReports = countByIds(EntityUtils.FRAUD_REPORT, FraudReport.ATTR_ENTITY_ID, createdTreatmentReviewIds);
			int fraudReportItems = countByIds(EntityUtils.FRAUD_REPORT_ITEM, FraudReport.ATTR_ENTITY_ID, createdTreatmentReviewIds);

			DBObject reviewSummary = treatmentReviewSummaryService.findById(diseaseId + sourceTreatmentId);
			DBObject genderStatistics = (DBObject) reviewSummary.get("genderStatistics");		
			int expectedFemaleCount = getInt("female", genderStatistics);
			int expectedMaleCount = getInt("male", genderStatistics);
			reviewSummary = treatmentReviewSummaryService.findById(diseaseId + targetTreatmentId);
			genderStatistics = (DBObject) reviewSummary.get("genderStatistics");		
			expectedFemaleCount += getInt("female", genderStatistics);
			expectedMaleCount += getInt("male", genderStatistics);

			loginTestUserAdmin();
			treatmentService.merge(sourceTreatmentId, targetTreatmentId);
			DBCursor cursor = treatmentReviewService.findAllTreatmentReviews(diseaseId, targetTreatmentId, "id");
			int actualReviewsCount = cursor.count();
			assertEquals(sourceReviewsCount + targetReviewsCount, actualReviewsCount);

			reviewSummary = treatmentReviewSummaryService.findById(diseaseId + targetTreatmentId);
			assertEquals(actualReviewsCount, getInt("reviewsCount", reviewSummary));
			genderStatistics = (DBObject) reviewSummary.get("genderStatistics");		
			int actualFemaleCount = getInt("female", genderStatistics);
			int actualMaleCount = getInt("male", genderStatistics);
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
			for (DBObject dbObject : cursor) {
				treatmentReviewIds.add(EntityUtils.getEntityId(dbObject));
			}
			int actualevents = countByIds(EntityUtils.TREATMENT_REVIEW_EVENT, "treatmentReviewId", treatmentReviewIds);
			assertEquals(events, actualevents);
			int actualnotifications = countByIds(UserNotification.ENTITY_NAME, "treatmentReviewId", treatmentReviewIds);
			assertEquals(notifications, actualnotifications);
			int actualfraudReports = countByIds(EntityUtils.FRAUD_REPORT, FraudReport.ATTR_ENTITY_ID, treatmentReviewIds);
			assertEquals(fraudReports, actualfraudReports);
			int actualfraudReportItemss = countByIds(EntityUtils.FRAUD_REPORT_ITEM, FraudReport.ATTR_ENTITY_ID, treatmentReviewIds);
			assertEquals(fraudReportItems, actualfraudReportItemss);
		} finally {
			loginAdminIfNeeded();
			result = treatmentService.delete(sourceTreatmentId);
			assertFalse(ErrorUtils.isError(result));
			result = treatmentService.delete(targetTreatmentId);
			assertFalse(ErrorUtils.isError(result));
			for (String id : createdTreatmentReviewIds) {
				result = treatmentReviewService.delete(id);
				// assertFalse(ErrorUtils.isError(result));
			}
		}
	}

	private int getInt(String propertyName, DBObject entity) {
		Object value = entity.get(propertyName);
		if (value==null) {
			return 0;
		}
		return ((Integer) value).intValue();
	}
	
	private DBObject addTreatmentReviewComment(String tReviewId) {
		String email = "peter.novak19@test.com";
		this.userService.signin(email, "peter.novak", "", false);
		DBObject comment = new BasicDBObject("text", email + " comment for treatmentReview " + tReviewId);
		return this.treatmentReviewService.commentTreatmentReview(tReviewId, comment);
	}

	private DBObject addFraudReport(String entityName, String entityId) {
		DBObject note = new BasicDBObject("note", "FraudReportNote for " + entityId);
		Integer fraudReportCategoryId = FraudReportCategory.UNPROFESSIONAL_REVIEW.getValue();
		note.put("fraudReportCategory", fraudReportCategoryId);
		return fraudReportService.reportEntity(entityName, entityId, note);
	}
}
