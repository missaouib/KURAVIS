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
 * EventServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 17.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.event.impl;

import static com.mobileman.kuravis.core.domain.util.EntityUtils.getEntityId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.Pair;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.event.Event;
import com.mobileman.kuravis.core.domain.event.EventAttributes;
import com.mobileman.kuravis.core.domain.event.EventType;
import com.mobileman.kuravis.core.domain.event.NoteEvent;
import com.mobileman.kuravis.core.domain.event.ReviewEvent;
import com.mobileman.kuravis.core.domain.event.TreatmentCategory;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.event.VoteEvent;
import com.mobileman.kuravis.core.domain.event.WeightEvent;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.UserUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.event.EventService;
import com.mobileman.kuravis.core.services.option_list.treatment_type.TreatmentTypeService;
import com.mobileman.kuravis.core.services.option_list.unit.UnitService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.services.user.ReminderService;
import com.mobileman.kuravis.core.services.util.CRUDAction;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

/**
 * @author MobileMan GmbH
 * 
 */
@Service("eventService")
public class EventServiceImpl extends AbstractEntityServiceImpl<Event> implements EventService, EventAttributes {
	private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
	
	@Autowired
	private UnitService unitService;

	@Autowired
	private TreatmentTypeService treatmentTypeService;

	@Autowired
	private TreatmentService treatmentService;

	@Autowired
	private DiseaseService diseaseService;

