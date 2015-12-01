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
 * SystemServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 13.1.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.system.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.system.HealthPlatformSystem;
import com.mobileman.kuravis.core.domain.system.VersionInfo;
import com.mobileman.kuravis.core.services.system.HealthPlatformSystemService;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class HealthPlatformSystemServiceImpl implements HealthPlatformSystemService {

	@Autowired
    private MongoTemplate mongoTemplate;
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.system.HealthPlatformSystemService#getVersion()
	 */
	@Override
	public VersionInfo getVersion() {
		initSystem();
		List<HealthPlatformSystem> list = this.mongoTemplate.findAll(HealthPlatformSystem.class);
		VersionInfo versionInfo = new VersionInfo();
		versionInfo.setModelVersion(list.get(0).getVersion());
		versionInfo.setSystemVersion(getPomVersion());
		
		return versionInfo;
	}
	
	private String getPomVersion() {
	    String version = "";

	    // try to load from maven properties first
	    try {
	        Properties p = new Properties();
	        InputStream is = getClass().getResourceAsStream("/system.properties");
	        if (is != null) {
	            p.load(is);
	            version = p.getProperty("MAVEN_PROJECT_VERSION", "");
	        }
	    } catch (Exception e) {
	        // ignore
	    }

	    // fallback to using Java API
	    if (version == null || version.trim().length() == 0) {
	        Package aPackage = getClass().getPackage();
	        if (aPackage != null) {
	            version = aPackage.getImplementationVersion();
	            if (version == null) {
	                version = aPackage.getSpecificationVersion();
	            }
	        }
	    }
	    
	    return version;
	} 

	/**
	 * 
	 */
	private void initSystem() {
		if (this.mongoTemplate.count(new Query(), HealthPlatformSystem.class) > 0) {
			return;
		}
		
		HealthPlatformSystem system = new HealthPlatformSystem();
		system.setId(HealthPlatformSystem.DEFAULT_SYSTEM_ID);
		system.setVersion("1.0.0");
		this.mongoTemplate.save(system);
	}

}
