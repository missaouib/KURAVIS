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
 * TreatmentReviewSummaryService.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 24.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.treatment_review_summary;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.services.entity.EntityService;
import com.mobileman.kuravis.core.services.util.CRUDAction;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public interface TreatmentReviewSummaryService extends EntityService<TreatmentReviewSummary> {
	
	/**
	 * @param summary
	 * @return suggestion ID
	 */
	String createSuggestion(TreatmentReviewSummary summary);

	/**
	 * @param diseaseId
	 * @param page
	 * @return all summaries filtered by disease
	 */
	List<DBObject> findAllByDisease(String diseaseId, Pageable page);

	/**
	 * @return all diseases index
	 */
	List<DBObject> diseasesIndex();
	
	/**
	 * @param index
	 * @return all diseases (grouped by disease) with treatments by disease name prefix
	 */
	List<DBObject> findAllDiseasesWithTreatmentsByIndex(String index);

	/**
	 * @param treatmentEvent
	 * @param oldTreatmentEvent 
	 * @param action 
	 */
	void updateTreatmentDurationStatistics(TreatmentEvent treatmentEvent, TreatmentEvent oldTreatmentEvent, CRUDAction action);
		
	/**
	 * @param review 
	 * @param oldReview 
	 * @param reviewCrudAction 
	 */
	void computeTreatmentReviewSummaryStatistics(DBObject review, DBObject oldReview, CRUDAction reviewCrudAction);
}
