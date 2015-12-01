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
 * TreatmentReviewRatingStatistics.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 13.4.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.treatment_review.statistics;

import java.math.BigDecimal;

/**
 * @author MobileMan GmbH
 *
 */
public class TreatmentRatingStatistics {

	private BigDecimal name;
	
	private int count;

	/**
	 *
	 * @return name
	 */
	public BigDecimal getName() {
		return this.name;
	}

	/**
	 *
	 * @param name name
	 */
	public void setName(BigDecimal name) {
		this.name = name;
	}

	/**
	 *
	 * @return count
	 */
	public int getCount() {
		return this.count;
	}

	/**
	 *
	 * @param count count
	 */
	public void setCount(int count) {
		this.count = count;
	}
	
	
}
