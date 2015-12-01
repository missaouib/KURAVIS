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

import org.springframework.data.annotation.Transient;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.option_list.treatment_type.TreatmentType;
import com.mobileman.kuravis.core.domain.option_list.unit.Unit;
import com.mobileman.kuravis.core.domain.treatment.Treatment;

public class TreatmentEvent extends Event implements TreatmentEventAttributes {
	private Disease disease;
	private Treatment treatment;
	private TreatmentCategory category;
	private FrequencyType frequency;
	private Integer quantity;
	private TreatmentType type;
	private Integer dose;
	private Unit unit;
	private Integer duration;
	private String text;
	private boolean reminder;
	
	@Transient
	private String reviewId;
	
	public TreatmentEvent() {
		setEventType(EventType.TREATMENT);
	}

	public TreatmentCategory getCategory() {
		return category;
	}

	public void setCategory(TreatmentCategory category) {
		this.category = category;
	}

	public FrequencyType getFrequency() {
		return frequency;
	}

	public void setFrequency(FrequencyType frequency) {
		this.frequency = frequency;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getDose() {
		return dose;
	}

	public void setDose(Integer dose) {
		this.dose = dose;
	}

	public Disease getDisease() {
		return disease;
	}

	public void setDisease(Disease disease) {
		this.disease = disease;
	}

	public Treatment getTreatment() {
		return treatment;
	}

	public void setTreatment(Treatment treatment) {
		this.treatment = treatment;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public TreatmentType getType() {
		return type;
	}

	public void setType(TreatmentType type) {
		this.type = type;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public boolean isReminder() {
		return this.reminder;
	}

	public void setReminder(boolean reminder) {
		this.reminder = reminder;
	}

	public String getReviewId() {
		return reviewId;
	}

	public void setReviewId(String reviewId) {
		this.reviewId = reviewId;
	}

	
}
