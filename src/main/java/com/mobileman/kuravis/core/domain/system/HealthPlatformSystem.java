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
 * System.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 13.1.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.system;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mobileman.kuravis.core.domain.util.EntityUtils;

/**
 * @author MobileMan GmbH
 *
 */
@Document(collection = EntityUtils.SYSTEM)
public class HealthPlatformSystem {

	@Id
	private String id;

	private String version;
	
	/**
	 * 
	 */
	public static final String DEFAULT_SYSTEM_ID = "e9a19351-cecd-4a59-8ed3-0845f10da523";

	
	/**
	 *
	 * @return id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 *
	 * @param id id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 *
	 * @return version
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 *
	 * @param version version
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	
}
