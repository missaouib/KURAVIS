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
 * TreatmentReviewSummary.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 20.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.treatment_review;

import java.math.BigDecimal;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mobileman.kuravis.core.domain.Entity;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentCostStatistics;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentDurationStatistics;
import com.mobileman.kuravis.core.domain.treatment_review.statistics.TreatmentRatingStatistics;

/**
 * @author MobileMan GmbH
 *
 */
@JsonSerialize(include=Inclusion.NON_NULL)
@Document(collection=TreatmentReviewSummary.ENTITY_NAME)
public class TreatmentReviewSummary extends Entity implements TreatmentReviewSummaryAttributes {

	private Disease disease;
	
	private Treatment treatment;
	
	private Long reviewsCount;
	
	private BigDecimal rating;
	
	private List<TreatmentRatingStatistics> ratings;
	
	@Transient
	private List<TreatmentCostStatistics> costsStatistics;
	
	@Transient
	private List<TreatmentDurationStatistics> treatmentDurationStatistics;
	
	@Indexed(unique = false)
	private boolean suggestion;

	/**
	 *
	 * @return disease
	 */
	public Disease getDisease() {
		return this.disease;
	}

	/**
	 *
	 * @param disease disease
	 */
	public void setDisease(Disease disease) {
		this.disease = disease;
	}

	/**
	 *
	 * @return treatment
	 */
	public Treatment getTreatment() {
		return this.treatment;
	}

	/**
	 *
	 * @param treatment treatment
	 */
	public void setTreatment(Treatment treatment) {
		this.treatment = treatment;
	}

	/**
	 *
	 * @return reviewsCount
	 */
	public Long getReviewsCount() {
		return this.reviewsCount;
	}

	/**
	 *
	 * @param reviewsCount reviewsCount
	 */
	public void setReviewsCount(Long reviewsCount) {
		this.reviewsCount = reviewsCount;
	}

	/**
	 *
	 * @return rating
	 */
	public BigDecimal getRating() {
		return this.rating;
	}

	/**
	 *
	 * @param rating rating
	 */
	public void setRating(BigDecimal rating) {
		this.rating = rating;
	}

	/**
	 *
	 * @return suggestion
	 */
	public boolean isSuggestion() {
		return this.suggestion;
	}

	/**
	 *
	 * @param suggestion suggestion
	 */
	public void setSuggestion(boolean suggestion) {
		this.suggestion = suggestion;
	}

	/**
	 *
	 * @return costsStatistics
	 */
	public List<TreatmentCostStatistics> getCostsStatistics() {
		return this.costsStatistics;
	}

	/**
	 *
	 * @param costsStatistics costsStatistics
	 */
	public void setCostsStatistics(List<TreatmentCostStatistics> costsStatistics) {
		this.costsStatistics = costsStatistics;
	}

	/**
	 *
	 * @return treatmentDurationStatistics
	 */
	public List<TreatmentDurationStatistics> getTreatmentDurationStatistics() {
		return this.treatmentDurationStatistics;
	}

	/**
	 *
	 * @param treatmentDurationStatistics treatmentDurationStatistics
	 */
	public void setTreatmentDurationStatistics(List<TreatmentDurationStatistics> treatmentDurationStatistics) {
		this.treatmentDurationStatistics = treatmentDurationStatistics;
	}

	/**
	 *
	 * @return ratings
	 */
	public List<TreatmentRatingStatistics> getRatings() {
		return this.ratings;
	}

	/**
	 *
	 * @param ratings ratings
	 */
	public void setRatings(List<TreatmentRatingStatistics> ratings) {
		this.ratings = ratings;
	}
	
	
}
