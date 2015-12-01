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
 * TreatmentTypeServiceTest.java
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.Ehcache;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.domain.option_list.treatment_type.TreatmentType;
import com.mobileman.kuravis.core.services.option_list.physiotherapie.PhysiotherapieService;
import com.mobileman.kuravis.core.services.option_list.psychotherapy.PsychotherapyService;
import com.mobileman.kuravis.core.services.option_list.treatment_type.TreatmentTypeService;
import com.mobileman.kuravis.core.services.option_list.unit.UnitService;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 * 
 */
@ContextConfiguration(locations = { "/spring/application-context.xml" })
public class TreatmentTypeServiceTest extends AbstractIntegrationTest {

	private static final List<TreatmentType> TREATMENT_TYPES;

	@Autowired
	private CacheManager cacheManager;

	static {
		URL url = TreatmentTypeServiceTest.class.getResource("/data/treatment_type.json");
		TreatmentType[] array = {};
		try {
			array = new ObjectMapper().readValue(new File(url.getFile()), TreatmentType[].class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		TREATMENT_TYPES = Arrays.asList(array);
	}

	@Autowired
	private UnitService unitService;

	@Autowired
	private PhysiotherapieService physiotherapieService;
	@Autowired
	private PsychotherapyService psychotherapyService;

	@Autowired
	private TreatmentTypeService treatmentTypeService;

	/**
	 * @throws Exception
	 */
	@Test
	public void testFindById() throws Exception {
		DBObject treatmentType = treatmentTypeService.findById(getTreatmentTypes().get(0).get_id());
		assertNotNull(treatmentType);

		DBObject treatmentType2 = treatmentTypeService.findById(getTreatmentTypes().get(0).get_id());
		assertNotNull(treatmentType);
		assertSame(treatmentType, treatmentType2);
	}

	/**
	 * @return all TREATMENT_TYPES
	 */
	public static List<TreatmentType> getTreatmentTypes() {
		return TREATMENT_TYPES;
	}

	@Test
	public void cacheTest() throws Exception {
		Cache cache = cacheManager.getCache(TreatmentType.ENTITY_NAME);
		Ehcache nativeCache = (Ehcache) cache.getNativeCache();
		cache.clear();
		String id = getTreatmentTypes().get(0).get_id();
		treatmentTypeService.findById(id);
		Assert.assertEquals(1, nativeCache.getStatistics().cacheMissCount());
		Assert.assertEquals(1, nativeCache.getSize());
		Assert.assertTrue(nativeCache.isKeyInCache(id));
		treatmentTypeService.findById(id);
		Assert.assertEquals(1, nativeCache.getStatistics().cacheHitCount());
	}

}
