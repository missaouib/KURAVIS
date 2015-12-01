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
 * FraudReportCategory.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 12.2.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.fraud_report;

/**
 * @author MobileMan GmbH
 *
 */
public enum FraudReportCategory {

	/**
	 * 
	 */
	SPAM(1),
	
	/**
	 * 
	 */
	ABUSIVE_CRITICISM(2),
	
	/**
	 * 
	 */
	UNPROFESSIONAL_REVIEW(3),
	
	/**
	 * 
	 */
	OTHERS(4);
	
	private final Integer value;
	
	FraudReportCategory(int val) {
		this.value = Integer.valueOf(val);
	}
	
	/**
	 * @return value
	 */
	public Integer getValue() {
		return this.value;
	}
	
	/**
	 * @param _v
	 * @return FraudReportCategory
	 */
	public static FraudReportCategory parse(int _v) {
		switch (_v) {
		case 1:
			return SPAM;
		case 2:
			return ABUSIVE_CRITICISM;
		case 3:
			return UNPROFESSIONAL_REVIEW;
		case 4:
			return OTHERS;
		default:
			break;
		}
		
		throw new IllegalArgumentException("Unsuported FraudReportCategory value: " + _v);
	}
}
