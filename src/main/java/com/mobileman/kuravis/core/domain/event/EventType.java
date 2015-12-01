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
 * EventType.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 17.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.event;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;


public enum EventType {

	REVIEW(ReviewEvent.class), REVIEW_VOTE(VoteEvent.class), NOTE(NoteEvent.class), TREATMENT(TreatmentEvent.class), WEIGHT(WeightEvent.class);

	private EventType(Class<? extends Event> clazz) {
		this.eventClass = clazz;
	}

	private final Class<? extends Event> eventClass;

	public Class<? extends Event> getEventClass() {
		return eventClass;
	}

	@JsonValue
	public String toJson() {
		return name().toUpperCase();
	}

	@JsonCreator
	public static EventType fromJson(String text) {
		return valueOf(text.toUpperCase());
	}

	public static EventType valueOf(Class<?> clazz) {
		for (EventType et : values()) {
			if (et.getEventClass().equals(clazz)) {
				return et;
			}
		}
		return null;
	}
}
