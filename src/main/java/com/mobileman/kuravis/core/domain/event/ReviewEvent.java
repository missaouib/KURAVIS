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
package com.mobileman.kuravis.core.domain.event;

import java.math.BigDecimal;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.treatment.Treatment;

public class ReviewEvent extends Event implements ReviewEventAttributes {
	
	private String reviewId;
		
	private Disease disease;
	
	private Treatment treatment;
	
	private BigDecimal rating;
	
	public ReviewEvent() {
		setEventType(EventType.REVIEW);
	}

	/**
	 *
	 * @return reviewId
	 */
	public String getReviewId() {
		return this.reviewId;
	}

	/**
	 *
	 * @param reviewId reviewId
	 */
	public void setReviewId(String reviewId) {
		this.reviewId = reviewId;
	}

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
	
	
}
