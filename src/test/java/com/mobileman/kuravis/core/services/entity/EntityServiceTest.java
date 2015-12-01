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
package com.mobileman.kuravis.core.services.entity;

import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.Data;
import com.mongodb.DBObject;

@ContextConfiguration(locations={"/spring/application-context.xml"})
public class EntityServiceTest extends AbstractIntegrationTest {
	
	@Autowired
	private CommonEntityService entityService;
	
	@BeforeClass
	public static void setupClass() {
		
	}
	
	@Override
	@Before
	public void setUp() {
		Data.setUp(mongoTemplate, userService);
	}

	@Test
	public void findUserById() throws Exception {
		DBObject user = entityService.findById("user", "xyz");
		assertNull(user);
	}
	
	@Override
	public String getCollectionName() {
		return "user";
	}
	

}
