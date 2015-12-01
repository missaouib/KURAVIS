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
 * TreatmentReviewUtilTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 26.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.util;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * @author MobileMan GmbH
 *
 */
public class TreatmentReviewUtilTest {

	/**
	 * @throws Exception
	 */
	@Test
	public void convertRating() throws Exception {
		
		BigDecimal result = TreatmentReviewUtil.convertRating(new BigDecimal("0"));
		assertEquals(result, new BigDecimal("5"));
		
		result = TreatmentReviewUtil.convertRating(new BigDecimal("0.1"));
		assertEquals(result, new BigDecimal("4"));
		
		result = TreatmentReviewUtil.convertRating(new BigDecimal("0.3333"));
		assertEquals(result, new BigDecimal("3"));
		
		result = TreatmentReviewUtil.convertRating(new BigDecimal("0.5"));
		assertEquals(result, new BigDecimal("3"));
		
		result = TreatmentReviewUtil.convertRating(new BigDecimal("0.8"));
		assertEquals(result, new BigDecimal("1"));
	}
}