	@Autowired
	private ReminderService reminderService;

	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return Event.ENTITY_NAME;
	}

	@Override
	public <T extends Event> T getEventById(Object id, Class<T> clazz) {
		return getMongoTemplate().findById(id, clazz);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.event.EventService#deleteAllUserEvents(java.lang.String)
	 */
	@Override
	public void deleteAllUserEvents(String userId) {
		getCollection().remove(new BasicDBObject(USER + "." + ID, userId));
	}

	/**
	 * @param type
	 * @param eventData
	 * @return event
	 */
	private DBObject createBaseEvent(EventType type, DBObject eventData) {
		if (eventData == null) {
			throw ErrorUtils.exception("Event '" + type + "' is null", ErrorCodes.INCORRECT_PARAMETER);
		}
		DBObject event = new BasicDBObject();
		Object value = eventData.get(START);
		if (value == null) {
			value = new Date();
		}
		event.put(START, value);

		value = eventData.get(END);
		if (value == null) {
			value = event.get(START);
		}
		event.put(END, value);

		event.put(EVENT_TYPE, type.name());
		return event;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.event.EventService#createReviewEvent(com.mongodb.DBObject)
	 */
	@Override
	public void createReviewEvent(DBObject treatmentReview) {
		if (treatmentReview.get(TreatmentReview.ID) == null) {
			throw ErrorUtils.exception("Review ID is null", ErrorCodes.INCORRECT_PARAMETER);
		}
		if (treatmentReview.get(TreatmentReview.DISEASE) == null) {
			throw ErrorUtils.exception("Review disease is null", ErrorCodes.INCORRECT_PARAMETER);
		}
		if (treatmentReview.get(TreatmentReview.TREATMENT) == null) {
			throw ErrorUtils.exception("Review treatment is null", ErrorCodes.INCORRECT_PARAMETER);
		}

		DBObject disease = (DBObject) treatmentReview.get(TreatmentReview.DISEASE);
		DBObject treatment = (DBObject) treatmentReview.get(TreatmentReview.TREATMENT);

		Date createdOn = (Date) treatmentReview.get(CREATED_ON);
		if (createdOn == null) {
			createdOn = new Date();
		}

		ReviewEvent reviewEvent = new ReviewEvent();
		reviewEvent.setStart(createdOn);
		reviewEvent.setEnd(createdOn);
		reviewEvent.setReviewId(getEntityId(treatmentReview));
		reviewEvent.setDisease(new Disease(EntityUtils.getEntityId(disease), EntityUtils.getEntityName(disease)));
		reviewEvent.setTreatment(new Treatment(EntityUtils.getEntityId(treatment), EntityUtils.getEntityName(treatment)));
		
		if (treatmentReview.get(TreatmentReview.RATING) != null) {
			reviewEvent.setRating(new BigDecimal(((Number) treatmentReview.get(TreatmentReview.RATING)).doubleValue()).setScale(4, RoundingMode.HALF_EVEN));
		}
		
		create(reviewEvent);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.event.EventService#createVoteEvent(com.mongodb.DBObject, com.mongodb.DBObject)
	 */
	@Override
	public void createVoteEvent(DBObject treatmentReview, DBObject treatmentReviewVote) {
		if (treatmentReview == null) {
			throw ErrorUtils.exception("treatmentReview is null", ErrorCodes.INCORRECT_PARAMETER);
		}

		if (treatmentReview.get(TreatmentReview.ID) == null) {
			throw ErrorUtils.exception("Review ID is null", ErrorCodes.INCORRECT_PARAMETER);
		}

		if (treatmentReview.get(TreatmentReview.DISEASE) == null) {
			throw ErrorUtils.exception("Review disease is null", ErrorCodes.INCORRECT_PARAMETER);
		}

		if (treatmentReview.get(TreatmentReview.TREATMENT) == null) {
			throw ErrorUtils.exception("Review treatment is null", ErrorCodes.INCORRECT_PARAMETER);
		}

		Date createdOn = (Date) treatmentReviewVote.get(CREATED_ON);
		if (createdOn == null) {
			createdOn = new Date();
		}

		DBObject author = (DBObject) treatmentReview.get(TreatmentReview.AUTHOR);
		DBObject disease = (DBObject) treatmentReview.get(TreatmentReview.DISEASE);
		DBObject treatment = (DBObject) treatmentReview.get(TreatmentReview.TREATMENT);

		VoteEvent voteEvent = new VoteEvent();
		voteEvent.setStart(createdOn);
		voteEvent.setEnd(createdOn);
		voteEvent.setReviewId(getEntityId(treatmentReview));
		voteEvent.setReviewVoteId(getEntityId(treatmentReviewVote));
		voteEvent.setDisease(new Disease(EntityUtils.getEntityId(disease), EntityUtils.getEntityName(disease)));
		voteEvent.setTreatment(new Treatment(EntityUtils.getEntityId(treatment), EntityUtils.getEntityName(treatment)));
		voteEvent.setAuthor(UserUtils.createBaseUser(author));
		create(voteEvent);
	}
	
	@Override
	public DBObject delete(Event event) {
		if (event == null) {
			throw new EmptyResultDataAccessException(1);
		}
		if (EventType.TREATMENT.equals(event.getEventType())) {
			TreatmentEvent te = (TreatmentEvent) event;
			this.treatmentReviewSummaryService.updateTreatmentDurationStatistics(te, null, CRUDAction.DELETE);
		}
		String id = event.get_id();
		DBObject result = super.delete(id);
		if (!ErrorUtils.isError(result)) {
			reminderService.deleteAllRemindersOfEvent(id);
		}
		return result;
	}

	@Override
	public DBObject delete(String id) {
		Event event = getEventById(id, TreatmentEvent.class);
		return delete(event);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#delete(java.lang.String, java.lang.String)
	 */
	@Override
	public DBObject delete(String type, String id) {
		DBObject result = super.delete(type, id);
		if (!ErrorUtils.isError(result)) {
			reminderService.deleteAllRemindersOfEvent(id);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.event.EventService#deleteAllReviewEvents(java.lang.String)
	 */
	@Override
	public void deleteAllReviewEvents(String reviewId, String diseaseId, String treatmentId, String userId) {
		if (log.isDebugEnabled()) {
			log.debug("deleteAllReviewEvents()");
		}		
		// delete ReviewEvents and VoteEvents
		WriteResult result = getCollection().remove(new BasicDBObject(ReviewEvent.REVIEW_ID, reviewId));
		if (log.isDebugEnabled()) {
			log.debug(getAffectedCount(result) + " events (ReviewEvent, VoteEvent) deteted.");
		}
		// delete all TreatmentEvents
		Query query = Query.query(Criteria.where(TreatmentEvent.USER_ID).is(userId).and(TreatmentEvent.DISEASE_ID).is(diseaseId).and(TreatmentEvent.TREATMENT_ID).is(treatmentId));
		result = getCollection().remove(query.getQueryObject());
		if (log.isDebugEnabled()) {
			log.debug(getAffectedCount(result) + " events (TreatmentEvent) deteted.");
		}
	}

	private int getAffectedCount(WriteResult result) {
		try {
			return result.getN();
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public DBObject createNoteEvent(DBObject eventData) {
		DBObject event = validateNoteEvent(eventData);
		return save(event);
	}

	@Override
	public DBObject createWeightEvent(DBObject eventData) {
		DBObject event = validateWeightEvent(eventData);
		return save(event);
	}

	@Override
	public String create(Event event) {
		String id = null;
		if (event instanceof TreatmentEvent) {
			TreatmentEvent te = (TreatmentEvent) event;
			te.setDisease(diseaseService.createOrFindByName(te.getDisease()));
			te.setTreatment(treatmentService.createOrFindByName(te.getTreatment()));
			if (te.getUnit() != null) {
				te.setUnit(unitService.createOrFindByName(te.getUnit()));
			}
			if (te.getType() != null) {
				te.setType(treatmentTypeService.createOrFindByName(te.getType()));
			}
			id = super.create(event);
			if (te.isReminder()) {
				this.reminderService.createReminderForEvent(te);
			}

			this.treatmentReviewSummaryService.updateTreatmentDurationStatistics(te, null, CRUDAction.CREATE);

		} else {
			id = super.create(event);
		}
		return id;
	}

	@Override
	public String save(Event event) {
		boolean actualReminder = false;
		TreatmentEvent actualEvent = null;
		if (event instanceof TreatmentEvent) {
			DBObject reminder = getCollection().findOne(QueryBuilder.start().put(TreatmentEvent.ID).is(event.get_id()).get(), new BasicDBObject(TreatmentEvent.REMINDER, 1));
			actualReminder = EntityUtils.getBoolean(TreatmentEvent.REMINDER, reminder);
			actualEvent = getEventById(event.get_id(), TreatmentEvent.class);
		}
		String id = super.save(event);
		if (event instanceof TreatmentEvent) {
			TreatmentEvent te = (TreatmentEvent) event;
			if (te.isReminder() && actualReminder != te.isReminder()) {
				reminderService.createReminderForEvent(te);
			} else {
				reminderService.deleteAllRemindersOfEvent(event.get_id());
			}
			this.treatmentReviewSummaryService.updateTreatmentDurationStatistics(te, actualEvent, CRUDAction.UPDATE);
		}
		return id;
	}

	@Override
	public DBObject createTreatmentEvent(DBObject eventData) {
		DBObject event = validateTreatmentEvent(eventData);
		return save(event);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateEvent(String id, DBObject updateCmd) {
		Object element = updateCmd.get("$set");
		if (element == null || ((Map<String, Object>) element).isEmpty()) {
			throw ErrorUtils.exception("Invalid update command!", ErrorCodes.INCORRECT_PARAMETER);
		}
		DBObject result = this.update(id, updateCmd);
		if (ErrorUtils.isError(result)) {
			throw new HealtPlatformException(result);
		}
	}

	private DBObject validateNoteEvent(DBObject eventData) {
		EventType eventType = EventType.NOTE;
		DBObject event = createBaseEvent(eventType, eventData);
		Object value = rejectIfEmpty(NoteEvent.TITLE, eventData, eventType);
		event.put(NoteEvent.TITLE, value);
		value = rejectIfEmpty(NoteEvent.TEXT, eventData, eventType);
		event.put(NoteEvent.TEXT, value);
		return event;
	}

	private DBObject validateWeightEvent(DBObject eventData) {
		EventType eventType = EventType.WEIGHT;
		Object value = rejectIfEmpty(WeightEvent.WEIGHT, eventData, eventType);
		DBObject event = createBaseEvent(eventType, eventData);
		event.put(WeightEvent.WEIGHT, value);
		return event;
	}

	private DBObject validateTreatmentEvent(DBObject eventData) {
		EventType eventType = EventType.TREATMENT;
		DBObject event = createBaseEvent(eventType, eventData);
		Object value = rejectIfEmpty(TreatmentEvent.DISEASE, eventData, eventType);
		event.put(TreatmentEvent.DISEASE, value);
		value = rejectIfEmpty(TreatmentEvent.TREATMENT, eventData, eventType);
		event.put(TreatmentEvent.TREATMENT, value);
		value = rejectIfEmpty(TreatmentEvent.CATEGORY, eventData, eventType);
		TreatmentCategory category = TreatmentCategory.fromJson((String) value);
		List<Pair<String, Boolean>> attributes = TreatmentCategory.getGroupAttributes(category.getGroup());
		for (Pair<String, Boolean> attribute : attributes) {
			Boolean required = attribute.getSecond();
			if (required) {
				rejectIfEmpty(attribute.getFirst(), eventData, eventType);
			}
			event.put(attribute.getFirst(), eventData.get(attribute.getFirst()));
		}
		event.put(Event.EVENT_TYPE, EventType.TREATMENT.toJson());
		return event;
	}

	private Object rejectIfEmpty(String attribute, DBObject eventData, EventType eventType) {
		Object value = eventData.get(attribute);
		boolean isEmpty = value == null;
		if (!isEmpty && (value instanceof String)) {
			isEmpty = StringUtils.isBlank((String) value);
		}
		if (isEmpty) {
			throw ErrorUtils.exception(eventType.toJson() + "Event." + attribute + " is required!", ErrorCodes.INCORRECT_PARAMETER);
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.event.EventService#findAllUserEventsByDateRange(java.lang.String, java.util.Date, java.util.Date, org.springframework.data.domain.Pageable)
	 */
	@Override
	public List<DBObject> findAllUserEventsByDateRange(String userId, Date start, Date end, Pageable page) {
		return findAllUserEventsByDateRange(null, userId, start, end, page);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.event.EventService#findAllUserEventsByDateRange(com.mobileman.kuravis.core.domain.event.EventType, java.lang.String, java.util.Date, java.util.Date,
	 *      org.springframework.data.domain.Pageable)
	 */
	@Override
	public List<DBObject> findAllUserEventsByDateRange(EventType eventType, String userId, Date start, Date end, Pageable page) {

		Query query = Query.query(Criteria.where(Event.USER + "." + Event.ID).is(userId).and(START).gte(start).orOperator(Criteria.where(END).lte(end), Criteria.where(END).is(null)));

		List<DBObject> result = null;
		if (eventType != null) {
			query.addCriteria(Criteria.where(Event.EVENT_TYPE).is(eventType));
			result = findAllByQuery(query.getQueryObject(), page);
		} else {
			/*
			 * try { URL url = this.getClass().getResource("/calendar_data.json"); DBObject[] array = objectMapper.readValue(new File(url.getFile()), BasicDBObject[].class); result =
			 * Arrays.asList(array); } catch (IOException e) {}
			 */
			result = findAllByQuery(query.getQueryObject(), page);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.event.EventService#findAllTreatmentEvents(java.lang.String, java.lang.String, java.lang.String, org.springframework.data.domain.Pageable)
	 */
	@Override
	public List<DBObject> findAllTreatmentEvents(String userId, String diseaseId, String treatmentId, Pageable page) {
		QueryBuilder query = QueryBuilder.start().put(Event.EVENT_TYPE).is(EventType.TREATMENT.name());
		if (!StringUtils.isBlank(userId)) {
			query = query.and(Event.USER + "." + Event.ID).is(userId);
		}

		if (!StringUtils.isBlank(diseaseId)) {
			query = query.and(TreatmentEvent.DISEASE + "." + TreatmentEvent.ID).is(diseaseId);
		}

		if (!StringUtils.isBlank(treatmentId)) {
			query = query.and(TreatmentEvent.TREATMENT + "." + TreatmentEvent.ID).is(treatmentId);
		}

		List<DBObject> result = findAllByQuery(query.get(), page);
		return result;
	}

	@Override
	public TreatmentEvent findLastTreatmentEvent(String userId, String diseaseId, String treatmentId) {
		Query query = Query.query(Criteria.where(TreatmentEvent.USER_ID).is(userId).and(TreatmentEvent.DISEASE_ID).is(diseaseId).and(TreatmentEvent.TREATMENT_ID).is(treatmentId));
		query.with(new Sort(Sort.Direction.DESC, TreatmentEvent.CREATED_ON));
		query.limit(1);
		return getMongoTemplate().findOne(query, TreatmentEvent.class);
	}
}
