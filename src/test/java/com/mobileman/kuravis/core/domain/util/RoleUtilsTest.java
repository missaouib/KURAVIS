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
 * RoleUtilsTest.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 17.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.mobileman.kuravis.core.domain.user.Roles;
import com.mongodb.BasicDBObject;

/**
 * @author MobileMan GmbH
 *
 */
public class RoleUtilsTest {

	/**
	 * @throws Exception
	 */
	@Test
	public void testIsAdmin() throws Exception {
		assertFalse(RoleUtils.isAdminAccount(null));
		
		assertFalse(RoleUtils.isAdminAccount(new BasicDBObject()));
		
		assertFalse(RoleUtils.isAdminAccount(new BasicDBObject("roles", new ArrayList<>())));
		
		List<String> roles = new ArrayList<>();
		assertFalse(RoleUtils.isAdminAccount(new BasicDBObject("roles", roles)));
		
		roles = Arrays.asList(Roles.USER);
		assertFalse(RoleUtils.isAdminAccount(new BasicDBObject("roles", roles)));
		
		roles = Arrays.asList(Roles.ADMIN);
		assertTrue(RoleUtils.isAdminAccount(new BasicDBObject("roles", roles)));
		
		roles = Arrays.asList(Roles.USER, Roles.ADMIN);
		assertTrue(RoleUtils.isAdminAccount(new BasicDBObject("roles", roles)));
	}
}
