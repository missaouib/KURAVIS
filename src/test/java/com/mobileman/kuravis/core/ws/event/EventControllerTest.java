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
package com.mobileman.kuravis.core.ws.event;

import static org.junit.Assert.assertNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
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
import org.springframework.util.CollectionUtils;

import com.mobileman.kuravis.core.domain.event.FrequencyType;
import com.mobileman.kuravis.core.domain.event.NoteEvent;
import com.mobileman.kuravis.core.domain.event.TreatmentCategory;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.option_list.treatment_type.TreatmentType;
import com.mobileman.kuravis.core.domain.option_list.unit.Unit;
import com.mobileman.kuravis.core.services.event.EventService;
import com.mobileman.kuravis.core.services.option_list.treatment_type.TreatmentTypeService;
import com.mobileman.kuravis.core.services.option_list.unit.UnitService;
import com.mobileman.kuravis.core.services.user.ReminderService;
import com.mobileman.kuravis.core.ws.BaseControllerTest;
import com.mongodb.DBObject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/application-context.xml")
public class EventControllerTest extends BaseControllerTest {

	@Autowired
	private EventController eventController;

	@Autowired
	private UnitService unitService;

	@Autowired
	private TreatmentTypeService treatmentTypeService;

	@Autowired
	private EventService eventService;

	@Autowired
	private ReminderService reminderService;

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		setUp(eventController);
	}

	@Test
	public void createNoteEvent_validationError() throws Exception {
		NoteEvent ne = new NoteEvent();
		ne.setText("myNoteText");
		MvcResult andReturn = doPost("/event/note", ne);
		Assert.assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(andReturn.getResponse().getStatus()));
	}

	@Test
	public void createUpdateDeleteNoteEvent() throws Exception {
		MvcResult result = null;
		NoteEvent event = new NoteEvent();
		event.setTitle("myNote");
		String text = "asdfdsssss sssssssssssss";
		event.setText(text);
		event.setStart(new Date());
		try {
			// create
			result = doPost("/event/note", event);
			Assert.assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(result.getResponse().getStatus()));
			assertNull(result.getResolvedException());
			setId(event, result);

			event = eventService.getEventById(event.get_id(), NoteEvent.class);
			event.setText(text.toUpperCase());
			result = doUpdate("/event/note", event);
			assertNull(result.getResolvedException());
			Assert.assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
			event = eventService.getEventById(event.get_id(), NoteEvent.class);
			Assert.assertEquals(text.toUpperCase(), event.getText());
		} finally {
			doDelete(event.get_id(), "/event");
		}
	}

	@Test
	public void createTreatmentEvent() throws Exception {
		TreatmentEvent te = new TreatmentEvent();
		DBObject item = null;
		te.setDisease(getDisease());
		te.setTreatment(getTreatment());
		te.setCategory(TreatmentCategory.PRESCRIPTION_MEDICINE);
		te.setFrequency(FrequencyType.DAILY);
		te.setQuantity(1);
		List<DBObject> items = treatmentTypeService.findAll();
		if (!CollectionUtils.isEmpty(items)) {
			item = items.get(0);
			TreatmentType type = objectMapper.readValue(item.toString(), TreatmentType.class);
			te.setType(type);
		}
		te.setDose(300);
		items = unitService.findAll();
		if (!CollectionUtils.isEmpty(items)) {
			item = items.get(0);
			Unit unit = objectMapper.readValue(item.toString(), Unit.class);
			te.setUnit(unit);
		}
		te.setStart(new Date(DateTime.parse("01.04.2014 08:30", DateTimeFormat.forPattern("dd.MM.yyyy hh:mm")).getMillis()));

		MvcResult andReturn = null;
		try {
			// create
			andReturn = doPost("/event/treatment", te);
			Assert.assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(andReturn.getResponse().getStatus()));
			assertNull(andReturn.getResolvedException());
			setId(te, andReturn);
		} finally {
			// delete
			doDelete(te.get_id(), "/event");
		}
	}

	@Test
	public void createOperationTreatmentEvent() throws Exception {
		boolean reminder = false;
		TreatmentEvent te = new TreatmentEvent();
		te.setDisease(getDisease());
		te.setTreatment(getTreatment());
		te.setCategory(TreatmentCategory.OPERATION);
		te.setStart(new Date(DateTime.parse("01.04.2014 08:30", DateTimeFormat.forPattern("dd.MM.yyyy hh:mm")).getMillis()));
		te.setReminder(reminder);
		te.setUser(getLoggedUser());

		MvcResult andReturn = null;
		try {
			// create
			andReturn = doPost("/event/treatment", te);
			Assert.assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(andReturn.getResponse().getStatus()));
			assertNull(andReturn.getResolvedException());
			setId(te, andReturn);
			// te = eventService.getEventById(te.get_id(), TreatmentEvent.class);
			// te.setReminder(!reminder);
			// doUpdate("/event/treatment", te);
		} finally {
			// delete
			eventService.delete(te);
			//doDelete(te.get_id(), "/event");
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	@Ignore("not running on jenkins")
	public void findAllEventsByDateRange() throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date start = df.parse("2014-03-01 12:00:00");
		Date end = df.parse("2014-03-02 12:00:00");

		String userId = getLoggedUser().get_id();

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/event").param("userId", userId).param("start", "" + start.getTime()).param("end", "" + end.getTime()).param("page.page", "0")
				.param("page.size", "20").param("page.page", "0").param("page.size", "20").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		MvcResult andReturn = this.mockMvc.perform(requestBuilder).andReturn();

		assertNull(andReturn.getResolvedException());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void findAllTreatmentEvents() throws Exception {

		String userId = getLoggedUser().get_id();
		String diseaseId = "bb330489-6ab8-49a7-a625-f3f7b559d08a";
		String treatmentId = "bb330489-6ab8-49a7-a625-f3f7b559d08a";

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/event/treatment/").param("userId", userId).param("diseaseId", "" + diseaseId).param("treatmentId", "" + treatmentId)
				.param("page.page", "0").param("page.size", "20").param("page.page", "0").param("page.size", "20").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		MvcResult andReturn = this.mockMvc.perform(requestBuilder).andReturn();

		assertNull(andReturn.getResolvedException());
	}
}
