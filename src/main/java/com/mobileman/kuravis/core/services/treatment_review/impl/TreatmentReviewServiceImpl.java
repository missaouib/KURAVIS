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
package com.mobileman.kuravis.core.services.treatment_review.impl;

import static com.mobileman.kuravis.core.domain.util.TreatmentReviewSummaryUtil.createTreatmentReviewSummarySideEffects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.mobileman.kuravis.core.domain.Entity;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewAttributes;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewEventType;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentCost;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentCostStatistics;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentDurationStatistics;
import com.mobileman.kuravis.core.domain.user.Gender;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.RoleUtils;
import com.mobileman.kuravis.core.domain.util.UserUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.event.EventService;
import com.mobileman.kuravis.core.services.fraud_report.FraudReportService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.services.treatment_side_effect.TreatmentSideEffectService;
import com.mobileman.kuravis.core.services.user.UserNotificationService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.services.util.CRUDAction;
import com.mobileman.kuravis.core.services.util.lock.LockManager;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException.DuplicateKey;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class TreatmentReviewServiceImpl extends AbstractEntityServiceImpl<TreatmentReview> implements TreatmentReviewService, TreatmentReviewAttributes {
	@Autowired
	private UserNotificationService userNotificationService;
	
	@Autowired
	private DiseaseService diseaseService;
	
	@Autowired
	private TreatmentService treatmentService;
	
	@Autowired
	private TreatmentSideEffectService sideEffectService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;
	
	@Autowired
	private TwitterTemplate twitterTemplate;
		
	@Autowired
	private MessageChannel reviewCreatedInChannel;
	
	@Autowired
	private FraudReportService fraudReportService;
	
	@Autowired
	private EventService eventService;
			
	@Autowired
	private LockManager lockManager;
	
	@Override
	protected String getEntityName() {
		return TreatmentReview.ENTITY_NAME;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#findAllByQuery(java.lang.String, com.mongodb.DBObject)
	 */
	@Override
	public List<DBObject> findAllByQuery(String entityName, DBObject query) {
		return super.findAllByQuery(getEntityName(), query);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#findAllByQuery(java.lang.String, com.mongodb.DBObject, org.springframework.data.domain.Pageable)
	 */
	@Override
	public List<DBObject> findAllByQuery(String entityName, DBObject query, Pageable page) {
		List<Order> orders = new ArrayList<>();
		if (page.getSort() != null) {
			Iterator<Order> orderIter = page.getSort().iterator();
			while (orderIter.hasNext()) {
				Order order = orderIter.next();
				if (order.getProperty().equals("rank")) {
					order = new Order(order.getDirection(), "rating");
				} else if (order.getProperty().equals("vote")) {
					order = new Order(order.getDirection(), "votesCount");
				} else if (order.getProperty().equals("last_update")) {
					order = new Order(order.getDirection(), "modifiedOn");
				}
				
				orders.add(order);
			}
			
		}
		
		final PageRequest newPage;
		if (orders.isEmpty()) {
			newPage = new PageRequest(page.getPageNumber(), page.getPageSize());
		} else {
			newPage = new PageRequest(page.getPageNumber(), page.getPageSize(), new Sort(orders));
		}
		
		if (query.containsField("treatment." + EntityUtils.NAME)) {
			String name = (String) query.get("treatment." + EntityUtils.NAME);		
			query.put("treatment." + EntityUtils.NAME, new BasicDBObject("$regex", "^" + name.toLowerCase() + "$").append("$options", "i"));
		}
		
		if (query.containsField("disease." + EntityUtils.NAME)) {
			String name = (String) query.get("disease." + EntityUtils.NAME);		
			query.put("disease." + EntityUtils.NAME, new BasicDBObject("$regex", "^" + name.toLowerCase() + "$").append("$options", "i"));
		}
		
		Set<String> usersId = new HashSet<String>();
		List<DBObject> result = super.findAllByQuery(getEntityName(), query, newPage);
		for (DBObject review : result) {
			String userId = EntityUtils.getEntityId(review.get("author"));
			usersId.add(userId);
		}
				
		if (!usersId.isEmpty()) {
			Map<String, DBObject> settingsMap = userService.findUsersData(usersId, "settings", User.ATTR_GENDER, User.ATTR_YEAR_OF_BIRTH);
			for (DBObject review : result) {
				DBObject author = (DBObject) review.get("author");
				String userId = EntityUtils.getEntityId(author);
				if (settingsMap.containsKey(userId)) {
					author.put("settings", settingsMap.get(userId).get("settings"));
					author.put(User.ATTR_GENDER, settingsMap.get(userId).get(User.ATTR_GENDER));
					author.put(User.ATTR_YEAR_OF_BIRTH, settingsMap.get(userId).get(User.ATTR_YEAR_OF_BIRTH));
				}
			}
		}
		
		return result;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService#createTreatmentReviewForSubscription(com.mongodb.DBObject)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DBObject createTreatmentReviewForSubscription(DBObject data) {
		if (data == null) {
			throw ErrorUtils.exception("data are nil", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		if (!data.containsField("treatmentReview")) {
			throw ErrorUtils.exception("treatmentReview is missing", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		if (!data.containsField("email")) {
			throw ErrorUtils.exception("email is missing", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		return createTempTreatmentReviewForSubscriber((Map<String, Object>) data.get("treatmentReview"), (String) data.get("email"));
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public DBObject createTreatmentReview(DBObject treatmentReview) {
		if (treatmentReview == null) {
			return ErrorUtils.error("treatmentReview si nil", ErrorCodes.INCORRECT_PARAMETER);
		}
		Subject userSubject = SecurityUtils.getSubject();
		if (userSubject == null || !userSubject.isAuthenticated()) {
			return ErrorUtils.error("Not authenticated: currentUser=" + userSubject, ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		DBObject loggedUser = (DBObject) userSubject.getPrincipal();
		
		DBObject disease = diseaseService.createOrFindByName((Map<String, Object>) treatmentReview.get(TreatmentReview.DISEASE));
		DBObject treatment = treatmentService.createOrFindByName((Map<String, Object>) treatmentReview.get(TreatmentReview.TREATMENT));
		
		if (this.reviewAlreadyExistsForUser((String) disease.get(EntityUtils.ID), (String) treatment.get(EntityUtils.ID))) {
			throw ErrorUtils.exception(ErrorCodes.REVIEW_ALREADY_EXISTS);
		}
		
		if (RoleUtils.isNonverifiedUser(loggedUser)) {
			return createTempTreatmentReview(treatmentReview, loggedUser);
		}
		
		final CRUDAction action = CRUDAction.CREATE;
		
		List<DBObject> trSideEffects = processSideEffectsOnCreateOrUpdate(treatmentReview);
		Number rating = processRatingOnCreateOrUpdate(treatmentReview);
		processUserOnCreateOrUpdate(treatmentReview, loggedUser);
		EntityUtils.setBasePropertiesOnCreate(treatmentReview);
		treatmentReview.put(TreatmentReview.DISEASE, disease);
		treatmentReview.put(TreatmentReview.TREATMENT, treatment);
		if (rating != null) {
			treatmentReview.put(TreatmentReview.RATING, rating);
		}
		
		DBObject reviewAuthor = null;
		if (!treatmentReview.containsField(TreatmentReview.AUTHOR)) {
			treatmentReview.put(TreatmentReview.AUTHOR, EntityUtils.createBaseUser(loggedUser));
		} else {
			reviewAuthor = userService.findById(EntityUtils.getEntityId(treatmentReview.get(TreatmentReview.AUTHOR)));
			treatmentReview.put(TreatmentReview.AUTHOR, EntityUtils.createBaseUser(reviewAuthor));
		}
		
		WriteResult wresult = getCollection().save(treatmentReview);
		if (ErrorUtils.isError(wresult)) {
			return ErrorUtils.error(wresult);
		}
		
		processTreatmentReviewSummary(treatmentReview, null, action, reviewAuthor != null ? reviewAuthor : loggedUser, disease, treatment, trSideEffects);
		
		getCollection(Disease.ENTITY_NAME).update(new BasicDBObject(EntityUtils.ID, disease.get(EntityUtils.ID)), 
				new BasicDBObject("$inc", new BasicDBObject(Disease.TREATMENT_REVIEWS_COUNT, 1)));
		getCollection(EntityUtils.USER).update(new BasicDBObject(EntityUtils.ID, loggedUser.get(EntityUtils.ID)), 
				new BasicDBObject("$inc", new BasicDBObject("invitationCount", 1)));
		
		
		try {
			Message<DBObject> twitterUpdate = new GenericMessage<DBObject>(treatmentReview);
			reviewCreatedInChannel.send(twitterUpdate);
		} catch (Exception e) {}
		
		this.eventService.createReviewEvent(treatmentReview);
		
		DBObject result = ErrorUtils.success();
		result.put(EntityUtils.ID, treatmentReview.get(EntityUtils.ID));
		return result;
	}

	/**
	 * @param treatmentReview
	 * @param user
	 */
	private void processUserOnCreateOrUpdate(DBObject treatmentReview, DBObject user) {
		DBObject newUserData = new BasicDBObject();
		for (String userProperty : new String[]{ User.ATTR_YEAR_OF_BIRTH, User.ATTR_GENDER }) {
			if (treatmentReview.containsField(userProperty)) {
				newUserData.put(userProperty, treatmentReview.get(userProperty));
			}
		}
		
		if (newUserData.toMap().size() > 0) {
			this.userService.updateUser((String) user.get(EntityUtils.ID), newUserData);
		}
		
		treatmentReview.removeField(User.ATTR_YEAR_OF_BIRTH);
		treatmentReview.removeField(User.ATTR_GENDER);
	}

	/**
	 * @param treatmentReview
	 * @return processed side effects
	 */
	@SuppressWarnings("unchecked")
	private List<DBObject> processSideEffectsOnCreateOrUpdate(DBObject treatmentReview) {
		List<DBObject> trSideEffects = new ArrayList<>();
		if (treatmentReview.get("sideEffects") == null) {
			treatmentReview.put("sideEffects", new ArrayList<>());
		} else {
			for (Map<String, Object> sideEffects : (List<Map<String, Object>>) treatmentReview.get("sideEffects")) {
				trSideEffects.add(new BasicDBObject(sideEffects));
			}
		}
		
		for (Map<String, Object> trSideEffect : (List<Map<String, Object>>)treatmentReview.get("sideEffects")) {
			Map<String, Object> sideEffect = (Map<String, Object>) trSideEffect.get("sideEffect");
			if (sideEffect != null && !sideEffect.isEmpty()) {
				if (sideEffect.get(EntityUtils.ID) == null) {
					DBObject dbSideEffect = sideEffectService.createOrFindByName(sideEffect);
					sideEffect.put(EntityUtils.ID, dbSideEffect.get(EntityUtils.ID));
				}
			}
		}
		return trSideEffects;
	}

	/**
	 * @param review
	 * @param user
	 * @param disease
	 * @param treatment
	 * @param trSideEffects
	 */
	private void processTreatmentReviewSummary(DBObject review, DBObject oldReview, CRUDAction action, DBObject user, DBObject disease, DBObject treatment,
			List<DBObject> trSideEffects) {
		
		final String trsid = EntityUtils.createTreatmentReviewSummaryId(disease.get(EntityUtils.ID), treatment.get(EntityUtils.ID));
		lockManager.lock(TreatmentReviewSummary.ENTITY_NAME, trsid);
		
		try {
			
			DBObject summary = findById(TreatmentReviewSummary.ENTITY_NAME, trsid);
			if (summary == null && !action.equals(CRUDAction.DELETE)) {
				String gender = UserUtils.getGender(user);
				summary = new BasicDBObject();
				summary.put(EntityUtils.ID, trsid);
				summary.put(EntityUtils.CREATED_ON, new Date());
				summary.put(EntityUtils.MODIFIED_ON, summary.get(EntityUtils.CREATED_ON));
				summary.put(TreatmentReviewSummary.DISEASE, disease);
				summary.put(TreatmentReviewSummary.TREATMENT, treatment);
				summary.put(TreatmentReviewSummary.REVIEWS_COUNT, 1);
				
				if (review.get(TreatmentReview.RATING) != null) {
					summary.put(TreatmentReviewSummary.RATINGS, Arrays.asList(new BasicDBObject(EntityUtils.NAME, review.get(TreatmentReview.RATING)).append("count", 1)));
					summary.put(TreatmentReviewSummary.RATING, review.get(TreatmentReview.RATING));
				}
				
				summary.put(TreatmentReviewSummary.SIDE_EFFECTS, createTreatmentReviewSummarySideEffects(trSideEffects, new HashMap<String, DBObject>()).values());
				summary.put(TreatmentReviewSummary.GENDER_STATISTICS, new BasicDBObject(gender, 1));
				
				try {
					WriteResult wresult = getCollection(TreatmentReviewSummary.ENTITY_NAME).insert(summary);
					if (!StringUtils.isEmpty(wresult.getError())) {
						throw ErrorUtils.exception(wresult);
					}
					
					// handle YEAR_OF_BIRTH statistics
					if (user.get(User.ATTR_YEAR_OF_BIRTH) != null) {
						
						DBObject filter = QueryBuilder.start()
								.put(EntityUtils.ID).is(trsid + user.get(User.ATTR_YEAR_OF_BIRTH) + gender)
								.put("summaryId").is(trsid)
								.and(User.ATTR_GENDER).is(gender)
								.and(EntityUtils.NAME).is(user.get(User.ATTR_YEAR_OF_BIRTH))
								.get();
						wresult = getCollection(EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS).update(filter, new BasicDBObject("$inc", new BasicDBObject("count", 1)), true, true);
						if (!StringUtils.isEmpty(wresult.getError())) {
							throw ErrorUtils.exception(wresult);
						}
					}
					
				} catch (DuplicateKey e) {
					summary = findById(TreatmentReviewSummary.ENTITY_NAME, trsid);
					updateTreatmentReviewSummary(summary, user, review, (String)disease.get(EntityUtils.ID), (String)treatment.get(EntityUtils.ID), action);
				}
				
			} else {
				updateTreatmentReviewSummary(summary, user, review, (String)disease.get(EntityUtils.ID), (String)treatment.get(EntityUtils.ID), action);
			}
			
			this.treatmentReviewSummaryService.computeTreatmentReviewSummaryStatistics(review, oldReview, action);
			
		} finally {
			lockManager.unlock(TreatmentReviewSummary.ENTITY_NAME, trsid);
		}
	}
	
	@Override
	public DBObject update(String type, String id, DBObject treatmentReview) {
		if (treatmentReview == null || treatmentReview.get(EntityUtils.ID) == null) {
			throw ErrorUtils.exception("treatmentReview si nil", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser == null || !currentUser.isAuthenticated()) {
			throw ErrorUtils.exception("Not authenticated: currentUser=" + currentUser, ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		
		DBObject user = (DBObject) currentUser.getPrincipal();
		if (RoleUtils.isNonverifiedUser(user)) {
			return createTempTreatmentReview(treatmentReview, user);
		}
		
		return update(treatmentReview, user);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService#update(com.mongodb.DBObject, com.mongodb.DBObject)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DBObject update(DBObject treatmentReview, DBObject author) {
		if (treatmentReview == null || treatmentReview.get(EntityUtils.ID) == null || author == null) {
			throw ErrorUtils.exception("treatmentReview si nil", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		String id = (String) treatmentReview.get(EntityUtils.ID);
		DBObject oldReview = findById(id);
		DBObject treatmentReviewOld = findById(id);
		if (treatmentReviewOld == null) {
			throw ErrorUtils.exception("Review does not exists with id: " + id, ErrorCodes.INTERNAL_ERROR);
		}
		
		if (!treatmentReview.containsField(TreatmentReview.DISEASE)) {
			throw ErrorUtils.exception("Disease is missing", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject disease = diseaseService.createOrFindByName((Map<String, Object>) treatmentReview.get("disease"));
		treatmentReview.put(TreatmentReview.DISEASE, disease);
		
		if (!treatmentReview.containsField(TreatmentReview.TREATMENT)) {
			throw ErrorUtils.exception("Treatment is missing", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject treatment = treatmentService.createOrFindByName((Map<String, Object>) treatmentReview.get(EntityUtils.TREATMENT));
		treatmentReview.put(TreatmentReview.TREATMENT, treatment);
				
		List<DBObject> trSideEffects = processSideEffectsOnCreateOrUpdate(treatmentReview);
		Number rating = processRatingOnCreateOrUpdate(treatmentReview);
		processUserOnCreateOrUpdate(treatmentReview, author);
		
		
		final DBObject setCommand = new BasicDBObject(TreatmentReview.DISEASE, disease)
			.append(TreatmentReview.SIDE_EFFECTS, treatmentReview.get(TreatmentReview.SIDE_EFFECTS))
			.append(TreatmentReview.TREATMENT, treatment)
			.append(TreatmentReview.MODIFIED_ON, new Date())
			.append(TreatmentReview.TEXT, treatmentReview.get(TreatmentReview.TEXT))
			.append(TreatmentReview.DATE_OF_FIRST_SYMPTOMS, treatmentReview.get(TreatmentReview.DATE_OF_FIRST_SYMPTOMS))
			.append(TreatmentReview.DATE_OF_DIAGNOSIS, treatmentReview.get(TreatmentReview.DATE_OF_DIAGNOSIS))
			.append(TreatmentReview.CURED, treatmentReview.get(TreatmentReview.CURED))
			.append(TreatmentReview.CURRENCY, treatmentReview.get(TreatmentReview.CURRENCY))
			.append(TreatmentReview.DOCTOR_COSTS, treatmentReview.get(TreatmentReview.DOCTOR_COSTS))
			.append(TreatmentReview.TREATMENT_PRICE, treatmentReview.get(TreatmentReview.TREATMENT_PRICE))
			.append(TreatmentReview.TREATMENT_QUANTITY, treatmentReview.get(TreatmentReview.TREATMENT_QUANTITY))
			.append(TreatmentReview.INSURANCE_COVERED, treatmentReview.get(TreatmentReview.INSURANCE_COVERED))
			.append(TreatmentReview.INSURANCE_COVERAGE, treatmentReview.get(TreatmentReview.INSURANCE_COVERAGE))
			.append(TreatmentReview.COINSURANCE, treatmentReview.get(TreatmentReview.COINSURANCE));
		
		final BasicDBObject command = new BasicDBObject();
		command.put("$set", setCommand);
		if (rating == null) {
			command.put("$unset", new BasicDBObject(TreatmentReview.RATING, ""));
		} else {
			setCommand.put(TreatmentReview.RATING, rating);
		}
		
		WriteResult result = getCollection().update(new BasicDBObject(EntityUtils.ID, id), command);
		if (ErrorUtils.isError(result)) {
			throw ErrorUtils.exception(result);
		}
				
		if (!EntityUtils.equals(treatmentReviewOld.get(EntityUtils.TREATMENT), treatment)
				|| !EntityUtils.equals(treatmentReviewOld.get("disease"), disease)) {
			DBObject diseaseOld = (DBObject) treatmentReviewOld.get("disease");
			DBObject treatmentOld = (DBObject) treatmentReviewOld.get(EntityUtils.TREATMENT);
			processTreatmentReviewSummary(treatmentReview, null, CRUDAction.DELETE, author, diseaseOld, treatmentOld, null);
		}
		
		processTreatmentReviewSummary(treatmentReview, oldReview, CRUDAction.UPDATE, author, disease, treatment, trSideEffects);
		
		return ErrorUtils.success();
	}

	/**
	 * @param treatmentReview
	 * @return normlized rating
	 */
	private Number processRatingOnCreateOrUpdate(DBObject treatmentReview) {
		Number rating = (Number) treatmentReview.get("rating");
		if (rating == null) {
			return null;
		}
		
		if (rating.doubleValue() > 1.0d) {
			rating = 1.0d;
		} else if (rating.doubleValue() < .0d) {
			rating = .0d;
		}
		return rating;
	}

	/**
	 * @param treatmentReview
	 * @param user
	 * @return success or error result
	 */
	@SuppressWarnings("unchecked")
	private DBObject createTempTreatmentReview(DBObject treatmentReview, DBObject user) {
		if (treatmentReview == null) {
			throw ErrorUtils.exception("treatmentReview si nil", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		Map<String, Object> disease = (Map<String, Object>) treatmentReview.get(TreatmentReview.DISEASE);
		Map<String, Object> treatment = (Map<String, Object>) treatmentReview.get(TreatmentReview.TREATMENT);
		
		DBObject filter = new BasicDBObject("disease._id", disease.get(Disease.ID)).append("treatment._id", treatment.get(Treatment.ID)).append("author._id", user.get(EntityUtils.ID));
		getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).remove(filter);
		
		treatmentReview.put("author", EntityUtils.createBaseUser(user));
		EntityUtils.setBasePropertiesOnCreate(treatmentReview);
		WriteResult result = getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).save(treatmentReview);
		if (!StringUtils.isEmpty(result.getError())) {
			throw ErrorUtils.exception(result);
		}
		
		DBObject resultObject = ErrorUtils.success();
		resultObject.put(EntityUtils.ID, treatmentReview.get(EntityUtils.ID));
		return resultObject;
	}
	
	/**
	 * @param treatmentReview
	 * @param subscriptionEmail
	 * @return success or error result
	 */
	@SuppressWarnings("unchecked")
	private DBObject createTempTreatmentReviewForSubscriber(Map<String, Object> treatmentReview, String subscriptionEmail) {
		if (treatmentReview == null) {
			throw ErrorUtils.exception("treatmentReview si nil", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		if (subscriptionEmail == null || subscriptionEmail.trim().length() == 0) {
			throw ErrorUtils.exception("subscriptionEmail si empty", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		Map<String, Object> disease = (Map<String, Object>) treatmentReview.get(TreatmentReview.DISEASE);
		Map<String, Object> treatment = (Map<String, Object>) treatmentReview.get(TreatmentReview.TREATMENT);
		DBObject filter = new BasicDBObject("disease._id", disease.get(Disease.ID)).append("treatment._id", treatment.get(Treatment.ID)).append("subscription.email", subscriptionEmail);
		getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).remove(filter);
		
		DBObject review = new BasicDBObject(treatmentReview);
		review.put("subscription", new BasicDBObject("email", subscriptionEmail));
		EntityUtils.setBasePropertiesOnCreate(review);
		WriteResult result = getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).save(review);
		if (!StringUtils.isEmpty(result.getError())) {
			throw ErrorUtils.exception(result);
		}
		
		DBObject resultObject = ErrorUtils.success();
		resultObject.put(EntityUtils.ID, review.get(EntityUtils.ID));
		return resultObject;
	}

	/**
	 * Updates existing treatment review summary
	 * @param treatmentReviewSummary
	 * @param review
	 * @param reviewAction
	 * @return error string in case error, null otherwise
	 */
	@SuppressWarnings("unchecked")
	private DBObject updateTreatmentReviewSummary(DBObject treatmentReviewSummary, DBObject user, DBObject review, String diseaseId, String treatmentId, CRUDAction reviewAction) {
		if (treatmentReviewSummary == null) {
			return null;
		}
		
		String gender = UserUtils.getGender(user);
		String trsid = (String) treatmentReviewSummary.get(EntityUtils.ID);
		
		DBCursor trCursor = findAllTreatmentReviews(diseaseId, treatmentId, 
				TreatmentReview.RATING, 
				TreatmentReview.SIDE_EFFECTS,
				//
				TreatmentReview.CURRENCY,
				TreatmentReview.DOCTOR_COSTS,
				TreatmentReview.TREATMENT_PRICE,
				TreatmentReview.TREATMENT_QUANTITY,
				TreatmentReview.INSURANCE_COVERED,
				TreatmentReview.INSURANCE_COVERAGE,
				TreatmentReview.COINSURANCE);
		
		// go through all reviews and count them based on their value (group it) + add rating of given review
		// go through all side effects and count them based on side effect name (group it) + add side effects of given review
		Map<Number, DBObject> ratingsCounts = new HashMap<Number, DBObject>();
		Map<String, DBObject> trsSideEffects = new HashMap<String, DBObject>();
		
		int reviewsCount = 0;
		int ratingsCount = 0;
		BigDecimal ratingSum = BigDecimal.ZERO;
		
		List<TreatmentCost> treatmentCosts = new ArrayList<TreatmentCost>();
		
		for (DBObject tmpReview : trCursor) {
			if (CRUDAction.DELETE.equals(reviewAction) && String.class.cast(review.get(ID)).equalsIgnoreCase((String)tmpReview.get(EntityUtils.ID))) {
				continue;
			}
			
			TreatmentCost treatmentCost = TreatmentCost.createFromReview(tmpReview);
			if (treatmentCost != null) {
				treatmentCosts.add(treatmentCost);
			}
			
			reviewsCount++;
			Number trRating = (Number) tmpReview.get(TreatmentReview.RATING);
			if (trRating != null) {
				++ratingsCount;
				
				ratingSum = ratingSum.add(new BigDecimal(trRating.doubleValue()));
				
				if (!ratingsCounts.containsKey(trRating)) {
					ratingsCounts.put(trRating, new BasicDBObject(EntityUtils.NAME, trRating).append("count", 1));
				} else {
					incrementCount(ratingsCounts.get(trRating));
				}
			}
			
			List<DBObject> trSideEffects = (List<DBObject>) tmpReview.get(TreatmentReview.SIDE_EFFECTS);
			if (trSideEffects != null) {
				createTreatmentReviewSummarySideEffects(trSideEffects, trsSideEffects);
			}
		}
		
		BigDecimal ratingAvg = null;
		if (ratingsCount > 0) {
			ratingAvg = ratingSum.divide(new BigDecimal(ratingsCount), 5, BigDecimal.ROUND_HALF_EVEN);
		}
				
		if (reviewsCount == 0 && CRUDAction.DELETE.equals(reviewAction)) {
			// no reviews then remove summary
			getCollection(TreatmentReviewSummary.ENTITY_NAME).remove(new BasicDBObject(EntityUtils.ID, trsid));
			getCollection(EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS).remove(new BasicDBObject("summaryId", trsid));
			getCollection(TreatmentCostStatistics.ENTITY_NAME).remove(new BasicDBObject(TreatmentCostStatistics.SUMMARY_ID, trsid));
			getCollection(TreatmentDurationStatistics.ENTITY_NAME).remove(new BasicDBObject(TreatmentCostStatistics.SUMMARY_ID, trsid));
		} else {
			DBObject setCommand = new BasicDBObject()
				.append(TreatmentReviewSummary.RATINGS, new ArrayList<>(ratingsCounts.values()))
				.append(TreatmentReviewSummary.SIDE_EFFECTS, new ArrayList<>(trsSideEffects.values()))
				.append(EntityUtils.MODIFIED_ON, new Date());
			BasicDBObject command = new BasicDBObject("$set", setCommand);
			
			if (ratingAvg != null) {
				setCommand.put(TreatmentReviewSummary.RATING, ratingAvg.doubleValue());
			} else {
				command.put("$unset", new BasicDBObject(TreatmentReviewSummary.RATING, ""));
			}
			
			int statsIncrement = 0;
			switch (reviewAction) {
			case CREATE:
				command.put("$unset", new BasicDBObject(TreatmentReviewSummary.SUGGESTION, ""));
				statsIncrement = 1;
				break;
			case DELETE:
				statsIncrement = -1;
				break;
			default:
				break;
			}
			
			if (statsIncrement != 0) {
				BasicDBObject incCommand = new BasicDBObject("reviewsCount", statsIncrement);
				if (user.get(User.ATTR_GENDER) != null) {
					incCommand.append("genderStatistics." + gender, statsIncrement);
				} else {
					incCommand.append("genderStatistics." + Gender.UNKNOWN.getValue(), statsIncrement);
				}
				
				command.append("$inc", incCommand);
			}
				
			getCollection(TreatmentReviewSummary.ENTITY_NAME).update(new BasicDBObject(EntityUtils.ID, trsid), command);
			
			for (Gender tmpgender : Gender.values()) {
				Query query = Query.query(Criteria.where("genderStatistics." + tmpgender.getValue()).lt(0));
				getCollection(TreatmentReviewSummary.ENTITY_NAME).update(query.getQueryObject(), new BasicDBObject("$set", new BasicDBObject("genderStatistics." + tmpgender.getValue(), 0)));
			}
						
			// handle YEAR_OF_BIRTH statistics - only in case CREATE or DELETE review
			if (statsIncrement != 0 && user.get(User.ATTR_YEAR_OF_BIRTH) != null) {
				boolean upsert = statsIncrement > 0;
				
				DBObject filter = QueryBuilder.start()
						.put(EntityUtils.ID).is(trsid + user.get(User.ATTR_YEAR_OF_BIRTH) + gender)
						.put("summaryId").is(trsid)
						.and(User.ATTR_GENDER).is(gender)
						.and(EntityUtils.NAME).is(user.get(User.ATTR_YEAR_OF_BIRTH))
						.get();
				
				command = new BasicDBObject("$inc", new BasicDBObject("count", statsIncrement));
				getCollection(EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS).update(filter, command, upsert, false);
			}
		}
		
		return null;
	}

	private void incrementCount(DBObject count) {
		int newCount = Number.class.cast(count.get("count")).intValue() + 1;
		count.put("count", newCount);
	}

	@Override
	public DBCursor findAllTreatmentReviews(String diseaseId, String treatmentId, String... projections) {
		DBCursor cursor = null;
		if (projections != null) {
			BasicDBObject projs = new BasicDBObject();
			for (String property : projections) {
				projs.append(property, true);
			}
			
			cursor = getCollection(ENTITY_NAME).find(
					new BasicDBObject("treatment._id", treatmentId).append("disease._id", diseaseId), projs);
		} else {
			cursor = getCollection(ENTITY_NAME).find(
					new BasicDBObject("treatment._id", treatmentId).append("disease._id", diseaseId));
		}
		
		
		return cursor;
	}
	
	@Override
	public DBObject voteForTreatmentReview(String entityId) {
		if (StringUtils.isEmpty(entityId)) {
			return ErrorUtils.error("Undefine entity ID", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser == null || !currentUser.isAuthenticated()) {
			return ErrorUtils.error("Not authenticated: currentUser=" + currentUser, ErrorCodes.USER_NOT_AUTHENTICATED);
		}
				
		DBObject review = getCollection().findOne(new BasicDBObject(EntityUtils.ID, entityId));
		if (review == null) {
			return ErrorUtils.error("Specified entity type does not exists: " + entityId, ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject user = (DBObject) currentUser.getPrincipal();
		if (user == null) {
			return ErrorUtils.error("Not authenticated: currentUser=" + currentUser, ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		
		if (RoleUtils.isNonverifiedUser(user)) {
			return ErrorUtils.error("Not authorized: user=" + user, ErrorCodes.UNAUTHORIZED);
		}

		DBObject atributesToUpdate = null;
		String treatmentReviewVoteId = EntityUtils.createTreatmentReviewVoteId(review.get(EntityUtils.ID), user.get(EntityUtils.ID));
		DBObject treatmentReviewVote = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).findOne(new BasicDBObject(EntityUtils.ID, treatmentReviewVoteId));
		
		// already voted - unvote
		if (treatmentReviewVote != null) {
			getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).remove(new BasicDBObject(EntityUtils.ID, treatmentReviewVoteId));
			atributesToUpdate = new BasicDBObject("$inc", new BasicDBObject("votesCount", -1))
				.append("$set", new BasicDBObject(EntityUtils.MODIFIED_ON, new Date()));
			getCollection(EntityUtils.USER).update(
					new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), new BasicDBObject("$inc", new BasicDBObject("statistics.votesCount", -1)));
		} else {
			// vote
			treatmentReviewVote = EntityUtils.createTreatmentReviewEvent(review, treatmentReviewVoteId, TreatmentReviewEventType.VOTE, user);
			getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).save(treatmentReviewVote);
			atributesToUpdate = new BasicDBObject("$inc", new BasicDBObject("votesCount", 1))
				.append("$set", new BasicDBObject("lastVotedOn", new Date()).append(EntityUtils.MODIFIED_ON, new Date()));
			getCollection(EntityUtils.USER).update(
					new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), new BasicDBObject("$inc", new BasicDBObject("statistics.votesCount", 1)));
			this.userNotificationService.createNotificationForTreatmentReviewVote((DBObject) review.get("author"), treatmentReviewVote);
			this.eventService.createVoteEvent(review, treatmentReviewVote);
		}
		
		WriteResult updateResult = getCollection().update(
				new BasicDBObject(EntityUtils.ID, entityId), atributesToUpdate, false, false, WriteConcern.SAFE);
		if (ErrorUtils.isError(updateResult)) {
			return ErrorUtils.error(updateResult);
		}
		
		DBCursor cursor = getMongoTemplate().getCollection(ENTITY_NAME).find(new BasicDBObject(EntityUtils.ID, entityId), new BasicDBObject("votesCount", 1));
		Number votesCount = (Number) review.get("votesCount");
		if (cursor.hasNext()) {
			DBObject obj = cursor.next();
			votesCount = (Number) obj.get("votesCount");
		}
		
		if (votesCount == null) {
			votesCount = 1;
		}
		
		DBObject result = ErrorUtils.success();
		result.put("count", votesCount.intValue());
		return result;
	}
	
	private DBObject delete(DBObject treatmentReview) {
		if (treatmentReview == null) {
			DBObject error = ErrorUtils.error("Not Exists");
			return error;
		}
		
		DBObject author = (DBObject)treatmentReview.get(TreatmentReview.AUTHOR);
		String diseaseId = (String) DBObject.class.cast(treatmentReview.get(TreatmentReview.DISEASE)).get(EntityUtils.ID);
		String treatmentId = (String) DBObject.class.cast(treatmentReview.get(TreatmentReview.TREATMENT)).get(EntityUtils.ID);
		
		
		final String trsid = EntityUtils.createTreatmentReviewSummaryId(diseaseId, treatmentId);
		lockManager.lock(TreatmentReviewSummary.ENTITY_NAME, trsid);
		
		DBObject error = super.delete((String) treatmentReview.get(EntityUtils.ID));
		if (ErrorUtils.isError(error)) {
			return error;
		}
		
		getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).remove(new BasicDBObject("treatmentReviewId", treatmentReview.get(EntityUtils.ID)));
		userNotificationService.deleteAllUserNotificationsForTreatmentReview(treatmentReview);
		
		// delete all report items pointing to this review
		this.fraudReportService.deleteFraudReportsForEntity(ENTITY_NAME, (String) treatmentReview.get(EntityUtils.ID));
		
		getCollection(Disease.ENTITY_NAME).update(new BasicDBObject(EntityUtils.ID, diseaseId), 
				new BasicDBObject("$inc", new BasicDBObject(Disease.TREATMENT_REVIEWS_COUNT, -1)));
		
		try {
			DBObject treatmentReviewSummary = treatmentReviewSummaryService.findById(trsid);
			this.treatmentReviewSummaryService.computeTreatmentReviewSummaryStatistics(treatmentReview, null, CRUDAction.DELETE);
			updateTreatmentReviewSummary(treatmentReviewSummary, author, treatmentReview, diseaseId, treatmentId, CRUDAction.DELETE);
			
			eventService.deleteAllReviewEvents(EntityUtils.getEntityId(treatmentReview), diseaseId, treatmentId, EntityUtils.getEntityId(author));
		} finally {
			lockManager.unlock(TreatmentReviewSummary.ENTITY_NAME, trsid);
		}
		
		DBObject disease = this.diseaseService.findById((String) DBObject.class.cast(treatmentReview.get(TreatmentReview.DISEASE)).get(EntityUtils.ID));
		if (disease != null && EntityUtils.equals((DBObject)disease.get(Disease.USER), author)) {
			if (diseaseService.diseaseHasNoOtherReviews(diseaseId, (String)treatmentReview.get(EntityUtils.ID))) {
				this.diseaseService.delete((String) diseaseId);
			}
			
			if (treatmentService.treatmentHasNoOtherReviews(treatmentId, (String)treatmentReview.get(EntityUtils.ID))) {
				this.treatmentService.delete((String) treatmentId);
			}
		}
		
		return ErrorUtils.success();
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService#delete(java.lang.String)
	 */
	@Override
	public DBObject delete(String entityId) {
		DBObject treatmentReview = findById(entityId);
		return delete(treatmentReview);
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService#deleteAllTreatmentReviews(java.lang.String)
	 */
	@Override
	public DBObject deleteAllTreatmentReviews(String userId) {
		if (StringUtils.isEmpty(userId)) {
			return ErrorUtils.error("Undefine user ID", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBCursor cursor = getMongoTemplate().getCollection(ENTITY_NAME).find(new BasicDBObject("author._id", userId), 
				new BasicDBObject("disease", 1).append(EntityUtils.TREATMENT, 1).append("author", 1));
		for (DBObject review : cursor.toArray()) {
			delete(review);
		}
		
		return ErrorUtils.success();
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService#commentTreatmentReview(java.lang.String, DBObject)
	 */
	@Override
	public DBObject commentTreatmentReview(String entityId, DBObject data) {
		if (StringUtils.isEmpty(entityId)) {
			return ErrorUtils.error("Undefine entity ID", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		if (data == null) {
			return ErrorUtils.error("Data missing", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		String text = (String) data.get("text");
		if (StringUtils.isEmpty(text)) {
			return ErrorUtils.error("Text missing", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		Subject subject = SecurityUtils.getSubject();
		if (subject == null || !subject.isAuthenticated()) {
			return ErrorUtils.error("Not authenticated: subject=" + subject, ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		
		DBObject user = (DBObject) subject.getPrincipal();
		if (user == null) {
			return ErrorUtils.error("Not authenticated: subject=" + subject, ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		
		if (RoleUtils.isNonverifiedUser(user)) {
			return ErrorUtils.error("Not authenticated: subject=" + subject, ErrorCodes.UNAUTHORIZED);
		}
		
		DBObject review = getCollection().findOne(new BasicDBObject(EntityUtils.ID, entityId));
		if (review == null) {
			return ErrorUtils.error("Specified entity type does not exists: " + entityId, ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject comment = EntityUtils.createTreatmentReviewEvent(review, TreatmentReviewEventType.COMMENT, user);
		comment.put("text", data.get("text"));
		
		getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).save(comment);
		
		getMongoTemplate().getCollection(EntityUtils.USER).update(
				new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), new BasicDBObject("$inc", new BasicDBObject("statistics.commentsCount", 1)));
		
		this.userNotificationService.createNotificationForTreatmentReviewComment((DBObject) review.get("author"), comment);
				
		WriteResult updateResult = getMongoTemplate().getCollection(ENTITY_NAME).update(
				new BasicDBObject(EntityUtils.ID, entityId),  
				new BasicDBObject("$set", new BasicDBObject("lastCommentedOn", new Date()).append(EntityUtils.MODIFIED_ON, new Date()))
					.append("$inc", new BasicDBObject("reviewCommentsCount", 1)));
		if (ErrorUtils.isError(updateResult)) {
			return ErrorUtils.error(updateResult);
		}
		
		DBObject result = ErrorUtils.success();
		return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#findById(java.lang.String, java.lang.String)
	 */
	@Override
	public DBObject findById(String entityName, String id) {
		DBObject review = super.findById(entityName, id);
		if (review != null && review.containsField("author")) {
			DBObject author = (DBObject) review.get("author");
			DBObject data = userService.findUsersData(Arrays.<String>asList(EntityUtils.getEntityId(author)), 
					User.SETTINGS, User.ATTR_GENDER, User.ATTR_YEAR_OF_BIRTH)
					.get(EntityUtils.getEntityId(author));
			author.put("settings", data.get("settings"));
			author.put(User.ATTR_GENDER, data.get(User.ATTR_GENDER));
			author.put(User.ATTR_YEAR_OF_BIRTH, data.get(User.ATTR_YEAR_OF_BIRTH));
		}
		
		return review;
	}
	
	public DBObject findByIdForUser(String id) {
		if (StringUtils.isEmpty(id)) {
			return ErrorUtils.error("Undefine entity ID", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser == null || !currentUser.isAuthenticated()) {
			return ErrorUtils.error("Not authenticated: currentUser=" + currentUser, ErrorCodes.USER_NOT_AUTHENTICATED);
		}
				
		DBObject review = getMongoTemplate().getCollection(ENTITY_NAME).findOne(new BasicDBObject(EntityUtils.ID, id));
		if (review == null) {
			return ErrorUtils.error("Specified entity type does not exists: " + id, ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject user = (DBObject) currentUser.getPrincipal();
		if (user == null) {
			return ErrorUtils.error("Not authenticated: currentUser=" + currentUser, ErrorCodes.UNAUTHORIZED);
		}

		
		String treatmentReviewVoteId = EntityUtils.createTreatmentReviewVoteId(id, user.get(EntityUtils.ID));
		long votesCount = getMongoTemplate().getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).count(new BasicDBObject(EntityUtils.ID, treatmentReviewVoteId));
		review.put("voted", Boolean.valueOf(votesCount > 0));
		
		return review;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService#updateTretmentReviewStatistics(com.mongodb.DBObject, com.mongodb.DBObject)
	 */
	@Override
	public void updateTretmentReviewStatistics(DBObject newData, DBObject oldData) {
		// if gender differs
		String prevGender = UserUtils.getGender(oldData);
		String newGender = newData.containsField(User.ATTR_GENDER) ? (String) newData.get(User.ATTR_GENDER) : null;
		
		Number prevYearOfBirth = oldData.containsField(User.ATTR_YEAR_OF_BIRTH) ? (Number) oldData.get(User.ATTR_YEAR_OF_BIRTH) : null;
		Number newYearOfBirth = newData.containsField(User.ATTR_YEAR_OF_BIRTH) ? (Number) newData.get(User.ATTR_YEAR_OF_BIRTH) : null;
		
		if (newGender == null && newYearOfBirth == null) {
			return;
		}
		
		boolean genderChanged = newGender != null && !ObjectUtils.nullSafeEquals(prevGender, newGender);
		boolean yearOfBirthChanged = newYearOfBirth != null && !ObjectUtils.nullSafeEquals(prevYearOfBirth, newYearOfBirth);
		
		DBCursor curReviews = getCollection(ENTITY_NAME).find(new BasicDBObject("author._id", oldData.get(EntityUtils.ID)), new BasicDBObject("disease._id", 1).append("treatment._id", 1));
		Set<String> summariesId = new HashSet<String>(); 
		for (DBObject data : curReviews) {
			DBObject disease = (DBObject) data.get("disease");
			DBObject treatment = (DBObject) data.get(EntityUtils.TREATMENT);
			String summaryId = EntityUtils.createTreatmentReviewSummaryId(disease.get(EntityUtils.ID), treatment.get(EntityUtils.ID));
			summariesId.add(summaryId);
		}
		
		
		if (!summariesId.isEmpty() && genderChanged) {
			DBObject filter = QueryBuilder.start().put(EntityUtils.ID).in(summariesId).get();
			getCollection(TreatmentReviewSummary.ENTITY_NAME).update(filter, new BasicDBObject("$inc", new BasicDBObject("genderStatistics." + prevGender, -1)), false, true);
			getCollection(TreatmentReviewSummary.ENTITY_NAME).update(filter, new BasicDBObject("$inc", new BasicDBObject("genderStatistics." + newGender, 1)), true, true);
			
			for (Gender gender : Gender.values()) {
				filter = QueryBuilder.start().put("genderStatistics." + gender.getValue()).lessThan(0).get();
				getCollection(TreatmentReviewSummary.ENTITY_NAME).update(filter, new BasicDBObject("$set", new BasicDBObject("genderStatistics." +  gender.getValue(), 0)), false, true);
			}
			
		}
		
		// if yearOfBirth differs
		if (yearOfBirthChanged || genderChanged) {
			// decrement old group if user has already entered YEAR_OF_BIRTH
			List<DBObject> oldStatfilters = new ArrayList<DBObject>();
			List<DBObject> newStatfilters = new ArrayList<DBObject>();
			if (yearOfBirthChanged && genderChanged) {
				
				for (String summaryId : summariesId) {
					
					if (prevYearOfBirth != null) {
						DBObject filter = QueryBuilder.start()
								.put(EntityUtils.ID).is(summaryId + prevYearOfBirth.intValue() + prevGender)
								.put("summaryId").is(summaryId)
								.put(User.ATTR_GENDER).is(prevGender)
								.and(EntityUtils.NAME).is(prevYearOfBirth)
								.get();
						oldStatfilters.add(filter);
					}
					
					DBObject filter = QueryBuilder.start()
							.put(EntityUtils.ID).is(summaryId + newYearOfBirth.intValue() + newGender)
							.put("summaryId").is(summaryId)
							.put(User.ATTR_GENDER).is(newGender)
							.and(EntityUtils.NAME).is(newYearOfBirth)
							.get();
					newStatfilters.add(filter);
				}
				
			} else if (yearOfBirthChanged) {
				
				for (String summaryId : summariesId) {
					if (prevYearOfBirth != null) {
						DBObject filter = QueryBuilder.start()
								.put(EntityUtils.ID).is(summaryId + prevYearOfBirth.intValue() + prevGender)
								.put("summaryId").is(summaryId)
								.put(User.ATTR_GENDER).is(prevGender)
								.and(EntityUtils.NAME).is(prevYearOfBirth)
								.get();
						oldStatfilters.add(filter);
					}
					
					
					DBObject filter = QueryBuilder.start()
							.put(EntityUtils.ID).is(summaryId + newYearOfBirth.intValue() + prevGender)
							.put("summaryId").is(summaryId)
							.put(User.ATTR_GENDER).is(prevGender)
							.and(EntityUtils.NAME).is(newYearOfBirth)
							.get();
					newStatfilters.add(filter);
				}
			} else if (genderChanged && prevYearOfBirth != null) { // genderChanged
				for (String summaryId : summariesId) {
					if (prevGender != null) {
						DBObject filter = QueryBuilder.start()
								.put(EntityUtils.ID).is(summaryId + prevYearOfBirth.intValue() + prevGender)
								.put("summaryId").is(summaryId)
								.put(User.ATTR_GENDER).is(prevGender)
								.and(EntityUtils.NAME).is(prevYearOfBirth)
								.get();
						oldStatfilters.add(filter);
					}					
					
					DBObject filter = QueryBuilder.start()
							.put(EntityUtils.ID).is(summaryId + prevYearOfBirth.intValue() + newGender)
							.put("summaryId").is(summaryId)
							.put(User.ATTR_GENDER).is(newGender)
							.and(EntityUtils.NAME).is(prevYearOfBirth)
							.get();
					newStatfilters.add(filter);
				}
			}
			
			for (DBObject filter : oldStatfilters) {
				getCollection(EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS).update(filter, new BasicDBObject("$inc", new BasicDBObject("count", -1)), false, true);
			}

			for (DBObject filter : newStatfilters) {
				getCollection(EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS).update(filter, new BasicDBObject("$inc", new BasicDBObject("count", 1)), true, true);
			}
			
			// remove old zero stats
			getMongoTemplate().remove(Query.query(Criteria.where("count").lte(0)), EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS);
		}
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService#reviewAlreadyExistsForUser(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean reviewAlreadyExistsForUser(String diseaseId, String treatmentId) {
		if (StringUtils.isEmpty(diseaseId) || StringUtils.isEmpty(treatmentId)) {
			return false;
		}
		
		DBObject user = UserUtils.getLoggedUser();
		if (user == null) {
			return false;
		}
		
		boolean exists = count(new BasicDBObject("disease._id", diseaseId).append("treatment._id", treatmentId).append("author._id", user.get("_id"))) > 0;
		return exists;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService#deleteTreatmentReviewComment(java.lang.String)
	 */
	@Override
	public void deleteTreatmentReviewComment(String commentId) {
		
		// fetch and delete comment
		DBObject commentFilter = new BasicDBObject(EntityUtils.ID, commentId);
		DBObject comment = getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).findOne(commentFilter);
		if (comment == null) {
			return;
		}
		
		getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).remove(commentFilter);
		// decrement count of comments on review
		String reviewId = (String) comment.get("treatmentReviewId");
		DBObject user = (DBObject) comment.get("user");
		getCollection().update(new BasicDBObject(EntityUtils.ID, reviewId), new BasicDBObject("$inc", new BasicDBObject("reviewCommentsCount", -1)));
	
		// decrement user statistics - count of comments on review
		getMongoTemplate().getCollection(EntityUtils.USER).update(
				new BasicDBObject(EntityUtils.ID, user.get(EntityUtils.ID)), new BasicDBObject("$inc", new BasicDBObject("statistics.commentsCount", -1)));
		
		// delete all report items pointing to this comment
		this.fraudReportService.deleteFraudReportsForEntity(EntityUtils.TREATMENT_REVIEW_EVENT, commentId);
	}

	@Override
	public List<DBObject> findUserReviewsGoupByDisease(String userId) {
		Query query = Query.query(Criteria.where(TreatmentReview.AUTHOR + "." + TreatmentReview.ID).is(userId));
		Sort sort = new Sort(TreatmentReview.CURED, TreatmentReview.DISEASE, TreatmentReview.TREATMENT);
		query.with(sort);
		List<DBObject> result = new ArrayList<>();
		Map<String, List<DBObject>> diseaseIdReviewsMap = new LinkedHashMap<>(); 
		List<DBObject> reviews = findAllByQuery(query.getQueryObject(), new PageRequest(0, 200, sort));
		for (DBObject review : reviews) {
			DBObject disease = (DBObject) review.get(TreatmentReview.DISEASE);
			String disaeseId = EntityUtils.getEntityId(disease);
			boolean curen = EntityUtils.getBoolean(TreatmentReview.CURED, review);
			String key = curen + "|" + disaeseId;
			List<DBObject> trs = diseaseIdReviewsMap.get(key);
			if (trs == null) {
				trs = new ArrayList<>();
				diseaseIdReviewsMap.put(key, trs);
				
				// put diseaseItem
				DBObject diseaseItem = new BasicDBObject(Disease.NAME, disease.get(Disease.NAME));
				diseaseItem.put(TreatmentReview.ENTITY_NAME + "s", trs);
				diseaseItem.put(TreatmentReview.CURED, curen);
				result.add(diseaseItem);
			}
			TreatmentEvent te = eventService.findLastTreatmentEvent(userId, disaeseId, EntityUtils.getEntityId(review.get(TreatmentReview.TREATMENT)));
			if (te != null) {
				review.put(TreatmentEvent.ENTITY_NAME, te);
			}
			trs.add(review);
		}
		return result;
	}

	@Override
	public String getReviewId(String userId, String diseaseId, String treatmentId) {
		Query query = Query.query(Criteria.where("author._id").is(userId).and(TreatmentEvent.DISEASE_ID).is(diseaseId).and(TreatmentEvent.TREATMENT_ID).is(treatmentId));
		DBObject findOne = getCollection().findOne(query.getQueryObject(), new BasicDBObject(Entity.ID, 1));
		return EntityUtils.getEntityId(findOne);
	}
}
