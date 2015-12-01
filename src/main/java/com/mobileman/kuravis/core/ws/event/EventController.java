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

import java.util.Date;
import java.util.List;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.event.Event;
import com.mobileman.kuravis.core.domain.event.EventType;
import com.mobileman.kuravis.core.domain.event.NoteEvent;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.event.WeightEvent;
import com.mobileman.kuravis.core.domain.user.UserSettings;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.EventUtils;
import com.mobileman.kuravis.core.domain.util.UserUtils;
import com.mobileman.kuravis.core.services.event.EventService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.util.ValidationUtils;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.DBObject;

@Controller
//@RequestMapping("/event")
public class EventController extends AbstractHealtPlatformController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private TreatmentReviewService treatmentReviewService;

	@Autowired
	private EventValidator eventValidator;

	@InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(eventValidator);
    }
	
	@RequestMapping(value = "/event/{eventType}/{id}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<Event> getEventById(@PathVariable EventType eventType, @PathVariable String id) {
		log.debug("getEventById(" + eventType + ", " + id + ")");
		Event event = eventService.getEventById(id, eventType.getEventClass());
		if (event instanceof TreatmentEvent) {
			TreatmentEvent te = (TreatmentEvent) event;
			String reviewId = treatmentReviewService.getReviewId(te.getUser().get_id(), te.getDisease().get_id(), te.getTreatment().get_id());
			te.setReviewId(reviewId);
		}
		ResponseEntity<Event> response = new ResponseEntity<Event>(event, HttpStatus.OK);
		log.debug("end: " + response);
		return response;
	}	
    
	@RequestMapping(value = "/event/note", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<ModelMap> createNoteEvent(@RequestBody @Validated NoteEvent object, BindingResult errors) {
		return doCreate(object, errors);
	}

	@RequestMapping(value = "/event/weight", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<ModelMap> createWeightEvent(@RequestBody @Validated WeightEvent object, BindingResult errors) {
		return doCreate(object, errors);
	}

	@RequestMapping(value = "/event/treatment", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<ModelMap> createTreatmentEvent(@RequestBody @Validated TreatmentEvent object, Errors errors) {
		return doCreate(object, errors);
	}

	@RequestMapping(value = "/event/note", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<ModelMap> updateNoteEvent(@RequestBody @Validated NoteEvent event, Errors errors) {
		return doUpdate(event, errors);
	}

	@RequestMapping(value = "/event/weight", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<ModelMap> updateWeightEvent(@RequestBody @Validated WeightEvent event, Errors errors) {
		return doUpdate(event, errors);
	}

	@RequestMapping(value = "/event/treatment", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<ModelMap> updateTreatmentEvent(@RequestBody @Validated TreatmentEvent event, Errors errors) {
		return doUpdate(event, errors);
	}

	
	private ResponseEntity<ModelMap> doCreate(Event event, Errors errors) {
		if (log.isDebugEnabled()) {
			log.debug("create(" + event + ") binding errors:" + errors.getErrorCount());
		}
		if (errors.hasErrors()) {
			return getErrorResponse(errors);
		}
		EventUtils.setBaseProperties(event);
		DBObject result = ErrorUtils.success(HttpStatus.CREATED);
		String id = eventService.create(event);
		result.put(NoteEvent.ID, id);
		ResponseEntity<ModelMap> response = new ResponseEntity<ModelMap>(new ModelMap(Event.ID, id), ErrorUtils.getStatus(result));
		if (log.isDebugEnabled()) {
			log.debug("end: " + response);
		}
		return response;
	}
	
	private void assertAuthorizedOwner(Event event) {
		if (!UserUtils.getLoggedUserId().equals(event.getUser().get_id())) {
			throw new AuthorizationException("User not authorized to process event!");
		}
	}
	
	private ResponseEntity<ModelMap> doUpdate(Event event, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, Event.ID, messageSource);
		if (log.isDebugEnabled()) {
			log.debug("doUpdate(" + event.getEventType() + "," + event.get_id() + ") binding errors:" + errors.getErrorCount());
			log.debug(" " + event);
		}
		try {
			assertAuthorizedOwner(event);
			if (errors.hasErrors()) {
				return getErrorResponse(errors);
			}
			EventUtils.setBaseProperties(event);
			String id = eventService.save(event);
			DBObject result = ErrorUtils.success();
			ResponseEntity<ModelMap> response = new ResponseEntity<ModelMap>(new ModelMap(Event.ID, id), ErrorUtils.getStatus(result));
			if (log.isDebugEnabled()) {
				log.debug("end: " + response);
			}
			return response;
		} catch (AuthorizationException e) {
			log.warn("doUpdate(" + event.getEventType() + "," + event.get_id() + ")", e);
			return new ResponseEntity<ModelMap>(HttpStatus.FORBIDDEN);
		}
	}

	@RequestMapping(value = "/event/{id}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> delete(@PathVariable String id) {
		log.debug("delete(" + id + ")");
		DBObject result = null;
		HttpStatus status = HttpStatus.OK;
		Event event = eventService.getEventById(id, TreatmentEvent.class);
		try {
			assertAuthorizedOwner(event);
			result = eventService.delete(event);
			if (ErrorUtils.isError(result)) {
				status = HttpStatus.NOT_FOUND;
			}
		} catch (EmptyResultDataAccessException e) {
			status = HttpStatus.NOT_FOUND;
		} catch (AuthorizationException e) {
			log.warn("delete(" + id + ")", e);
			return new ResponseEntity<DBObject>(HttpStatus.FORBIDDEN);
		}
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, status);
		log.debug("end: " + response);
		return response;
	}

	/**
	 * 
	 * @param eventType 
	 * @param userId 
	 * @param start
	 * @param end
	 * @param page
	 * @return Page<DBObject>
	 */
	@RequestMapping(value="/event", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@RequiresAuthentication
	public Page<DBObject> findAllEventsByDateRange(
			@RequestParam(value = "eventType", required = false) String eventType,
			@RequestParam(value = "userId", required = true) String userId,
			@RequestParam(value = "start", required = true) Long start,
			@RequestParam(value = "end", required = true) Long end,
			@PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable page) {
		log.debug("findAllEventsByDateRange(" + eventType + "," + userId + "," + start + "," + end + "," + page + ")");
		if (!UserUtils.getLoggedUserId().equals(userId)) {
			DBObject userSettings = userService.findUserSettings(userId);
			boolean isDiaryPublic = EntityUtils.getBoolean(UserSettings.DIARY_PUBLIC, (DBObject) userSettings.get(UserSettings.PROFILE));
			if (!isDiaryPublic) {
				log.warn("User not authorized to see private diary!");
				throw new AuthorizationException("User not authorized to see private diary!");
				// return new ResponseEntity<DBObject>(HttpStatus.FORBIDDEN);
			}
		}
		final List<DBObject> entities;
		if (eventType != null && eventType.trim().length() > 0) {
			entities = eventService.findAllUserEventsByDateRange(EventType.fromJson(eventType), userId, new Date(start), new Date(end), page);
		} else {
			entities = eventService.findAllUserEventsByDateRange(userId, new Date(start), new Date(end), page);
		}
		for (DBObject event : entities) {
			EventType type = EventUtils.getEventType(event);
			if (EventType.TREATMENT.equals(type)) {
				String diseaseId = EntityUtils.getEntityId(event.get(TreatmentEvent.DISEASE));
				String treatmentId = EntityUtils.getEntityId(event.get(TreatmentEvent.TREATMENT));
				String reviewId = treatmentReviewService.getReviewId(userId, diseaseId, treatmentId);
				event.put(TreatmentEvent.REVIEWID, reviewId);
			}
		}
		Page<DBObject> result = new PageImpl<DBObject>(entities, page, entities.size());
		log.debug("return " + entities.size() + " items.");
		return result;
	}
	
	@RequestMapping(value = "/event/treatment", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	//@RequiresAuthentication
	public Page<DBObject> findAllTreatmentEvents(
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "diseaseId", required = false) String diseaseId,
			@RequestParam(value = "treatmentId", required = false) String treatmentId,
			@PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable page) {
		log.debug("findAllTreatmentEvents(" + userId + "," + diseaseId + "," + treatmentId + "," + page + ")");
		final List<DBObject> entities = eventService.findAllTreatmentEvents(userId, diseaseId, treatmentId, page);
		Page<DBObject> result = new PageImpl<DBObject>(entities, page, entities.size());
		log.debug("return " + entities.size() + " items.");
		return result;
	}
}
