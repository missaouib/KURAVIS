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
 * TreatmentCostStat.java
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import com.mobileman.kuravis.core.domain.Entity;
import com.mobileman.kuravis.core.domain.Pair;

/**
 * @author MobileMan GmbH
 *
 */
@Document(collection=TreatmentCostStatistics.ENTITY_NAME)
public class TreatmentCostStatistics extends Entity implements TreatmentCostStatisticsAttributes {
	
	/**
	 * 
	 */
	public static final int CATEGORY_UNDEFINED = -1;
	
	private int category;
	private int count;
	private String diseaseId;
	private String treatmentId;
	private String summaryId;
	
	private static final List<Pair<BigDecimal, BigDecimal>> CATEGORIES_BOUNDS;
	private static final Set<Integer> CATEGORIES_SET;
	
	static {
		
		CATEGORIES_BOUNDS = Arrays.asList(
				Pair.create(BigDecimal.ZERO, new BigDecimal(25.d)),
				Pair.create(new BigDecimal(25.d), new BigDecimal(51.d)),
				Pair.create(new BigDecimal(51.d), new BigDecimal(101.d)),
				Pair.create(new BigDecimal(101.d), new BigDecimal(201.d)),
				Pair.create(new BigDecimal(201.d), new BigDecimal(201.d))
				);
		
		Set<Integer> categoriesSet = new HashSet<>();
		
		for (int i = 0; i < CATEGORIES_BOUNDS.size(); i++) {
			categoriesSet.add(Integer.valueOf(i));
		}
		
		CATEGORIES_SET = Collections.unmodifiableSet(categoriesSet);
	}
	
	/**
	 * @return CATEGORIES_BOUNDS
	 */
	public static List<Pair<BigDecimal, BigDecimal>> getCategoriesBounds() {
		return CATEGORIES_BOUNDS;
	}
	
	/**
	 * @return CATEGORIES_SET
	 */
	public static Set<Integer> getCategoriesSet() {
		return  CATEGORIES_SET;
	}
	
	/**
	 * 
	 */
	public TreatmentCostStatistics() {
		super();
	}
	/**
	 * @param group
	 * @param count
	 */
	public TreatmentCostStatistics(int group, int count) {
		super();
		this.category = group;
		this.count = count;
	}
	/**
	 *
	 * @return group
	 */
	public int getCategory() {
		return this.category;
	}
	/**
	 *
	 * @param group group
	 */
	public void setCategory(int group) {
		this.category = group;
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
	 * @param cost
	 * @return cost category ID based on input cost
	 */
	public static int computeCategoryId(BigDecimal cost) {
		if (cost == null) {
			return TreatmentCostStatistics.CATEGORY_UNDEFINED;
		}
		
		for (byte i = 0; i < getCategoriesBounds().size(); i++) {
			Pair<BigDecimal, BigDecimal> bound = getCategoriesBounds().get(i);
			if (cost.compareTo(bound.getFirst()) == -1) {
				return i;
			}
			
			if (cost.compareTo(bound.getSecond()) == -1) {
				return i;
			}
		}
				
		return getCategoriesBounds().size() - 1;
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
	
	
}
