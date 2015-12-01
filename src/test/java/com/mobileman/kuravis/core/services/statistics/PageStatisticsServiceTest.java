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
 * PageStatisticsServiceTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 27.8.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.statistics;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.DBObject;
import static org.junit.Assert.*;
/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class PageStatisticsServiceTest extends AbstractIntegrationTest {

	@Autowired
	private PageStatisticsService pageStatisticsService;
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.AbstractIntegrationTest#getCollectionName()
	 */
	@Override
	public String getCollectionName() {
		return EntityUtils.PAGE_STATISTICS;
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void savePageStatistics_Error() throws Exception {
		DBObject result = pageStatisticsService.save("", null);
		assertTrue(ErrorUtils.isError(result));
		
		result = pageStatisticsService.save("", "param1");
		assertTrue(ErrorUtils.isError(result));
		
		result = pageStatisticsService.save("page1", "");
		assertTrue(ErrorUtils.isError(result));
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void savePageStatistics() throws Exception {
		
		DBObject stat = pageStatisticsService.computeStatistics("page1", "param1");
		assertEquals(0L, stat.get("viewsCount"));
		
		DBObject result = pageStatisticsService.save("page1", "param1");
		assertFalse(ErrorUtils.isError(result));
		
		stat = pageStatisticsService.computeStatistics("page1", "param1");
		assertEquals(1L, stat.get("viewsCount"));
		
		stat = pageStatisticsService.computeStatistics("page1", "param2");
		assertEquals(0L, stat.get("viewsCount"));
		
		result = pageStatisticsService.save("page1", "param1");
		assertFalse(ErrorUtils.isError(result));
		stat = pageStatisticsService.computeStatistics("page1", "param1");
		assertEquals(2L, stat.get("viewsCount"));
	}
}
