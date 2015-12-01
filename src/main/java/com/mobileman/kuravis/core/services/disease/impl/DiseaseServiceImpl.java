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
package com.mobileman.kuravis.core.services.disease.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.disease.DiseaseAttributes;
import com.mobileman.kuravis.core.domain.event.Event;
import com.mobileman.kuravis.core.domain.fraud_report.FraudReport;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.domain.user.notification.UserNotification;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class DiseaseServiceImpl extends AbstractEntityServiceImpl<Disease> implements DiseaseService, DiseaseAttributes {
	
	@Autowired
	private TreatmentService treatmentService;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;
	
	@Override
	protected String getEntityName() {
		return ENTITY_NAME;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.disease.DiseaseService#findTopDiseases(int)
	 */
	@Override
	public List<DBObject> findTopDiseases(int top) {
		
		DBCursor cursor = getMongoTemplate().getCollection(ENTITY_NAME).find(
				new BasicDBObject(TREATMENT_REVIEWS_COUNT, new BasicDBObject("$gt", 0)), 
				new BasicDBObject(EntityUtils.ID, 1).append(NAME, 1).append(TREATMENT_REVIEWS_COUNT, 1))
				.limit(top)
				.sort(new BasicDBObject(TREATMENT_REVIEWS_COUNT, -1));
		
		List<DBObject> result = new ArrayList<DBObject>();
		while (cursor.hasNext()) {
			DBObject disease = cursor.next();
			result.add(disease);
		}
		
		return result;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#delete(java.lang.String, java.lang.String)
	 */
	@Override
	@RequiresAuthentication
	public DBObject delete(String type, String id) {
		
		DBObject disease = findById(id);
		if (disease == null) {
			return ErrorUtils.success();
		}
		
		DBObject user = (DBObject) SecurityUtils.getSubject().getPrincipal();
		if (!EntityUtils.equals((DBObject)disease.get(Disease.USER), user)) {
			requiresAdminRole();
		}
		
		DBCursor cursor = getCollection(TreatmentReview.ENTITY_NAME).find(
				new BasicDBObject("disease." + EntityUtils.ID, id), 
				new BasicDBObject("treatment." + EntityUtils.ID, 1));
		
		for (DBObject dbObject : cursor) {
			String treatmentReviewId = (String) dbObject.get(EntityUtils.ID);
			DBObject treatment = (DBObject) dbObject.get("treatment");
			String treatmentId = (String) treatment.get(EntityUtils.ID);
			getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).remove(new BasicDBObject("treatmentReviewId", treatmentReviewId));
			String summaryId = EntityUtils.createTreatmentReviewSummaryId(id, treatmentId);
			getCollection(TreatmentReviewSummary.ENTITY_NAME).remove(new BasicDBObject(EntityUtils.ID, summaryId));
			getCollection(EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS).remove(new BasicDBObject("summaryId", summaryId));
			getCollection(TreatmentReview.ENTITY_NAME).remove(new BasicDBObject(EntityUtils.ID, treatmentReviewId));
			
			if (treatmentService.treatmentHasNoOtherReviews(treatmentId, treatmentReviewId)) {
				getCollection(EntityUtils.TREATMENT).remove(new BasicDBObject(EntityUtils.ID, treatmentId));
			}
		}
		
		getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).remove(new BasicDBObject("disease.name", disease.get(NAME)));
		
		return super.delete(type, id);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#update(java.lang.String, java.lang.String, com.mongodb.DBObject)
	 */
	@Override
	public DBObject update(String type, String id, DBObject disease) {
		DBObject result = validateName(type, id, disease);
		String dbName = EntityUtils.getEntityName(result);
		result = super.update(type, id, disease);
		if (ErrorUtils.isError(result)) {
			return result;
		}
		String givenName = EntityUtils.getEntityName(disease);
		if (givenName != null && !ObjectUtils.nullSafeEquals(givenName, dbName)) {
			getCollection(TreatmentReview.ENTITY_NAME).update(new BasicDBObject("disease." + EntityUtils.ID, id),
					new BasicDBObject("$set", new BasicDBObject("disease." + EntityUtils.NAME, givenName)), false, true);
			getCollection(TreatmentReviewSummary.ENTITY_NAME).update(new BasicDBObject("disease." + EntityUtils.ID, id),
					new BasicDBObject("$set", new BasicDBObject("disease." + EntityUtils.NAME, givenName)), false, true);
			getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).update(new BasicDBObject("disease." + EntityUtils.ID, id),
					new BasicDBObject("$set", new BasicDBObject("disease." + EntityUtils.NAME, givenName)), false, true);

			getCollection(EntityUtils.USER).update(new BasicDBObject("diseases.disease." + EntityUtils.ID, id),
					new BasicDBObject("$set", new BasicDBObject("diseases.$.disease." + EntityUtils.NAME, givenName)), false, true);
			
			getCollection(Event.ENTITY_NAME).update(new BasicDBObject("disease." + EntityUtils.ID, id),
					new BasicDBObject("$set", new BasicDBObject("disease." + EntityUtils.NAME, givenName)), false, true);
		}
		return result;
	}
	
	/**
	 * 
	 */
	public boolean diseaseHasNoOtherReviews(String diseaseId, String treatmentReviewId) {
		DBObject query = new BasicDBObject("disease." + ID, diseaseId).append(ID, new BasicDBObject("$ne", treatmentReviewId));
		long count = getCollection(TreatmentReview.ENTITY_NAME).count(query);
		return count == 0;
	}

	@Override
	@RequiresRoles(Roles.ADMIN)
	public void merge(String sourceId, String targetId) {
		requiresAdminRole(); // annotations doesn't work
		
		DBObject targetDisease = findById(targetId);
		if (sourceId == null || targetDisease == null) {
			return;
		}
		
		// find all treatment reviews by old/source Disease ID
		DBCursor reviews = getCollection(TreatmentReview.ENTITY_NAME).find(new BasicDBObject(TreatmentReview.DISEASE + "." + ID, sourceId));
		Map<String, String> reviewIdsMap = new HashMap<String, String>();
		
		// create new TRs for target treatment ID
		for (DBObject oldReview : reviews) {
			DBObject newReview = new BasicDBObject();
			EntityUtils.copyTreatmentReview(TreatmentReview.DISEASE, targetDisease, oldReview, newReview);
			DBObject newTreatmentReview = treatmentReviewService.createTreatmentReview(newReview);
			reviewIdsMap.put(EntityUtils.getEntityId(oldReview), EntityUtils.getEntityId(newTreatmentReview));
		}

		// set new TRs to {fraudReport, event, notification} and delete old TRs
		for (Iterator<String> iterator = reviewIdsMap.keySet().iterator(); iterator.hasNext();) {
			String oldId = iterator.next();
			String newId = reviewIdsMap.get(oldId);
			getCollection(EntityUtils.FRAUD_REPORT).update(new BasicDBObject(FraudReport.ATTR_ENTITY_ID, oldId), new BasicDBObject("$set", new BasicDBObject(FraudReport.ATTR_ENTITY_ID, newId)));
			getCollection(EntityUtils.FRAUD_REPORT_ITEM).update(new BasicDBObject(FraudReport.ATTR_ENTITY_ID, oldId), new BasicDBObject("$set", new BasicDBObject(FraudReport.ATTR_ENTITY_ID, newId)));
			getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).update(new BasicDBObject("treatmentReviewId", oldId), new BasicDBObject("$set", new BasicDBObject("treatmentReviewId", newId)));
			getCollection(UserNotification.ENTITY_NAME).update(new BasicDBObject("treatmentReviewId", oldId), new BasicDBObject("$set", new BasicDBObject("treatmentReviewId", newId)));
			treatmentReviewService.delete(oldId);
		}
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.disease.DiseaseService#findAllDiseasesWithTreatmentsForSuggestionAdmin(org.springframework.data.domain.Pageable)
	 */
	@Override
	public List<DBObject> findAllDiseasesWithTreatmentsForSuggestionAdmin(Pageable page) {
		
		List<DBObject> diseases = findAll(page, Disease.NAME + "," + Disease.ID);
		List<String> diseaseIds = new ArrayList<String>(diseases.size());
		Map<String, DBObject> diseaseId2Disease = new HashMap<String, DBObject>();
		for (DBObject disease : diseases) {
			String diseaseId = EntityUtils.getEntityId(disease);
			diseaseIds.add(diseaseId);
			diseaseId2Disease.put(diseaseId, disease);
			disease.put("summariesCount", new Integer(0));
		}
		
		Query query = Query.query(Criteria.where(TreatmentReviewSummary.DISEASE + "." + TreatmentReviewSummary.ID).in(diseaseIds));
		List<DBObject> summaries = findAllByQuery(TreatmentReviewSummary.ENTITY_NAME, query.getQueryObject(), new BasicDBObject(
				TreatmentReviewSummary.DISEASE, 1)
				.append(TreatmentReviewSummary.TREATMENT, 1)
				.append(TreatmentReviewSummary.SUGGESTION, 1));
		Map<String, List<DBObject>> disease2Tretaments = new HashMap<String, List<DBObject>>();
		for (DBObject summary : summaries) {
			
			DBObject disease = (DBObject) summary.get(TreatmentReviewSummary.DISEASE);
			DBObject treatment = (DBObject) summary.get(TreatmentReviewSummary.TREATMENT);
			Boolean suggestion = (Boolean) summary.get(TreatmentReviewSummary.SUGGESTION);
			String summaryId = (String) summary.get(TreatmentReviewSummary.ID);
			String diseaseId = EntityUtils.getEntityId(disease);
			if (suggestion == null || Boolean.FALSE.equals(suggestion)) {
				// it is not a suggestion
				Integer summariesCount = (Integer) diseaseId2Disease.get(diseaseId).get("summariesCount");
				if (summariesCount == null) {
					summariesCount = Integer.valueOf(1);
				} else {
					summariesCount = Integer.valueOf(summariesCount.intValue() + 1);
				}
				
				diseaseId2Disease.get(diseaseId).put("summariesCount", summariesCount);
				
			} else {
				// it is a suggestion - get suggested treatments
				List< DBObject> treatments = disease2Tretaments.get(EntityUtils.getEntityId(disease));
				if (treatments == null) {
					treatments = new ArrayList<DBObject>();
					disease2Tretaments.put(EntityUtils.getEntityId(disease), treatments);
				}
				
				treatment.put("treatmentReviewSummaryId", summaryId);
				treatments.add(treatment);
			}
		}
		
		for (DBObject disease : diseases) {
			disease.put("suggestedTreatments", disease2Tretaments.get(EntityUtils.getEntityId(disease)));
		}
		
		return diseases;
	}
}
