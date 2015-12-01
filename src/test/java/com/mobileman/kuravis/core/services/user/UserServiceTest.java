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
package com.mobileman.kuravis.core.services.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static com.mobileman.kuravis.core.util.ErrorUtils.isError;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.Data;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.user.Gender;
import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.user.User.DiseaseState;
import com.mobileman.kuravis.core.domain.user.UserState;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.UserUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.disease.DiseaseServiceTest;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.util.security.SecurityUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class UserServiceTest extends AbstractIntegrationTest {
	
	private static List<DBObject> entities = new ArrayList<DBObject>();
	private static List<DBObject> accounts = new ArrayList<DBObject>();
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserNotificationService userNotificationService;

	@Autowired
	private DiseaseService diseaseService;
	
	@Autowired
	private TreatmentService treatmentService;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;
	
	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;

	@Override
	public String getCollectionName() {
		return "user";
	}
	
	@Override
	@Before
	public void setUp() {
		super.setUp();
		this.userService.signout();
		Data.setUp(mongoTemplate, userService);
	}
	
	/**
	 * @return test accounts
	 */
	public static List<DBObject> getAccounts() {
		getUsers();
		return accounts;
	}
	
	/**
	 * @return test users
	 */
	public static List<DBObject> getUsers() {
		if (!entities.isEmpty()) {
			return entities;
		}
		
		String[] locations = {"Berlin", "Vienna", "Munchen", "Frankfurt", "Zurich"};
		int count = 22;
		for (int i = 0; i < count; i++) {
			String email = "peter.novak" + i + "@test.com";
			DBObject user = new BasicDBObject();
			user.put(EntityUtils.ID, UUID.randomUUID().toString());
			user.put("name", "Peter Novak " + i);
			user.put("registrationDate", new Date());
			user.put("lastLoginDate", new Date());
			user.put("aboutMe", "Simple person");
			user.put("yearOfBirth", 1977 + i%20);
			user.put("gender", i%3 == 0 ? Gender.FEMALE.getValue() : Gender.MALE.getValue());
			user.put("location", locations[i%5]);
			user.put("state", i%10 == 0 ? UserState.INACTIVE.getValue() : UserState.ACTIVE.getValue());
			user.put("pageViewsCount", i % 30);
			user.put("email", email);
			user.put("invitationCount", 0);
			
			if (i%4 == 0) {
				user.put("gender", Gender.UNKNOWN.getValue());
			}
			
			user.put("settings", 
					new BasicDBObject("privacySettings", 
							new BasicDBObject("emailNotification", 
									new BasicDBObject("weeklyUpdatesCommentsAndVotes", true)
											  .append("news_announcements", true)))
					.append("profile", new BasicDBObject("avatarColor", "#79ae3d"))
			);
			
			entities.add(user);
			
			DBObject account = new BasicDBObject();
			account.put(EntityUtils.ID, UUID.randomUUID().toString());
			account.put("email", email);
			account.put("createdOn", new Date());
			
			if (i == 2) {
				account.put("roles", Arrays.asList(Roles.ADMIN));
			} else if (i == count - 1 || i == count - 2) {
				account.put("roles", Arrays.asList(Roles.NONVERIFIED_USER));
				account.put("activationUuid", UUID.randomUUID().toString());
				user.put("state", UserState.UNVERIFIED.getValue());
			} else {
				account.put("roles", Arrays.asList("user"));
			}
			
			
			
			try {
				account.put("password", SecurityUtils.getSaltedHash("peter.novak"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			accounts.add(account);
			
			user.put("accountId", account.get(EntityUtils.ID));
			account.put("userId", user.get(EntityUtils.ID));
		}
		
		return entities;
	}
	

	/**
	 * @throws Exception
	 */
	@Test
	public void testSignIn_Failure() throws Exception {
		
		DBObject result = userService.signin("test", "test", "", false);
		assertTrue(ErrorUtils.isError(result));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testSignIn_UnknownUser_Captcha_Failure() throws Exception {
		
		DBObject result = userService.signin("test", "test", "", false);
		assertNull(result.get("show_captcha"));
		assertTrue(ErrorUtils.isError(result));
		
		result = userService.signin("test", "test", "", false);
		assertNull(result.get("show_captcha"));
		assertTrue(ErrorUtils.isError(result));
		
		result = userService.signin("test", "test", "", false);
		assertNull(result.get("show_captcha"));
		assertTrue(ErrorUtils.isError(result));
		
		result = userService.signin("test", "test", "", false);
		assertNull(result.get("show_captcha"));
		assertTrue(ErrorUtils.isError(result));
		
		result = userService.signin("test", "test", "", false);
		assertNotNull(result.get("show_captcha"));
		assertTrue(ErrorUtils.isError(result));
		
		userService.signin("test", "test", "", false);
		assertNotNull(result.get("show_captcha"));
		assertTrue(ErrorUtils.isError(result));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testSignIn_WrongPasswordFailure() throws Exception {
		
		Subject currentUser = org.apache.shiro.SecurityUtils.getSubject();
		if (currentUser != null) {
			currentUser.logout();
		}
		
		User user = userService.findUserByEmail("peter.novak1@test.com");
		assertNotNull(user);
		
		DBObject result = userService.signin("peter.novak1@test.com", "peter.novak1", "", false);
		assertNotNull(result);
		assertEquals("error", result.get("result"));
		
		currentUser = org.apache.shiro.SecurityUtils.getSubject();
		assertFalse(currentUser.isAuthenticated());
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testUserExistsWithEmail() throws Exception {
		assertFalse(userService.userExistsWithEmail("aaa"));
		assertTrue(userService.userExistsWithEmail("peter.novak12@test.com"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testSignIn() throws Exception {
		String email = "peter.novak2@test.com";
		DBObject user = userService.findDBUserByEmail(email);
		Date lastLoginDate = (Date) user.get("lastLoginDate");
		
		DBObject result = userService.signin(email, "peter.novak", "", false);
		assertNotNull(result);
		assertEquals("ok", result.get("result"));
		assertEquals(Arrays.asList("admin"), result.get("roles"));
		assertNotNull(result.get("lastLoginDate"));
		assertEquals(0, result.get("unsuccessful_login_count"));
		
		Subject currentUser = org.apache.shiro.SecurityUtils.getSubject();
		assertTrue(currentUser.isAuthenticated());
		assertFalse(currentUser.isRemembered());
		
		user = userService.findDBUserByEmail(email);
		DBObject account = userService.findDBUserAccountByEmail(email);
		assertNotNull(user.get("lastLoginDate"));
		assertEquals(0, user.get("unsuccessful_login_count"));
		assertEquals(0, account.get("unsuccessful_login_count"));
		assertEquals(result.get("lastLoginDate"), user.get("lastLoginDate"));
		
		if (lastLoginDate != null) {
			Date lastLoginDate2 = (Date) user.get("lastLoginDate");
			assertTrue(lastLoginDate.compareTo(lastLoginDate2) < 0);
		}
		
		
		DBObject sessioData = userService.checkSession();
		assertFalse(ErrorUtils.isError(sessioData));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testSignIn_RememberMe() throws Exception {
		Subject currentUser = org.apache.shiro.SecurityUtils.getSubject();
		if (currentUser != null) {
			currentUser.logout();
		}
		
		DBObject result = userService.signin("peter.novak1@test.com", "peter.novak", "", true);
		assertNotNull(result);
		assertEquals("ok", result.get("result"));
		assertEquals(Arrays.asList("user"), result.get("roles"));
		
		currentUser = org.apache.shiro.SecurityUtils.getSubject();
		assertTrue(currentUser.isAuthenticated());
		assertFalse(currentUser.isRemembered());
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testResetCredentials() throws Exception {
		
		DBObject result = this.userService.signin("peter.novak1@test.com", "peter.novak", "", false);
		assertNotNull(result);
		assertFalse(ErrorUtils.isError(result));
		
		result = userService.resetCredentials("peter.novak@test.com");
		assertNotNull(result);
		assertTrue(ErrorUtils.isError(result));

		
		result = userService.resetCredentials("peter.novak1@test.com");
		assertNotNull(result);
		assertFalse(ErrorUtils.isError(result));
		
		// password is not reseted (only email is sent)
		result = this.userService.signin("peter.novak1@test.com", "peter.novak", "", false);
		assertNotNull(result);
		assertFalse(ErrorUtils.isError(result));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testChangePasswordStringString_UnathorizedFailure() throws Exception {
		DBObject result = this.userService.changePassword("test1", "test");
		result.toString();
		assertEquals(ErrorCodes.PASSWORD_NOT_SAME.getValue(), result.get(ErrorUtils.ATTR_CODE));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testChangePasswordStringString_PasswordNotSameFailure() throws Exception {
		this.userService.signout();
		
		String email = "peter.novak3@test.com";
		DBObject result = this.userService.changePassword("test1", "test");
		result.toString();
		assertEquals(ErrorCodes.PASSWORD_NOT_SAME.getValue(), result.get(ErrorUtils.ATTR_CODE));
		
		result = this.userService.signin(email, "peter.novak", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		result = this.userService.changePassword("test1", "test");
		assertTrue(ErrorUtils.isError(result));
		assertEquals(ErrorCodes.PASSWORD_NOT_SAME.getValue(), result.get(ErrorUtils.ATTR_CODE));
		
		this.userService.signout();
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testChangePasswordStringString() throws Exception {
		this.userService.signout();
		
		String email = "peter.novak3@test.com";
		String oldPassword = "peter.novak";
		DBObject result = this.userService.signin(email, oldPassword, "", false);
		assertFalse(ErrorUtils.isError(result));
		
		result = this.userService.changePassword("test", "test");
		assertFalse(ErrorUtils.isError(result));
		
		this.userService.signout();
		DBObject sessioData = userService.checkSession();
		assertTrue(ErrorUtils.isError(sessioData));
		
		result = this.userService.signin(email, "test", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		result = this.userService.changePassword(oldPassword, oldPassword);
		assertFalse(ErrorUtils.isError(result));
		
		this.userService.signout();
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testChangePasswordStringStringString_UnathorizedFailure() throws Exception {
		DBObject result = this.userService.changeResetedPassword("aaa", "test1");
		result.toString();
		assertEquals(ErrorCodes.UNAUTHORIZED.getValue(), result.get(ErrorUtils.ATTR_CODE));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testChangePasswordStringStringString() throws Exception {
		String email = "peter.novak4@test.com";
		String oldPassword = "peter.novak";
		
		DBObject result = this.userService.signin(email, "peter.novak", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		result = userService.resetCredentials(email);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject user = (DBObject) org.apache.shiro.SecurityUtils.getSubject().getPrincipal();
		assertNotNull(user);
		DBObject account = getMongoTemplate().getCollection(EntityUtils.USERACCOUNT).findOne(new BasicDBObject(EntityUtils.ID, user.get("accountId")));
		assertNotNull(account);
		
		result = this.userService.changeResetedPassword((String) account.get("resetPasswordUuid"), "test");
		assertFalse(ErrorUtils.isError(result));
		this.userService.signout();
		
		result = this.userService.signin(email, "test", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		result = this.userService.changePassword(oldPassword, oldPassword);
		assertFalse(ErrorUtils.isError(result));
		
		result = this.userService.signin(email, "test", "", false);
		assertEquals(ErrorUtils.ERROR, result.get(ErrorUtils.ATTR_RESULT));
		
		this.userService.signout();
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testDeleteUserAccount() throws Exception {
		String email = "peter.novak5@test.com";
		DBObject user = userService.findDBUserByEmail(email);
		assertNotNull(user.get(User.ATTR_ACCOUNT_ID));
		
		DBObject result = userService.deleteUserAccount((String) user.get(EntityUtils.ID));
		assertEquals(ErrorCodes.USER_NOT_AUTHENTICATED.getValue(), result.get(ErrorUtils.ATTR_CODE));
		
		result = this.userService.signin(email, "peter.novak", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		result = userService.deleteUserAccount("123");
		assertEquals(ErrorCodes.UNAUTHORIZED.getValue(), result.get(ErrorUtils.ATTR_CODE));
		
		result = userService.deleteUserAccount((String) user.get(EntityUtils.ID));
		
		Subject currentUser = org.apache.shiro.SecurityUtils.getSubject();
		assertFalse(currentUser.isAuthenticated());
		assertFalse(currentUser.isRemembered());
		
		assertNull(userService.findDBUserAccountByEmail(email));
		assertNull(userService.findDBUserByEmail(email));
		assertNull(this.mongoTemplate.getCollection(EntityUtils.USER).findOne(new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID))));
		
		result = userService.deleteUserAccount((String) user.get(EntityUtils.ID));
		assertEquals(ErrorCodes.USER_NOT_AUTHENTICATED.getValue(), result.get(ErrorUtils.ATTR_CODE));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testDeleteUser() throws Exception {
		String email = "testDeleteUser@test.com";
		
		DBObject data = BasicDBObjectBuilder.start()
				.add("email", email)
				.add("password", "testDeleteUser")
				.add("name", "testDeleteUser")
				.get();
		DBObject result = userService.signup(data);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject user = userService.findDBUserByEmail(email);
		assertNotNull(user.get(User.ATTR_ACCOUNT_ID));
		
		result = userService.delete((String) user.get(EntityUtils.ID));
		assertEquals(ErrorCodes.UNAUTHORIZED.getValue(), result.get(ErrorUtils.ATTR_CODE));
		
		// signup standard user
		result = this.userService.signin("peter.novak12@test.com", "peter.novak", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		result = userService.delete((String) user.get(EntityUtils.ID));
		assertEquals(ErrorCodes.UNAUTHORIZED.getValue(), result.get(ErrorUtils.ATTR_CODE));
		
		// signup admin
		loginTestUserAdmin();
		
		result = userService.delete("123");
		assertFalse(ErrorUtils.isError(result));
		
		result = userService.delete((String) user.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
		
		Subject currentUser = org.apache.shiro.SecurityUtils.getSubject();
		assertTrue(currentUser.isAuthenticated());
		assertNotNull(currentUser.getPrincipal());
		
		DBObject account = userService.findDBUserAccountByEmail(email);
		assertNull(account);
		
		user = userService.findById((String) user.get(EntityUtils.ID));
		assertNull(user);
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testDeleteUserAccount_NoAdminFailure() throws Exception {
		String email = "peter.novak8@test.com";
		DBObject user = userService.findDBUserByEmail(email);
		assertNotNull(user);
		
		assertFalse(ErrorUtils.isError(this.userService.signin("peter.novak6@test.com", "peter.novak", "", false)));
		
		DBObject result = userService.deleteUserAccount((String) user.get(EntityUtils.ID));
		assertEquals(ErrorCodes.UNAUTHORIZED.getValue(), result.get(ErrorUtils.ATTR_CODE));
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testSignUp_Failure() throws Exception {
		String email = "peter.novak2@test.com";
		
		DBObject data = BasicDBObjectBuilder.start()
				.add("email", "")
				.add("password", "peter.novak")
				.add("name", "Peter Novak")
				.get();
		DBObject result = userService.signup(data);
		assertTrue(ErrorUtils.isError(result));
		
		data = BasicDBObjectBuilder.start()
				.add("email", email)
				.add("password", "")
				.add("name", "Peter Novak")
				.get();
		result = userService.signup(data);
		assertTrue(ErrorUtils.isError(result));
		
		data = BasicDBObjectBuilder.start()
				.add("email", email)
				.add("password", "peter.novak")
				.add("name", "")
				.get();
		result = userService.signup(data);
		assertTrue(ErrorUtils.isError(result));
		
	}
	
	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testSignUp_Succcess() throws Exception {
		String email = "peter.novak@test.com";
		
		DBObject data = BasicDBObjectBuilder.start()
				.add("email", email)
				.add("password", "peter.novak")
				.add("name", "Peter Novak")
				.get();
		DBObject result = userService.signup(data);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject account = userService.findDBUserAccountByEmail(email);
		assertNotNull(account);
		
		DBObject user = getMongoTemplate().getCollection(EntityUtils.USER).findOne(new BasicDBObject("name", "Peter Novak"));
		assertNotNull(user);
		assertThat((String)user.get("state"), is(UserState.UNVERIFIED.getValue()));
		assertThat((List<String>)account.get("roles"), hasItem(Roles.NONVERIFIED_USER));

		result = userService.signin(email, "peter.novak", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		userService.signout();
		
		getMongoTemplate().getCollection(EntityUtils.USER).remove(new BasicDBObject("name", "Peter Novak"));
		getMongoTemplate().getCollection(EntityUtils.USERACCOUNT).remove(new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testActivateAccount_Succcess() throws Exception {
		String email = "peter.novak@test.com";
		
		DBObject data = BasicDBObjectBuilder.start()
				.add("email", email)
				.add("password", "peter.novak")
				.add("name", "Peter Novak")
				.get();
		DBObject result = userService.signup(data);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject account = userService.findDBUserAccountByEmail(email);
		assertNotNull(account);
			
		result = userService.activateAccount((String) account.get("activationUuid"));
		assertFalse(ErrorUtils.isError(result));
		
		DBObject user = getMongoTemplate().getCollection(EntityUtils.USER).findOne(new BasicDBObject("accountId", account.get(EntityUtils.ID)));
		account = userService.findDBUserAccountByEmail(email);
		
		assertThat((String)user.get("state"), is(UserState.ACTIVE.getValue()));
		assertThat((List<String>)account.get("roles"), hasItem(Roles.USER));
		assertThat((List<String>)account.get("roles"), not(hasItem(Roles.NONVERIFIED_USER)));
		assertNull(account.get("activationUuid"));
		
		
		userService.signout();
		
		getMongoTemplate().getCollection(EntityUtils.USER).remove(new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)));
		getMongoTemplate().getCollection(EntityUtils.USERACCOUNT).remove(new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testActivateAccount_CreateTreatmentReview() throws Exception {
		// sign in as nonverified user
		String email = "peter.novak20@test.com";
		assertFalse(ErrorUtils.isError(userService.signin(email, "peter.novak", "", false)));
		
		long reviewsCount = mongoTemplate.getDb().getCollection(EntityUtils.TREATMENT_REVIEW).count();
		
		DBObject review = new BasicDBObject();
		review.put("disease", new BasicDBObject("name", "Angina").toMap());
		review.put("treatment", new BasicDBObject("name", "Vitamin B6").toMap());
		review.put("rating", 0.45d);
		review.put("text", "createTreatmentReview_NonVerifiedUser");
		
		Map<String, Object> sideEffect = new HashMap<>();
		sideEffect.put("severity", 0.1d);
		sideEffect.put("sideEffect", new BasicDBObject("name", "Headache").toMap());
		review.put("sideEffects", Arrays.asList(sideEffect));
		
		DBObject result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject account = userService.findDBUserAccountByEmail(email);
		assertNotNull(account);
		assertNotNull(account.get("activationUuid"));
		
		result = userService.activateAccount((String) account.get("activationUuid"));
		assertFalse(ErrorUtils.isError(result));
		
		assertEquals(reviewsCount + 1, mongoTemplate.getDb().getCollection(EntityUtils.TREATMENT_REVIEW).count());
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testActivateAccount_Failure() throws Exception {

		DBObject result = userService.activateAccount("123");
		assertTrue(ErrorUtils.isError(result));
		assertEquals(ErrorCodes.UNKNOWN_ACCOUNT.getValue(), result.get(ErrorUtils.ATTR_CODE));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testSignUp_Diseases_Succcess() throws Exception {
		String email = "peter.novak@test.com";
		
		DBObject disaese = DiseaseServiceTest.getDiseases().get(0);
		
		List<DBObject> diseases = Arrays.asList(BasicDBObjectBuilder.start()
				.add(EntityUtils.ID, disaese.get(EntityUtils.ID))
				.add("name", disaese.get("name"))
				.get());
		
		DBObject data = BasicDBObjectBuilder.start()
				.add("email", email)
				.add("password", "peter.novak")
				.add("name", "Peter Novak")
				.add("diseases", diseases)
				.get();
		DBObject result = userService.signup(data);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject account = userService.findDBUserAccountByEmail(email);
		assertNotNull(account);
		
		DBObject user = getMongoTemplate().getCollection(EntityUtils.USER).findOne(new BasicDBObject("name", "Peter Novak"));
		assertNotNull(user);
		assertNotNull(user.get("diseases"));
		List<?> savedDiseases = (List<?>) user.get("diseases");
		assertEquals(diseases.size(), savedDiseases.size());

		result = userService.signin(email, "peter.novak", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		getMongoTemplate().getCollection(EntityUtils.USER).remove(new BasicDBObject("name", "Peter Novak"));
		getMongoTemplate().getCollection(EntityUtils.USERACCOUNT).remove(new BasicDBObject(EntityUtils.ID, account.get(EntityUtils.ID)));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testUpdateDiseases_Succcess() throws Exception {
		String email = "peter.novak2@test.com";
		
		DBObject disaese1 = DiseaseServiceTest.getDiseases().get(0);
		DBObject disaese2 = DiseaseServiceTest.getDiseases().get(1);
		
		DBObject userDisease1 = BasicDBObjectBuilder.start().add(EntityUtils.ID, disaese1.get(EntityUtils.ID)).add(EntityUtils.NAME, disaese1.get(EntityUtils.NAME)).get();
		DBObject userDisease2 = BasicDBObjectBuilder.start().add(EntityUtils.ID, disaese2.get(EntityUtils.ID)).add(EntityUtils.NAME, disaese2.get(EntityUtils.NAME)).get();
			
		DBObject result = userService.signin(email, "peter.novak", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject user = userService.findDBUserByEmail(email);
		assertThat(List.class.cast(user.get("diseases")).size(), is(1));
		
		result= userService.updateDiseases(Arrays.asList(userDisease1, userDisease2));
		assertFalse(ErrorUtils.isError(result));
		user = userService.findDBUserByEmail(email);
		assertThat(List.class.cast(user.get("diseases")).size(), is(2));
		
		result= userService.updateDiseases(Arrays.asList(userDisease1, userDisease2));
		assertFalse(ErrorUtils.isError(result));
		user = userService.findDBUserByEmail(email);
		assertThat(List.class.cast(user.get("diseases")).size(), is(2));
		
		result= userService.updateDiseases(Arrays.asList(userDisease1));
		assertFalse(ErrorUtils.isError(result));
		user = userService.findDBUserByEmail(email);
		assertThat(List.class.cast(user.get("diseases")).size(), is(1));
		
		result= userService.updateDiseases(null);
		assertFalse(ErrorUtils.isError(result));
		user = userService.findDBUserByEmail(email);
		assertThat(List.class.cast(user.get("diseases")).size(), is(0));
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testUpdateUserDiseases_Succcess() throws Exception {
		String email = "peter.novak4@test.com";
		DBObject result = userService.signin(email, "peter.novak", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject disaese1 = DiseaseServiceTest.getDiseases().get(5);
		DBObject disaese2 = DiseaseServiceTest.getDiseases().get(6);
		
		DBObject user = userService.findDBUserByEmail(email);
		
		List<DBObject> diseases = (List<DBObject>) user.get("diseases");
		assertThat(diseases.size(), is(0));
		
		List<Map<String, Object>> diseasesToUpdate = new ArrayList<Map<String, Object>>();
		
		DBObject userDisease1 = new BasicDBObject("disease", new BasicDBObject(EntityUtils.NAME, disaese1.get(EntityUtils.NAME)).toMap())
		.append("state", DiseaseState.IN_THERAPY.getValue())
		.append("treatmentHeardFrom", "internet");
		diseasesToUpdate.add(userDisease1.toMap());
		
		DBObject userDisease2 = new BasicDBObject("disease", new BasicDBObject(EntityUtils.NAME, disaese2.get(EntityUtils.NAME)).toMap())
		.append("state", DiseaseState.IN_THERAPY.getValue())
		.append("treatmentHeardFrom", "internet");
		diseasesToUpdate.add(userDisease2.toMap());
		
		result= userService.updateUser((String)user.get(EntityUtils.ID), new BasicDBObject("diseases", diseasesToUpdate));
		assertFalse(ErrorUtils.isError(result));
		user = userService.findDBUserByEmail(email);
		assertThat(List.class.cast(user.get("diseases")).size(), is(2));
		
		diseasesToUpdate.clear();
		diseasesToUpdate.add(userDisease2.toMap());
		
		result= userService.updateUser((String)user.get(EntityUtils.ID), new BasicDBObject("diseases", diseasesToUpdate));
		assertFalse(ErrorUtils.isError(result));
		user = userService.findDBUserByEmail(email);
		assertThat(List.class.cast(user.get("diseases")).size(), is(1));
		
		result= userService.updateUser((String)user.get(EntityUtils.ID), new BasicDBObject("diseases", null));
		assertFalse(ErrorUtils.isError(result));
		user = userService.findDBUserByEmail(email);
		assertThat(List.class.cast(user.get("diseases")).size(), is(0));
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserGender_Succcess() throws Exception {
		String email = "peter.novak3@test.com";
		DBObject result = userService.signin(email, "peter.novak", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject disease = diseaseService.findAllByQuery(new BasicDBObject("name", "across"), new PageRequest(0, 1)).get(0);
		DBObject treatment = treatmentService.findAllByQuery(new BasicDBObject("name", "be"), new PageRequest(0, 1)).get(0);
		String trsid = EntityUtils.createTreatmentReviewSummaryId(disease.get(EntityUtils.ID), treatment.get(EntityUtils.ID));
		DBObject trs = treatmentReviewSummaryService.findById(trsid);
		DBObject genderStatistics = (DBObject) trs.get("genderStatistics");
		int femaleCount = (int) genderStatistics.get(Gender.FEMALE.getValue());
		int maleCount = (int) genderStatistics.get(Gender.MALE.getValue());
		
		DBObject user = userService.findDBUserByEmail(email);
		assertEquals(Gender.FEMALE.getValue(), user.get("gender"));
		
		DBObject data = new BasicDBObject("gender", Gender.MALE.getValue());
		result= userService.updateUser((String)user.get(EntityUtils.ID), data);
		Thread.sleep(500);
		assertFalse(ErrorUtils.isError(result));
		user = userService.findDBUserByEmail(email);
		assertEquals(Gender.MALE.getValue(), user.get("gender"));
		
		trs = treatmentReviewSummaryService.findById(trsid);
		genderStatistics = (DBObject) trs.get("genderStatistics");
		assertEquals(femaleCount - 1, genderStatistics.get(Gender.FEMALE.getValue()));
		assertEquals(maleCount + 1, genderStatistics.get(Gender.MALE.getValue()));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserAge_Succcess() throws Exception {
		Thread.sleep(800);
		String email = "peter.novak4@test.com";
		DBObject result = userService.signin(email, "peter.novak", "", false);
		assertFalse(isError(result));
		
		DBObject user = userService.findDBUserByEmail(email);
		assertEquals(1981, user.get(User.ATTR_YEAR_OF_BIRTH));
		assertEquals(Gender.UNKNOWN.getValue(), user.get(User.ATTR_GENDER));
		
		DBObject disease = diseaseService.findAllByQuery(new BasicDBObject("name", "Angina"), new PageRequest(0, 1)).get(0);
		DBObject treatment = treatmentService.findAllByQuery(new BasicDBObject("name", "Vitamin C"), new PageRequest(0, 1)).get(0);
		
		DBObject review = new BasicDBObject();
		review.put("disease", disease.toMap());
		review.put("treatment", treatment.toMap());
		review.put("rating", 0.45d);
		review.put("text", "testUpdateUserAge_Succcess");
		
		result = treatmentReviewService.createTreatmentReview(review);
		assertFalse(isError(result));
		
		String trsid = EntityUtils.createTreatmentReviewSummaryId(disease.get(EntityUtils.ID), treatment.get(EntityUtils.ID));
		DBObject trs = treatmentReviewSummaryService.findById(trsid);
		assertNotNull(trs);
		BasicDBObject ageStatistics = (BasicDBObject)trs.get("ageStatistics");
		assertNotNull(ageStatistics.get(Gender.UNKNOWN.getValue()));
		
		boolean found = false;
		for (int i = 0; i < List.class.cast(ageStatistics.get(Gender.UNKNOWN.getValue())).size(); i++) {
			DBObject obj = (DBObject) List.class.cast(ageStatistics.get(Gender.UNKNOWN.getValue())).get(i);
			Number name = (Number) obj.get(EntityUtils.NAME);
			Number count = (Number) obj.get("count");
			if (name.intValue() == 1981) {
				assertEquals(1, count);
				found = true;
			}
		}

		assertTrue(found);
		
		
		DBObject data = new BasicDBObject("yearOfBirth", 1940);
		result= userService.updateUser((String)user.get(EntityUtils.ID), data);
		assertFalse(ErrorUtils.isError(result));
		Thread.sleep(600);
		
		trs = treatmentReviewSummaryService.findById(trsid);
		assertNotNull(trs);
		ageStatistics = (BasicDBObject) trs.get("ageStatistics");
		assertNotNull(ageStatistics.get(Gender.UNKNOWN.getValue()));
		
		boolean found1 = false;
		boolean found2 = false;
		for (int i = 0; i < List.class.cast(ageStatistics.get(Gender.UNKNOWN.getValue())).size(); i++) {
			DBObject obj = (DBObject) List.class.cast(ageStatistics.get(Gender.UNKNOWN.getValue())).get(i);
			Number name = (Number) obj.get(EntityUtils.NAME);
			Number count = (Number) obj.get("count");
			if (name.intValue() == 1981) {
				assertEquals(0, count);
				found1 = true;
			} else if (name.intValue() == 1940) {
				assertEquals(1, count);
				found2 = true;
			}
		}

		assertFalse(found1);
		assertTrue(found2);
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserName_Succcess() throws Exception {
		String testEmail = "peter.novak14@test.com";
		DBObject testUser = userService.findDBUserByEmail(testEmail);
		assertFalse(ErrorUtils.isError(this.userService.signin(testEmail, "peter.novak", "", false)));
		
		// do a vote
		DBObject user = userService.findDBUserByEmail("peter.novak17@test.com");
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(new BasicDBObject("author._id", user.get(EntityUtils.ID)));		
		DBObject result = this.treatmentReviewService.voteForTreatmentReview((String)review.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
		Thread.sleep(100);
		
		String newName = "TEST NAME";
		long count = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).count(new BasicDBObject("user.name", testUser.get(EntityUtils.NAME)));
		
		assertThat(count, greaterThan(0L));
		count = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).count(new BasicDBObject("user.name", newName));
		assertThat(count, is(0L));
		
		result = this.userService.updateUser((String)testUser.get(EntityUtils.ID), new BasicDBObject("name", newName));
		assertFalse(ErrorUtils.isError(result));
		count = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).count(new BasicDBObject("user.name", newName));
		assertThat(count, greaterThan(0L));
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testUnreadNotificationsCount() throws Exception {

		DBObject peterNovak14 = userService.findDBUserByEmail("peter.novak12@test.com");
		DBObject peterNovak17 = userService.findDBUserByEmail("peter.novak17@test.com");
		assertFalse(ErrorUtils.isError(this.userService.signin((String) peterNovak17.get("email"), "peter.novak", "", false)));
		
		long oldUnreadNotificationsCount = (long) userNotificationService.getUnreadNotificationsCount().get("count");
		
		this.userService.signout();
		assertFalse(ErrorUtils.isError(this.userService.signin((String) peterNovak14.get("email"), "peter.novak", "", false)));
		
		// do a vote
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(new BasicDBObject("author._id", peterNovak17.get(EntityUtils.ID)));		
		DBObject result = this.treatmentReviewService.voteForTreatmentReview((String)review.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
		result = this.treatmentReviewService.commentTreatmentReview((String)review.get(EntityUtils.ID), new BasicDBObject("text", "testNotifications"));
		assertFalse(ErrorUtils.isError(result));
		
		this.userService.signout();
				
		assertFalse(ErrorUtils.isError(this.userService.signin((String) peterNovak17.get("email"), "peter.novak", "", false)));
		
		long unreadNotificationsCount = (long) userNotificationService.getUnreadNotificationsCount().get("count");
		assertEquals(oldUnreadNotificationsCount + 1L, unreadNotificationsCount);
		
		List<DBObject> unreadNotifications = userNotificationService.getUnreadNotifications(new PageRequest(0, 20, Direction.DESC, "createdOn"));
		assertEquals(unreadNotificationsCount, unreadNotifications.size());
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testNotifications() throws Exception {

		DBObject peterNovak14 = userService.findDBUserByEmail("peter.novak14@test.com");
		DBObject peterNovak17 = userService.findDBUserByEmail("peter.novak17@test.com");
		assertFalse(ErrorUtils.isError(this.userService.signin((String) peterNovak17.get("email"), "peter.novak", "", false)));
		
		List<DBObject> notifications = userNotificationService.getNotifications(new PageRequest(0, 20, Direction.DESC, "createdOn"));
		long unreadNotificationsCount = (long) userNotificationService.getUnreadNotificationsCount().get("count");
		
		getMongoTemplate().getCollection(EntityUtils.USER).update(
				new BasicDBObject(EntityUtils.ID, peterNovak17.get(EntityUtils.ID)), new BasicDBObject("$unset", new BasicDBObject("lastUserNotificationsReadTimestamp", "")));
		
		this.userService.signout();
		assertFalse(ErrorUtils.isError(this.userService.signin((String) peterNovak14.get("email"), "peter.novak", "", false)));
		
		// do a vote
		DBObject review = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW).findOne(new BasicDBObject("author._id", peterNovak17.get(EntityUtils.ID)));		
		DBObject result = this.treatmentReviewService.voteForTreatmentReview((String)review.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
		result = this.treatmentReviewService.commentTreatmentReview((String)review.get(EntityUtils.ID), new BasicDBObject("text", "testNotifications"));
		assertFalse(ErrorUtils.isError(result));
		
		Thread.sleep(500);
		
		this.userService.signout();
		assertFalse(ErrorUtils.isError(this.userService.signin((String) peterNovak17.get("email"), "peter.novak", "", false)));
		
		long unreadNotificationsCount2 = (long) userNotificationService.getUnreadNotificationsCount().get("count");
		assertEquals(unreadNotificationsCount + 4, unreadNotificationsCount2);
		
		List<DBObject> notifications2 = userNotificationService.getNotifications(new PageRequest(0, 20, Direction.DESC, "createdOn"));
		assertEquals(notifications.size() + 2, notifications2.size());
		assertEquals(Boolean.TRUE, notifications2.get(0).get("unread"));
		unreadNotificationsCount = (long) userNotificationService.getUnreadNotificationsCount().get("count");
		assertEquals(0L, unreadNotificationsCount);
		
		notifications2 = userNotificationService.getNotifications(new PageRequest(0, 20, Direction.DESC, "createdOn"));
		assertEquals(notifications.size() + 2, notifications2.size());
		assertEquals(null, notifications2.get(0).get("unread"));
		
		Subject currentUser = org.apache.shiro.SecurityUtils.getSubject();
		DBObject user = (DBObject) currentUser.getPrincipal();
		
		user.put("lastUserNotificationsReadTimestamp", null);
		getMongoTemplate().getCollection(EntityUtils.USER).update(
				new BasicDBObject(EntityUtils.ID, peterNovak17.get(EntityUtils.ID)), new BasicDBObject("$unset", new BasicDBObject("lastUserNotificationsReadTimestamp", "")));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testUnreadNotifications() throws Exception {

		DBObject peterNovak14 = userService.findDBUserByEmail("peter.novak12@test.com");
		DBObject peterNovak17 = userService.findDBUserByEmail("peter.novak17@test.com");
		assertFalse(ErrorUtils.isError(this.userService.signin((String) peterNovak17.get("email"), "peter.novak", "", false)));
		
		List<DBObject> unreadNotifications = userNotificationService.getUnreadNotifications(new PageRequest(0, 20, Direction.DESC, "createdOn"));
		assertEquals(0, unreadNotifications.size());
		
		this.userService.signout();
		assertFalse(ErrorUtils.isError(this.userService.signin((String) peterNovak14.get("email"), "peter.novak", "", false)));
		
		// do a vote
		DBObject review = getMongoTemplate().getCollection(TreatmentReview.ENTITY_NAME).findOne(new BasicDBObject("author._id", peterNovak17.get(EntityUtils.ID)));
		DBObject result = this.treatmentReviewService.voteForTreatmentReview((String)review.get(EntityUtils.ID));
		assertFalse(ErrorUtils.isError(result));
		result = this.treatmentReviewService.commentTreatmentReview((String)review.get(EntityUtils.ID), new BasicDBObject("text", "testNotifications"));
		assertFalse(ErrorUtils.isError(result));
		
		this.userService.signout();
				
		assertFalse(ErrorUtils.isError(this.userService.signin((String) peterNovak17.get("email"), "peter.novak", "", false)));
		
		List<DBObject> newUnreadNotifications = userNotificationService.getUnreadNotifications(new PageRequest(0, 20, Direction.DESC, "createdOn"));
		assertEquals(unreadNotifications.size() + 2, newUnreadNotifications.size());
		
		unreadNotifications = userNotificationService.getUnreadNotifications(new PageRequest(0, 20, Direction.DESC, "createdOn"));
		assertEquals(0, unreadNotifications.size());
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserAvatarColor_Succcess() throws Exception {
		String email = "peter.novak14@test.com";
		DBObject testUser = userService.findDBUserByEmail(email);
		assertFalse(ErrorUtils.isError(this.userService.signin(email, "peter.novak", "", false)));
		
		DBObject user = userService.findDBUserByEmail(email);
		DBObject userData = new BasicDBObject();
		Map<String, Object> settings = new HashMap<>();
		Map<String, Object> profileData = new HashMap<>();
		userData.put("settings", settings);
		settings.put("profile", profileData);
		profileData.put("avatarColor", "ff00ff");
		user.put(email, profileData);
		
		DBObject result = this.userService.updateUser((String)testUser.get(EntityUtils.ID), userData);
		assertFalse(ErrorUtils.isError(result));
		
		user = userService.findDBUserByEmail(email);
		DBObject profile = UserUtils.getProfileSettings(user);
		assertEquals("ff00ff", profile.get("avatarColor"));
	}
	
	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateEmail_Succcess() throws Exception {
		String testEmail = "peter.novak13@test.com";
		DBObject testUser = userService.findDBUserByEmail(testEmail);
		assertFalse(ErrorUtils.isError(this.userService.signin(testEmail, "peter.novak", "", false)));
		
		assertThat((String)testUser.get("state"), not(is(UserState.UNVERIFIED.getValue())));
		assertThat((List<String>)testUser.get("roles"), not(hasItem(Roles.NONVERIFIED_USER)));
		
		String newEmail = "peter.polak@server.com";
		DBObject result = this.userService.updateEmail(newEmail);
		assertFalse(ErrorUtils.isError(result));
		
		DBObject account = userService.findDBUserAccountByEmail(newEmail);
		assertNotNull(account);
		
		DBObject user = getMongoTemplate().getCollection(EntityUtils.USER).findOne(new BasicDBObject("email", newEmail));
		assertNotNull(user);
		
		assertThat((String)user.get("state"), is(UserState.UNVERIFIED.getValue()));
		assertThat((List<String>)account.get("roles"), hasItem(Roles.NONVERIFIED_USER));
		
	}
	
	/**
	 * 
	 */
	@Test(expected = HealtPlatformException.class)
	public void testUpdatePrivacySettings_NotAuthenticatedFailure() {
		signout();
		userService.updatePrivacySettings(new HashMap<String, Object>());
		
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdatePrivacySettings_Success() {
		
		signout();
		String testEmail = "peter.novak14@test.com";
		assertFalse(ErrorUtils.isError(this.userService.signin(testEmail, "peter.novak", "", false)));
		
		DBObject testUser = userService.findDBUserByEmail(testEmail);
		assertThat((String)testUser.get("state"), not(is(UserState.UNVERIFIED.getValue())));
		assertThat((List<String>)testUser.get("roles"), not(hasItem(Roles.NONVERIFIED_USER)));
		
		DBObject account = userService.findDBUserAccountByEmail(testEmail);
		assertNotNull(account);
		DBObject privacySettings = UserUtils.getPrivacySettings(testUser);
		assertNotNull(privacySettings);
		DBObject emailNotification = (DBObject) privacySettings.get("emailNotification");
		assertNotNull(emailNotification);
		assertTrue((Boolean)emailNotification.get("weeklyUpdatesCommentsAndVotes"));
		assertTrue((Boolean)emailNotification.get("news_announcements"));
		
		emailNotification.put("weeklyUpdatesCommentsAndVotes", Boolean.FALSE);
		emailNotification.put("news_announcements", Boolean.FALSE);
		
		DBObject result = this.userService.updatePrivacySettings(privacySettings.toMap());
		assertFalse(ErrorUtils.isError(result));
		
		account = userService.findDBUserAccountByEmail(testEmail);
		assertNotNull(account);
		privacySettings = UserUtils.getPrivacySettings(testUser);
		assertNotNull(privacySettings);
		emailNotification = (DBObject) privacySettings.get("emailNotification");
		assertNotNull(emailNotification);
		assertFalse((Boolean)emailNotification.get("weeklyUpdatesCommentsAndVotes"));
		assertFalse((Boolean)emailNotification.get("news_announcements"));
		
		emailNotification.put("weeklyUpdatesCommentsAndVotes", Boolean.TRUE);
		emailNotification.put("news_announcements", Boolean.TRUE);
		result = this.userService.updatePrivacySettings(privacySettings.toMap());
		assertFalse(ErrorUtils.isError(result));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testFindUsersByDiseaseAndTreatment() throws Exception {
				
		List<DBObject> users = userService.findUsersByDiseaseAndTreatment(null, new PageRequest(0, 10));
		assertEquals(10, users.size());
		
		DBObject disease = diseaseService.findAllByQuery(new BasicDBObject("name", "across"), new PageRequest(0, 1)).get(0);
		DBObject treatment = treatmentService.findAllByQuery(new BasicDBObject("name", "be"), new PageRequest(0, 1)).get(0);
		
		users = userService.findUsersByDiseaseAndTreatment(new BasicDBObject("treatmentId", treatment.get(EntityUtils.ID)).append("diseaseId", disease.get(EntityUtils.ID)), new PageRequest(0, 10));
		assertFalse(users.isEmpty());
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void checkSession() throws Exception {
		this.userService.signout();
		DBObject sessioData = userService.checkSession();
		assertTrue(ErrorUtils.isError(sessioData));
		
		String email = "peter.novak3@test.com";
		DBObject result = this.userService.signin(email, "peter.novak", "", false);
		assertFalse(ErrorUtils.isError(result));
		
		sessioData = userService.checkSession();
		assertFalse(ErrorUtils.isError(sessioData));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testGenerateCaptcha_Succcess() throws Exception {
		String userEmail = "peter.novak14@test.com";
		
		DBObject account = userService.findDBUserAccountByEmail(userEmail);
		assertNotNull(account);
		assertEquals("", account.get("captcha_answer"));
		
		byte[] data = userService.generateCaptcha(userEmail);
		assertNotNull(data);
		assertTrue(data.length > 0);
		
		account = userService.findDBUserAccountByEmail(userEmail);
		assertNotNull(account);
		assertTrue(!"".equals(account.get("captcha_answer")));
	}
}
