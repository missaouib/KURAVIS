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
 * TreatmentReviewSummaryController.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 24.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.treatment_review;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PageableDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.services.entity.CommonEntityService;
import com.mobileman.kuravis.core.services.treatment_review_summary.TreatmentReviewSummaryService;
import com.mobileman.kuravis.core.util.DateTimeUtils;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class TreatmentReviewSummaryController extends AbstractHealtPlatformController {
	
	@Autowired
	private TreatmentReviewSummaryService treatmentReviewSummaryService;
	
	@Autowired
	private CommonEntityService commonEntityService;

	/**
	 * @param id
	 * @return entity data
	 */
	@RequestMapping(value="/" + TreatmentReviewSummary.ENTITY_NAME +"/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public ResponseEntity<DBObject> findById(@PathVariable String id) {
		log.info("findById(" + id + ") - start");
		
		DBObject entity = this.treatmentReviewSummaryService.findById(id);
		
		log.info("findById(" + id + ") - end: " + entity);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(entity, entity == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
		return response;
	}
	
	/**
	 * @param query
	 * @param page
	 * @return entities data collection
	 */
	@RequestMapping(value="/" + TreatmentReviewSummary.ENTITY_NAME +"/query", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody 
	public Page<DBObject> findAllByQuery(@RequestBody BasicDBObject query, @PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page) {
		log.info("findAllByQuery(" + query + "," + page + ") - start");
		
		List<DBObject> content = this.treatmentReviewSummaryService.findAllByQuery(query, page);
		for (DBObject tr : content) {
			tr.put("lastChanged", DateTimeUtils.fmtElapsedTime((Date) tr.get(TreatmentReview.MODIFIED_ON)));
		}
		Page<DBObject> result = new PageImpl<>(content, page, content.size());
		
		log.info("findAllByQuery(" + query + "," + page + ") - end: " + result);
		return result;
	}
	
	/**
	 * @param diseaseId
	 * @param page
	 * @return entities data collection
	 */
	@RequestMapping(value="/" + TreatmentReviewSummary.ENTITY_NAME +"/bydisease/{diseaseId}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody 
	public ResponseEntity<ModelMap> findAllByDisease(@PathVariable String diseaseId, @PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable page) {
		log.info("findAllByDisease(" + diseaseId + ", " + page + ")");
		
		Query query = Query.query(Criteria.where(TreatmentReviewSummary.DISEASE + "." + Disease.ID).is(diseaseId));
		long totalCount = treatmentReviewSummaryService.count(query.getQueryObject());
		List<DBObject> content = Collections.emptyList();
		if (totalCount > 0) {
			content = this.treatmentReviewSummaryService.findAllByDisease(diseaseId, page);
			// calculate elapsed time from now for creation date of a review
			for (DBObject tr : content) {
				tr.put("lastChanged", DateTimeUtils.fmtElapsedTime((Date) tr.get(TreatmentReview.MODIFIED_ON)));
			}
		}
		ModelMap model = new ModelMap();
		model.addAttribute("page", content);
		model.addAttribute("totalElements", totalCount);
		log.info("return " + content.size() + "/" + totalCount);
		return new ResponseEntity<ModelMap>(model, HttpStatus.OK);
	}
	
	/**
	 * @return disease index
	 */
	@RequestMapping(value="/" + TreatmentReviewSummary.ENTITY_NAME +"/diseaseindex", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody 
	public List<DBObject> diseaseIndex() {
		log.info("diseaseIndex() - start");
		
		List<DBObject> list = this.treatmentReviewSummaryService.diseasesIndex();
		
		log.info("diseaseIndex() - end: " + list);
		return list;
	}
	
	/**
	 * @param prefix 
	 * @return summaries by disease prefix
	 */
	@RequestMapping(value="/" + TreatmentReviewSummary.ENTITY_NAME +"/bydiseasenameprefix/{prefix}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody 
	public List<DBObject> findAllSummariesDiseaseWithTreatmentByIndex(@PathVariable String prefix) {
		log.info("findAllSummariesDiseaseWithTreatmentByIndex(" + prefix + ") - start");
		
		List<DBObject> list = this.treatmentReviewSummaryService.findAllDiseasesWithTreatmentsByIndex(prefix);
		
		log.info("findAllSummariesDiseaseWithTreatmentByIndex(" + prefix + ") - end: " + list);
		return list;
	}
	
	/**
	 * @param summary
	 * @return result message
	 */
	@RequestMapping(value = "/" + TreatmentReviewSummary.ENTITY_NAME, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresRoles({ Roles.ADMIN })
	public ResponseEntity<DBObject> createSuggestion(@RequestBody TreatmentReviewSummary summary) {
		log.debug("createSuggestion(" + summary + ") - start");
		
		String summaryId = this.treatmentReviewSummaryService.createSuggestion(summary);
		DBObject result = ErrorUtils.success();
		result.put(TreatmentReviewSummary.ID, summaryId);
		
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, HttpStatus.CREATED);
		log.debug("createSuggestion(" + summary + ") - end");
		return response;
	}
	
	/**
	 * @param id
	 * @return result message
	 */
	@RequestMapping(value = "/" + TreatmentReviewSummary.ENTITY_NAME + "/{id}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresRoles({ Roles.ADMIN })
	public ResponseEntity<DBObject> deleteSuggestion(@PathVariable String id) {
		log.debug("deleteSuggestion(" + id + ") - start");
		
		this.treatmentReviewSummaryService.delete(id);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(ErrorUtils.success(), HttpStatus.OK);
		log.debug("deleteSuggestion(" + id + ") - end");
		return response;
	}
}
