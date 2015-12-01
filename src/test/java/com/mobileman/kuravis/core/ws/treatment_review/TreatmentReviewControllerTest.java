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

import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.services.event.EventService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.ws.BaseControllerTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/application-context.xml")
public class TreatmentReviewControllerTest extends BaseControllerTest {
		
	@Autowired
	private TreatmentReviewController controller;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;
	
	@Autowired
	private EventService eventService;
	
//	@Override
//	protected String getUserEmail() {
//		return "3@mobileman.com";
//	}
	
	@Before
	public void setUp() throws Exception {
		super.setUp(controller);
	}

	@Test
	@Ignore
	public void findUserReviews() throws Exception {
		MvcResult result = null;
		try {
			RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/" + TreatmentReview.ENTITY_NAME + "/user").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
			result = this.mockMvc.perform(requestBuilder).andReturn();
			Assert.assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
			assertNull(result.getResolvedException());
		} finally {
		}
	}
	
}
