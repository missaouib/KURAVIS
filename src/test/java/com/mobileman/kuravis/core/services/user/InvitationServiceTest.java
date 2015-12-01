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
 * InvitationServiceTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 6.11.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.user;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.Data;
import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class InvitationServiceTest extends AbstractIntegrationTest {

	@Autowired
	private InvitationService invitationService;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;
		
	@Override
	@Before
	public void setUp() {
		super.setUp();
		this.userService.signout();
		Data.setUp(mongoTemplate, userService);
	}
	
	/**
	 * @throws Exception
	 */
	@Test(expected = HealtPlatformException.class)
	public void findAllForUser_NoUser() throws Exception {
		invitationService.findAllForUser(null);
	}
	
	/**
	 * @throws Exception
	 */
	@Test(expected = HealtPlatformException.class)
	public void findAllForUser_NoId() throws Exception {
		invitationService.findAllForUser(null);
	}
		
	/**
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void findAllForUser() throws Exception {
		
		
		String email = "peter.novak17@test.com";
		DBObject user = userService.findDBUserByEmail(email);
		assertFalse(ErrorUtils.isError(this.userService.signin((String) user.get("email"), "peter.novak", "", false)));
		
		List<DBObject> list = (List<DBObject>) invitationService.findAllForUser((String) user.get("_id")).get("invitations");
		assertTrue(list.isEmpty());
	}
	
	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void sendInvitation() throws Exception {
		
		
		String email = "jens.gulp@test.com";
		DBObject user = userService.findDBUserByEmail(email);
		assertFalse(ErrorUtils.isError(this.userService.signin(email, "jens.gulp", "", false)));
		
		DBObject review = new BasicDBObject();
		review.put("disease", new BasicDBObject("name", "Flu").toMap());
		review.put("treatment", new BasicDBObject("name", "Vitamin B13").toMap());
		review.put("rating", 0.45d);
		review.put("text", "createTreatmentReview_NonVerifiedUser");
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		user = userService.findDBUserByEmail(email);
		int ic = (int) user.get("invitationCount");
		
		/// SEND
		invitationService.sendInvitation("test@test.com");
		List<DBObject> list = (List<DBObject>) invitationService.findAllForUser((String) user.get("_id")).get("invitations");
		assertEquals(1, list.size());
		assertEquals(null, list.get(0).get("createdUser"));
		user = userService.findDBUserByEmail(email);
		assertEquals(ic - 1, user.get("invitationCount"));
		
		/// RESEND
		invitationService.sendInvitation("test@test.com");
		list = (List<DBObject>) invitationService.findAllForUser((String) user.get("_id")).get("invitations");
		assertEquals(1, list.size());
		user = userService.findDBUserByEmail(email);
		assertEquals(ic - 1, user.get("invitationCount"));
		
		// SEND
		try {
			invitationService.sendInvitation("test1@test.com");
		} catch (HealtPlatformException e) {}
	}
	
	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void inviteeCanSignUp() throws Exception {
		
		String email = "jens.gulp@test.com";
		DBObject user = userService.findDBUserByEmail(email);
		assertFalse(ErrorUtils.isError(this.userService.signin(email, "jens.gulp", "", false)));
		
		DBObject review = new BasicDBObject();
		review.put("disease", new BasicDBObject("name", "Flu").toMap());
		review.put("treatment", new BasicDBObject("name", "Vitamin C14").toMap());
		review.put("rating", 0.45d);
		review.put("text", "inviteeCanSignUp");
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		user = userService.findDBUserByEmail(email);
				
		/// SEND
		String userToInvite = "test2@test.com";
		invitationService.sendInvitation(userToInvite);
		List<DBObject> list = (List<DBObject>) invitationService.findAllForUser((String) user.get("_id")).get("invitations");
		assertFalse(list.isEmpty());
		assertEquals(null, list.get(list.size() - 1).get("createdUser"));
		assertEquals(userToInvite, list.get(list.size() - 1).get("email"));
		
		DBObject data = invitationService.inviteeData(userToInvite);
		assertTrue((boolean)data.get("canberegistered"));
		assertNull(data.get("treatmentReview"));
		
		data = invitationService.inviteeData("test4@test.com");
		assertFalse((boolean)data.get("canberegistered"));
		assertNull(data.get("treatmentReview"));
	}
}
