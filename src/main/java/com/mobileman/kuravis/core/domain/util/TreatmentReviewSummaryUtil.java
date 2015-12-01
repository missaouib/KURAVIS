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
 * TreatmentReviewSummaryUtil.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 29.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentCost;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentCostStatistics;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentDurationStatistics;
import com.mobileman.kuravis.core.domain.treatment_side_effect.TreatmentSideEffect;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public abstract class TreatmentReviewSummaryUtil {

	private TreatmentReviewSummaryUtil(){}

	/**
	 * @param treatmentCosts
	 * @return cost statictics
	 */
	public static List<DBObject> computeCostStatistics(List<TreatmentCost> treatmentCosts) {
		List<DBObject> statisticsData = new ArrayList<DBObject>(5);
		
		for (int i = 0; i < TreatmentCostStatistics.getCategoriesBounds().size(); i++) {
			DBObject stat = new BasicDBObject();
			stat.put(TreatmentReviewSummary.CATEGORY, Integer.valueOf(i));
			stat.put(TreatmentReviewSummary.NAME, "");
			stat.put(TreatmentReviewSummary.COUNT, 0);
			statisticsData.add(stat);
		}
				
		return computeCostStatistics(statisticsData, treatmentCosts);
	}
	
	/**
	 * @param statisticsData 
	 * @param treatmentCosts
	 * @return cost statictics
	 */
	public static List<DBObject> computeCostStatistics(List<DBObject> statisticsData, List<TreatmentCost> treatmentCosts) {		
		Map<Integer, DBObject> statsByGroup = new HashMap<Integer, DBObject>();
		for (DBObject statData : statisticsData) {
			statsByGroup.put((Integer) statData.get(TreatmentReviewSummary.CATEGORY), statData);
		}
		
		if (treatmentCosts == null) {
			return statisticsData;
		}
		
		for (TreatmentCost cost : treatmentCosts) {
			int category = TreatmentCostStatistics.computeCategoryId(cost.costOfMedication());
			DBObject statData = statsByGroup.get(Integer.valueOf(category));
			if (statData != null) {
				Integer count = (Integer) statData.get(TreatmentReviewSummary.COUNT);
				if (count == null) {
					count = Integer.valueOf(1);
				} else {
					count = count.intValue() + 1;
				}
				
				statData.put(TreatmentReviewSummary.COUNT, count);
			}
		}
				
		return statisticsData;
	}

	/**
	 * @param review
	 * @return cost statictics
	 */
	public static List<DBObject> computeCostStatistics(DBObject review) {
		TreatmentCost cost = TreatmentCost.createFromReview(review);
		return computeCostStatistics(cost != null ? Arrays.asList(cost) : null);
	};
	
	/**
	 * @param treatmentEvents
	 * @return treatment duration statictics
	 */
	public static List<DBObject> computeTreatmentDurationStatistics(List<TreatmentEvent> treatmentEvents) {
		List<DBObject> statisticsData = new ArrayList<DBObject>(5);
		Map<Integer, DBObject> statsByGroup = new HashMap<Integer, DBObject>();
		for (int i = 0; i < TreatmentDurationStatistics.getCategoriesBounds().size(); i++) {
			DBObject stat = new BasicDBObject();
			stat.put(TreatmentReviewSummary.CATEGORY, Integer.valueOf(i));
			stat.put(TreatmentReviewSummary.NAME, "");
			stat.put(TreatmentReviewSummary.COUNT, 0);
			statisticsData.add(stat);
			statsByGroup.put((Integer) stat.get(TreatmentReviewSummary.CATEGORY), stat);
		}
		
		if (treatmentEvents == null || treatmentEvents.isEmpty()) {
			return statisticsData;
		}
		
		for (TreatmentEvent event : treatmentEvents) {
			int category = TreatmentDurationStatistics.computeCategoryId(event);
			DBObject statData = statsByGroup.get(Integer.valueOf(category));
			if (statData != null) {
				Integer count = (Integer) statData.get(TreatmentReviewSummary.COUNT);
				if (count == null) {
					count = Integer.valueOf(1);
				} else {
					count = count.intValue() + 1;
				}
				
				statData.put(TreatmentReviewSummary.COUNT, count);
			}
		}
				
		return statisticsData;
	}
		
	/**
	 * @param trSideEffects
	 * @param trsSideEffects
	 * @return summary side efects
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, DBObject> createTreatmentReviewSummarySideEffects(List<DBObject> trSideEffects, Map<String, DBObject> trsSideEffects) {
		if (trsSideEffects == null) {
			trsSideEffects = new HashMap<>();
		}
		
		if (trSideEffects.isEmpty()) {
			DBObject trSideEffect = new BasicDBObject("severity", new Double(0));	
			Map<String, Object> sideEffect = new HashMap<String, Object>();
			sideEffect.put(EntityUtils.NAME, TreatmentSideEffect.NO_SIDE_EFFECT_NAME);
			sideEffect.put("noSideEffect", Boolean.TRUE);
			trSideEffect.put("sideEffect", sideEffect);
			trSideEffects.add(trSideEffect);
		}
		
		for (DBObject trSideEffect : trSideEffects) {
			Number severity = (Number) trSideEffect.get("severity");
			Map<String, Object> sideEffect = DBObject.class.isInstance(trSideEffect.get("sideEffect")) ? DBObject.class.cast(trSideEffect.get("sideEffect")).toMap() : (Map<String, Object>)trSideEffect.get("sideEffect");
			if (sideEffect.get(EntityUtils.NAME) != null && severity != null) {
				String sideEffectName = (String) sideEffect.get(EntityUtils.NAME);
				Boolean noSideEffect = (Boolean) sideEffect.get("noSideEffect");
				if (!trsSideEffects.containsKey(sideEffectName)) {
					// any group exists, so create complete new structure
					trsSideEffects.put(sideEffectName, new BasicDBObject(EntityUtils.NAME, sideEffectName)
						.append("noSideEffect", noSideEffect)
						.append("counts", new ArrayList<>(Arrays.asList(new BasicDBObject(EntityUtils.NAME, severity).append("count", 1)))));
				} else {
					// some count group already exists, try to find appropriate group by severity value and increment count or create new group otherwise
					List<DBObject> counts = (List<DBObject>)DBObject.class.cast(trsSideEffects.get(sideEffectName)).get("counts");
					boolean updated = false;
					for (DBObject count : counts) {
						Number value = (Number) count.get(EntityUtils.NAME);
						if (value != null) {
							if (value.equals(severity)) {
								int newCount = Number.class.cast(count.get("count")).intValue() + 1;
								count.put("count", newCount);
								updated = true;
								break;
							}
						}
					}
					
					if (updated == false) {
						counts.add(new BasicDBObject(EntityUtils.NAME, severity).append("count", 1));
					}
				}
			}
		}
		
		return trsSideEffects;
	}
}
