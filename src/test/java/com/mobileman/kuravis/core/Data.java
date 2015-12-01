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
package com.mobileman.kuravis.core;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.option_list.physiotherapie.Physiotherapie;
import com.mobileman.kuravis.core.domain.option_list.psychotherapy.Psychotherapy;
import com.mobileman.kuravis.core.domain.option_list.treatment_type.TreatmentType;
import com.mobileman.kuravis.core.domain.option_list.unit.Unit;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.user.User.DiseaseState;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.disease.DiseaseServiceTest;
import com.mobileman.kuravis.core.services.option_list.PhysiotherapieTest;
import com.mobileman.kuravis.core.services.option_list.PsychotherapyTest;
import com.mobileman.kuravis.core.services.option_list.TreatmentTypeServiceTest;
import com.mobileman.kuravis.core.services.option_list.UnitServiceTest;
import com.mobileman.kuravis.core.services.treatment.TreatmentServiceTest;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewServiceTest;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.services.user.UserServiceTest;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

/**
 * @author MobileMan GmbH
 *
 */
public class Data {

	static boolean initialized = false;
	
	/**
	 * @param template
	 * @param userService 
	 */
	public static void setUp(MongoTemplate template, UserService userService) {
		if (initialized) {
			return;
		}
		
		initialized = true;
		
		for (String collection : EntityUtils.getAllEntityNames()) {
			template.getDb().getCollection(collection).remove(new BasicDBObject(), WriteConcern.SAFE);
		}
		
		List<DBObject> users = UserServiceTest.getUsers();
		for (DBObject user : users) {
			if ("peter.novak@test.com".equals(user.get("email"))) {
				continue;
			}
			
			userService.signup(user);
			DBObject account = template.getDb().getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject("email", user.get("email")));
			account.put("roles", Arrays.asList(String.class.cast(user.get("roles")).toLowerCase()));
			template.getDb().getCollection(EntityUtils.USERACCOUNT).update(new BasicDBObject("email", user.get("email")), account);
			
			DBObject savedUser = template.getDb().getCollection(EntityUtils.USER).findOne(new BasicDBObject("email", user.get("email")));
			savedUser.put("state", Arrays.asList(user.get("state")));
			template.getDb().getCollection(EntityUtils.USER).update(new BasicDBObject("email", user.get("email")), savedUser);
		}
		
		List<DBObject> diseases = DiseaseServiceTest.getDiseases();
		for (DBObject dbObject : diseases) {
			template.getDb().getCollection(Disease.ENTITY_NAME).save(dbObject);
		}
		
		List<Unit> units = UnitServiceTest.getUnits();
		for (Unit dbObject : units) {
			template.save(dbObject);
		}
		
		List<TreatmentType> treatmentTypes = TreatmentTypeServiceTest.getTreatmentTypes();
		for (TreatmentType dbObject : treatmentTypes) {
			template.save(dbObject);
		}
		
		List<Psychotherapy> psychotherapies = PsychotherapyTest.getPsychotherapies();
		for (Psychotherapy object : psychotherapies) {
			template.save(object);
		}
		
		List<Physiotherapie> physiotherapies = PhysiotherapieTest.getPsychotherapies();
		for (Physiotherapie object : physiotherapies) {
			template.save(object);
		}
		
		users = UserServiceTest.getUsers();
		int idx = 0;
		for (DBObject user : users) {
			DBObject disease = diseases.get(idx++ % 3);
			DBObject diseaseCopy = new BasicDBObject(EntityUtils.ID, disease.get(EntityUtils.ID)).append(Disease.NAME, disease.get(Disease.NAME));
			DiseaseState state = idx % 5 == 0 ? User.DiseaseState.CURED : User.DiseaseState.IN_THERAPY;
			DBObject userDisease = new BasicDBObject("createdOn", new Date()).append("state", state.getValue()).append("disease", diseaseCopy);
			user.put("diseases", Arrays.asList(userDisease));
			template.getDb().getCollection(EntityUtils.USER).save(user);
		}
		
		List<DBObject> accounts = UserServiceTest.getAccounts();
		for (DBObject dbObject : accounts) {
			template.getDb().getCollection(EntityUtils.USERACCOUNT).save(dbObject);
		}
		
		List<DBObject> treatments = TreatmentServiceTest.getTreatments();
		for (DBObject dbObject : treatments) {
			template.getDb().getCollection(EntityUtils.TREATMENT).save(dbObject);
		}
		
		List<DBObject> treatmentreviews = TreatmentReviewServiceTest.createTreatmentReviews(diseases.get(3), treatments.get(4));
		for (DBObject dbObject : treatmentreviews) {
			template.getDb().getCollection(TreatmentReview.ENTITY_NAME).save(dbObject);
		}
		
		for (DBObject dbObject : TreatmentReviewServiceTest.getTreatmentReviewsSummaries()) {
			template.getDb().getCollection(TreatmentReviewSummary.ENTITY_NAME).save(dbObject);
		}
		
		for (DBObject dbObject : TreatmentReviewServiceTest.getTreatmentReviewSummaryAgeStatistics()) {
			template.getDb().getCollection(EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS).save(dbObject);
		}
		
		initialized = true;
	}
}
