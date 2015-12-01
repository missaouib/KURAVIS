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
 * TreatmentReviewSummaryControllerTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 20.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.treatment_review;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.shiro.authz.UnauthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.ws.BaseControllerTest;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

/**
 * @author MobileMan GmbH
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/application-context.xml")
public class TreatmentReviewSummaryControllerTest extends BaseControllerTest {
		
	@Autowired
	private TreatmentReviewSummaryController treatmentReviewSummaryController;
	
	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;
	
	@Autowired
	private TreatmentService treatmentService;
	
	@Autowired
	private DiseaseService diseaseService;
		
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp(treatmentReviewSummaryController);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void createSuggestion() throws Exception {
		signinAuthorizedUser();
		TreatmentReviewSummary summary = new TreatmentReviewSummary();
		
		DBObject treatment = treatmentService.findByProperty("name", "Vitamin D");
		DBObject disease = diseaseService.findByProperty("name", "Migraine");
		summary.setDisease(objectMapper.readValue(new BasicDBObject(Disease.ID, disease.get(Disease.ID)).append(Disease.NAME, disease.get(Disease.NAME)).toString(), Disease.class));
		summary.setTreatment(objectMapper.readValue(new BasicDBObject(Treatment.ID, treatment.get(Treatment.ID)).append(Treatment.NAME, treatment.get(Treatment.NAME)).toString(), Treatment.class));
		summary.setSuggestion(true);
		
		QueryBuilder query = QueryBuilder.start().put(TreatmentReviewSummary.DISEASE + "." + Disease.ID).is(disease.get(Disease.ID))
				.and(TreatmentReviewSummary.TREATMENT + "." + Treatment.ID).is(treatment.get(Treatment.ID));
		long count = treatmentReviewSummaryService.count(query.get());
		String content = objectMapper.writeValueAsString(summary);
		
		RequestBuilder createRequest = MockMvcRequestBuilders.post("/" + TreatmentReviewSummary.ENTITY_NAME)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(content);
		MvcResult response = this.mockMvc.perform(createRequest).andReturn();
		assertNotNull(response.getResolvedException());
		assertEquals(UnauthorizedException.class, response.getResolvedException().getClass());
		
		////////////// ADMIN
		signinAdminUser();
		response = this.mockMvc.perform(createRequest).andReturn();
		assertNull(response.getResolvedException());
		assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(response.getResponse().getStatus()));
		// second time - must work
		response = this.mockMvc.perform(createRequest).andReturn();
		assertNull(response.getResolvedException());
		assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(response.getResponse().getStatus()));
		
		long count2 = treatmentReviewSummaryService.count(query.get());
		assertTrue(count2 > 0);
		assertTrue(count2 >= count);
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void deleteSuggestion() throws Exception {
				
		DBObject treatment = treatmentService.findByProperty("name", "Vitamin D");
		DBObject disease = diseaseService.findByProperty("name", "Migraine");
		
		QueryBuilder query = QueryBuilder.start().put(TreatmentReviewSummary.DISEASE + "." + Disease.ID).is(disease.get(Disease.ID))
				.and(TreatmentReviewSummary.TREATMENT + "." + Treatment.ID).is(treatment.get(Treatment.ID));
		long count = treatmentReviewSummaryService.count(query.get());
		if (count == 0) {
			createSuggestion();
			count = treatmentReviewSummaryService.count(query.get());
		}
				
		final String summaryId = EntityUtils.createTreatmentReviewSummaryId(disease.get(EntityUtils.ID), treatment.get(EntityUtils.ID));	
		////////////////////////////////////
		// USER
		signinAuthorizedUser();
		RequestBuilder deleteRequest = MockMvcRequestBuilders.delete("/" + TreatmentReviewSummary.ENTITY_NAME + "/" + summaryId)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
		MvcResult response = this.mockMvc.perform(deleteRequest).andReturn();
		assertEquals(UnauthorizedException.class, response.getResolvedException().getClass());
		
		//////////////////////////// ADMIN
		signinAdminUser();
		
		response = this.mockMvc.perform(deleteRequest).andReturn();
		assertNull(response.getResolvedException());
		assertEquals(HttpStatus.OK, HttpStatus.valueOf(response.getResponse().getStatus()));
		
		long count2 = treatmentReviewSummaryService.count(query.get());
		assertEquals(count - 1, count2);
		
		signinAuthorizedUser();
	}
}
