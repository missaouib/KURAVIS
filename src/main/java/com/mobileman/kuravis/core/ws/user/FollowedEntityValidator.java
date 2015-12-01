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
package com.mobileman.kuravis.core.ws.user;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.mobileman.kuravis.core.domain.user.FollowedEntity;
import com.mobileman.kuravis.core.util.ValidationUtils;
import com.mobileman.kuravis.core.ws.BaseValidator;

@Component
public class FollowedEntityValidator extends BaseValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return FollowedEntity.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, FollowedEntity.ENTITY_TYPE, messageSource);
		ValidationUtils.rejectIfEmpty(errors, FollowedEntity.ENTITY_ID, messageSource);
	}

}
