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
package com.mobileman.kuravis.core.domain.treatment_review;

import java.math.BigDecimal;
import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.mobileman.kuravis.core.domain.Entity;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.user.User;

/**
 * @author MobileMan GmbH
 *
 */
@JsonSerialize(include=Inclusion.NON_NULL)
@Document(collection=TreatmentReview.ENTITY_NAME)
public class TreatmentReview extends Entity implements TreatmentReviewAttributes {
	
	private Disease disease;
	
	private Treatment treatment;
	
	private User author;
	
	private String text;
	
	private BigDecimal rating;
	
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date dateOfFirstSymptoms;
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date dateOfDiagnosis;
	private boolean cured;
	
	//costs
	private String currency = "EUR";
	private BigDecimal doctorCosts;
	private BigDecimal treatmentPrice;
	private Integer treatmentQuantity;
	private boolean insuranceCovered;
	private BigDecimal insuranceCoverage;
	private BigDecimal coinsurance;
	
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
	public Date getDateOfFirstSymptoms() {
		return dateOfFirstSymptoms;
	}
	public void setDateOfFirstSymptoms(Date dateOfFirstSymptoms) {
		this.dateOfFirstSymptoms = dateOfFirstSymptoms;
	}
	public Date getDateOfDiagnosis() {
		return dateOfDiagnosis;
	}
	public void setDateOfDiagnosis(Date dateOfDiagnosis) {
		this.dateOfDiagnosis = dateOfDiagnosis;
	}
	public boolean isCured() {
		return cured;
	}
	public void setCured(boolean cured) {
		this.cured = cured;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public BigDecimal getDoctorCosts() {
		return doctorCosts;
	}
	public void setDoctorCosts(BigDecimal doctorCosts) {
		this.doctorCosts = doctorCosts;
	}
	public BigDecimal getTreatmentPrice() {
		return treatmentPrice;
	}
	public void setTreatmentPrice(BigDecimal treatmentPrice) {
		this.treatmentPrice = treatmentPrice;
	}
	public Integer getTreatmentQuantity() {
		return treatmentQuantity;
	}
	public void setTreatmentQuantity(Integer treatmentQuantity) {
		this.treatmentQuantity = treatmentQuantity;
	}
	public boolean isInsuranceCovered() {
		return insuranceCovered;
	}
	public void setInsuranceCovered(boolean insuranceCovered) {
		this.insuranceCovered = insuranceCovered;
	}
	public BigDecimal getInsuranceCoverage() {
		return insuranceCoverage;
	}
	public void setInsuranceCoverage(BigDecimal insuranceCoverage) {
		this.insuranceCoverage = insuranceCoverage;
	}
	public BigDecimal getCoinsurance() {
		return coinsurance;
	}
	public void setCoinsurance(BigDecimal coinsurance) {
		this.coinsurance = coinsurance;
	}
	/**
	 *
	 * @return author
	 */
	public User getAuthor() {
		return this.author;
	}
	/**
	 *
	 * @param author author
	 */
	public void setAuthor(User author) {
		this.author = author;
	}
	/**
	 *
	 * @return text
	 */
	public String getText() {
		return this.text;
	}
	/**
	 *
	 * @param text text
	 */
	public void setText(String text) {
		this.text = text;
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
