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
 * EventService.java
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

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.mobileman.kuravis.core.domain.event.Event;
import com.mobileman.kuravis.core.domain.event.EventType;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.services.entity.EntityService;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public interface EventService extends EntityService<Event> {
	
	<T extends Event> T getEventById(Object id, Class<T> clazz);

	/**
	 * @param userId
	 */
	void deleteAllUserEvents(String userId);
	
	/**
	 * Deletes all events ponting to given treatment review
	 * @param reviewId
	 * @param diseaseId
	 * @param treatmentId
	 * @param userId
	 */
	void deleteAllReviewEvents(String reviewId, String diseaseId, String treatmentId, String userId);

	/**
	 * Creates event for newly created treatment review
	 * @param treatmentReview
	 */
	void createReviewEvent(DBObject treatmentReview);

	/**
	 * Creates event for treatment review vote
	 * @param treatmentReview
	 * @param treatmentReviewVote
	 */ 
	void createVoteEvent(DBObject treatmentReview, DBObject treatmentReviewVote);
	
	/**
	 * use {@link #create(Event)}<p>
	 * Creates new note event
	 * 
	 * @param eventData
	 * @return
	 */
	@Deprecated
	DBObject createNoteEvent(DBObject eventData);
	
	/**
	 * use {@link #create(Event)}<p>
	 * Creates new weight event
	 * 
	 * @param eventData
	 * @return
	 */
	@Deprecated
	DBObject createWeightEvent(DBObject eventData);

	@Deprecated
	DBObject createTreatmentEvent(DBObject eventData);

	void updateEvent(String id, DBObject updateCmd);
	
	/**
	 * @param userId
	 * @param start
	 * @param end
	 * @param page
	 * @return all users events
	 */
	List<DBObject> findAllUserEventsByDateRange(String userId, Date start, Date end, Pageable page);
	
	/**
	 * @param eventType 
	 * @param userId
	 * @param start
	 * @param end
	 * @param page
	 * @return all users events
	 */
	List<DBObject> findAllUserEventsByDateRange(EventType eventType, String userId, Date start, Date end, Pageable page);
	
	/**
	 * Finds all treatment events
	 * @param userId
	 * @param diseaseId
	 * @param treatmentId
	 * @param page
	 * @return all treatment events
	 */
	List<DBObject> findAllTreatmentEvents(String userId, String diseaseId, String treatmentId, Pageable page);
	
	TreatmentEvent findLastTreatmentEvent(String userId, String diseaseId, String treatmentId);
	
	DBObject delete(Event event);

}
