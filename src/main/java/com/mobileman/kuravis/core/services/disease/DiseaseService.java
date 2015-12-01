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
package com.mobileman.kuravis.core.services.disease;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.services.entity.EntityService;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public interface DiseaseService extends EntityService<Disease> {

	/**
	 * @param top 
	 * @return top n diseases ()
	 */
	List<DBObject> findTopDiseases(int top);
	
	/**
	 * @param diseaseId
	 * @param treatmentReviewId
	 * @return true if disease is not associated to any other review but given
	 */
	boolean diseaseHasNoOtherReviews(String diseaseId, String treatmentReviewId);
	
	/**
	 * 
	 * @param sourceId old Disease Id
	 * @param targetId new Disease Id
	 */
	void merge(String sourceId, String targetId);

	/**
	 * @param page
	 * @return { disease:{ _id, name }, summariesCount:1, suggestedTreatments:[{ _id, name}]}
	 */
	List<DBObject> findAllDiseasesWithTreatmentsForSuggestionAdmin(Pageable page);
}
