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

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

/**
 * @author MobileMan GmbH
 *
 */
public class ErrorUtils {

	/**
	 * 
	 */
	public static final String ERROR = "error";
	
	/**
	 * 
	 */
	public static final String ATTR_MESSAGE = "message";
	
	/**
	 * 
	 */
	public static final String ATTR_CODE = "code";
	
	/**
	 * 
	 */
	public static final String ATTR_RESULT = "result";
	
	/**
	 * 
	 */
	public static final String ATTR_STATUS = "status";
	
	/**
	 * 
	 */
	public static final String OK = "ok";

	/**
	 * @param result
	 * @return if given dataholder contains error result
	 */
	public static boolean isError(DBObject result) {
		return result != null && result.get(EntityUtils.ATTR_RESULT) != null && result.get(EntityUtils.ATTR_RESULT).equals(ErrorUtils.ERROR);
	}

	/**
	 * @param code
	 * @return json error result with given message
	 */
	public static DBObject error(ErrorCodes code) {
		return error(null, code);
	}
	
	/**
	 * @param message
	 * @return json error result with given message
	 */
	public static DBObject error(String message) {
		return error(message, ErrorCodes.INTERNAL_ERROR);
	}
	
	/**
	 * @param message error message
	 * @param code 
	 * @return json error result with given message
	 */
	public static DBObject error(String message, ErrorCodes code) {
		return BasicDBObjectBuilder.start()
				.add(EntityUtils.ATTR_RESULT, ErrorUtils.ERROR)
				.add(ATTR_MESSAGE, message)
				.add(ATTR_CODE, code.getValue())
				.add(ATTR_STATUS, code.getCode())
				.get();
	}
	
	/**
	 * @return json success result
	 */
	public static DBObject success() {
		return success(HttpStatus.OK);
	}

	/**
	 * @param result
	 * @return HttpStatus from result
	 */
	public static HttpStatus getStatus(DBObject result) {
		if (result == null) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		if (result.get(ATTR_STATUS) == null) {
			return HttpStatus.OK;
		}
		
		return HttpStatus.valueOf((int)result.get(ATTR_STATUS));
	}

	/**
	 * @param e
	 * @return DBObject
	 */
	public static DBObject createErrorResult(Exception e) {
		return BasicDBObjectBuilder.start()
				.add(EntityUtils.ATTR_RESULT, ErrorUtils.ERROR)
				.add(ATTR_MESSAGE, e.getMessage())
				.add(ATTR_CODE, ErrorCodes.INTERNAL_ERROR.getValue())
				.add(ATTR_STATUS, ErrorCodes.INTERNAL_ERROR.getCode())
				.get();
	}

	/**
	 * @param result
	 * @return ATTR_MESSAGE content
	 */
	public static String getMessage(DBObject result) {
		return (String)result.get(ATTR_MESSAGE);
	}

	/**
	 * @param status
	 * @return DBObject
	 */
	public static DBObject success(HttpStatus status) {
		return BasicDBObjectBuilder.start()
				.add(EntityUtils.ATTR_RESULT, ErrorCodes.OK.getValue())
				.add(ATTR_CODE, status.getReasonPhrase())
				.add(ATTR_STATUS, status.value())
				.get();
	}

	/**
	 * @param result
	 * @return INTERNAL_ERROR
	 */
	public static DBObject error(WriteResult result) {
		return error(result.getError(), ErrorCodes.INTERNAL_ERROR);
	}

	/**
	 * @param result
	 * @return true if result has finished with error
	 */
	public static boolean isError(WriteResult result) {
		return !StringUtils.isEmpty(result.getError());
	}

	/**
	 * @param code
	 * @return HealtPlatformException
	 */
	public static HealtPlatformException exception(ErrorCodes code) {
		return new HealtPlatformException(error(code));
	}
	
	/**
	 * @param message
	 * @param code
	 * @return HealtPlatformException
	 */
	public static HealtPlatformException exception(String message, ErrorCodes code) {
		return new HealtPlatformException(error(message, code));
	}
	
	/**
	 * @param message
	 * @return HealtPlatformException
	 */
	public static HealtPlatformException exception(String message) {
		return new HealtPlatformException(error(message));
	}
	
	/**
	 * @param message
	 * @param e 
	 * @return HealtPlatformException
	 */
	public static HealtPlatformException exception(String message, Throwable e) {
		return new HealtPlatformException(error(message), e);
	}
	
	/**
	 * @param result
	 * @return HealtPlatformException
	 */
	public static HealtPlatformException exception(WriteResult result) {
		Integer code =  (Integer)result.getField(ATTR_CODE);
		if (code.equals(new Integer(11000))) {
			throw new com.mongodb.MongoException.DuplicateKey(result.getLastError());
		}
		
		return new HealtPlatformException(error(result));
	}
}
