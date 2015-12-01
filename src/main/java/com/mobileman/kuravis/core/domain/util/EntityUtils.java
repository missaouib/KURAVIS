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
package com.mobileman.kuravis.core.domain.util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.mobileman.kuravis.core.domain.Attributes;
import com.mobileman.kuravis.core.domain.Entity;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.event.Event;
import com.mobileman.kuravis.core.domain.option_list.treatment_type.TreatmentType;
import com.mobileman.kuravis.core.domain.option_list.unit.Unit;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewEventType;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentCostStatistics;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentDurationStatistics;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.user.notification.UserNotification;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public abstract class EntityUtils implements Attributes {
	
	public static final String ATTR_RESULT = "result";
	
	public static final String ATTR_USER = "user";
		
	public static final String ROLE = "role";
	
	public static final String USERACCOUNT = "useraccount";
	
	public static final String USER_PRIVACY_SETTINGS = "userprivacysettings";
	
	/**
	 * use @see {@link Disease}.ENTITY_NAME
	 */
	@Deprecated 
	public static final String DISEASE = Disease.ENTITY_NAME;
	
	public static final String DISEASE_SUGGESTION = "diseasesuggestion";
	
	public static final String TREATMENT = "treatment";
	
	public static final String PAGE_STATISTICS = "pagestatistics";
	
	public static final String TEMP_TREATMENT_REVIEW = "tmptreatmentreview";
		
	public static final String TREATMENT_SUMMARY_AGE_STATISTICS = "treatmentsummaryagestatistics";
	
	public static final String TREATMENT_REVIEW_EVENT = "treatmentreviewevent";
		
	public static final String TREATMENT_SIDE_EFFECT = "treatmentsideeffect";
	
	public static final String FRAUD_REPORT_ITEM = "fraudreportitem";
	
	public static final String FRAUD_REPORT = "fraudreport";
	
	public static final String USER_INVITATION = "userinvitation";
	
	public static final String SUBSCRIPTION = "subscription";
	
	public static final String SYSTEM = "system";

	/**
	 * use @see {@link TreatmentReview}.ENTITY_NAME
	 */
	@Deprecated 
	public static final String TREATMENT_REVIEW = TreatmentReview.ENTITY_NAME;


	/**
	 * @return all entity names
	 */
	public static List<String> getAllEntityNames() {
		return Arrays.asList(Disease.ENTITY_NAME, 
				EntityUtils.TREATMENT, 
				EntityUtils.TREATMENT_SIDE_EFFECT, 
				TreatmentReview.ENTITY_NAME, 
				TreatmentReviewSummary.ENTITY_NAME, 
				EntityUtils.TREATMENT_REVIEW_EVENT,
				EntityUtils.TREATMENT_SIDE_EFFECT, 
				EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS,
				EntityUtils.TEMP_TREATMENT_REVIEW,
				EntityUtils.USER,
				EntityUtils.USERACCOUNT,
				UserNotification.ENTITY_NAME,
				EntityUtils.USER_INVITATION,
				EntityUtils.FRAUD_REPORT_ITEM,
				EntityUtils.FRAUD_REPORT,
				EntityUtils.PAGE_STATISTICS,
				EntityUtils.DISEASE_SUGGESTION,
				EntityUtils.SUBSCRIPTION,
				EntityUtils.SYSTEM,
				Unit.ENTITY_NAME,
				TreatmentType.ENTITY_NAME,
				Event.ENTITY_NAME,
				"followedentity",
				TreatmentCostStatistics.ENTITY_NAME,
				TreatmentDurationStatistics.ENTITY_NAME
				);
	}
	
	/**
	 * @param diseaseId
	 * @param treatmentId
	 * @return id of an treatment review
	 */
	public static String createTreatmentReviewId(String diseaseId, String treatmentId) {
		return "" + diseaseId + treatmentId;
	}
	
	/**
	 * @param reviewId
	 * @param userId
	 * @return id of an treatment review vote
	 */
	public static String createTreatmentReviewVoteId(Object reviewId, Object userId) {
		return "" + reviewId + userId;
	}
	
	/**
	 * @param diseaseId
	 * @param treatmentId
	 * @return id of an treatment review summary
	 */
	public static String createTreatmentReviewSummaryId(Object diseaseId, Object treatmentId) {
		return "" + diseaseId + treatmentId;
	}
	
	/**
	 * @param id
	 * @return BasicDBObject(ID, id)
	 */
	public static DBObject createDBObjectId(String id) {
		return new BasicDBObject(ID, id);
	}
	
	/**
	 * @return BasicDBObject(ID, UUID)
	 */
	public static String newId() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * @return BasicDBObject(ID, UUID)
	 */
	public static DBObject newDBObjectId() {
		return new BasicDBObject(ID, UUID.randomUUID().toString());
	}

	/**
	 * @param user
	 * @return base user object - id and name
	 */
	public static DBObject createBaseUser(DBObject user) {
		if (user == null) {
			return null;
		}
		
		return new BasicDBObject(ID, user.get(ID))
			.append(User.NAME, user.get(User.NAME))
			.append(User.ATTR_GENDER, user.get(User.ATTR_GENDER))
			.append(User.ATTR_YEAR_OF_BIRTH, user.get(User.ATTR_YEAR_OF_BIRTH))
			.append(User.EMAIL, user.get(User.EMAIL))
			.append(User.SETTINGS, user.get(User.SETTINGS));
	}
	
	/**
	 * @param obj1 
	 * @param obj2 
	 * @return true if attr ID of both objects are same
	 * 
	 */
	public static boolean equals(DBObject obj1, DBObject obj2) {
		if (obj1 == obj2) {
			return true;
		}
		
		if (obj1 == null || obj2 == null) {
			return false;
		}
		
		return ObjectUtils.nullSafeEquals(obj1.get(ID), obj2.get(ID));
	}
	
	/**
	 * @param obj1 
	 * @param obj2 
	 * @return true if attr ID of both objects are same
	 * 
	 */
	public static boolean equals(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		}
		
		if (obj1 == null || obj2 == null) {
			return false;
		}
		
		if (!DBObject.class.isInstance(obj1) || !DBObject.class.isInstance(obj2)) {
			return false;
		}
		
		return ObjectUtils.nullSafeEquals(DBObject.class.cast(obj1).get(ID), DBObject.class.cast(obj2).get(ID));
	}
	
	/**
	 * @param treatmentReview
	 * @param type
	 * @param user 
	 * @return event object
	 */
	public static DBObject createTreatmentReviewEvent(DBObject treatmentReview, TreatmentReviewEventType type, DBObject user) {
		return createTreatmentReviewEvent(treatmentReview, newId(), type, user);
	}

	/**
	 * @param treatmentReview
	 * @param entityId 
	 * @param type
	 * @param user 
	 * @return event object
	 */
	public static DBObject createTreatmentReviewEvent(DBObject treatmentReview, String entityId, TreatmentReviewEventType type, DBObject user) {
		DBObject event = entityId == null ? newDBObjectId() : new BasicDBObject(ID, entityId);
		EntityUtils.setBasePropertiesOnCreate(event);
		
		DBObject baseUser = EntityUtils.createBaseUser(user);
		baseUser.put(User.ATTR_GENDER, user.get(User.ATTR_GENDER));
		baseUser.put(User.ATTR_YEAR_OF_BIRTH, user.get(User.ATTR_YEAR_OF_BIRTH));
		baseUser.put("settings", new BasicDBObject("profile", DBObject.class.cast(user.get("settings")).get("profile")));
		
		event.put("user", baseUser);
		event.put("treatmentReviewId", getEntityId(treatmentReview));
		event.put("treatmentReviewAuthorId", getEntityId(treatmentReview.get(TreatmentReview.AUTHOR)));
		event.put("type", type.getValue().toLowerCase());
				
		return event;
	}

	/**
	 * @param entity
	 * @return entity
	 */
	public static DBObject setBasePropertiesOnCreate(DBObject entity) {
		entity.put(CREATED_ON, new Date());
		entity.put(MODIFIED_ON, entity.get(CREATED_ON));
		if (!entity.containsField(ID)) {
			entity.put(ID, newId());
		}
		
		return entity;
	}

	/**
	 * @param entity
	 * @return ID of an entity
	 */
	public static String getEntityId(Object entity) {
		if (DBObject.class.isInstance(entity)) {
			return (String) DBObject.class.cast(entity).get(ID);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param command
	 * @return returns entity name from given command (set or entity)
	 */
	public static String getEntityName(DBObject command) {
		return (String) getProperty(command, NAME);
	}

	@SuppressWarnings("unchecked")
	public static Object getProperty(DBObject command, String property) {
		Object value = null;
		Object element = command.get("$set");
		if (element != null) {
			value = ((Map<String, Object>) element).get(property);
		} else {
			value = command.get(property);
		}
		return value;
	}

	/**
	 * @param content
	 * @param projection
	 * @return string representation
	 */
	public static List<Object> transformToStringList(List<DBObject> content, String projection) {
		List<Object> result = new ArrayList<>(content.size());
		for (DBObject entity : content) {
			result.add(entity.get(projection));
		}
		
		return result;
	}
	
	public static void copyAttribute(String propertyName, DBObject source, DBObject target) {
		if (source.containsField(propertyName)) {
			target.put(propertyName, source.get(propertyName));
		}
	}

	/**
	 * @param replaceProperty TreatmentReview property name to be replaced
	 * @param replaceObj property to be replaced
	 * @param oldReview
	 * @param newReview
	 */
	public static void copyTreatmentReview(String replaceProperty, DBObject replaceObj, DBObject oldReview, DBObject newReview) {
		switch (replaceProperty) {
		case TreatmentReview.DISEASE:
			newReview.put(TreatmentReview.DISEASE, replaceObj);
			EntityUtils.copyAttribute(TreatmentReview.TREATMENT, oldReview, newReview);
			break;
		case TreatmentReview.TREATMENT:
			newReview.put(TreatmentReview.TREATMENT, replaceObj);
			EntityUtils.copyAttribute(TreatmentReview.DISEASE, oldReview, newReview);
			break;
		default:
			break;
		}
		EntityUtils.copyAttribute(TreatmentReview.TEXT, oldReview, newReview);
		EntityUtils.copyAttribute(CREATED_ON, oldReview, newReview);
		EntityUtils.copyAttribute(TreatmentReview.RATING, oldReview, newReview);
		EntityUtils.copyAttribute(TreatmentReview.SIDE_EFFECTS, oldReview, newReview);
		EntityUtils.copyAttribute(TreatmentReview.AUTHOR, oldReview, newReview);
		EntityUtils.copyAttribute(TreatmentReview.VOTES_COUNT, oldReview, newReview);
		EntityUtils.copyAttribute(TreatmentReview.LAST_VOTED, oldReview, newReview);
		EntityUtils.copyAttribute(TreatmentReview.COMMENTS_COUNT, oldReview, newReview);
		EntityUtils.copyAttribute(TreatmentReview.LAST_COMMENTED, oldReview, newReview);
		EntityUtils.copyAttribute(TreatmentReview.RECENT_EVENT, oldReview, newReview);
	}

	/**
	 * @param propertyName
	 * @param entity
	 * @return
	 */
	public static int getInt(String propertyName, DBObject entity) {
		Object value = entity.get(propertyName);
		if (value == null) {
			return 0;
		}
		return ((Integer) value).intValue();
	}

	public static boolean getBoolean(String propertyName, DBObject entity) {
		Object value = entity.get(propertyName);
		if (value == null) {
			return false;
		}
		return (Boolean) value;
	}

	/**
	 * @param object
	 */
	public static void setBaseProperties(Entity object) {
		if (StringUtils.isEmpty(object.get_id())) {
			object.set_id(EntityUtils.newId());
		}
		if (object.getCreatedOn() == null) {
			object.setCreatedOn(new Date());
		}
		if (object.getModifiedOn() == null) {
			object.setModifiedOn(object.getCreatedOn());
		}
		if (object.getUser() == null) {
			object.setUser(UserUtils.createBaseUser(UserUtils.getLoggedUser()));
		}
	}

	/**
	 * @param sourceEntity
	 * @return copy of sourceEntity
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> T copyBaseEntity(T sourceEntity) {
		T copy = null;
		try {
			Class<T> entityType = (Class<T>)sourceEntity.getClass();
			Constructor<T> constructor = entityType.getConstructor();
			copy = constructor.newInstance((Object[])null);
			copy.set_id(sourceEntity.get_id());
			copy.setUser(sourceEntity.getUser());
			
		} catch (Exception e) {
			throw new RuntimeException("Can not create copy of entity " + sourceEntity + " - (type= " + sourceEntity.getClass() + ")", e);
		}
		
		return copy;
	}

	/**
	 * @param domainEntity
	 * @return return entity simple class name, lower-case
	 */
	public static String getDocumentCollectionName(Entity domainEntity) {
		if (domainEntity == null) {
			return null;
		}
		return getDocumentCollectionName(domainEntity.getClass());
	}

	public static String getDocumentCollectionName(Class<?> domainClass) {
		if (domainClass == null) {
			return null;
		}
		return domainClass.getSimpleName().toLowerCase();
	}
}
