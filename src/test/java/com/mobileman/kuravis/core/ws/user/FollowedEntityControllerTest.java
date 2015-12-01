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
package com.mobileman.kuravis.core.ws.user;

import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.FollowedEntity;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.services.user.FollowedEntityService;
import com.mobileman.kuravis.core.ws.BaseControllerTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/application-context.xml")
public class FollowedEntityControllerTest extends BaseControllerTest {
	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;

	@Autowired
	private FollowedEntityService userFollowedEntityService;

	@Autowired
	private FollowedEntityController controller;
	
	@Before
	public void setUp() throws Exception {
		setUp(controller);
	}
	
	@Test
	@Ignore("locale is test running, on jenkins not")
	public void followUnfollowDisease() throws Exception {
		MvcResult result = null;
		FollowedEntity given = new FollowedEntity();
		given.setEntityType(EntityUtils.getDocumentCollectionName(Disease.class));
		given.setEntityId(getDisease().get_id());
		FollowedEntity actual;
		try {
			result = doPost("/user/follow", given);
			Assert.assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(result.getResponse().getStatus()));
			assertNull(result.getResolvedException());
			setId(given, result);
			actual = userFollowedEntityService.getByEntity(getLoggedUser().get_id(), given.getEntityType(), given.getEntityId());
			Assert.assertNotNull(actual);
		} finally {
			doDelete(given.get_id(), "/user/follow");
			actual = userFollowedEntityService.getByEntity(getLoggedUser().get_id(), given.getEntityType(), given.getEntityId());
			Assert.assertNull(actual);
		}
	}

	@Test
	@Ignore
	public void followUnfollowDiseaseTreatment() throws Exception {
		MvcResult result = null;
		FollowedEntity given = new FollowedEntity();
		given.setEntityType(EntityUtils.getDocumentCollectionName(TreatmentReviewSummary.class));
		given.setEntityId(EntityUtils.getEntityId(treatmentReviewSummaryService.findAll().get(0)));
		FollowedEntity actual;
		try {
			userFollowedEntityService.unfollowAll(getLoggedUser().get_id());
			result = doPost("/user/follow", given);
			Assert.assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(result.getResponse().getStatus()));
			assertNull(result.getResolvedException());
			setId(given, result);
			actual = userFollowedEntityService.getByEntity(getLoggedUser().get_id(), given.getEntityType(), given.getEntityId());
			Assert.assertNotNull(actual);
			List<FollowedEntity> findByUserId = userFollowedEntityService.findByUserId(getLoggedUser().get_id());
			Assert.assertNotNull(findByUserId);
		} finally {
			doDelete(given.get_id(), "/user/follow");
			actual = userFollowedEntityService.getByEntity(getLoggedUser().get_id(), given.getEntityType(), given.getEntityId());
			Assert.assertNull(actual);
		}
	}

}
