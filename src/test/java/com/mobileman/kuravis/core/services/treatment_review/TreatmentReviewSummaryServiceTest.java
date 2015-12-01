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
 * TreatmentReviewSummaryServiceTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 21.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.treatment_review;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.mobileman.kuravis.core.AbstractIntegrationTest;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.fraud_report.FraudReportService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.services.user.InvitationService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@ContextConfiguration(locations={"/spring/application-context.xml"})
public class TreatmentReviewSummaryServiceTest extends AbstractIntegrationTest {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private InvitationService invitationService;
	
	@Autowired
	private DiseaseService diseaseService;

	@Autowired
	private TreatmentService treatmentService;
	
	@Autowired
	private TreatmentReviewService treatmentReviewService;
	
	@Autowired
	private FraudReportService fraudReportService;
	
	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;
	
	/**
	 * @throws Exception
	 */
	@Test(expected=HealtPlatformException.class)
	public void createSuggestion_NoSuggestionFailure() throws Exception {
		DBObject treatment = treatmentService.findByProperty("name", "Vitamin C");
		DBObject disease = diseaseService.findByProperty("name", "Migraine");
		TreatmentReviewSummary summary = new TreatmentReviewSummary();
		summary.setDisease(diseaseService.getById(disease.get(Treatment.ID)));
		summary.setTreatment(treatmentService.getById(treatment.get(Treatment.ID)));
		summary.setSuggestion(false);
		
		treatmentReviewSummaryService.create(new TreatmentReviewSummary());
	}
	
	/**
	 * @throws Exception
	 */
	@Test(expected=HealtPlatformException.class)
	public void createSuggestion_NoTreatmentFailure() throws Exception {
		DBObject disease = diseaseService.findByProperty("name", "Migraine");
		TreatmentReviewSummary summary = new TreatmentReviewSummary();
		summary.setDisease(diseaseService.getById(disease.get(Treatment.ID)));
		summary.setSuggestion(true);
		
		treatmentReviewSummaryService.create(new TreatmentReviewSummary());
	}
	
	/**
	 * @throws Exception
	 */
	@Test(expected=HealtPlatformException.class)
	public void createSuggestion_NoDiseaseFailure() throws Exception {
		DBObject treatment = treatmentService.findByProperty("name", "Vitamin C");
		TreatmentReviewSummary summary = new TreatmentReviewSummary();
		summary.setTreatment(treatmentService.getById(treatment.get(Treatment.ID)));
		summary.setSuggestion(true);
		
		treatmentReviewSummaryService.create(new TreatmentReviewSummary());
	}
}
