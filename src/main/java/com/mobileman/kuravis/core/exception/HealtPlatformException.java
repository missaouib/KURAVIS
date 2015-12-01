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
 * HealtPlatformException.java
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

import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public class HealtPlatformException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final DBObject result;

	/**
	 * @param result 
	 * 
	 */
	public HealtPlatformException(DBObject result) {
		super();
		this.result = result;
	}
	
	/**
	 * @param result 
	 * @param e 
	 * 
	 */
	public HealtPlatformException(DBObject result, Throwable e) {
		super(e);
		this.result = result;
	}
	
	public HealtPlatformException(String message, ErrorCodes code) {
		this(ErrorUtils.error(message, code));
	}

	/** 
	 * {@inheritDoc}
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		if (getResult() != null) {
			return ErrorUtils.getMessage(getResult());
		}
		
		return super.getMessage();
	}
	
	/**
	 *
	 * @return result
	 */
	public DBObject getResult() {
		return this.result;
	}
}
