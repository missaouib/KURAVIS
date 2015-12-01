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
 * TreatmentDurationStatistics.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 29.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.treatment_review.statistics;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import com.mobileman.kuravis.core.domain.Entity;
import com.mobileman.kuravis.core.domain.Pair;
import com.mobileman.kuravis.core.domain.event.FrequencyType;
import com.mobileman.kuravis.core.domain.event.TreatmentEvent;

/**
 * @author MobileMan GmbH
 *
 */
@Document(collection=TreatmentDurationStatistics.ENTITY_NAME)
public class TreatmentDurationStatistics extends Entity implements TreatmentDurationStatisticsAttributes {

	/**
	 * 
	 */
	public static final int CATEGORY_UNDEFINED = -1;
	
	static final int MILLI_TO_HOUR = 1000 * 60 * 60;
	
	private int category;
	private int count;
	
	private String diseaseId;
	private String treatmentId;
	private String summaryId;
	
	private static final List<Pair<Long, Long>> CATEGORIES_BOUNDS;
	private static final Set<Integer> CATEGORIES_SET;
	
	static {
		CATEGORIES_BOUNDS = Arrays.asList(
				Pair.create(new Long(0), new Long(24)),
				Pair.create(new Long(24), new Long(24 * 7)),
				Pair.create(new Long(24 * 7), new Long((24 *7 * 4) + 24*2)),
				Pair.create(new Long((24 *7 * 4) + 24*2), new Long(24 * 7 * 52)),
				Pair.create(new Long(24 * 7 * 52), new Long(24 * 7 * 52))
				); 
		
		Set<Integer> categoriesSet = new HashSet<>();
		
		for (int i = 0; i < CATEGORIES_BOUNDS.size(); i++) {
			categoriesSet.add(Integer.valueOf(i));
		}
		
		CATEGORIES_SET = Collections.unmodifiableSet(categoriesSet);
	}
	
	/**
	 * @return CATEGORIES_SET
	 */
	public static Set<Integer> getCategoriesSet() {
		return  CATEGORIES_SET;
	}

	/**
	 *
	 * @return category
	 */
	public int getCategory() {
		return this.category;
	}

	/**
	 *
	 * @param category category
	 */
	public void setCategory(int category) {
		this.category = category;
	}

	/**
	 *
	 * @return count
	 */
	public int getCount() {
		return this.count;
	}

	/**
	 *
	 * @param count count
	 */
	public void setCount(int count) {
		this.count = count;
	}
	
	/**
	 *
	 * @return diseaseId
	 */
	public String getDiseaseId() {
		return this.diseaseId;
	}

	/**
	 *
	 * @param diseaseId diseaseId
	 */
	public void setDiseaseId(String diseaseId) {
		this.diseaseId = diseaseId;
	}

	/**
	 *
	 * @return treatmentId
	 */
	public String getTreatmentId() {
		return this.treatmentId;
	}

	/**
	 *
	 * @param treatmentId treatmentId
	 */
	public void setTreatmentId(String treatmentId) {
		this.treatmentId = treatmentId;
	}

	/**
	 *
	 * @return summaryId
	 */
	public String getSummaryId() {
		return this.summaryId;
	}

	/**
	 *
	 * @param summaryId summaryId
	 */
	public void setSummaryId(String summaryId) {
		this.summaryId = summaryId;
	}

	/**
	 * @return CATEGORIES_BOUNDS
	 */
	public static List<Pair<Long, Long>> getCategoriesBounds() {
		return CATEGORIES_BOUNDS;
	}
	
	/**
	 * @param hoursDuration
	 * @return category ID based on input hours duration
	 */
	public static int computeCategoryId(long hoursDuration) {
		if (hoursDuration == 0) {
			return 0;
		}
		
		for (byte i = 0; i < getCategoriesBounds().size(); i++) {
			Pair<Long, Long> bound = getCategoriesBounds().get(i);
			if (bound.getFirst().compareTo(Long.valueOf(hoursDuration)) == 1) {
				return i;
			}
			
			if (bound.getSecond().compareTo(Long.valueOf(hoursDuration)) == 1) {
				return i;
			}
		}
				
		return getCategoriesBounds().size() - 1;
	}
	
	private static long hoursDifference(Date date1, Date date2) {
		long result = (long) (date1.getTime() - date2.getTime()) / MILLI_TO_HOUR;
	    return result;
	}

	/**
	 * @param event
	 * @return category ID based on input hours duration
	 */
	public static int computeCategoryId(TreatmentEvent event) {
		if (event == null) {
			return CATEGORY_UNDEFINED;
		}
		
		if (FrequencyType.ONCE.equals(event.getFrequency())) {
			return 0;
		}
		
		if (event.getStart() == null || event.getEnd() == null) {
			return CATEGORY_UNDEFINED;
		}
		
		return computeCategoryId(hoursDifference(event.getEnd(), event.getStart()));
	}
}
