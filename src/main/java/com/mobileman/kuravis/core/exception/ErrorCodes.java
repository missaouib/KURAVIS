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
 * HealtPlatformErrorCodes.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 12.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.exception;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.http.HttpStatus;

/**
 * @author MobileMan GmbH
 *
 */
@XmlRootElement
public class ErrorCodes implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String value;

	private final int code;

	/**
	 * @param value
	 * @param code
	 */
	public ErrorCodes(String value, HttpStatus code) {
		this.value = value;
		this.code = code.value();
	}

	/**
	 *
	 * @return value
	 */
	@XmlAttribute
	public String getValue() {
		return this.value;
	}

	/**
	 *
	 * @return code
	 */
	@XmlAttribute
	public int getCode() {
		return this.code;
	}

	/**
	 * 
	 */
	public static final ErrorCodes OK = new ErrorCodes("ok", HttpStatus.OK);
	
	/**
	 * 
	 */
	public static final ErrorCodes ENTITY_NOT_FOUND = new ErrorCodes("not_found", HttpStatus.NOT_FOUND);
	
	/**
	 * 
	 */
	public static final ErrorCodes UNKNOWN_EMAIL = new ErrorCodes("unknown_email", HttpStatus.NOT_FOUND);
	
	/**
	 * 
	 */
	public static final ErrorCodes PASSWORD_TOO_LONG = new ErrorCodes("password_too_long", HttpStatus.FORBIDDEN);
	
	/**
	 * 
	 */
	public static final ErrorCodes PASSWORD_TOO_SHORT = new ErrorCodes("password_too_short", HttpStatus.FORBIDDEN);
	
	
	/**
	 * 
	 */
	public static final ErrorCodes UNAUTHORIZED = new ErrorCodes("unauthorized", HttpStatus.UNAUTHORIZED);
	
	/**
	 * 
	 */
	public static final ErrorCodes USER_NOT_AUTHENTICATED = new ErrorCodes("user_not_signedin", HttpStatus.UNAUTHORIZED);
	
	/**
	 * 
	 */
	public static final ErrorCodes INTERNAL_ERROR = new ErrorCodes("internal_error", HttpStatus.INTERNAL_SERVER_ERROR);
	
	/**
	 * 
	 */
	public static final ErrorCodes UNKNOWN_ACCOUNT = new ErrorCodes("unknown_account", HttpStatus.NOT_FOUND);
	
	/**
	 * 
	 */
	public static final ErrorCodes PASSWORD_NOT_SAME = new ErrorCodes("password_not_same", HttpStatus.FORBIDDEN);
	
	/**
	 * 
	 */
	public static final ErrorCodes ACCOUNT_LOCKED = new ErrorCodes("account_locked", HttpStatus.FORBIDDEN);
	
	/**
	 * <b>incorrect_parameter - BAD_REQUEST
	 */
	public static final ErrorCodes INCORRECT_PARAMETER = new ErrorCodes("incorrect_parameter", HttpStatus.BAD_REQUEST);
	
	/**
	 * <b>email_already_registered - FORBIDDEN
	 */
	public static final ErrorCodes EMAIL_ALREADY_REGISTERED = new ErrorCodes("email_already_registered", HttpStatus.FORBIDDEN);
	
	/**
	 * <b>email_is_banned - FORBIDDEN
	 */
	public static final ErrorCodes EMAIL_IS_BANNED = new ErrorCodes("email_is_banned", HttpStatus.FORBIDDEN);
	
	/**
	 * <b>user_not_verified - FORBIDDEN
	 */
	public static final ErrorCodes USER_NOT_VERIFIED = new ErrorCodes("user_not_verified", HttpStatus.FORBIDDEN);
	
	/**
	 * <b>user_already_verified - FORBIDDEN
	 */
	public static final ErrorCodes ALREADY_VERIFIED = new ErrorCodes("user_already_verified", HttpStatus.FORBIDDEN);
	
	/**
	 * <b>user_not_verified - FORBIDDEN
	 */
	public static final ErrorCodes USER_INACTIVE = new ErrorCodes("user_is_inactive", HttpStatus.FORBIDDEN);
	
	/**
	 * 
	 */
	public static final ErrorCodes INVITATION_NOT_POSSIBLE = new ErrorCodes("invitation_not_possible", HttpStatus.FORBIDDEN);
	
	/**
	 * <b>review_already_exists - CONFLICT
	 */
	public static final ErrorCodes REVIEW_ALREADY_EXISTS = new ErrorCodes("review_already_exists", HttpStatus.CONFLICT);

	public static final ErrorCodes ENTITY_BYNAME_EXISTS = new ErrorCodes("alreadyExistsByName", HttpStatus.CONFLICT);
	
	/**
	 * <b>entity_is_referenced - FORBIDDEN
	 */
	public static final ErrorCodes ENTITY_IS_REFERENCED = new ErrorCodes("entity_is_referenced", HttpStatus.FORBIDDEN);
	
	/**
	 * <b>treatment_review_summary_is_not_suggestion - FORBIDDEN
	 */
	public static final ErrorCodes TREATMENT_REVIEW_SUMMARY_IS_NOT_SUGGESTION = new ErrorCodes("treatment_review_summary_is_not_suggestion", HttpStatus.FORBIDDEN);
}
