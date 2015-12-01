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
 * HealtPlatformSystemServiceTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 13.1.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.system;

import static org.junit.Assert.assertEquals;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.domain.system.VersionInfo;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class HealtPlatformSystemServiceTest extends AbstractIntegrationTest {

	@Autowired
	private HealthPlatformSystemService healtPlatformSystemService;
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testVersion() throws Exception {
		
		VersionInfo version = healtPlatformSystemService.getVersion();
		assertEquals("1.0.0", version.getModelVersion());
		assertEquals("1.4.0-SNAPSHOT", version.getSystemVersion());
		
		ObjectMapper mapper = new ObjectMapper();
		
		String versionString = mapper.writerWithType(VersionInfo.class).writeValueAsString(version);
		assertEquals("{\"modelVersion\":\"1.0.0\",\"systemVersion\":\"1.4.0-SNAPSHOT\"}", versionString);
	}
}
