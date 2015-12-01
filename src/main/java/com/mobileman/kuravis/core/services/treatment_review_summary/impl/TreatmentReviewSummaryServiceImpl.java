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
 * TreatmentReviewSummaryServiceImpl.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 24.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.treatment_review_summary.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.event.Event;
import com.mobileman.kuravis.core.domain.event.EventType;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentCost;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentCostStatistics;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentDurationStatistics;
import com.mobileman.kuravis.core.domain.user.Gender;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.UserUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.event.EventService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.services.util.CRUDAction;
import com.mobileman.kuravis.core.services.util.lock.LockManager;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class TreatmentReviewSummaryServiceImpl extends AbstractEntityServiceImpl<TreatmentReviewSummary> implements TreatmentReviewSummaryService {
	
	@Autowired
	private DiseaseService diseaseService;
	
	@Autowired
	private TreatmentService treatmentService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private LockManager lockManager;

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return TreatmentReviewSummary.ENTITY_NAME;
	}

	@Override
	public List<DBObject> findAllByQuery(DBObject query, Pageable page) {
		
		List<DBObject> result = new ArrayList<>();
		
		if (query.containsField("treatment." + EntityUtils.NAME)) {
			String name = (String) query.get("treatment." + EntityUtils.NAME);		
			query.put("treatment." + EntityUtils.NAME, new BasicDBObject("$regex", "^" + name.toLowerCase() + "$").append("$options", "i"));
		}
		
		if (query.containsField("disease." + EntityUtils.NAME)) {
			String name = (String) query.get("disease." + EntityUtils.NAME);		
			query.put("disease." + EntityUtils.NAME, new BasicDBObject("$regex", "^" + name.toLowerCase() + "$").append("$options", "i"));
		}

		DBCursor cursor = getCollection().find(query).sort(createSort(page)).skip(page.getOffset()).limit(page.getPageSize());
		
		while (cursor.hasNext()) {
			DBObject summary = cursor.next();
			String summaryId = (String) summary.get(EntityUtils.ID);
			DBObject ageStatistics = findAgeStatistics(summaryId);
			summary.put("ageStatistics", ageStatistics);
			
			List<DBObject> treatmentDurationStatistics = findTreatmentDurationStatistics(summary);
			summary.put(TreatmentReviewSummary.TREATMENT_DURATION_STATISTICS, treatmentDurationStatistics);
			
			List<DBObject> costsStatistics = findCostStatistics(summary);
			summary.put(TreatmentReviewSummary.COSTS_STATISTICS, costsStatistics);
			
			result.add(summary);
		}
		
		return result;
	}
	
	@Override
	public List<DBObject> findAllByDisease(String diseaseId, Pageable page) {
		
		List<DBObject> result = new ArrayList<>();

		DBCursor cursor = getCollection(getEntityName())
				.find(new BasicDBObject("disease." + EntityUtils.ID, diseaseId))
				.sort(new BasicDBObject("rating", 1).append(Disease.ENTITY_NAME + "." + Disease.NAME, 1))
				.skip(page.getOffset())
				.limit(page.getPageSize());
		
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			String summaryId = (String) obj.get(EntityUtils.ID);
			DBObject ageStatistics = findAgeStatistics(summaryId);
			obj.put("ageStatistics", ageStatistics);
			result.add(obj);
		}
		
		return result;
	}

	/**
	 * @param summaryId
	 * @return DBObject
	 */
	@SuppressWarnings("unchecked")
	private DBObject findAgeStatistics(String summaryId) {
		DBObject ageStatistics = new BasicDBObject();
		DBCursor ageStatCursor = getCollection(EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS).find(new BasicDBObject("summaryId", summaryId));
		for (DBObject ageStatistic : ageStatCursor) {
			String gender = (String) ageStatistic.get(User.ATTR_GENDER);
			if (gender == null) {
				gender = Gender.UNKNOWN.getValue();
			}
			
			final List<DBObject> genderAgeStats;
			if (!ageStatistics.containsField(gender)) {
				genderAgeStats = new ArrayList<DBObject>();
				ageStatistics.put(gender, genderAgeStats);
			} else {
				genderAgeStats = (List<DBObject>) ageStatistics.get(gender);
			}
			
			genderAgeStats.add(ageStatistic);
		}
		return ageStatistics;
	}
	
	/**
	 * @param summary
	 * @return DBObject
	 */
	private List<DBObject> findTreatmentDurationStatistics(DBObject summary) {
		
		Query query = Query.query(
				Criteria.where(TreatmentDurationStatistics.SUMMARY_ID).is(summary.get(EntityUtils.ID)))
						.with(new Sort(new Order(TreatmentDurationStatistics.CATEGORY)));
				
		List<DBObject> statistics = getCollection(TreatmentDurationStatistics.ENTITY_NAME).find(query.getQueryObject()).toArray();
		if (statistics.size() < TreatmentDurationStatistics.getCategoriesBounds().size()) {
			Set<Integer> initialCategories = new HashSet<>(TreatmentDurationStatistics.getCategoriesSet());
			
			Iterator<DBObject> iterator = statistics.iterator();
			while (iterator.hasNext()) {
				DBObject statObj = iterator.next();
				Integer category = (Integer) statObj.get(TreatmentDurationStatistics.CATEGORY);
				if (category.intValue() == TreatmentDurationStatistics.CATEGORY_UNDEFINED) {
					iterator.remove();
				} else {
					initialCategories.remove(category);
				}
			}
			
			for (Integer category : initialCategories) {
				DBObject stat = new BasicDBObject();
				stat.put(TreatmentDurationStatistics.CATEGORY, category);
				stat.put(TreatmentDurationStatistics.COUNT, 0);
				statistics.add(stat);
			}
		}
		
		Collections.sort(statistics, new Comparator<DBObject>() {

			@Override
			public int compare(DBObject o1, DBObject o2) {
				Integer i1 = (Integer) o1.get(TreatmentReviewSummary.CATEGORY);
				Integer i2 = (Integer) o2.get(TreatmentReviewSummary.CATEGORY);
				return i1.compareTo(i2);
			}
		});
		
		return statistics;
	}
	
	/**
	 * @param summary
	 * @return DBObject
	 */
	private List<DBObject> findCostStatistics(DBObject summary) {
		
		Query query = Query.query(
				Criteria.where(TreatmentCostStatistics.SUMMARY_ID).is(summary.get(EntityUtils.ID)))
						.with(new Sort(new Order(TreatmentCostStatistics.CATEGORY)));
		
		List<DBObject> statistics = getCollection(TreatmentCostStatistics.ENTITY_NAME).find(query.getQueryObject()).toArray();
		if (statistics.size() < TreatmentCostStatistics.getCategoriesBounds().size()) {
			
			Set<Integer> initialCategories = new HashSet<>(TreatmentCostStatistics.getCategoriesSet());
			Iterator<DBObject> iterator = statistics.iterator();
			while (iterator.hasNext()) {
				DBObject statObj = iterator.next();
				Integer category = (Integer) statObj.get(TreatmentCostStatistics.CATEGORY);
				if (category.intValue() == TreatmentCostStatistics.CATEGORY_UNDEFINED) {
					iterator.remove();
				} else {
					initialCategories.remove(category);
				}
			}
			
			for (Integer category : initialCategories) {
				DBObject stat = new BasicDBObject();
				stat.put(TreatmentCostStatistics.CATEGORY, category);
				stat.put(TreatmentCostStatistics.COUNT, 0);
				statistics.add(stat);
			}
		}
		
		Collections.sort(statistics, new Comparator<DBObject>() {

			@Override
			public int compare(DBObject o1, DBObject o2) {
				Integer i1 = (Integer) o1.get(TreatmentReviewSummary.CATEGORY);
				Integer i2 = (Integer) o2.get(TreatmentReviewSummary.CATEGORY);
				return i1.compareTo(i2);
			}
		});
		
		return statistics;
	}
	
	@Override
	public DBObject findById(String id) {
		DBObject summary = findById(getEntityName(), id);
		if (summary == null) {
			return null;
		}
		
		DBObject ageStatistics = findAgeStatistics(id);
		summary.put("ageStatistics", ageStatistics);
		
		List<DBObject> treatmentDurationStatistics = findTreatmentDurationStatistics(summary);
		summary.put(TreatmentReviewSummary.TREATMENT_DURATION_STATISTICS, treatmentDurationStatistics);
		
		List<DBObject> costsStatistics = findCostStatistics(summary);
		summary.put(TreatmentReviewSummary.COSTS_STATISTICS, costsStatistics);
		
		return summary;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getById(java.lang.Object, java.lang.Class)
	 */
	@Override
	public TreatmentReviewSummary getById(Object id, Class<? extends TreatmentReviewSummary> entityType) {
		TreatmentReviewSummary summary = super.getById(id, entityType);
		
		List<DBObject> treatmentDurationStatistics = findTreatmentDurationStatistics(new BasicDBObject(TreatmentReviewSummary.ID, summary.get_id()));
		List<DBObject> costsStatistics = findCostStatistics(new BasicDBObject(TreatmentReviewSummary.ID, summary.get_id()));
		
		try {
			TreatmentDurationStatistics[] data = objectMapper.readValue(treatmentDurationStatistics.toString(), TreatmentDurationStatistics[].class);
			summary.setTreatmentDurationStatistics(Arrays.asList(data));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			TreatmentCostStatistics[] data = objectMapper.readValue(costsStatistics.toString(), TreatmentCostStatistics[].class);
			summary.setCostsStatistics(Arrays.asList(data));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return summary;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService#diseasesIndex()
	 */
	@Override
	public List<DBObject> diseasesIndex() {
		
		AggregationOutput out = getCollection().aggregate(
				new BasicDBObject("$group", new BasicDBObject("_id", new BasicDBObject("$toUpper", Arrays.asList(new BasicDBObject("$substr", Arrays.asList("$disease.name", 0, 1))))).append("diseasesPerIndex", new BasicDBObject("$sum", 1))),
				new BasicDBObject("$project", 
						new BasicDBObject("index", "$_id").append("diseasesPerIndex", "$diseasesPerIndex")),
				new BasicDBObject("$sort", new BasicDBObject("index", 1))
				//new BasicDBObject("$skip", page.getOffset()),
				//new BasicDBObject("$limit", page.getPageSize())
				
				);
				
		List<DBObject> result = new ArrayList<>();
		for (DBObject dbObject : out.results()) {
			String index = (String) dbObject.get("index");
			if (index != null && index.trim().length() > 0) {
				result.add(new BasicDBObject("index", index).append("diseasesPerIndex", dbObject.get("diseasesPerIndex")));
			}	
		}
		
		return result;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public List<DBObject> findAllDiseasesWithTreatmentsByIndex(String index) {
		if (index == null || index.trim().length() == 0) {
			throw ErrorUtils.exception("Index missing", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		BasicDBObject filter = new BasicDBObject("disease.name", new BasicDBObject("$regex", "^" + index.trim().toLowerCase() + "").append("$options", "i"));
		DBCursor cursor = getCollection().find(filter, new BasicDBObject(TreatmentReviewSummary.DISEASE, 1).append(TreatmentReviewSummary.TREATMENT, 1).append(TreatmentReviewSummary.SUGGESTION, 1))
				.sort(new BasicDBObject("disease.name", 1).append("treatment.name", 1));
		
		List<DBObject> result = new ArrayList<DBObject>();
		
		Map<String, List<DBObject>> diseaseTreatments = new HashMap<String, List<DBObject>>();
		for (DBObject summary : cursor) {
			DBObject disease = (DBObject) summary.get("disease");
			DBObject treatment = (DBObject) summary.get("treatment");
			
			List<DBObject> treatments = diseaseTreatments.get(disease.get("_id"));
			if (treatments == null) {
				DBObject item = new BasicDBObject();
				result.add(item);
				
				item.put("_id", disease.get("_id"));
				item.put("name", disease.get("name"));
				treatments = new ArrayList<DBObject>();
				item.put("treatments", treatments);
				item.put(TreatmentReviewSummary.SUGGESTION, summary.get(TreatmentReviewSummary.SUGGESTION));
				
				diseaseTreatments.put((String) item.get("_id"), treatments);
			}
			
			treatments.add(treatment);
		}
		
		return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService#createSuggestion(com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary)
	 */
	@Override
	public String createSuggestion(TreatmentReviewSummary data) {
		if (!data.isSuggestion()) {
			throw ErrorUtils.exception(ErrorCodes.TREATMENT_REVIEW_SUMMARY_IS_NOT_SUGGESTION);
		}
		
		if (data.getDisease() == null || StringUtils.isEmpty(data.getDisease().get_id())) {
			throw ErrorUtils.exception("Disease param is missing", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		if (data.getTreatment() == null || StringUtils.isEmpty(data.getTreatment().get_id())) {
			throw ErrorUtils.exception("Treatment param is missing", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject disease = diseaseService.findById(data.getDisease().get_id());
		DBObject treatment = treatmentService.findById(data.getTreatment().get_id());
		final String _id = EntityUtils.createTreatmentReviewSummaryId(disease.get(EntityUtils.ID), treatment.get(EntityUtils.ID));
		if (count(new BasicDBObject(TreatmentReviewSummary.ID, _id)) > 0) {
			return _id;
		}
		
		lockManager.lock(getEntityName(), _id);
		
		TreatmentReviewSummary summary;
		try {
			summary = new TreatmentReviewSummary();
			summary.set_id(_id);
			summary.setDisease(new Disease(data.getDisease().get_id(), (String) disease.get(Disease.NAME)));
			summary.setTreatment(new Treatment(data.getTreatment().get_id(), (String) treatment.get(Disease.NAME)));
			summary.setSuggestion(true);
			summary.setRating(new BigDecimal(1.d));
			super.create(summary);
			return summary.get_id();
		} finally {
			lockManager.unlock(getEntityName(), _id);
		}
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#delete(java.lang.String, java.lang.String)
	 */
	@Override
	public DBObject delete(String type, String id) {
		TreatmentReviewSummary summary = getMongoTemplate().findById(id, TreatmentReviewSummary.class);
		if (!summary.isSuggestion()) {
			throw ErrorUtils.exception(ErrorCodes.TREATMENT_REVIEW_SUMMARY_IS_NOT_SUGGESTION);
		}
		
		lockManager.lock(getEntityName(), id);
		
		try {
			DBObject result = super.delete(type, id);
			if (ErrorUtils.isError(result)) {
				return result;
			}
			
			getCollection(EntityUtils.TREATMENT_SUMMARY_AGE_STATISTICS).remove(new BasicDBObject("summaryId", id));
			
		} finally {
			lockManager.unlock(getEntityName(), id);
		}
		
		return ErrorUtils.success();
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#create(com.mobileman.kuravis.core.domain.Entity)
	 */
	@Override
	public String create(TreatmentReviewSummary summary) {
		if (!summary.isSuggestion()) {
			throw ErrorUtils.exception(ErrorCodes.TREATMENT_REVIEW_SUMMARY_IS_NOT_SUGGESTION);
		}
		
		return createSuggestion(summary);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateTreatmentDurationStatistics(TreatmentEvent treatmentEvent, TreatmentEvent oldTreatmentEvent, CRUDAction action) {
		
		final String trsid = EntityUtils.createTreatmentReviewSummaryId(treatmentEvent.getDisease().get_id(), treatmentEvent.getTreatment().get_id());
		int category = TreatmentDurationStatistics.computeCategoryId(treatmentEvent);
		Query query = Query.query(
				Criteria.where(TreatmentDurationStatistics.ID).is(trsid + String.valueOf(category))
						.and(TreatmentDurationStatistics.SUMMARY_ID).is(trsid)
						.and(TreatmentDurationStatistics.DISEASE_ID).is(treatmentEvent.getDisease().get_id())
						.and(TreatmentDurationStatistics.TREATMENT_ID).is(treatmentEvent.getTreatment().get_id())
						.and(TreatmentDurationStatistics.CATEGORY).is(Integer.valueOf(category)));
		
		switch (action) {
		case CREATE: {
			if (category != TreatmentDurationStatistics.CATEGORY_UNDEFINED) {
				getMongoTemplate().upsert(query, new Update().inc(TreatmentDurationStatistics.COUNT, 1), TreatmentDurationStatistics.class);
			}			
		}
			break;
		case UPDATE: {
			int oldCategory = TreatmentDurationStatistics.computeCategoryId(oldTreatmentEvent);
			if (oldCategory != category) {
				// increment stat in new category
				if (category != TreatmentDurationStatistics.CATEGORY_UNDEFINED) {
					getMongoTemplate().upsert(query, new Update().inc(TreatmentDurationStatistics.COUNT, 1), TreatmentDurationStatistics.class);
				}
				
				// decrement stat in old category
				
				if (oldCategory != TreatmentCostStatistics.CATEGORY_UNDEFINED) {
					query = Query.query(
							Criteria.where(TreatmentDurationStatistics.ID).is(trsid + String.valueOf(oldCategory))
									.and(TreatmentDurationStatistics.SUMMARY_ID).is(trsid)
									.and(TreatmentDurationStatistics.DISEASE_ID).is(treatmentEvent.getDisease().get_id())
									.and(TreatmentDurationStatistics.TREATMENT_ID).is(treatmentEvent.getTreatment().get_id())
									.and(TreatmentDurationStatistics.CATEGORY).is(Integer.valueOf(oldCategory)));
					
					getMongoTemplate().updateFirst(query, new Update().inc(TreatmentDurationStatistics.COUNT, -1), TreatmentDurationStatistics.class);
				}
			}
		}
			break;
		case DELETE: {
			if (category != TreatmentDurationStatistics.CATEGORY_UNDEFINED) {
				getMongoTemplate().updateFirst(query, new Update().inc(TreatmentDurationStatistics.COUNT, -1), TreatmentDurationStatistics.class);
			}			
		}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void updateTreatmentCostStatistics(DBObject newReview, DBObject oldReview, CRUDAction action) {
		
		Map<String, Object> disease = (Map<String, Object>) newReview.get(TreatmentReview.DISEASE);
		Map<String, Object> treatment = (Map<String, Object>) newReview.get(TreatmentReview.TREATMENT);
		final String trsid = EntityUtils.createTreatmentReviewSummaryId(disease.get(Disease.ID), treatment.get(Treatment.ID));
		TreatmentCost newCost = TreatmentCost.createFromReview(newReview);
		if (newCost == null) {
			return;
		}
		
		int category = TreatmentCostStatistics.computeCategoryId(newCost.costOfMedication());
				
		Query query = Query.query(
				Criteria.where(TreatmentCostStatistics.ID).is(trsid + String.valueOf(category))
						.and(TreatmentCostStatistics.SUMMARY_ID).is(trsid)
						.and(TreatmentCostStatistics.DISEASE_ID).is(disease.get(Disease.ID))
						.and(TreatmentCostStatistics.TREATMENT_ID).is(treatment.get(Treatment.ID))
						.and(TreatmentCostStatistics.CATEGORY).is(Integer.valueOf(category)));
		
		switch (action) {
		case CREATE: {
			if (category != TreatmentCostStatistics.CATEGORY_UNDEFINED) {
				getMongoTemplate().upsert(query, new Update().inc(TreatmentCostStatistics.COUNT, 1), TreatmentCostStatistics.class);
			}			
		}
			break;
		case UPDATE: {
			int oldCategory = TreatmentCostStatistics.CATEGORY_UNDEFINED;
			TreatmentCost oldCost = TreatmentCost.createFromReview(oldReview);
			if (oldCost != null) {
				oldCategory = TreatmentCostStatistics.computeCategoryId(oldCost.costOfMedication());
			}
			
			if (oldCategory != category) {
				// increment cost stat in new category
				if (category != TreatmentCostStatistics.CATEGORY_UNDEFINED) {
					getMongoTemplate().upsert(query, new Update().inc(TreatmentCostStatistics.COUNT, 1), TreatmentCostStatistics.class);
				}
				
				if (oldCategory != TreatmentCostStatistics.CATEGORY_UNDEFINED) {
					query = Query.query(
							Criteria.where(TreatmentCostStatistics.ID).is(trsid + String.valueOf(oldCategory))
									.and(TreatmentCostStatistics.SUMMARY_ID).is(trsid)
									.and(TreatmentCostStatistics.DISEASE_ID).is(disease.get(Disease.ID))
									.and(TreatmentCostStatistics.TREATMENT_ID).is(treatment.get(Treatment.ID))
									.and(TreatmentCostStatistics.CATEGORY).is(Integer.valueOf(oldCategory)));
					
					getMongoTemplate().updateFirst(query, new Update().inc(TreatmentCostStatistics.COUNT, -1), TreatmentCostStatistics.class);
				}
			}
		}
			break;
		case DELETE: {
			if (category != TreatmentCostStatistics.CATEGORY_UNDEFINED) {
				getMongoTemplate().updateFirst(query, new Update().inc(TreatmentCostStatistics.COUNT, -1), TreatmentCostStatistics.class);
			}
		}
			break;
		default:
			break;
		}
		
		query = Query.query(Criteria.where(TreatmentCostStatistics.COUNT).lt(Integer.valueOf(0)));
		getMongoTemplate().remove(query, TreatmentCostStatistics.class);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void computeTreatmentReviewSummaryStatistics(DBObject review, DBObject oldReview, CRUDAction reviewCrudAction) {
		
		updateTreatmentCostStatistics(review, oldReview, reviewCrudAction);
		
		String diseaseId = (String) DBObject.class.cast(review.get(TreatmentReview.DISEASE)).get(EntityUtils.ID);
		String treatmentId = (String) DBObject.class.cast(review.get(TreatmentReview.TREATMENT)).get(EntityUtils.ID);
		final String summaryId = EntityUtils.createTreatmentReviewSummaryId(diseaseId, treatmentId);
		lockManager.lock(TreatmentReviewSummary.ENTITY_NAME, summaryId);
		
		try {
			TreatmentReviewSummary treatmentReviewSummary = super.getById(summaryId);
			// summary dose not exists -  deletion of summery deletes all stats structures
			if (treatmentReviewSummary == null) {
				return;
			}
			
			switch (reviewCrudAction) {
			case DELETE: {
				
				Query query = Query.query(Criteria.where(Event.EVENT_TYPE).is(EventType.TREATMENT.name())
						.and(Event.USER + "." + Event.ID).is(UserUtils.getLoggedUserId())
						.and(TreatmentEvent.DISEASE + "." + Event.ID).is(diseaseId)
						.and(TreatmentEvent.TREATMENT + "." + Event.ID).is(treatmentId));
				List<TreatmentEvent> medicationEntries = getMongoTemplate().find(query, TreatmentEvent.class);
				for (TreatmentEvent medicationEntry : medicationEntries) {
					updateTreatmentDurationStatistics(medicationEntry, null, reviewCrudAction);;
				}
			}
				break;

			default:
				break;
			}
			
			// author.gener
			// author.yearOfBirth
			/*
			AggregationOperation matchOp = Aggregation.match(
					Criteria
						.where(TreatmentReview.DISEASE + "." + TreatmentReview.ID).is(treatmentReviewSummary.getDisease().get_id())
						.and(TreatmentReview.TREATMENT + "." + TreatmentReview.ID).is(treatmentReviewSummary.getTreatment().get_id()));
			
			AggregationOperation groupOp = Aggregation.group(TreatmentReview.AUTHOR + "." + User.ATTR_GENDER).count().as("genderCount");
			AggregationOperation projectOp = Aggregation.project().andInclude(TreatmentReview.AUTHOR + "." + User.ATTR_GENDER, "genderCount");
			
			AggregationResults<BasicDBObject> aggregationResults = getMongoTemplate().aggregate(
					Aggregation.newAggregation(matchOp, projectOp, groupOp), TreatmentReview.ENTITY_NAME, BasicDBObject.class);
			List<BasicDBObject> mappedResults = aggregationResults.getMappedResults();
			for (BasicDBObject result : mappedResults) {
				String gender = (String) result.get(TreatmentReview.AUTHOR + "." + User.ATTR_GENDER);
				
			}
			*/
		} finally {
			lockManager.unlock(TreatmentReviewSummary.ENTITY_NAME, summaryId);
		}
	}
}
