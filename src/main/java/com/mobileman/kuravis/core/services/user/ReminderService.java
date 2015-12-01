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
 * ReminderService.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 23.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.user;

import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.user.reminder.Reminder;
import com.mobileman.kuravis.core.services.entity.EntityService;

/**
 * @author MobileMan GmbH
 *
 */
public interface ReminderService extends EntityService<Reminder> {

	/**
	 * @param event
	 * @return reminder ID
	 */
	String createReminderForEvent(TreatmentEvent event);
	
	/**
	 * @param eventId
	 */
	void deleteAllRemindersOfEvent(String eventId);

}
