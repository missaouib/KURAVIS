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
 * TreatmentReviewUtil.java
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

import java.math.BigDecimal;

/**
 * @author MobileMan GmbH
 *
 */
public abstract class TreatmentReviewUtil {

	/**
	 * @param rating
	 * @return converted value
	 */
	public static BigDecimal convertRating(BigDecimal rating) {
		if(rating == null)	{
			return null;
		}
		
		//0 -> 5
		//0.25 -> 4
		//0.5 -> 3
		//0.75 -> 2
		//1 -> 1
		int decimalPlaces = 2;
		int r = 1;
		for(int i=0;i<decimalPlaces;i++){
			r *= 10;
		}
		
		return BigDecimal.valueOf(Math.round((5 + rating.doubleValue() * (-4)) * r) / r);
	}

}
