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
 * TreatmentReviewSummaryUtilTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 29.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.mobileman.kuravis.core.domain.event.FrequencyType;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentCostStatistics;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentDurationStatistics;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public class TreatmentReviewSummaryUtilTest {

	/**
	 * @throws Exception
	 */
	@Test
	public void computeCostStatistics_Review() throws Exception {
		List<DBObject> stats = TreatmentReviewSummaryUtil.computeCostStatistics((DBObject)null);
		assertNotNull(stats);
		assertEquals(5, stats.size());
		for (DBObject dbObject : stats) {
			assertEquals(0, dbObject.get(TreatmentReviewSummary.COUNT));
		}
		
		DBObject review = new BasicDBObject();
		stats = TreatmentReviewSummaryUtil.computeCostStatistics(review);
		assertNotNull(stats);
		assertEquals(5, stats.size());
		for (DBObject dbObject : stats) {
			assertEquals(0, dbObject.get(TreatmentReviewSummary.COUNT));
		}
		
		//////////////////////////
		review.put(TreatmentReview.TREATMENT_PRICE, 12.0d);
		review.put(TreatmentReview.TREATMENT_QUANTITY, 2.0d);
		stats = TreatmentReviewSummaryUtil.computeCostStatistics(review);
		assertNotNull(stats);
		assertEquals(5, stats.size());
		assertEquals(1, stats.get(0).get(TreatmentReviewSummary.COUNT));
		assertEquals(0, stats.get(1).get(TreatmentReviewSummary.COUNT));
		assertEquals(0, stats.get(2).get(TreatmentReviewSummary.COUNT));
		assertEquals(0, stats.get(3).get(TreatmentReviewSummary.COUNT));
		assertEquals(0, stats.get(4).get(TreatmentReviewSummary.COUNT));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void computeTreatmentCostGroupId() throws Exception {
		assertEquals(TreatmentCostStatistics.CATEGORY_UNDEFINED, TreatmentCostStatistics.computeCategoryId(null));
		
		assertEquals(0, TreatmentCostStatistics.computeCategoryId(new BigDecimal(24.99d)));
		
		assertEquals(1, TreatmentCostStatistics.computeCategoryId(new BigDecimal(25.0d)));
		assertEquals(1, TreatmentCostStatistics.computeCategoryId(new BigDecimal(50.99d)));
		
		assertEquals(2, TreatmentCostStatistics.computeCategoryId(new BigDecimal(51.00d)));
		
		assertEquals(3, TreatmentCostStatistics.computeCategoryId(new BigDecimal(101.00d)));
		
		assertEquals(3, TreatmentCostStatistics.computeCategoryId(new BigDecimal(200.99d)));
		assertEquals(4, TreatmentCostStatistics.computeCategoryId(new BigDecimal(201.00d)));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void computeTreatmentDurationGroupId() throws Exception {
		assertEquals(TreatmentDurationStatistics.CATEGORY_UNDEFINED, TreatmentDurationStatistics.computeCategoryId(null));
		
		assertEquals(1, TreatmentDurationStatistics.computeCategoryId(24));
		assertEquals(2, TreatmentDurationStatistics.computeCategoryId(24 * 7));
		assertEquals(3, TreatmentDurationStatistics.computeCategoryId((24 * 7 * 4) + 24*2));
		assertEquals(2, TreatmentDurationStatistics.computeCategoryId((24 * 7 * 4) - 1));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void computeTreatmentDurationGroupId_TreatmentEvent() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		assertEquals(TreatmentDurationStatistics.CATEGORY_UNDEFINED, TreatmentDurationStatistics.computeCategoryId(null));
		
		TreatmentEvent event = new TreatmentEvent();
		event.setFrequency(FrequencyType.ONCE);
		assertEquals(0, TreatmentDurationStatistics.computeCategoryId(event));
		
		event.setFrequency(FrequencyType.DAILY);
		event.setStart(dateFormat.parse("2013-01-01"));
		event.setEnd(dateFormat.parse("2013-01-07"));
		assertEquals(1, TreatmentDurationStatistics.computeCategoryId(event));

		event.setEnd(dateFormat.parse("2013-01-08"));
		assertEquals(2, TreatmentDurationStatistics.computeCategoryId(event));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void computeTreatmentDurationStatistics() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		assertEquals(5, TreatmentReviewSummaryUtil.computeTreatmentDurationStatistics(null).size());
		
		TreatmentEvent event = new TreatmentEvent();		
		event.setFrequency(FrequencyType.DAILY);
		event.setStart(dateFormat.parse("2013-01-01"));
		event.setEnd(dateFormat.parse("2013-01-30"));
		
		List<DBObject> stats = TreatmentReviewSummaryUtil.computeTreatmentDurationStatistics(Arrays.asList(event));
		assertEquals(5, stats.size());
		assertEquals(0, stats.get(0).get(TreatmentReviewSummary.COUNT));
		assertEquals(0, stats.get(1).get(TreatmentReviewSummary.COUNT));
		assertEquals(1, stats.get(2).get(TreatmentReviewSummary.COUNT));
		assertEquals(0, stats.get(3).get(TreatmentReviewSummary.COUNT));
		assertEquals(0, stats.get(4).get(TreatmentReviewSummary.COUNT));
	}
}
