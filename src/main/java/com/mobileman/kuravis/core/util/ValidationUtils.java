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
package com.mobileman.kuravis.core.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;

import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;

public abstract class ValidationUtils extends org.springframework.validation.ValidationUtils {

	public static void rejectIfEmpty(Errors errors, String field, MessageSource messageSource) {
		String capitalizedField = StringUtils.capitalize(field);
		rejectIfEmpty(errors, field, "error.required", new String[] { messageSource.getMessage(field, null, capitalizedField, AbstractHealtPlatformController.LOCALE_DEFAULT) }, capitalizedField
				+ " is required!");
	}

}
