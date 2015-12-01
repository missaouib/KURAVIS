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
package com.mobileman.kuravis.core.services.treatment_review;

import java.util.List;

import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.services.entity.EntityService;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public interface TreatmentReviewService extends EntityService<TreatmentReview> {

	/**
	 * @param data
	 * @return error message in case of error
	 */
	DBObject createTreatmentReview(DBObject data);
	
	/**
	 * @param data
	 * @return error message in case of error
	 */
	DBObject createTreatmentReviewForSubscription(DBObject data);
	
	/**
	 * @param diseaseId
	 * @param treatmentId
	 * @param projections
	 * @return cursor
	 */
	DBCursor findAllTreatmentReviews(String diseaseId, String treatmentId, String... projections);
	
	/**
	 * @param entityId
	 * @return result data from vote response
	 */
	DBObject voteForTreatmentReview(String entityId);

	/**
	 * Deletes all treatment reviews created byt given user
	 * @param userId
	 * @return error message in case of error, ok in case of success
	 */
	DBObject deleteAllTreatmentReviews(String userId);

	/**
	 * @param entityId
	 * @param data
	 * @return error message in case of error, ok in case of success
	 */
	DBObject commentTreatmentReview(String entityId, DBObject data);
	
	 /**
	 * @param id
	 * @return tretment review by id with additional information for called user
	 */
	DBObject findByIdForUser(String id);

	/**
	 * @param newUserData
	 * @param oldUserData
	 */
	void updateTretmentReviewStatistics(DBObject newUserData, DBObject oldUserData);

	/**
	 * @param diseaseId
	 * @param treatmentId
	 * @return true if review already exists for currently signedin user
	 */
	boolean reviewAlreadyExistsForUser(String diseaseId, String treatmentId);
	
	/**
	 * @param review 
	 * @param author 
	 * @return update result
	 */
	public DBObject update(DBObject review, DBObject author);

	/**
	 * @param commentId
	 */
	void deleteTreatmentReviewComment(String commentId);
	
	List<DBObject> findUserReviewsGoupByDisease(String userId);
	
	String getReviewId(String userId, String diseaseId, String treatmentId);	
}
