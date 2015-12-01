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
package com.mobileman.kuravis.core.services.disease;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.Data;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.fraud_report.FraudReport;
import com.mobileman.kuravis.core.domain.fraud_report.FraudReportCategory;
import com.mobileman.kuravis.core.domain.option_list.unit.Unit;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.notification.UserNotification;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.services.fraud_report.FraudReportService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment.TreatmentServiceTest;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewServiceTest;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class DiseaseServiceTest extends AbstractIntegrationTest {
	
	@Autowired
	private DiseaseService diseaseService;
	
	@Autowired
	private TreatmentService treatmentService;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;
	
	@Autowired
	private FraudReportService fraudReportService;
	
	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.AbstractIntegrationTest#getCollectionName()
	 */
	@Override
	public String getCollectionName() {
		return EntityUtils.DISEASE;
	}

	private static List<DBObject> entities = new ArrayList<DBObject>();
	
	@Override
	@Before
	public void setUp() {
		Data.setUp(mongoTemplate, userService);
	}
	
	/**
	 * @return diseases test data
	 */
	public static List<DBObject> getDiseases() {
		if (entities.isEmpty()) {
			try {
				InputStream fis = DiseaseServiceTest.class.getResourceAsStream("/data/diseases.csv");
				BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
				String line = null;
				Set<String> names = new HashSet<>();
				
				while ((line = br.readLine()) != null) {
					if (names.contains(line)) {
						continue;
					}
					
					DBObject obj = new BasicDBObject();
					obj.put("createdOn", new Date());
					obj.put("treatmentReviewsCount", 0);
					
					if (line.contains("{")) {
						Disease disease = new ObjectMapper().readValue(line, Disease.class);
						obj.put("_id", disease.get_id());
						obj.put("name", disease.getName());
					} else {
						names.add(line);
						obj.put("_id", UUID.randomUUID().toString());
						obj.put("name", line);
						entities.add(obj);
					}
				}

				// Done with the file
				br.close();
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			Collections.sort(entities, new Comparator<DBObject>() {
				@Override
				public int compare(DBObject o1, DBObject o2) {
					String name1 = (String) o1.get("name");
					String name2 = (String) o2.get("name");
					return name1.compareToIgnoreCase(name2);
				}
			});
		}
		
		return entities;
	}
	
	/**
	 * @return diseases test data
	 */
	public static List<DBObject> getProductionDiseases() {
		
		List<DBObject> result = new ArrayList<DBObject>();
		try {
			InputStream fis = DiseaseServiceTest.class.getResourceAsStream("/data/prod_diseases.csv");
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			String line = null;

			while ((line = br.readLine()) != null) {
				DBObject obj = new BasicDBObject();
				obj.put("name", line);
				result.add(obj);
			}

			// Done with the file
			br.close();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void admin_createDeleteDisease_AnyReviews() throws Exception {
		userService.signout();
		
		diseaseService.save(new BasicDBObject("name", "admin_createDeleteDisease"));
		
		List<DBObject> diseases = diseaseService.findAllByQuery(new BasicDBObject("name", "admin_createDeleteDisease"), new PageRequest(0, 1));
		assertFalse(diseases.isEmpty());
		
		loginAuthorizedUser();
		
		DBObject result = null;
		try {
			result = diseaseService.delete((String) diseases.get(0).get(EntityUtils.ID));
			assertTrue(false);
		} catch (HealtPlatformException e) {
			assertEquals(HttpStatus.UNAUTHORIZED, ErrorUtils.getStatus(e.getResult()));
		}
		assertFalse(ErrorUtils.isError(userService.signin("peter.novak1@test.com", "peter.novak", "", false)));
		
		try {
			result = diseaseService.delete((String) diseases.get(0).get(EntityUtils.ID));
			assertTrue(false);
		} catch (HealtPlatformException e) {
			assertEquals(HttpStatus.UNAUTHORIZED, ErrorUtils.getStatus(e.getResult()));
		}
		
		loginTestUserAdmin();
		
		result = diseaseService.delete((String) diseases.get(0).get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void admin_createDeleteDisease_WithReviews() throws Exception {
		loginTestUserAdmin();
		
		diseaseService.save(new BasicDBObject("name", "Flu 1"));
		treatmentService.save(new BasicDBObject("name", "Aspirin 1"));
		
		DBObject disease = diseaseService.findAllByQuery(new BasicDBObject("name", "Flu 1"), new PageRequest(0, 1)).get(0);
		DBObject treatment = treatmentService.findAllByQuery(new BasicDBObject("name", "Aspirin 1"), new PageRequest(0, 1)).get(0);
				
		DBObject review = new BasicDBObject();
		review.put("disease", disease.toMap());
		review.put("treatment", treatment.toMap());
		review.put("rating", 0.8d);
		review.put("text", "admin_createDeleteDisease_WithReviews");
		
		Map<String, Object> sideEffect = new HashMap<>();
		sideEffect.put("severity", 0.2d);
		sideEffect.put("sideEffect", new BasicDBObject("name", "Pain 1").toMap());
		review.put("sideEffects", Arrays.asList(sideEffect));
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		int count = treatmentReviewService.findAllByQuery(new BasicDBObject("text", "admin_createDeleteDisease_WithReviews")).size();
		assertEquals(1, count);
		
		result = diseaseService.delete((String) disease.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
		
		count = treatmentReviewService.findAllByQuery(new BasicDBObject("text", "admin_createDeleteDisease_WithReviews")).size();
		assertEquals(0, count);
		
		assertEquals(0, diseaseService.findAllByQuery(new BasicDBObject(EntityUtils.ID, disease.get(EntityUtils.ID))).size());
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void admin_updateDisease_AnyReviews() throws Exception {
		loginTestUserAdmin();
		
		String id = (String) diseaseService.save(new BasicDBObject("name", "admin_updateDisease_AnyReviews")).get(EntityUtils.ID);
		
		DBObject disease = diseaseService.findById(id);
	
		disease.put(EntityUtils.NAME, "xxx");
		
		DBObject result = diseaseService.update((String) disease.get(EntityUtils.ID), disease);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject newDisease = diseaseService.findById(id);
		assertEquals("xxx", newDisease.get(EntityUtils.NAME));
		
		result = diseaseService.delete((String) disease.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
	}

	@Test(expected = HealtPlatformException.class)
	public void admin_updateDisease_newNameAlreadyExists_returnError() throws Exception {
		loginTestUserAdmin();
		String migraine = "KuravisMigraine";
		DBObject migraineObj = diseaseService.createOrFindByName(new BasicDBObject(EntityUtils.NAME, migraine));
		String id = (String) diseaseService.save(new BasicDBObject(EntityUtils.NAME, "admin_updateDisease_AnyReviews")).get(EntityUtils.ID);
		DBObject disease = diseaseService.findById(id);
		DBObject result = null;
		try {
			disease.put(EntityUtils.NAME, migraine.toLowerCase());
			result = diseaseService.update((String) disease.get(EntityUtils.ID), disease);
			assertTrue(ErrorUtils.isError(result));
		} finally {
			result = diseaseService.delete(EntityUtils.getEntityId(migraineObj));
			result = diseaseService.delete(id);
			assertFalse(ErrorUtils.isError(result));
		}
	}
	
	@Test
	public void admin_updateDisease_renameExisting_toLowerCase() throws Exception {
		loginTestUserAdmin();
		DBObject result = null;
		String name = "KuravisMigraine";
		String id = (String) diseaseService.save(new BasicDBObject(EntityUtils.NAME, name)).get(EntityUtils.ID);
		try {
			DBObject disease = diseaseService.findById(id);
			disease.put(EntityUtils.NAME, name.toLowerCase());
			result = diseaseService.update(id, disease);
			assertFalse(ErrorUtils.isError(result));
			
			DBObject newdisease = diseaseService.findById(id);
			assertEquals(name.toLowerCase(), newdisease.get(EntityUtils.NAME));
		} finally {
			result = diseaseService.delete(id);
			assertFalse(ErrorUtils.isError(result));
		}
	}

	@Test(expected = HealtPlatformException.class)
	public void admin_createDisease_newNameAlreadyExists_returnError() {
		loginTestUserAdmin();
		DBObject result = null;
		String migraine = "KuravisMigraine2";
		DBObject migraineObj = diseaseService.create(EntityUtils.DISEASE, new BasicDBObject(EntityUtils.NAME, migraine));
		try {
			diseaseService.create(EntityUtils.DISEASE, new BasicDBObject(EntityUtils.NAME, migraine));
		} finally {
			result = diseaseService.delete(EntityUtils.getEntityId(migraineObj));
			assertFalse(ErrorUtils.isError(result));
		}
	}
	
	@Test
	public void mergeTreatments() {
		loginAuthorizedUser();

		String diseaseName = "KuravisMigraine";
		DBObject sourceDisease = diseaseService.createOrFindByName(new BasicDBObject(Disease.NAME, diseaseName + "1"));
		DBObject targetDisease = diseaseService.createOrFindByName(new BasicDBObject(Disease.NAME, diseaseName + "2"));
		String sourceDiseaseId = EntityUtils.getEntityId(sourceDisease);
		String targetDiseaseId = EntityUtils.getEntityId(targetDisease);
		DBObject treatment = TreatmentServiceTest.getTreatments().get(0);
		String treatmentId = EntityUtils.getEntityId(treatment);
		String treatmentReviewId = null;
		DBObject result = null;
		List<String> createdTreatmentReviewIds = new ArrayList<String>();
		try {
			for (DBObject treview : TreatmentReviewServiceTest.createTreatmentReviews(sourceDisease, treatment, 2)) {
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
			
			int sourceReviewsCount = treatmentReviewService.findAllTreatmentReviews(sourceDiseaseId, treatmentId, "id").count();
			int targetReviewsCount = treatmentReviewService.findAllTreatmentReviews(targetDiseaseId, treatmentId, "id").count();
			
			int events = countByIds(EntityUtils.TREATMENT_REVIEW_EVENT, "treatmentReviewId", createdTreatmentReviewIds);
			int notifications = countByIds(UserNotification.ENTITY_NAME, "treatmentReviewId", createdTreatmentReviewIds);
			int fraudReports = countByIds(EntityUtils.FRAUD_REPORT, FraudReport.ATTR_ENTITY_ID, createdTreatmentReviewIds);
			int fraudReportItems = countByIds(EntityUtils.FRAUD_REPORT_ITEM, FraudReport.ATTR_ENTITY_ID, createdTreatmentReviewIds);
			
			loginAdminIfNeeded();
			diseaseService.merge(sourceDiseaseId, targetDiseaseId);
			DBCursor cursor = treatmentReviewService.findAllTreatmentReviews(targetDiseaseId, treatmentId, "id");
			int actualReviewsCount = cursor.count();
			assertEquals(sourceReviewsCount + targetReviewsCount, actualReviewsCount);
			DBObject treatmentReviewSummary = treatmentReviewSummaryService.findById(targetDiseaseId + treatmentId);
			assertEquals(actualReviewsCount, ((Integer) treatmentReviewSummary.get("reviewsCount")).intValue());
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
			result = diseaseService.delete(sourceDiseaseId);
			assertFalse(ErrorUtils.isError(result));
			result = diseaseService.delete(targetDiseaseId);
			assertFalse(ErrorUtils.isError(result));
			for (String id : createdTreatmentReviewIds) {
				result = treatmentReviewService.delete(id);
				// assertFalse(ErrorUtils.isError(result));
			}
		}
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

	/**
	 * @throws Exception
	 */
	@Test
	public void findAllDiseasesWithTreatmentsForSuggestionAdmin() throws Exception {
		DBObject disease = diseaseService.findByProperty("name", "A Disease");
		DBObject treatment = treatmentService.findByProperty("name", "Vitamin C");
		String summaryId = "";
		try {
			TreatmentReviewSummary summary = new TreatmentReviewSummary();
			summary.setDisease(diseaseService.getById(disease.get(Treatment.ID)));
			summary.setTreatment(treatmentService.getById(treatment.get(Treatment.ID)));
			summary.setSuggestion(true);
			summaryId = treatmentReviewSummaryService.create(summary);
			
			List<DBObject> diseases = diseaseService.findAllDiseasesWithTreatmentsForSuggestionAdmin(new PageRequest(0, 20, new Sort(new Sort.Order(Disease.NAME))));
			assertTrue(diseases.size() > 0);
			
			
		} finally {
			treatmentReviewSummaryService.delete(summaryId);
		}
	}

}
