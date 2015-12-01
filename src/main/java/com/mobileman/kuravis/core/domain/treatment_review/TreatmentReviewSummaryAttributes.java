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
 * TreatmentReviewSummaryAttributes.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 20.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.treatment_review;

import com.mobileman.kuravis.core.domain.Attributes;

/**
 * @author MobileMan GmbH
 *
 */
public interface TreatmentReviewSummaryAttributes extends Attributes {

	/**
	 * 
	 */
	String ENTITY_NAME = "treatmentreviewsummary";
	
	/**
	 * 
	 */
	String DISEASE = "disease";
	
	/**
	 * 
	 */
	String TREATMENT = "treatment";

	/**
	 * 
	 */
	String SIDE_EFFECTS = "sideEffects";

	/**
	 * 
	 */
	String RATING = "rating";
	

	/**
	 * 
	 */
	String RATINGS = "ratings";

	/**
	 * 
	 */
	String VOTES_COUNT = "ageStatistics";

	/**
	 * 
	 */
	String GENDER_STATISTICS = "genderStatistics";
	
	/**
	 * 
	 */
	String COSTS_STATISTICS = "costsStatistics";
	
	/**
	 * 
	 */
	String TREATMENT_DURATION_STATISTICS = "treatmentDurationStatistics";

	/**
	 * 
	 */
	String REVIEWS_COUNT = "reviewsCount";
	
	/**
	 * 
	 */
	String SUGGESTION = "suggestion";
	
	/**
	 * 
	 */
	String COUNT = "count";
	
	/**
	 * 
	 */
	String CATEGORY = "category";
}
