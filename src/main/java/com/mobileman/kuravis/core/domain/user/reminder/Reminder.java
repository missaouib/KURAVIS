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
 * Reminder.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 22.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.user.reminder;

import org.springframework.data.mongodb.core.mapping.Document;

import com.mobileman.kuravis.core.domain.Entity;

/**
 * @author MobileMan GmbH
 *
 */
@Document(collection=Reminder.ENTITY_NAME)
public class Reminder extends Entity implements ReminderAttributes {

	private String eventId;
	
	private String quartzJobId;

	/**
	 *
	 * @return eventId
	 */
	public String getEventId() {
		return this.eventId;
	}

	/**
	 *
	 * @param eventId eventId
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	/**
	 *
	 * @return quartzJobId
	 */
	public String getQuartzJobId() {
		return this.quartzJobId;
	}

	/**
	 *
	 * @param quartzJobId quartzJobId
	 */
	public void setQuartzJobId(String quartzJobId) {
		this.quartzJobId = quartzJobId;
	}
	
	
}
