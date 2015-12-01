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
package com.mobileman.kuravis.core.domain.treatment_review;

import com.mobileman.kuravis.core.domain.Attributes;

public interface TreatmentReviewAttributes extends Attributes {
	String ENTITY_NAME = "treatmentreview";
	
	String DISEASE = "disease";
	String TREATMENT = "treatment";
	String TEXT = "text";
	String SIDE_EFFECTS = "sideEffects";
	String RATING = "rating";
	String AUTHOR = "author";
	String VOTES_COUNT = "votesCount";
	String LAST_VOTED = "lastVotedOn";
	String LAST_COMMENTED = "lastCommentedOn";
	String COMMENTS_COUNT = "reviewCommentsCount";
	String RECENT_EVENT = "recentEvent";

	String DATE_OF_FIRST_SYMPTOMS = "dateOfFirstSymptoms";
	String DATE_OF_DIAGNOSIS = "dateOfDiagnosis";
	String CURED = "cured";
	
	//costs
	String CURRENCY = "currency";
	String DOCTOR_COSTS = "doctorCosts";
	String TREATMENT_PRICE = "treatmentPrice";
	String TREATMENT_QUANTITY = "treatmentQuantity";
	String INSURANCE_COVERED = "insuranceCovered";
	String INSURANCE_COVERAGE = "insuranceCoverage";
	String COINSURANCE = "coinsurance";
}
