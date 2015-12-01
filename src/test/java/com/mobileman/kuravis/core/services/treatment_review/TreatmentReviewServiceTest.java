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
package com.mobileman.kuravis.core.services.treatment_review;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.Data;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.event.FrequencyType;
import com.mobileman.kuravis.core.domain.event.TreatmentCategory;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.fraud_report.FraudReportCategory;
import com.mobileman.kuravis.core.domain.option_list.unit.Unit;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentCostStatistics;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentDurationStatistics;
import com.mobileman.kuravis.core.domain.treatment_side_effect.TreatmentSideEffect;
import com.mobileman.kuravis.core.domain.user.Gender;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.disease.DiseaseServiceTest;
import com.mobileman.kuravis.core.services.event.EventService;
import com.mobileman.kuravis.core.services.fraud_report.FraudReportService;
import com.mobileman.kuravis.core.services.option_list.treatment_type.TreatmentTypeService;
import com.mobileman.kuravis.core.services.option_list.unit.UnitService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment.TreatmentServiceTest;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.services.treatment_side_effect.TreatmentSideEffectService;
import com.mobileman.kuravis.core.services.user.InvitationService;
import com.mobileman.kuravis.core.services.user.UserNotificationService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.services.user.UserServiceTest;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class TreatmentReviewServiceTest extends AbstractIntegrationTest {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private InvitationService invitationService;
	
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
	
	@Autowired
	private TreatmentSideEffectService treatmentSideEffectService;
	
	@Autowired
	private UserNotificationService userNotificationService;
	
	@Autowired
	private TreatmentTypeService treatmentTypeService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private UnitService unitService;
	
	private static List<DBObject> treatmentReviewsSummary = new ArrayList<DBObject>();
	
	private static Map<String, DBObject> summariesMap = new HashMap<String, DBObject>();

	private static List<DBObject> treatmentReviewSummaryAgeStatistics = new ArrayList<DBObject>();
		
	/**
	 *
	 * @return treatmentReviewSummaryAgeStatistics
	 */
	public static List<DBObject> getTreatmentReviewSummaryAgeStatistics() {
		return treatmentReviewSummaryAgeStatistics;
	}
	
	@SuppressWarnings("unchecked")
	public static List<DBObject> createTreatmentReviews(DBObject disaese, DBObject treatment) {
		return createTreatmentReviews(disaese, treatment, 20);
	}

	/**
	 * @param disaese
	 * @param treatment
	 * @return new treatment reviews
	 */
	@SuppressWarnings("unchecked")
	public static List<DBObject> createTreatmentReviews(DBObject disaese, DBObject treatment, int nrOfReviews) {
		
		List<DBObject> entities = new ArrayList<DBObject>();
		List<DBObject> users = UserServiceTest.getUsers();

		Map<String, Integer> genderStat = new HashMap<String, Integer>();
		Map<Integer, Integer> ageStat = new HashMap<Integer, Integer>();
		
		for (int i = 1; i <= nrOfReviews; i++) {
			DBObject review = new BasicDBObject();
			
			review.put(EntityUtils.ID, UUID.randomUUID().toString());
			review.put("text", "Amazing treatment " + treatment.get("name") + " " + i + "!");
			review.put("createdOn", new Date());
			review.put("disease", disaese);
			review.put("treatment", treatment);
			review.put("rating", new Random().nextDouble());
			review.put("author", users.get(i));
			review.put("votesCount", 0);
			
			Integer gs = genderStat.get(users.get(i).get("gender"));
			if (gs == null) {
				gs = 1;
			} else {
				gs = gs.intValue() + 1;
			}
			genderStat.put((String) users.get(i).get("gender"), gs);
			
			Integer as = ageStat.get(users.get(i).get("gender"));
			if (as == null) {
				as = 1;
			} else {
				as = as.intValue() + 1;
			}
			
			ageStat.put((Integer) users.get(i).get("yearOfBirth"), gs);
			
			entities.add(review);
			
			Number treatmentReviewsCount = (Number) disaese.get("treatmentReviewsCount");
			if (treatmentReviewsCount == null) {
				treatmentReviewsCount = 1;
			} else {
				treatmentReviewsCount = treatmentReviewsCount.intValue() + 1;
			}
			
			disaese.put("treatmentReviewsCount", treatmentReviewsCount);
		}
		
		String trsid = EntityUtils.createTreatmentReviewSummaryId(disaese.get("_id"), treatment.get("_id"));
		if (!summariesMap.containsKey(trsid)) {
			DBObject treatmentReviewSummary = new BasicDBObject();
			treatmentReviewSummary.put("_id", trsid);
			treatmentReviewSummary.put("createdOn", new Date());
			
			List<DBObject> ratings = new ArrayList<DBObject>();
			ratings.add(new BasicDBObject(EntityUtils.NAME, new Random().nextDouble()).append("count", 2));
			ratings.add(new BasicDBObject(EntityUtils.NAME, new Random().nextDouble()).append("count", 100));
			ratings.add(new BasicDBObject(EntityUtils.NAME, new Random().nextDouble()).append("count", 8));
			treatmentReviewSummary.put("ratings", ratings);
			
			List<DBObject> sideEffects = new ArrayList<DBObject>();
			sideEffects.add(new BasicDBObject(EntityUtils.NAME, "Pain 1").append("counts", new BasicDBObject(EntityUtils.NAME, 0.45d).append("count", 3)));
			sideEffects.add(new BasicDBObject(EntityUtils.NAME, "Pain 2").append("counts", new BasicDBObject(EntityUtils.NAME, 0.125d).append("count", 5)));
			treatmentReviewSummary.put("sideEffects", sideEffects);
			
			treatmentReviewSummary.put("reviewsCount", entities.size());
			treatmentReviewSummary.put("disease", disaese);
			treatmentReviewSummary.put("treatment", treatment);
			
			DBObject genderStatistics = new BasicDBObject();
			for (String gender : genderStat.keySet()) {
				genderStatistics.put(gender, genderStat.get(gender));
			}
			
			for (Integer age : ageStat.keySet()) {
				//treatmentReviewSummaryAgeStatistics.add(
				//		new BasicDBObject(EntityUtils.ID, trsid + age).append("summaryId", trsid).append("name", age).append("count", ageStat.get(age)));
			}
			
			treatmentReviewSummary.put("genderStatistics", genderStatistics);
					
			treatmentReviewsSummary.add(treatmentReviewSummary);
			summariesMap.put(trsid, treatmentReviewSummary);
		} else {
			DBObject treatmentReviewSummary = summariesMap.get(trsid);
			List<DBObject> ratings = (List<DBObject>) treatmentReviewSummary.get("ratings");
			if (ratings == null) {
				ratings = new ArrayList<>();
				treatmentReviewSummary.put("ratings", ratings);
			}
			
			double rand = new Random().nextDouble();
			boolean updated = false;
			for (DBObject rating : ratings) {
				Double val = (Double)rating.get(EntityUtils.NAME);
				if (val.equals(rand)) {
					int count = (Integer)rating.get("count");
					count += 1;
					rating.put("count", count);
					updated = true;
					break;
				}
			}
			
			if (!updated) {
				ratings.add(new BasicDBObject(EntityUtils.NAME, rand).append("count", 1));
			}
			/*
			Double rat = (Double) treatmentReviewSummary.get("rating");
			rat = (rat.doubleValue() + rand) / 2;
			treatmentReviewSummary.put("ratings", rat);
			*/
			Integer count = (Integer) treatmentReviewSummary.get("reviewsCount");
			count = count.intValue() + 1;
			treatmentReviewSummary.put("reviewsCount", count);
		}
		
		return entities;
	}
	
	/**
	 * @return reviews summaries
	 */
	public static List<DBObject> getTreatmentReviewsSummaries() {
		return treatmentReviewsSummary;
	}
	
	@Override
	@Before
	public void setUp() {
		Data.setUp(mongoTemplate, userService);
		this.userService.signout();
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void createTreatmentReview() throws Exception {
		String userEmail = "peter.novak1@test.com";
		DBObject user = userService.findDBUserByEmail(userEmail);
		assertFalse(ErrorUtils.isError(userService.signin(userEmail, "peter.novak", "", false)));
		
		long sideeffectsCount = mongoTemplate.getDb().getCollection(EntityUtils.TREATMENT_SIDE_EFFECT).count();
		
		DBObject disease = diseaseService.createOrFindByName(new BasicDBObject("name", "A Disease"));
		DBObject treatment = treatmentService.createOrFindByName(new BasicDBObject("name", "A Aspirin"));
		int invitationCount = (int) user.get("invitationCount");
		
		List<DBObject> users = userService.findUsersByDiseaseAndTreatment(new BasicDBObject("treatmentId", treatment.get(EntityUtils.ID)).append("diseaseId", disease.get(EntityUtils.ID)), new PageRequest(0, 10));
		
		DBObject review = new BasicDBObject();
		review.put("disease", new BasicDBObject("name", disease.get("name")).toMap());
		review.put("treatment", new BasicDBObject("name", treatment.get("name")).toMap());
		review.put("rating", 0.45d);
		review.put("text", "Test treatment review");
		
		Map<String, Object> sideEffect = new HashMap<>();
		sideEffect.put("severity", 0.1d);
		sideEffect.put("sideEffect", new BasicDBObject("name", "Pain 1").toMap());
		Map<String, Object> sideEffect2 = new HashMap<>();
		sideEffect2.put("severity", 0.31d);
		sideEffect2.put("sideEffect", new BasicDBObject("name", "Pain 2").toMap());
		Map<String, Object> sideEffect3 = new HashMap<>();
		sideEffect3.put("severity", 0.75d);
		sideEffect3.put("sideEffect", new BasicDBObject("name", "Pain 3").toMap());
		Map<String, Object> sideEffect4 = new HashMap<>();
		sideEffect4.put("severity", 0.75d);
		sideEffect4.put("sideEffect", new BasicDBObject("name", "Pain 4").toMap());
		review.put("sideEffects", Arrays.asList(sideEffect, sideEffect2, sideEffect3, sideEffect4));
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		assertTrue(sideeffectsCount < mongoTemplate.getDb().getCollection(EntityUtils.TREATMENT_SIDE_EFFECT).count());
		
		List<DBObject> users2 = userService.findUsersByDiseaseAndTreatment(new BasicDBObject("treatmentId", treatment.get(EntityUtils.ID)).append("diseaseId", disease.get(EntityUtils.ID)), new PageRequest(0, 10));
		assertEquals(users.size() + 1, users2.size());
		
		List<DBObject> summaries = this.treatmentReviewSummaryService.findAllByQuery(
				new BasicDBObject("treatment._id", treatment.get(EntityUtils.ID))
					.append("disease._id", disease.get(EntityUtils.ID)), new PageRequest(0, 10));
		assertThat(summaries.size(), is(1));
		assertThat(BasicDBObject.class.cast(summaries.get(0).get("ageStatistics")).size(), is(1));
		
		review = treatmentReviewService.findAllByQuery(new BasicDBObject("text", "Test treatment review")).get(0);
		assertNotNull(review.get("author"));
		
		user = userService.findById((String) user.get(EntityUtils.ID));
		assertEquals(invitationCount + 1, user.get("invitationCount"));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void createTreatmentReview_NewDiseaseAndTreatment() throws Exception {
		DBObject user = UserServiceTest.getUsers().get(0);
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "peter.novak", "", false)));
		
		DBObject disaese = new BasicDBObject("name", "My Test Disease 3");
		DBObject treatment = new BasicDBObject("name", "My Test Treatment 3");
		
		DBObject review = new BasicDBObject();
		review.put("disease", disaese.toMap());
		review.put("treatment", treatment.toMap());
		review.put("rating", 0.45d);
		review.put("text", "Test treatment review");
		
		Map<String, Object> sideEffect = new HashMap<>();
		sideEffect.put("severity", 0.1d);
		sideEffect.put("sideEffect", new BasicDBObject("name", "Pain 1").toMap());
		Map<String, Object> sideEffect2 = new HashMap<>();
		sideEffect2.put("severity", 0.31d);
		sideEffect2.put("sideEffect", new BasicDBObject("name", "Pain 2").toMap());
		Map<String, Object> sideEffect3 = new HashMap<>();
		sideEffect3.put("severity", 0.75d);
		sideEffect3.put("sideEffect", new BasicDBObject("name", "Pain 3").toMap());
		review.put("sideEffects", Arrays.asList(sideEffect, sideEffect2, sideEffect3));
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject diseasenew = diseaseService.findAllByQuery(disaese).get(0);
		assertEquals(1, diseasenew.get("treatmentReviewsCount"));

	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void createTreatmentReview_NoRating() throws Exception {
		DBObject disaese = new BasicDBObject("name", "Test Disease");
		DBObject treatment = new BasicDBObject("name", "Test Treatment");
		
		DBObject user = UserServiceTest.getUsers().get(0);
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "peter.novak", "", false)));
		
		DBObject review = new BasicDBObject();
		review.put("disease", disaese.toMap());
		review.put("treatment", treatment.toMap());
		review.put("text", "Test treatment review");
		String _id = "";
		try {
			_id = EntityUtils.getEntityId(treatmentReviewService.createTreatmentReview(review));
			
			TreatmentReview treatmentReview = treatmentReviewService.getById(_id);
			assertNotNull(treatmentReview);
			assertNull(treatmentReview.getRating());
			
			String summaryId = EntityUtils.createTreatmentReviewSummaryId(treatmentReview.getDisease().get_id(), treatmentReview.getTreatment().get_id());
			TreatmentReviewSummary summary = treatmentReviewSummaryService.getById(summaryId);
			assertNotNull(summary);
			assertNull(summary.getRating());
			assertNull(summary.getRatings());
			
		} catch (HealtPlatformException e) {
			Assert.fail("Rating is optional");
		} finally {
			treatmentReviewService.delete(_id);
		}
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void updateTreatmentReview_NoRating() throws Exception {
		DBObject disaese = new BasicDBObject("name", "Test Disease");
		DBObject treatment = new BasicDBObject("name", "Test Treatment");
		
		DBObject user = UserServiceTest.getUsers().get(0);
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "peter.novak", "", false)));
		
		DBObject review = new BasicDBObject();
		review.put("disease", disaese.toMap());
		review.put("treatment", treatment.toMap());
		review.put("text", "Test treatment review");
		review.put(TreatmentReview.RATING, 0.5d);
		
		String _id = "";
		try {
			_id = EntityUtils.getEntityId(treatmentReviewService.createTreatmentReview(review));
			review.put(EntityUtils.ID, _id);
			TreatmentReview treatmentReview = treatmentReviewService.getById(_id);
			assertEquals(new BigDecimal(0.5d), treatmentReview.getRating());
			
			String summaryId = EntityUtils.createTreatmentReviewSummaryId(treatmentReview.getDisease().get_id(), treatmentReview.getTreatment().get_id());
			TreatmentReviewSummary summary = treatmentReviewSummaryService.getById(summaryId);
			assertEquals(new BigDecimal(0.5d), summary.getRating());
			assertEquals(1, summary.getRatings().size());
			assertEquals(new BigDecimal(0.5d), summary.getRatings().get(0).getName());
			assertEquals(1, summary.getRatings().get(0).getCount());
			
			review.put(TreatmentReview.RATING, null);
			treatmentReviewService.update(review);
			
			summary = treatmentReviewSummaryService.getById(summaryId);
			assertEquals(null, summary.getRating());
			assertEquals(new ArrayList<>(), summary.getRatings());
			
		} catch (HealtPlatformException e) {
			Assert.fail("Rating is optional");
		} finally {
			treatmentReviewService.delete(_id);
		}
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void createTreatmentReview_AlreadyExists() throws Exception {
		DBObject disaese = new BasicDBObject("name", "Test Disease 2");
		DBObject treatment = new BasicDBObject("name", "Test Treatment 2");
		
		DBObject user = UserServiceTest.getUsers().get(0);
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "peter.novak", "", false)));
		
		DBObject review = new BasicDBObject();
		review.put("disease", disaese.toMap());
		review.put("treatment", treatment.toMap());
		review.put("text", "Test treatment review");
		review.put("rating", 0.33);
		
		DBObject sideEffect = new BasicDBObject();
		sideEffect.put("severity", 0.1d);
		sideEffect.put("sideEffect", new BasicDBObject("name", "Pain 3").toMap());
		review.put("sideEffects", Arrays.asList(sideEffect));
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		try {
			result = treatmentReviewService.createTreatmentReview(review);
		} catch (HealtPlatformException e) {}
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void createTreatmentReview_Gender_YearOfBirth() throws Exception {
		DBObject disaese = new BasicDBObject("name", "Test Disease");
		DBObject treatment = new BasicDBObject("name", "Test Treatment");
		
		DBObject user = UserServiceTest.getUsers().get(0);
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "peter.novak", "", false)));
		user = userService.findDBUserByEmail((String) user.get("email"));
		
		assertEquals(Gender.UNKNOWN.getValue(), user.get(User.ATTR_GENDER));
		assertEquals(1977, user.get(User.ATTR_YEAR_OF_BIRTH));
		
		DBObject review = new BasicDBObject();
		review.put("disease", disaese.toMap());
		review.put("treatment", treatment.toMap());
		review.put("rating", 0.5);
		review.put("text", "createTreatmentReview_Gender_YearOfBirth");
		review.put(User.ATTR_GENDER, Gender.MALE.getValue());
		review.put(User.ATTR_YEAR_OF_BIRTH, 1980);
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		review = treatmentReviewService.findAllByQuery(new BasicDBObject("text", "createTreatmentReview_Gender_YearOfBirth")).get(0);
				
		user = userService.findDBUserByEmail((String) user.get("email"));
		assertEquals(Gender.MALE.getValue(), user.get(User.ATTR_GENDER));
		assertEquals(1980, user.get(User.ATTR_YEAR_OF_BIRTH));
		userService.updateUser((String) user.get(EntityUtils.ID), 
				new BasicDBObject(User.ATTR_GENDER, Gender.UNKNOWN.getValue()).append(User.ATTR_YEAR_OF_BIRTH, 1977));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void createTreatmentReview_AgeStatistics() throws Exception {
		DBObject disaese = new BasicDBObject("name", "Angina");
		DBObject treatment = new BasicDBObject("name", "Vitamin B7");
		
		DBObject user1 = userService.findDBUserByEmail("james.thomas@test.com");
		DBObject user2 = userService.findDBUserByEmail("petra.hernandez@test.com");
		DBObject user3 = userService.findDBUserByEmail("jens.gulp@test.com");
		String[] pwds = {"james.thomas", "petra.hernandez", "jens.gulp"};
		
		int idx = 0;
		for (DBObject user : new DBObject[]{ user1, user2, user3 }) {
			assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), pwds[idx++], "", false)));		
			
			DBObject review = new BasicDBObject();
			review.put("disease", disaese.toMap());
			review.put("rating", 0.5);
			review.put("treatment", treatment.toMap());
			review.put("text", "createTreatmentReview_AgeStatistics" + idx);
			
			DBObject result = treatmentReviewService.createTreatmentReview(review);
			assertFalse(ErrorUtils.isError(result));
		}
		
		Thread.sleep(500);
		
		List<DBObject> summaries = treatmentReviewSummaryService.findAllByQuery(new BasicDBObject("disease.name", disaese.get("name")).append("treatment.name", treatment.get("name")));
		assertEquals(1, summaries.size());
		
		DBObject summary = summaries.get(0);
		assertTrue(summary.containsField("ageStatistics"));
		DBObject ageStatistics = (DBObject) summary.get("ageStatistics");
		assertTrue(ageStatistics.containsField(Gender.MALE.getValue()));
		assertTrue(ageStatistics.containsField(Gender.FEMALE.getValue()));
		assertTrue(ageStatistics.containsField(Gender.UNKNOWN.getValue()));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void createTreatmentReview_SummariesOrder() throws Exception {
		DBObject disaese = diseaseService.findByProperty(EntityUtils.NAME, "Angina");
		DBObject treatment1 = new BasicDBObject("name", "Vitamin K");
		DBObject treatment2 = new BasicDBObject("name", "Vitamin D");
		
		DBObject user = userService.findDBUserByEmail("james.thomas@test.com");
		
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "james.thomas", "", false)));		
		
		DBObject review = new BasicDBObject();
		review.put("disease", disaese.toMap());
		review.put("treatment", treatment1.toMap());
		review.put("rating", 0.6d);
		review.put("text", "createTreatmentReview_SummariesOrder1");
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		review = new BasicDBObject();
		review.put("disease", disaese.toMap());
		review.put("treatment", treatment2.toMap());
		review.put("rating", 0.3d);
		review.put("text", "createTreatmentReview_SummariesOrder2");
		result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		List<DBObject> summaries = treatmentReviewSummaryService.findAllByDisease((String) disaese.get(EntityUtils.ID), new PageRequest(0, 10));
		assertTrue(summaries.size() > 1);
		assertTrue(Double.class.cast(summaries.get(0).get("rating")).compareTo(Double.class.cast(summaries.get(1).get("rating"))) <= 0);
		
	}
	
	/**
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void createTreatmentReview_NoSideEffect() throws Exception {
		DBObject disaese = diseaseService.findByProperty(EntityUtils.NAME, "Angina");
		DBObject treatment1 = new BasicDBObject("name", "Vitamin B12");
		
		DBObject user = userService.findDBUserByEmail("james.thomas@test.com");
		
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "james.thomas", "", false)));		
		
		DBObject review = new BasicDBObject();
		review.put("disease", disaese.toMap());
		review.put("treatment", treatment1.toMap());
		review.put("rating", 0.6d);
		review.put("text", "createTreatmentReview_NoSideEffect");
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		List<DBObject> summaries = treatmentReviewSummaryService.findAllByQuery(new BasicDBObject("disease.name", disaese.get(EntityUtils.NAME)).append("treatment.name", treatment1.get(EntityUtils.NAME)));
		assertEquals(1, summaries.size());

		List<DBObject> sideEffects = (List) summaries.get(0).get("sideEffects");
		assertEquals(1, sideEffects.size());
		assertEquals(TreatmentSideEffect.NO_SIDE_EFFECT_NAME, sideEffects.get(0).get("name"));
		List<DBObject> counts = (List) sideEffects.get(0).get("counts");
		assertEquals(1, counts.size());
		assertEquals(0.0d, counts.get(0).get("name"));
		assertEquals(1, counts.get(0).get("count"));
		
		review = treatmentReviewService.findAllByQuery(new BasicDBObject("text", "createTreatmentReview_NoSideEffect")).get(0);
		DBObject sideEffect = new BasicDBObject("severity", 0.1d).append("sideEffect", new BasicDBObject("name", "A Pain 3"));
		review.put("sideEffects", Arrays.asList(sideEffect));
		assertFalse(ErrorUtils.isError(treatmentReviewService.update(review)));
		
		summaries = treatmentReviewSummaryService.findAllByQuery(new BasicDBObject("disease.name", disaese.get(EntityUtils.NAME)).append("treatment.name", treatment1.get(EntityUtils.NAME)));
		assertEquals(1, summaries.size());
		sideEffects = (List) summaries.get(0).get("sideEffects");
		assertEquals(1, sideEffects.size());
		Collections.sort(sideEffects, new Comparator<DBObject>() {

			@Override
			public int compare(DBObject o1, DBObject o2) {
				return String.class.cast(o1.get("name")).compareTo(String.class.cast(o2.get("name")));
			}
		});
		assertEquals("A Pain 3", sideEffects.get(0).get("name"));
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void createTreatmentReview_CostStatistics() throws Exception {
		loginAuthorizedUser();
		
		DBObject disaese = diseaseService.findByProperty(EntityUtils.NAME, "Angina");
		DBObject treatment = treatmentService.createOrFindByName(new BasicDBObject(EntityUtils.NAME, "Vitamin B12"));
		
		DBObject user = userService.findDBUserByEmail("james.thomas@test.com");
		
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "james.thomas", "", false)));		
		
		DBObject review = new BasicDBObject();
		review.put(TreatmentReview.DISEASE, disaese.toMap());
		review.put(TreatmentReview.TREATMENT, treatment.toMap());
		review.put(TreatmentReview.RATING, 0.0d);
		review.put(TreatmentReview.TEXT, "createTreatmentReview_CostStatistics");

		review.put(TreatmentReview.CURRENCY, "€");
		review.put(TreatmentReview.DOCTOR_COSTS, 100.0d);
		review.put(TreatmentReview.TREATMENT_PRICE, 40.0d);
		review.put(TreatmentReview.TREATMENT_QUANTITY, 2.0d);
		review.put(TreatmentReview.INSURANCE_COVERED, true);
		review.put(TreatmentReview.INSURANCE_COVERAGE, 90.0d);
		review.put(TreatmentReview.COINSURANCE, 15.0d);
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		String reviewId = EntityUtils.getEntityId(result);
		
		try {
			String summaryId = EntityUtils.createTreatmentReviewSummaryId(disaese.get(EntityUtils.ID), treatment.get(EntityUtils.ID));
			TreatmentReviewSummary summary = treatmentReviewSummaryService.getById(summaryId);
			
			List<TreatmentCostStatistics> costsStatistics = summary.getCostsStatistics();
			assertEquals(5, costsStatistics.size());
			assertEquals(1, costsStatistics.get(2).getCount());
			
		} finally {
			treatmentReviewService.delete(reviewId);
		}
				
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void createTreatmentReview_TreatmentDurationStatistics() throws Exception {
		loginAuthorizedUser();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		DBObject disaese = diseaseService.findByProperty(EntityUtils.NAME, "Angina");
		DBObject treatment = treatmentService.createOrFindByName(new BasicDBObject(EntityUtils.NAME, "Vitamin B12"));
		
		DBObject user = userService.findDBUserByEmail("james.thomas@test.com");
		
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "james.thomas", "", false)));		
		
		DBObject review = new BasicDBObject();
		review.put(TreatmentReview.DISEASE, disaese.toMap());
		review.put(TreatmentReview.TREATMENT, treatment.toMap());
		review.put(TreatmentReview.RATING, 0.0d);
		review.put(TreatmentReview.TEXT, "createTreatmentReview_CostStatistics");
		
		TreatmentEvent event = new TreatmentEvent();
		event.setFrequency(FrequencyType.DAILY);
		event.setStart(dateFormat.parse("2013-01-01"));
		event.setEnd(dateFormat.parse("2013-01-30"));
		event.setCategory(TreatmentCategory.PRESCRIPTION_MEDICINE);
		event.setQuantity(1);
		event.setType(treatmentTypeService.findEntityByProperty(Disease.NAME, "Filmtabletten"));
		event.setDisease(diseaseService.getById(disaese.get(EntityUtils.ID)));
		event.setTreatment(treatmentService.getById(treatment.get(EntityUtils.ID)));
		event.setUnit(unitService.findEntityByProperty(Unit.NAME, "Teelöffel"));
		String eventId = eventService.create(event);
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		String reviewId = EntityUtils.getEntityId(result);
				
		try {
						
			String summaryId = EntityUtils.createTreatmentReviewSummaryId(disaese.get(EntityUtils.ID), treatment.get(EntityUtils.ID));
			TreatmentReviewSummary summary = treatmentReviewSummaryService.getById(summaryId);
			
			List<TreatmentDurationStatistics> treatmentDurationStatistics = summary.getTreatmentDurationStatistics();
			assertEquals(5, treatmentDurationStatistics.size());
			assertEquals(1, treatmentDurationStatistics.get(2).getCount());
			
		} finally {
			treatmentReviewService.delete(reviewId);
			eventService.delete(eventId);
		}
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void updateTreatmentReview_CostStatistics() throws Exception {
		loginAuthorizedUser();
		
		DBObject disaese = diseaseService.findByProperty(EntityUtils.NAME, "Angina");
		DBObject treatment = diseaseService.createOrFindByName(new BasicDBObject(EntityUtils.NAME, "Vitamin B12"));
		
		DBObject user = userService.findDBUserByEmail("james.thomas@test.com");
		
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "james.thomas", "", false)));		
		
		DBObject review = new BasicDBObject();
		review.put(TreatmentReview.DISEASE, disaese.toMap());
		review.put(TreatmentReview.TREATMENT, treatment.toMap());
		review.put(TreatmentReview.RATING, 0.0d);
		review.put(TreatmentReview.TEXT, "createTreatmentReview_CostStatistics");

		review.put(TreatmentReview.CURRENCY, "€");
		review.put(TreatmentReview.DOCTOR_COSTS, 100.0d);
		review.put(TreatmentReview.TREATMENT_PRICE, 40.0d);
		review.put(TreatmentReview.TREATMENT_QUANTITY, 2.0d);
		review.put(TreatmentReview.INSURANCE_COVERED, true);
		review.put(TreatmentReview.INSURANCE_COVERAGE, 90.0d);
		review.put(TreatmentReview.COINSURANCE, 15.0d);
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		String reviewId = EntityUtils.getEntityId(result);
		
		try {
			String summaryId = EntityUtils.createTreatmentReviewSummaryId(disaese.get(EntityUtils.ID), treatment.get(EntityUtils.ID));
			TreatmentReviewSummary summary = treatmentReviewSummaryService.getById(summaryId);
			assertEquals(5, summary.getCostsStatistics().size());
			assertEquals(1, summary.getCostsStatistics().get(2).getCount());
			assertEquals(0, summary.getCostsStatistics().get(3).getCount());
			
			review.put(TreatmentReview.TREATMENT_QUANTITY, 3.0d);
			treatmentReviewService.update(review);
			summary = treatmentReviewSummaryService.getById(summaryId);
			assertEquals(5, summary.getCostsStatistics().size());
			assertEquals(0, summary.getCostsStatistics().get(2).getCount());
			assertEquals(1, summary.getCostsStatistics().get(3).getCount());
			
		} finally {
			treatmentReviewService.delete(reviewId);
		}
				
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testDeleteTreatmentReview_EmptyIdFailure() throws Exception {
		DBObject result = treatmentReviewService.delete("");
		assertTrue(ErrorUtils.isError(result));
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testDeleteTreatmentReview_IdFailure() throws Exception {
		DBObject result = treatmentReviewService.delete("123");
		assertTrue(ErrorUtils.isError(result));
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testDeleteTreatmentReview() throws Exception {
		DBObject user = UserServiceTest.getUsers().get(0);
		assertFalse(ErrorUtils.isError(userService.signin((String) user.get("email"), "peter.novak", "", false)));
		
		DBObject disaese = new BasicDBObject("name", "Test Disease 4");
		DBObject treatment = new BasicDBObject("name", "Test Treatment 3");
		
		DBObject review = new BasicDBObject();
		review.put("disease", disaese.toMap());
		review.put("treatment", treatment.toMap());
		review.put("text", "Test treatment review 2");
		review.put("rating", 0.45d);
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		review = treatmentReviewService.findAllByQuery(new BasicDBObject("text", review.get("text"))).get(0);
		
		disaese = diseaseService.findByProperty(EntityUtils.NAME, "Test Disease 4");
		assertNotNull(disaese);
		int treatmentReviewsCount = (int) disaese.get("treatmentReviewsCount");
		
		try {
			loginUnathorizedUser();
			result = treatmentReviewService.delete((String) review.get(EntityUtils.ID));
			assertTrue(false);
		} catch (HealtPlatformException e) {
			assertEquals(HttpStatus.UNAUTHORIZED, ErrorUtils.getStatus(e.getResult()));
		}
		
		disaese = diseaseService.findByProperty(EntityUtils.NAME, "Test Disease 4");
		assertEquals(treatmentReviewsCount - 1, disaese.get("treatmentReviewsCount"));
		
	}

	@Override
	public String getCollectionName() {
		return EntityUtils.TREATMENT_REVIEW;
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void voteForTreatmentReview_Failure() throws Exception {
		
		DBObject result = this.treatmentReviewService.voteForTreatmentReview("aaa");
		assertTrue(ErrorUtils.isError(result));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void voteForTreatmentReview_Success() throws Exception {
		
		this.userService.signin("peter.novak12@test.com", "peter.novak", "", false);
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).find().limit(1).next();
		
		review = this.treatmentReviewService.findByIdForUser((String) review.get(EntityUtils.ID));
		assertEquals(Boolean.FALSE, review.get("voted"));
		
		Integer votesCount = (Integer) review.get("votesCount");
		if (votesCount == null) {
			votesCount = 0;
		}
		
		DBObject result = this.treatmentReviewService.voteForTreatmentReview((String) review.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
		assertNotNull(result.get("count"));
		
		review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(review.get(EntityUtils.ID));
		assertEquals(votesCount + 1, review.get("votesCount"));
		assertTrue(review.containsField("lastVotedOn"));
		
		review = this.treatmentReviewService.findByIdForUser((String) review.get(EntityUtils.ID));
		assertEquals(Boolean.TRUE, review.get("voted"));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void voteForTreatmentReview_UnauthorizedFailure() throws Exception {
		
		this.userService.signin("peter.novak12@test.com", "peter.novak", "", false);
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).find().limit(1).next();
		
		loginUnathorizedUser();
		DBObject result = this.treatmentReviewService.voteForTreatmentReview((String) review.get(EntityUtils.ID));
		assertEquals(ErrorCodes.UNAUTHORIZED.getValue(), result.get(ErrorUtils.ATTR_CODE));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void voteForTreatmentReview_NotSignedInFailure() throws Exception {
		
		this.userService.signin("peter.novak12@test.com", "peter.novak", "", false);
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).find().limit(1).next();
		
		userService.signout();
		
		DBObject result = this.treatmentReviewService.voteForTreatmentReview((String) review.get(EntityUtils.ID));
		assertEquals(ErrorCodes.USER_NOT_AUTHENTICATED.getValue(), result.get(ErrorUtils.ATTR_CODE));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void deleteAllTreatmentReviews_UserSuccess() throws Exception {
		String email = "peter.novak7@test.com";
		DBObject user = userService.findDBUserByEmail(email);
		assertFalse(ErrorUtils.isError(userService.signin(email, "peter.novak", "", false)));
		
		List<DBObject> reviews = treatmentReviewService.findAllByQuery(EntityUtils.TREATMENT_REVIEW, new BasicDBObject("author._id", user.get(EntityUtils.ID)), new PageRequest(0, 100));
		assertThat(reviews.size(), is(greaterThan(0)));
		
		DBObject disaese = DiseaseServiceTest.getDiseases().get(0);
		DBObject treatment = TreatmentServiceTest.getTreatments().get(0);
		
		DBObject review = new BasicDBObject();
		review.put("disease", disaese.toMap());
		review.put("treatment", treatment.toMap());
		review.put("text", "Test treatment review");
		review.put("rating", 0.5);
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject data = treatmentReviewService.deleteAllTreatmentReviews((String) user.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(data));
		
		reviews = treatmentReviewService.findAllByQuery(EntityUtils.TREATMENT_REVIEW, new BasicDBObject("author._id", user.get(EntityUtils.ID)), new PageRequest(0, 100));
		assertThat(reviews.size(), is(equalTo(0)));
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void commentTreatmentReview_Failure() throws Exception {
		DBObject user = userService.findDBUserByEmail("peter.novak19@test.com");
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(new BasicDBObject("author._id", user.get(EntityUtils.ID)));
		assertNotNull(review);
		
		DBObject comment = new BasicDBObject("text", "My Comment");
		DBObject result = this.treatmentReviewService.commentTreatmentReview(null, comment);
		assertTrue(ErrorUtils.isError(result));
		
		result = this.treatmentReviewService.commentTreatmentReview((String)review.get(EntityUtils.ID), null);
		assertEquals(ErrorCodes.INCORRECT_PARAMETER.getValue(), result.get(ErrorUtils.ATTR_CODE));
		
		result = this.treatmentReviewService.commentTreatmentReview((String)review.get(EntityUtils.ID), new BasicDBObject());
		assertEquals(ErrorCodes.INCORRECT_PARAMETER.getValue(), result.get(ErrorUtils.ATTR_CODE));
		
		result = this.treatmentReviewService.commentTreatmentReview((String)review.get(EntityUtils.ID), comment);
		assertEquals(ErrorCodes.USER_NOT_AUTHENTICATED.getValue(), result.get(ErrorUtils.ATTR_CODE));
		
		loginUnathorizedUser();
		
		result = this.treatmentReviewService.commentTreatmentReview((String)review.get(EntityUtils.ID), comment);
		assertEquals(ErrorCodes.UNAUTHORIZED.getValue(), result.get(ErrorUtils.ATTR_CODE));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void commentTreatmentReview_Success() throws Exception {
		DBObject user = userService.findDBUserByEmail("peter.novak19@test.com");
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(new BasicDBObject("author._id", user.get(EntityUtils.ID)));
		assertNotNull(review);
		int reviewCommentsCount = review.get("reviewCommentsCount") == null ? 0 : (int) review.get("reviewCommentsCount");
		this.userService.signin("peter.novak12@test.com", "peter.novak", "", false);
		
		DBObject comment = new BasicDBObject("text", "commentTreatmentReview_Success");
		DBObject result = this.treatmentReviewService.commentTreatmentReview((String)review.get(EntityUtils.ID), comment);
		assertFalse(ErrorUtils.isError(result));
		
		review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(review.get(EntityUtils.ID));
		assertTrue(review.containsField("lastCommentedOn"));
		assertEquals(reviewCommentsCount + 1, review.get("reviewCommentsCount"));
		
		comment = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).find(new BasicDBObject("text", "commentTreatmentReview_Success")).toArray().get(0);
		String trcId = (String) comment.get(EntityUtils.ID);
		List<DBObject> notifications = userNotificationService.findAllByQuery(new BasicDBObject("treatmentReviewCommentId", trcId));
		assertEquals(1, notifications.size());
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void deleteTreatmentReviewComment_Success() throws Exception {
		// comented review
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(new BasicDBObject("author._id",  userService.findDBUserByEmail("peter.novak19@test.com").get(EntityUtils.ID)));
		assertNotNull(review);
		int reviewCommentsCount = review.get("reviewCommentsCount") == null ? 0 : (int) review.get("reviewCommentsCount");
		
		/// user commenting 
		DBObject userLoggedIn = userService.findDBUserByEmail("peter.novak12@test.com");
		DBObject statistics = (DBObject) userLoggedIn.get("statistics");
		int userStatsReviewCommentsCount = statistics.get("commentsCount") == null ? 0 : (int) statistics.get("commentsCount");
		this.userService.signin((String) userLoggedIn.get("email"), "peter.novak", "", false);
		
		String commentText = "deleteTreatmentReviewComment_Success";
		DBObject comment = new BasicDBObject("text", commentText);
		DBObject result = this.treatmentReviewService.commentTreatmentReview((String)review.get(EntityUtils.ID), comment);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject filter = new BasicDBObject("text", commentText);
		comment = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).findOne(filter);
		
		this.treatmentReviewService.deleteTreatmentReviewComment((String)comment.get(EntityUtils.ID));
		
		review = treatmentReviewService.findById((String) review.get(EntityUtils.ID));
		assertEquals(reviewCommentsCount, review.get("reviewCommentsCount"));
		
		userLoggedIn = userService.findDBUserByEmail((String) userLoggedIn.get("email"));
		DBObject statistics2 = (DBObject) userLoggedIn.get("statistics");
		int userStatsReviewCommentsCount2 = (int) statistics2.get("commentsCount");
		assertEquals(userStatsReviewCommentsCount, userStatsReviewCommentsCount2);
		
		comment = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).findOne(filter);
		assertNull(comment);
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void diseasesIndex_Success() throws Exception {
		List<DBObject> index = treatmentReviewSummaryService.diseasesIndex();
		assertFalse(index.isEmpty());
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void findAllSummariesDiseaseWithTreatmentByIndex() throws Exception {
		List<DBObject> index = treatmentReviewSummaryService.diseasesIndex();
		assertFalse(index.isEmpty());
		
		List<DBObject> summaries = treatmentReviewSummaryService.findAllDiseasesWithTreatmentsByIndex((String) index.get(0).get("index"));
		assertFalse(summaries.isEmpty());
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void reportTreatmentReviewComment() throws Exception {
		DBObject user = userService.findDBUserByEmail("peter.novak19@test.com");
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(new BasicDBObject("author._id", user.get(EntityUtils.ID)));
		assertNotNull(review);
		
		DBObject user1 = userService.findDBUserByEmail("peter.novak12@test.com");
		this.userService.signin("peter.novak12@test.com", "peter.novak", "", false);
		
		DBObject comment = new BasicDBObject("note", "My Note");
		Integer fraudReportCategoryId = FraudReportCategory.SPAM.getValue();
		comment.put("fraudReportCategory", fraudReportCategoryId );
		DBObject result = this.fraudReportService.reportEntity(EntityUtils.TREATMENT_REVIEW_EVENT, "testId", comment);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject report = getMongoTemplate().getCollection(EntityUtils.FRAUD_REPORT_ITEM).findOne(new BasicDBObject("user._id", user1.get(EntityUtils.ID)));
		assertEquals("My Note", report.get("note"));
		assertEquals(EntityUtils.TREATMENT_REVIEW_EVENT, report.get("entityName"));
		assertEquals(fraudReportCategoryId, report.get("fraudReportCategory"));
		
		result = this.fraudReportService.reportEntity(EntityUtils.TREATMENT_REVIEW_EVENT, "testId", comment);
		assertFalse(ErrorUtils.isError(result));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void reportTreatmentReview() throws Exception {
		DBObject user = userService.findDBUserByEmail("peter.novak19@test.com");
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(new BasicDBObject("author._id", user.get(EntityUtils.ID)));
		assertNotNull(review);
		
		DBObject user1 = userService.findDBUserByEmail("peter.novak12@test.com");
		this.userService.signin("peter.novak12@test.com", "peter.novak", "", false);
		
		DBObject comment = new BasicDBObject("note", "My Note");
		Integer fraudReportCategoryId = FraudReportCategory.UNPROFESSIONAL_REVIEW.getValue();
		comment.put("fraudReportCategory", fraudReportCategoryId);
		DBObject result = this.fraudReportService.reportEntity(EntityUtils.TREATMENT_REVIEW, (String) review.get(EntityUtils.ID), comment);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject report = getMongoTemplate().getCollection(EntityUtils.FRAUD_REPORT_ITEM).findOne(new BasicDBObject("user._id", user1.get(EntityUtils.ID)).append("note", "My Note"));
		assertNotNull(report);
		assertEquals(EntityUtils.TREATMENT_REVIEW, report.get("entityName"));
		assertEquals(fraudReportCategoryId, report.get("fraudReportCategory"));
		
		result = this.fraudReportService.reportEntity(EntityUtils.TREATMENT_REVIEW, (String) review.get(EntityUtils.ID), comment);
		assertFalse(ErrorUtils.isError(result));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void deleteTreatmentReviewReport() throws Exception {
			
		DBObject user = userService.findDBUserByEmail("peter.novak19@test.com");
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(new BasicDBObject("author._id", user.get(EntityUtils.ID)));
		assertNotNull(review);
		
		DBObject user1 = userService.findDBUserByEmail("peter.novak12@test.com");
		this.userService.signin("peter.novak12@test.com", "peter.novak", "", false);
		
		DBObject comment = new BasicDBObject("note", "deleteTreatmentReviewReport");
		comment.put("fraudReportCategory", FraudReportCategory.SPAM.getValue());
		DBObject result = this.fraudReportService.reportEntity(EntityUtils.TREATMENT_REVIEW, (String) review.get(EntityUtils.ID), comment);
		assertFalse(ErrorUtils.isError(result));
		DBObject reportItem = getMongoTemplate().getCollection(EntityUtils.FRAUD_REPORT_ITEM).findOne(new BasicDBObject("user._id", user1.get(EntityUtils.ID)).append("note", "deleteTreatmentReviewReport"));
		assertNotNull(reportItem);
		DBObject report = getMongoTemplate().getCollection(EntityUtils.FRAUD_REPORT).findOne(new BasicDBObject(EntityUtils.ID, reportItem.get("fraudReportId")));
		assertNotNull(report);
		
		result = this.fraudReportService.delete((String) report.get(EntityUtils.ID));
		assertTrue(ErrorUtils.isError(result));
		
		loginTestUserAdmin();
		
		result = this.fraudReportService.delete((String) report.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
		assertNull(getMongoTemplate().getCollection(EntityUtils.FRAUD_REPORT_ITEM).findOne(new BasicDBObject(EntityUtils.ID, reportItem.get(EntityUtils.ID))));
		assertNull(getMongoTemplate().getCollection(EntityUtils.FRAUD_REPORT).findOne(new BasicDBObject(EntityUtils.ID, report.get(EntityUtils.ID))));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void createTreatmentReview_NonVerifiedUser() throws Exception {

		assertFalse(ErrorUtils.isError(userService.signin("peter.novak21@test.com", "peter.novak", "", false)));
		
		long sideeffectsCount = mongoTemplate.getDb().getCollection(EntityUtils.TREATMENT_SIDE_EFFECT).count();
		long reviewsCount = mongoTemplate.getDb().getCollection(EntityUtils.TREATMENT_REVIEW).count();
		long tmpreviewsCount = mongoTemplate.getDb().getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).count();
		
		DBObject review = new BasicDBObject();
		review.put("disease", new BasicDBObject("name", "Angina").toMap());
		review.put("treatment", new BasicDBObject("name", "Vitamin C").toMap());
		review.put("rating", 0.45d);
		review.put("text", "createTreatmentReview_NonVerifiedUser");
		
		Map<String, Object> sideEffect = new HashMap<>();
		sideEffect.put("severity", 0.1d);
		sideEffect.put("sideEffect", new BasicDBObject("name", "Headache").toMap());
		review.put("sideEffects", Arrays.asList(sideEffect));
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		assertEquals(sideeffectsCount, mongoTemplate.getDb().getCollection(EntityUtils.TREATMENT_SIDE_EFFECT).count());
		assertEquals(reviewsCount, mongoTemplate.getDb().getCollection(EntityUtils.TREATMENT_REVIEW).count());
		assertEquals(reviewsCount, mongoTemplate.getDb().getCollection(EntityUtils.TREATMENT_REVIEW).count());
		assertEquals(tmpreviewsCount + 1, mongoTemplate.getDb().getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).count());
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void updateTreatmentReview_Success() throws Exception {

		assertFalse(ErrorUtils.isError(userService.signin("peter.novak2@test.com", "peter.novak", "", false)));
		
		long sideeffectsCount = mongoTemplate.getDb().getCollection(EntityUtils.TREATMENT_SIDE_EFFECT).count();
		
		DBObject disease = DiseaseServiceTest.getDiseases().get(3);
		DBObject treatment = TreatmentServiceTest.getTreatments().get(3);
		
		DBObject review = new BasicDBObject();
		review.put("disease", new BasicDBObject("name", disease.get("name")).toMap());
		review.put("treatment", new BasicDBObject("name", treatment.get("name")).toMap());
		review.put("rating", 0.35d);
		review.put("text", "updateTreatmentReview_Success");
		
		Map<String, Object> sideEffect = new HashMap<>();
		sideEffect.put("severity", 0.1d);
		sideEffect.put("sideEffect", new BasicDBObject("name", "Headache").toMap());
		review.put("sideEffects", Arrays.asList(sideEffect));
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		List<DBObject> summaries = this.treatmentReviewSummaryService.findAllByQuery(
				new BasicDBObject("treatment._id", treatment.get(EntityUtils.ID))
					.append("disease._id", disease.get(EntityUtils.ID)), new PageRequest(0, 10));
		assertThat(summaries.size(), is(1));
		assertEquals(new Double(0.35d), summaries.get(0).get("rating"));
		
		String trId = (String) result.get(EntityUtils.ID);
		assertNotNull(trId);
		review = treatmentReviewService.findById(trId);
		Date modifiedOn = (Date) review.get(EntityUtils.MODIFIED_ON);
		
		Map<String, Object> sideEffect2 = new HashMap<>();
		sideEffect2.put("severity", 0.2d);
		sideEffect2.put("sideEffect", new BasicDBObject("name", "Headache 2").toMap());
		review.put("disease", new BasicDBObject("name", disease.get("name")).toMap());
		review.put("treatment", new BasicDBObject("name", treatment.get("name")).toMap());
		review.put("sideEffects", Arrays.asList(sideEffect, sideEffect2));
		review.put("rating", 0.44d);
		review.put("text", "updateTreatmentReview_Success 2");
		Thread.sleep(500);
		treatmentReviewService.update(review);
		Thread.sleep(500);
		review = treatmentReviewService.findById(trId);
		assertTrue(modifiedOn.compareTo((Date)review.get(EntityUtils.MODIFIED_ON)) < 0);
		
		assertTrue(sideeffectsCount < mongoTemplate.getDb().getCollection(EntityUtils.TREATMENT_SIDE_EFFECT).count());
		
		summaries = this.treatmentReviewSummaryService.findAllByQuery(
				new BasicDBObject("treatment._id", treatment.get(EntityUtils.ID))
					.append("disease._id", disease.get(EntityUtils.ID)), new PageRequest(0, 10));
		assertThat(summaries.size(), is(1));
		assertEquals(new Double(0.44), summaries.get(0).get("rating"));
	}
	
	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void createTreatmentReviewForSubcription() throws Exception {
		
		String email = "jens.gulp@test.com";
		DBObject user = userService.findDBUserByEmail(email);
		assertFalse(ErrorUtils.isError(this.userService.signin(email, "jens.gulp", "", false)));
		
		DBObject review = new BasicDBObject();
		review.put("disease", new BasicDBObject("name", "Flu").toMap());
		review.put("treatment", new BasicDBObject("name", "Vitamin C15").toMap());
		review.put("rating", 0.45d);
		review.put("text", "inviteeCanSignUp");
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		user = userService.findDBUserByEmail(email);
				
		/// SEND
		String userToInvite = "test3@test.com";
		invitationService.sendInvitation(userToInvite);
		treatmentReviewService.createTreatmentReviewForSubscription(new BasicDBObject("email", userToInvite).append("treatmentReview", review));
		List<DBObject> list = (List<DBObject>) invitationService.findAllForUser((String) user.get("_id")).get("invitations");
		assertFalse(list.isEmpty());
		assertEquals(null, list.get(list.size() - 1).get("createdUser"));
		assertEquals(userToInvite, list.get(list.size() - 1).get("email"));
		
		DBObject data = invitationService.inviteeData(userToInvite);
		assertTrue((boolean)data.get("canberegistered"));
		assertNotNull(data.get("treatmentReview"));
		
		data = invitationService.inviteeData("test4@test.com");
		assertFalse((boolean)data.get("canberegistered"));
		assertNull(data.get("treatmentReview"));
	}
}
