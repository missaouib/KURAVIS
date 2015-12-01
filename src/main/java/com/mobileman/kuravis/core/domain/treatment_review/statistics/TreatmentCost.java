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
 * TreatmentCosts.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 28.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.treatment_review.statistics;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mongodb.DBObject;


/**
 * @author MobileMan GmbH
 *
 */
public class TreatmentCost {	
	
	private String currency;
	
	private BigDecimal doctorCosts;
	
	private BigDecimal treatmentPrice;
	
	private BigDecimal treatmentQuantity;
	
	private Boolean insuranceCovered;
	
	private BigDecimal insuranceCoverage;
	
	private BigDecimal coinsurance;

	/**
	 *
	 * @return currency
	 */
	public String getCurrency() {
		return this.currency;
	}

	/**
	 *
	 * @param currency currency
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	/**
	 *
	 * @return doctorCosts
	 */
	public BigDecimal getDoctorCosts() {
		return this.doctorCosts;
	}

	/**
	 *
	 * @param doctorCosts doctorCosts
	 */
	public void setDoctorCosts(BigDecimal doctorCosts) {
		this.doctorCosts = doctorCosts;
	}

	/**
	 *
	 * @return treatmentPrice
	 */
	public BigDecimal getTreatmentPrice() {
		return this.treatmentPrice;
	}

	/**
	 *
	 * @param treatmentPrice treatmentPrice
	 */
	public void setTreatmentPrice(BigDecimal treatmentPrice) {
		this.treatmentPrice = treatmentPrice;
	}

	/**
	 *
	 * @return treatmentQuantity
	 */
	public BigDecimal getTreatmentQuantity() {
		return this.treatmentQuantity;
	}

	/**
	 *
	 * @param treatmentQuantity treatmentQuantity
	 */
	public void setTreatmentQuantity(BigDecimal treatmentQuantity) {
		this.treatmentQuantity = treatmentQuantity;
	}

	/**
	 *
	 * @return insuranceCovered
	 */
	public Boolean getInsuranceCovered() {
		return this.insuranceCovered;
	}

	/**
	 *
	 * @param insuranceCovered insuranceCovered
	 */
	public void setInsuranceCovered(Boolean insuranceCovered) {
		this.insuranceCovered = insuranceCovered;
	}

	/**
	 *
	 * @return insuranceCoverage
	 */
	public BigDecimal getInsuranceCoverage() {
		return this.insuranceCoverage;
	}

	/**
	 *
	 * @param insuranceCoverage insuranceCoverage
	 */
	public void setInsuranceCoverage(BigDecimal insuranceCoverage) {
		this.insuranceCoverage = insuranceCoverage;
	}

	/**
	 *
	 * @return coinsurance
	 */
	public BigDecimal getCoinsurance() {
		return this.coinsurance;
	}

	/**
	 *
	 * @param coinsurance coinsurance
	 */
	public void setCoinsurance(BigDecimal coinsurance) {
		this.coinsurance = coinsurance;
	}

	/**
	 * @param object
	 * @return BigDecimal
	 */
	private static BigDecimal getBigDecimal(Object object) {
		if (object == null) {
			return null;
		} else if (Integer.class.isInstance(object) || Short.class.isInstance(object) || Byte.class.isInstance(object) || AtomicInteger.class.isInstance(object)) {
			return new BigDecimal(Number.class.cast(object).intValue());
		} else if (Long.class.isInstance(object) || AtomicLong.class.isInstance(object)) {
			return new BigDecimal(Number.class.cast(object).longValue());
		} else if (Double.class.isInstance(object) || Number.class.isInstance(object)) {
			return new BigDecimal(Number.class.cast(object).doubleValue());
		} else if (String.class.isInstance(object)) {
			return new BigDecimal(String.class.cast(object));
		}
		
		return null;
	}
	
	/**
	 * @param review
	 * @return TreatmentCost
	 */
	public static TreatmentCost createFromReview(DBObject review) {
		if (review == null || review.get(TreatmentReview.TREATMENT_PRICE) == null || review.get(TreatmentReview.TREATMENT_QUANTITY) == null) {
			return null;
		}
		
		TreatmentCost cost = new TreatmentCost();
		cost.setCurrency((String) review.get(TreatmentReview.CURRENCY));
		cost.setDoctorCosts(getBigDecimal(review.get(TreatmentReview.DOCTOR_COSTS)));
		cost.setTreatmentPrice(getBigDecimal(review.get(TreatmentReview.TREATMENT_PRICE)));
		cost.setTreatmentQuantity(getBigDecimal(review.get(TreatmentReview.TREATMENT_QUANTITY)));
		cost.setInsuranceCovered((Boolean)review.get(TreatmentReview.INSURANCE_COVERED));
		cost.setInsuranceCoverage(getBigDecimal(review.get(TreatmentReview.INSURANCE_COVERAGE)));
		cost.setCoinsurance(getBigDecimal(review.get(TreatmentReview.COINSURANCE)));
		
		return cost;
	}

	/**
	 * @return total costs of medication
	 */
	public BigDecimal costOfMedication() {
		if (getTreatmentPrice() == null || getTreatmentQuantity() == null) {
			return null;
		}
		
		return getTreatmentPrice().multiply(getTreatmentQuantity());
	}
	
}
