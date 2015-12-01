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
package com.mobileman.kuravis.core.services.treatment;

import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.services.entity.EntityService;

/**
 * @author MobileMan GmbH
 *
 */
public interface TreatmentService extends EntityService<Treatment> {

	/**
	 * @param treatmentId
	 * @param treatmentReviewId
	 * @return true if treatment is not associated to any other review but given
	 */
	boolean treatmentHasNoOtherReviews(String treatmentId, String treatmentReviewId);
	
	/**
	 * 
	 * @param sourceId old treatment Id
	 * @param targetId new treatment Id
	 */
	void merge(String sourceId, String targetId);
}
