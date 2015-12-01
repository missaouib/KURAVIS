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
 * AbstractHealtPlatformController.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 23.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Component
public abstract class AbstractHealtPlatformController {
	protected Logger log = LoggerFactory.getLogger(getClass());

	public static final Locale LOCALE_DEFAULT = Locale.GERMANY;

	@Autowired
	protected MessageSource messageSource;
	
	
	protected ResponseEntity<ModelMap> getErrorResponse(Errors errors) {
		return new ResponseEntity<ModelMap>(new ModelMap("errors", getErrors(errors)), HttpStatus.BAD_REQUEST);
	}
	
	private List<String> getErrors(Errors errors) {
		List<String> messages = new ArrayList<>();
		for (ObjectError oe : errors.getAllErrors()) {
			messages.add(messageSource.getMessage(oe, LOCALE_DEFAULT));
		}
		return messages;
	}

	/**
	 * @param ex
	 * @param request
	 * @return ResponseEntity
	 */
	@ExceptionHandler({UnauthenticatedException.class, AuthorizationException.class})
	public ResponseEntity<String> handleUnauthenticatedException(AuthorizationException ex, HttpServletRequest request) {
		log.error(ex.getMessage(), ex);
		ResponseEntity<String> response  = new ResponseEntity<String>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
		return response;
	}
	
	/**
	 * @param ex
	 * @param request
	 * @return ResponseEntity
	 */
	@ExceptionHandler({HealtPlatformException.class})
	public ResponseEntity<DBObject> handleHealtPlatformException(HealtPlatformException ex, HttpServletRequest request) {
		log.error(ex.getMessage(), ex);
		// TODO localize error message
		// messageSource.getMessage("code", null,  ex.getMessage(), LOCALE_DEFAULT);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(ex.getResult(), ErrorUtils.getStatus(ex.getResult()));
		return response;
	}
}
