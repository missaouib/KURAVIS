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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.mobileman.kuravis.core.domain.event.Event;
import com.mobileman.kuravis.core.domain.event.NoteEvent;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.event.WeightEvent;
import com.mobileman.kuravis.core.util.ValidationUtils;
import com.mobileman.kuravis.core.ws.BaseValidator;
import com.mongodb.DBObject;

@Component
public class EventValidator extends BaseValidator implements Validator {
	private static final Logger log = LoggerFactory.getLogger(EventValidator.class);

	@Override
	public boolean supports(Class<?> clazz) {
		boolean assignableFrom = Event.class.isAssignableFrom(clazz);
		if (!assignableFrom) {
			assignableFrom = DBObject.class.isAssignableFrom(clazz);
		}
		return assignableFrom;
	}

	@Override
	public void validate(Object target, Errors errors) {
		if (log.isDebugEnabled()) {
			log.debug("validate(" + target + ")");
		}
		if (target instanceof NoteEvent) {
			ValidationUtils.rejectIfEmpty(errors, NoteEvent.START, messageSource);
			ValidationUtils.rejectIfEmpty(errors, NoteEvent.TITLE, messageSource);
			ValidationUtils.rejectIfEmpty(errors, NoteEvent.TEXT, messageSource);
		} else if (target instanceof WeightEvent) {
			ValidationUtils.rejectIfEmpty(errors, WeightEvent.START, messageSource);
			ValidationUtils.rejectIfEmpty(errors, WeightEvent.WEIGHT, messageSource);
		} else if (target instanceof TreatmentEvent) {
			try {
				TreatmentEvent e = (TreatmentEvent) target;
				ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.DISEASE, messageSource);
				ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.TREATMENT, messageSource);
				ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.START, messageSource);
				switch (e.getCategory().getGroup()) {
				case TREATMENT_PLAN:
					ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.FREQUENCY, messageSource);
					ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.QUANTITY, messageSource);
					ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.TREATMENT_TYPE, messageSource);
					ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.DOSE, messageSource);
					ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.UNIT, messageSource);
					break;
				case THERAPIE:
					ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.FREQUENCY, messageSource);
					//ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.QUANTITY, messageSource);
					ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.DURATION, messageSource);
					ValidationUtils.rejectIfEmpty(errors, TreatmentEvent.UNIT, messageSource);
					break;
				case DEVICES:
					break;
				case OPERATION:
					break;
				case OTHERS:
					break;
				default:
					break;
				}
			} catch (Throwable e) {
				log.error("Error while validating the event!", e);
				throw e;
			}
		} else if (target instanceof DBObject) {
			throw new IllegalAccessError("Validation of DBObject not implemented!");
		}
	}

}
