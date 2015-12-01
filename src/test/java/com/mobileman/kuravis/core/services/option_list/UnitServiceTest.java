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
 * UnitServiceTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 19.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.option_list;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.*;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.domain.option_list.unit.Unit;
import com.mobileman.kuravis.core.services.option_list.unit.UnitService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class UnitServiceTest extends AbstractIntegrationTest {
	
	private static final List<Unit> UNITS;
	
	static {
		URL url = UnitServiceTest.class.getResource("/data/units.json");
		Unit[] array = {};
		try {
			array = new ObjectMapper().readValue(new File(url.getFile()), Unit[].class);
			UNITS = Arrays.asList(array);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Autowired
	private UnitService unitService;

	/**
	 * @throws Exception
	 */
	@Test
	public void testFindById() throws Exception {
		
		String unitId = getUnits().get(0).get_id();
		DBObject unit = unitService.findById(unitId);
		assertNotNull(unit);
		
		DBObject unit2 = unitService.findById(unitId);
		assertNotNull(unit);
		
		assertSame(unit, unit2);
		
		unitService.update(unit);
		unit2 = unitService.findById(unitId);
		assertNotNull(unit);
		
		assertNotSame(unit, unit2);
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testFindAll() throws Exception {
		
		String unitId = getUnits().get(0).get_id();
		DBObject unit = unitService.findById(unitId);
		assertNotNull(unit);
		
		List<DBObject> units = unitService.findAll();
		assertNotNull(units);
		
		List<DBObject> units2 = unitService.findAll();
		assertNotNull(units2);
		
		assertSame(units, units2);
		
		DBObject unit2Update = new BasicDBObject();
		unit2Update.put(Unit.ID, unit.get(Unit.ID));
		unitService.update(unit);
		units2 = unitService.findAll();
		assertNotNull(units2);
		assertSame(units, units2);
		
		DBObject unit2 = unitService.findById(unitId);
		assertNotSame(unit, unit2);
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testCreate_Update_Delete() throws Exception {
		
		Unit unit = new Unit();
		unit.setName("testCreate_Update");
		String _id = unitService.create(unit);
		assertNotNull(_id);
		
		assertNotNull(unitService.findByProperty(Unit.NAME, "testCreate_Update"));
		
		unit.setName("testCreate_UpdateX");
		DBObject updateCommand = new BasicDBObject("$set", new BasicDBObject(Unit.NAME, "testCreate_UpdateX"));
		unitService.update(_id, updateCommand);
		assertNotNull(unitService.findByProperty(Unit.NAME, "testCreate_UpdateX"));
		
		unitService.delete(_id);
	}

	/**
	 * @return all units
	 */
	public static List<Unit> getUnits() {
		return UNITS;
	}
}
