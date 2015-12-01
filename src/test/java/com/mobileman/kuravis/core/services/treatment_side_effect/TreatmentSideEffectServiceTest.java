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
 * TreatmentSideEffectServiceTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 29.8.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.treatment_side_effect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.Data;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class TreatmentSideEffectServiceTest extends AbstractIntegrationTest {
	
	@Autowired
	private DiseaseService diseaseService;
	
	@Autowired
	private TreatmentService treatmentService;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;
	
	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TreatmentSideEffectService treatmentSideEffectService;
	
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
	public void admin_createDeleteSideEffect_WithReviews() throws Exception {
		loginTestUserAdmin();
		String reviewText = "admin_createDeleteSideEffect_WithReviews";
		diseaseService.save(new BasicDBObject("name", "Flu 1"));
		treatmentService.save(new BasicDBObject("name", "Aspirin 1"));
		treatmentSideEffectService.save(new BasicDBObject("name", "Leg Pain"));
		
		DBObject disease = diseaseService.findAllByQuery(new BasicDBObject("name", "Flu 1")).get(0);
		DBObject treatment = treatmentService.findAllByQuery(new BasicDBObject("name", "Aspirin 1")).get(0);
		DBObject sideEffect = treatmentSideEffectService.findAllByQuery(new BasicDBObject("name", "Leg Pain")).get(0);
				
		DBObject review = new BasicDBObject();
		review.put("disease", disease.toMap());
		review.put("treatment", treatment.toMap());
		review.put("text", reviewText);
		review.put("rating", 0.45d);
		
		Map<String, Object> trSideEffect = new HashMap<>();
		trSideEffect.put("severity", 0.3d);
		trSideEffect.put("sideEffect", sideEffect.toMap());
		review.put("sideEffects", Arrays.asList(trSideEffect));
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		int count = treatmentReviewService.findAllByQuery(new BasicDBObject("text", reviewText)).size();
		assertEquals(1, count);
		
		count = treatmentReviewSummaryService.findAllByQuery(new BasicDBObject(EntityUtils.ID, 
				EntityUtils.createTreatmentReviewSummaryId(disease.get(EntityUtils.ID), treatment.get(EntityUtils.ID)))).size();
		assertEquals(1, count);
		
		result = treatmentSideEffectService.delete((String) sideEffect.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
		
		count = treatmentReviewService.findAllByQuery(new BasicDBObject("text", reviewText)).size();
		assertEquals(0, count);
		
		assertEquals(0, diseaseService.findAllByQuery(new BasicDBObject(EntityUtils.ID, disease.get(EntityUtils.ID))).size());
		assertEquals(0, treatmentService.findAllByQuery(new BasicDBObject(EntityUtils.ID, treatment.get(EntityUtils.ID))).size());
		assertEquals(0, treatmentSideEffectService.findAllByQuery(new BasicDBObject(EntityUtils.ID, sideEffect.get(EntityUtils.ID))).size());
	}
	
	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void admin_updateSideEffect_WithReviews() throws Exception {
		
		assertFalse(ErrorUtils.isError(userService.signin("peter.novak3@test.com", "peter.novak", "", false)));
		String reviewText = "admin_updateSideEffect_WithReviews";
		
		DBObject disease = diseaseService.findAllByQuery(new BasicDBObject(EntityUtils.NAME, "Flu")).get(0);
		DBObject treatment = treatmentService.findAllByQuery(new BasicDBObject(EntityUtils.NAME, "Vitamin C")).get(0);
		DBObject sideEffect = new BasicDBObject(EntityUtils.NAME, "Leg Pain");
		
		assertNull(treatmentSideEffectService.findByProperty(EntityUtils.NAME, sideEffect.get(EntityUtils.NAME)));
				
		DBObject review = new BasicDBObject();
		review.put("disease", disease.toMap());
		review.put("treatment", treatment.toMap());
		review.put("rating", 0.45d);
		review.put("text", reviewText);
		
		Map<String, Object> trSideEffect = new HashMap<>();
		trSideEffect.put("severity", 0.3d);
		trSideEffect.put("sideEffect", sideEffect.toMap());
		review.put("sideEffects", Arrays.asList(trSideEffect));
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		sideEffect = treatmentSideEffectService.findByProperty(EntityUtils.NAME, sideEffect.get(EntityUtils.NAME));
		assertNotNull(sideEffect);
		
		/////////////////////// UPDATE
		loginTestUserAdmin();
		sideEffect.put(EntityUtils.NAME, "Hand Pain");
		result = treatmentSideEffectService.update((String) sideEffect.get(EntityUtils.ID), sideEffect);
		assertFalse(ErrorUtils.isError(result));
		
		assertNotNull(treatmentSideEffectService.findByProperty(EntityUtils.NAME, sideEffect.get(EntityUtils.NAME)));
		
		review = treatmentReviewService.findByProperty("text", reviewText);
		List<DBObject> sideEffects = (List<DBObject>) review.get("sideEffects");
		DBObject dbSideEffect = (DBObject) sideEffects.get(0).get("sideEffect");
		assertEquals("Hand Pain", dbSideEffect.get(EntityUtils.NAME));
	}
	
	@Test(expected = HealtPlatformException.class)
	public void admin_updateSideEffect_newNameAlreadyExists_returnError() throws Exception {
		loginTestUserAdmin();
		String effect = "Leg Pain";
		Map<String, Object> entity = new HashMap<String, Object>();
		entity.put("name", effect);
		DBObject effectObj = treatmentSideEffectService.createOrFindByName(entity);
		if (effectObj == null) {
			return;
		}
		String id = (String) treatmentSideEffectService.save(new BasicDBObject("name", "newLookEffect")).get(EntityUtils.ID);
		DBObject newSideEffect = treatmentSideEffectService.findById(id);
		DBObject result = null;
		try {
			newSideEffect.put(EntityUtils.NAME, effect.toLowerCase());
			result = treatmentSideEffectService.update(id, newSideEffect);
			assertTrue(ErrorUtils.isError(result));
		} finally {
			result = treatmentSideEffectService.delete(id);
			assertFalse(ErrorUtils.isError(result));
			treatmentSideEffectService.delete((String) effectObj.get(EntityUtils.ID));
		}
	}

	@Test(expected = HealtPlatformException.class)
	public void admin_create_newNameAlreadyExists_returnError() {
		loginTestUserAdmin();
		DBObject result = null;
		String entity = "sideEFF";
		DBObject entityObj = treatmentSideEffectService.create(EntityUtils.TREATMENT_SIDE_EFFECT, new BasicDBObject(EntityUtils.NAME, entity));
		try {
			treatmentSideEffectService.create(EntityUtils.TREATMENT_SIDE_EFFECT, new BasicDBObject(EntityUtils.NAME, entity));
		} finally {
			result = treatmentSideEffectService.delete(EntityUtils.getEntityId(entityObj));
			assertFalse(ErrorUtils.isError(result));
		}
	}
	
}
