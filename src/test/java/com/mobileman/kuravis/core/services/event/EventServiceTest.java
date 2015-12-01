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
 * EventServiceTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 17.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.domain.Pair;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.event.Event;
import com.mobileman.kuravis.core.domain.event.EventAttributes;
import com.mobileman.kuravis.core.domain.event.EventType;
import com.mobileman.kuravis.core.domain.event.FrequencyType;
import com.mobileman.kuravis.core.domain.event.NoteEvent;
import com.mobileman.kuravis.core.domain.event.TreatmentCategory;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.event.WeightEvent;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.user.reminder.Reminder;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.option_list.treatment_type.TreatmentTypeService;
import com.mobileman.kuravis.core.services.option_list.unit.UnitService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.user.ReminderService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class EventServiceTest extends AbstractIntegrationTest implements EventAttributes {
	
	@Autowired
	private UserService userService;

	@Autowired
	private TreatmentReviewService treatmentReviewService;
	
	@Autowired
	private DiseaseService diseaseService;
	
	@Autowired
	private TreatmentService treatmentService;
	
	@Autowired
	private TreatmentTypeService treatmentTypeService;
	
	@Autowired
	private UnitService unitService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private ReminderService reminderService;
	
	private static boolean loggedAthorizedUser = false;
	
	@Override
	public void setUp() {
		super.setUp();
		if (!loggedAthorizedUser) {
			try {
				loginAuthorizedUser();
				loggedAthorizedUser = true;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void createReviewEvent() throws Exception {
		DBObject disaese = null;
		
		DBObject treatment = null;
		try {
			disaese = diseaseService.createOrFindByName(new BasicDBObject(Disease.NAME, "createReviewEvent"));
			treatment = treatmentService.createOrFindByName(new BasicDBObject(Disease.NAME, "createReviewEvent"));
			
			QueryBuilder eventsFilter = QueryBuilder.start().put("disease._id").is(disaese.get(Disease.ID)).and("treatment._id").is(treatment.get(Treatment.ID));
			long count = this.eventService.count(eventsFilter.get());
			
			DBObject review = new BasicDBObject();
			review.put("disease", disaese.toMap());
			review.put("treatment", treatment.toMap());
			review.put("rating", 0.6d);
			review.put("text", "createReviewEvent");
			DBObject result = treatmentReviewService.createTreatmentReview(review);
			assertFalse(ErrorUtils.isError(result));
			
			long count2 = this.eventService.count(eventsFilter.get());
			assertEquals(count + 1, count2);
			
			treatmentReviewService.delete((String) result.get(EntityUtils.ID));
			count2 = this.eventService.count(eventsFilter.get());
			assertEquals(count, count2);
			
		} finally {
			loginAdminIfNeeded();
			diseaseService.delete((String) disaese.get(EntityUtils.ID));
			treatmentService.delete((String) treatment.get(EntityUtils.ID));
		}
	}
	
	@Test
	public void createNoteEvent() {
		DBObject eventData = new BasicDBObject();
		eventData.put(NoteEvent.TITLE, "myNoteEvent");
		eventData.put(NoteEvent.TEXT, "some text");
		createEventAndValidate(EventType.NOTE,eventData, true);
	}

	@Test(expected=HealtPlatformException.class)
	public void createNoteEvent_validationError() {
		DBObject event = new BasicDBObject();
		event.put(NoteEvent.TITLE, "myNoteEvent");
		event.put(NoteEvent.TEXT, null);
		eventService.createNoteEvent(event);
	}

	@Test
	public void createWeightEvent() {
		DBObject eventData = new BasicDBObject();
		eventData.put(WeightEvent.WEIGHT, new Double("72.5"));
		createEventAndValidate(EventType.WEIGHT, eventData, true);
	}

	@Test(expected=HealtPlatformException.class)
	public void createTreatmentEvent_validationError() {
		DBObject eventData = new BasicDBObject();
		eventData.put(TreatmentEvent.CATEGORY, TreatmentCategory.PRESCRIPTION_MEDICINE.toJson());
		eventData.put(TreatmentEvent.FREQUENCY, FrequencyType.DAILY.toJson());
		eventService.createTreatmentEvent(eventData);
		//createEventAndValidate(EventType.TREATMENT, eventData);
	}
	
	@Test
	public void createTreatmentEvent() {
		DBObject disaese = null;
		DBObject treatment = null;
		String eventId = null;
		DBObject result = null;
		TreatmentCategory category = TreatmentCategory.PRESCRIPTION_MEDICINE;
		try {
			disaese = diseaseService.createOrFindByName(new BasicDBObject(Disease.NAME, "myDisease4event"));
			treatment = treatmentService.createOrFindByName(new BasicDBObject(Disease.NAME, "myTreatment4event"));
			DBObject eventData = createTreatmentEventData(category, disaese, treatment);
			result = eventService.createTreatmentEvent(eventData);
			eventId = EntityUtils.getEntityId(result);
			assertFalse(ErrorUtils.isError(result));
			
			DBObject actualEvent = eventService.findById(eventId);
			assertNotNull(actualEvent);
			List<Pair<String, Boolean>> attributes = TreatmentCategory.getGroupAttributes(category.getGroup());
			for (Pair<String, Boolean> att : attributes) {
				if (att.getSecond()) {
					assertEquals(eventData.get(att.getFirst()), actualEvent.get(att.getFirst()));
				}
			}
			String newFrequency = FrequencyType.DAILY.toJson();
			eventService.updateEvent(eventId, new BasicDBObject("$set", new BasicDBObject(TreatmentEvent.FREQUENCY, newFrequency)));
			actualEvent = eventService.findById(eventId);
			assertEquals(newFrequency, actualEvent.get(TreatmentEvent.FREQUENCY));
		} finally {
			loginAdminIfNeeded();
			diseaseService.delete(EntityUtils.getEntityId(disaese));
			treatmentService.delete(EntityUtils.getEntityId(treatment));
			if (eventId != null) {
				eventService.delete(eventId);
			}
		}
	}

	private DBObject createTreatmentEventData(TreatmentCategory category, DBObject disaese, DBObject treatment) {
		DBObject eventData = new BasicDBObject();
		eventData.put(TreatmentEvent.EVENT_TYPE, EventType.TREATMENT.toJson());
		eventData.put(TreatmentEvent.DISEASE, disaese.toMap());
		eventData.put(TreatmentEvent.TREATMENT, treatment.toMap());
		eventData.put(TreatmentEvent.CATEGORY, category.toJson());
		eventData.put(TreatmentEvent.FREQUENCY, FrequencyType.ONCE.toJson());
		eventData.put(TreatmentEvent.QUANTITY, 1);
		eventData.put(TreatmentEvent.TREATMENT_TYPE, new BasicDBObject(ID,"id1").append(NAME, "Filmtablette"));
		eventData.put(TreatmentEvent.DOSE, 300);
		eventData.put(TreatmentEvent.UNIT, new BasicDBObject(ID,"id2").append(NAME, "mg"));
		eventData.put(TreatmentEvent.START, new Date(DateTime.parse("01.04.2014 08:30", DateTimeFormat.forPattern("dd.MM.yyyy hh:mm")).getMillis()));
		return eventData;
	}

	private String createEventAndValidate(EventType eventType, DBObject eventData, boolean delete) {
		DBObject result = null;
		String id = null;
		try {
			switch (eventType) {
			case NOTE:
				result = eventService.createNoteEvent(eventData);
				break;
			case WEIGHT:
				result = eventService.createWeightEvent(eventData);
				break;
			case TREATMENT:
				result = eventService.createTreatmentEvent(eventData);
				break;
			default:
				break;
			}
			id = EntityUtils.getEntityId(result);
			assertFalse(ErrorUtils.isError(result));

			int count = new Long(eventService.count(QueryBuilder.start().put(ID).is(id).get())).intValue();
			assertEquals(1, count);
		} finally {
			if (delete) {
				if (id != null) {
					eventService.delete(id);
				}
			}
		}
		return id;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void findAllTreatmentEventsByReviewId() throws Exception {
		
		List<DBObject> events = eventService.findAll(new PageRequest(0, 10));
		assertTrue(events.size() > 0);
		DBObject user = (DBObject) events.get(0).get(Event.USER);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date start = dateFormat.parse("2010-01-01 10:00:00");
		Date end = new Date();
		String userId = (String) user.get(User.ID);
		
		List<DBObject> data = eventService.findAllUserEventsByDateRange(userId, start, end, new PageRequest(0, 20));
		assertFalse(data.isEmpty());
		
		data = eventService.findAllUserEventsByDateRange(userId, new Date(), new Date(), new PageRequest(0, 20));
		assertTrue(data.isEmpty());
		
	}
	
	/**
	 * @throws ParseException 
	 * 
	 */
	@Test
	public void createTreatmentEvent_WithReminder() throws ParseException {
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 5);
		
		TreatmentEvent event = new TreatmentEvent();
		event.setCategory(TreatmentCategory.PRESCRIPTION_MEDICINE);
		event.setType(treatmentTypeService.findEntityByProperty(Disease.NAME, "Filmtabletten"));
		event.setDisease(diseaseService.findEntityByProperty(Disease.NAME, "Flu"));
		event.setTreatment(treatmentService.findEntityByProperty(Treatment.NAME, "Vitamin B"));
		event.setDose(1);
		event.setQuantity(300);
		event.setUnit(unitService.findEntityByProperty(NAME, "Teelöffel"));
		event.setFrequency(FrequencyType.ONCE);
		event.setDuration(1);
		event.setReminder(Boolean.TRUE);
		event.setStart(calendar.getTime());
		
		String eventId = null;
		
		try {
			eventId = eventService.create(event);
			DBObject actualEvent = eventService.findById(eventId);
			assertNotNull(actualEvent);
			
			Reminder reminder = reminderService.findEntityByProperty(Reminder.EVENT_ID, eventId);
			assertNotNull(reminder);
			
		} finally {
			loginAdminIfNeeded();
			if (eventId != null) {
				eventService.delete(eventId);
			}
		}
	}
}
