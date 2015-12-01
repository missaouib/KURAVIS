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
package com.mobileman.kuravis.core.services.treatment.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.mobileman.kuravis.core.domain.Attributes;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.event.Event;
import com.mobileman.kuravis.core.domain.fraud_report.FraudReport;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.fraud_report.FraudReportService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewEventService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
import com.mobileman.kuravis.core.services.user.UserNotificationService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class TreatmentServiceImpl extends AbstractEntityServiceImpl<Treatment> implements TreatmentService, Attributes {

	@Autowired
	private TreatmentReviewService treatmentReviewService;
	
	@Autowired
	private FraudReportService fraudReportService;
	
	@Autowired
    private DiseaseService diseaseService;
	
	@Autowired
	private UserNotificationService userNotificationService;
	
	@Autowired
	private TreatmentReviewEventService treatmentReviewEventService;
	
	@Override
	protected String getEntityName() {
		return EntityUtils.TREATMENT;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#delete(java.lang.String, java.lang.String)
	 */
	@Override
	@RequiresAuthentication
	public DBObject delete(String type, String id) {
				
		DBObject treatment = findById(id);
		if (treatment == null) {
			return ErrorUtils.success();
		}
		
		DBObject user = (DBObject) SecurityUtils.getSubject().getPrincipal();
		if (!EntityUtils.equals((DBObject)treatment.get(Disease.USER), user)) {
			requiresAdminRole();
		}
		
		DBCursor cursor = getCollection(TreatmentReview.ENTITY_NAME).find(new BasicDBObject("treatment." + ID, id), new BasicDBObject("disease." + ID, 1));
		for (DBObject dbObject : cursor) {
			String treatmentReviewId = (String) dbObject.get(ID);
			DBObject disease = (DBObject) dbObject.get("disease");
			String diseaseId = (String) disease.get(ID);
			getCollection(EntityUtils.TREATMENT_REVIEW_EVENT).remove(new BasicDBObject("treatmentReviewId", treatmentReviewId));
			String summaryId = EntityUtils.createTreatmentReviewSummaryId(diseaseId, id);
			getCollection(TreatmentReviewSummary.ENTITY_NAME).remove(new BasicDBObject(ID, summaryId));
			getCollection(EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS).remove(new BasicDBObject("summaryId", summaryId));
			getCollection(TreatmentReview.ENTITY_NAME).remove(new BasicDBObject(ID, treatmentReviewId));
						
			if (diseaseService.diseaseHasNoOtherReviews(diseaseId, treatmentReviewId)) {
				getCollection(Disease.ENTITY_NAME).remove(new BasicDBObject(EntityUtils.ID, diseaseId));
			} else {
				getMongoTemplate().getCollection(Disease.ENTITY_NAME).update(
						new BasicDBObject(ID, diseaseId), new BasicDBObject("$inc", new BasicDBObject(Disease.TREATMENT_REVIEWS_COUNT, -1)));
			}
		}
		
		getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).remove(new BasicDBObject("treatment.name", treatment.get("name")));
		
		return super.delete(type, id);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#update(java.lang.String, java.lang.String, com.mongodb.DBObject)
	 */
	@Override
	public DBObject update(String type, String id, DBObject treatment) {
		DBObject result = validateName(type, id, treatment);
		result = super.update(type, id, treatment);
		if (ErrorUtils.isError(result)) {
			return result;
		}
		String dbName = EntityUtils.getEntityName(result);
		String givenName = EntityUtils.getEntityName(treatment);
		if (givenName != null && !ObjectUtils.nullSafeEquals(givenName, dbName)) {
			getCollection(TreatmentReview.ENTITY_NAME).update(new BasicDBObject("treatment." + ID, id),
					new BasicDBObject("$set", new BasicDBObject("treatment." + NAME, givenName)), false, true);
			getCollection(TreatmentReviewSummary.ENTITY_NAME).update(new BasicDBObject("treatment." + ID, id),
					new BasicDBObject("$set", new BasicDBObject("treatment." + NAME, givenName)), false, true);
			getCollection(EntityUtils.TEMP_TREATMENT_REVIEW).update(new BasicDBObject("treatment." + ID, id),
					new BasicDBObject("$set", new BasicDBObject("treatment." + NAME, givenName)), false, true);
			
			getCollection(Event.ENTITY_NAME).update(new BasicDBObject("treatment." + ID, id),
					new BasicDBObject("$set", new BasicDBObject("treatment." + NAME, givenName)), false, true);
		}
		return result;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#save(com.mongodb.DBObject)
	 */
	@Override
	public DBObject save(DBObject object) {
		return super.save(object);
	}
	
	/**
	 * 
	 */
	public boolean treatmentHasNoOtherReviews(String treatmentId, String treatmentReviewId) {		
		DBObject query = new BasicDBObject("treatment." + EntityUtils.ID, treatmentId).append(EntityUtils.ID, new BasicDBObject("$ne", treatmentReviewId));
		long count = getCollection(TreatmentReview.ENTITY_NAME).count(query);
		return count == 0;
	}

	@Override
	@RequiresRoles(Roles.ADMIN)
	public void merge(String sourceId, String targetId) {
		requiresAdminRole();
		
		DBObject targetTreatment = findById(targetId);
		if (sourceId == null || targetTreatment == null) {
			return;
		}
	
		// find all treatment reviews by source treatment ID
		DBCursor reviews = getCollection(TreatmentReview.ENTITY_NAME).find(new BasicDBObject(TreatmentReview.TREATMENT + "." + ID, sourceId));
		Map<String, String> treatmentReviewIdsMap = new HashMap<String, String>();
		
		// create new TRs for target treatment ID
		for (DBObject oldReview : reviews) {
			DBObject newReview = new BasicDBObject();
			EntityUtils.copyTreatmentReview(TreatmentReview.TREATMENT, targetTreatment, oldReview, newReview);
			DBObject newTreatmentReview = treatmentReviewService.createTreatmentReview(newReview);
			treatmentReviewIdsMap.put(EntityUtils.getEntityId(oldReview), EntityUtils.getEntityId(newTreatmentReview));
		}
		
		// set new TRs to {fraudReport, event, notification} and delete old TRs
		for (Iterator<String> iterator = treatmentReviewIdsMap.keySet().iterator(); iterator.hasNext();) {
			String oldId = iterator.next();
			String newId = treatmentReviewIdsMap.get(oldId);
			fraudReportService.updateAll(new BasicDBObject(FraudReport.ATTR_ENTITY_ID, oldId), new BasicDBObject("$set", new BasicDBObject(FraudReport.ATTR_ENTITY_ID, newId)));
			getCollection(EntityUtils.FRAUD_REPORT_ITEM).update(new BasicDBObject(FraudReport.ATTR_ENTITY_ID, oldId), new BasicDBObject("$set", new BasicDBObject(FraudReport.ATTR_ENTITY_ID, newId)), false, true);
			treatmentReviewEventService.updateAll(new BasicDBObject("treatmentReviewId", oldId), new BasicDBObject("$set", new BasicDBObject("treatmentReviewId", newId)));
			userNotificationService.updateAll(new BasicDBObject("treatmentReviewId", oldId), new BasicDBObject("$set", new BasicDBObject("treatmentReviewId", newId)));
			treatmentReviewService.delete(oldId);
		}
	}
}
