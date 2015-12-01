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
 * DiseaseController.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 10.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.disease;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.entity.CommonEntityService;
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
public class DiseaseController extends AbstractHealtPlatformController {
	
	private static final Logger log = LoggerFactory.getLogger(DiseaseController.class);
	
	@Autowired
	private DiseaseService diseaseService;
	
	@Autowired
	private CommonEntityService entityService;

	/**
	 * @param object
	 * @return error messahe in case of error
	 */
	@RequestMapping(value="/" + Disease.ENTITY_NAME + "/topten", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public ResponseEntity<List<DBObject>> topTen(@RequestBody(required=false) BasicDBObject object) {
		log.info("topTen(" + object + ") - start");
		
		List<DBObject> data = Collections.emptyList();
		HttpStatus status = HttpStatus.OK;
		try {
			data = diseaseService.findTopDiseases(10);
		} catch (Exception e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		ResponseEntity<List<DBObject>> response  = new ResponseEntity<>(data, new HttpHeaders(), status);
		log.info("topTen(" + object + ") - end: " + response);
		return response;
	}
	
	/**
	 * @param data
	 * @return response message
	 */
	@RequestMapping(value="/" + Disease.ENTITY_NAME + "/suggestion", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> suggestion(@RequestBody BasicDBObject data) {
		log.info("suggestion(" + data + ") - start");
		
		DBObject result = this.entityService.saveWithUserData(EntityUtils.DISEASE_SUGGESTION, data);		
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("suggestion(" + data + ") - end");
		return response;
	}
	
	/**
	 * @param query
	 * @param page
	 * @return entities data collection
	 */
	@RequestMapping(value="/" + Disease.ENTITY_NAME + "/query", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody 
	public Page<DBObject> findDiseasesByQuery( @RequestBody BasicDBObject query, @PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page) {
		log.info("findDiseasesByQuery(" + query + "," + page + ") - start");
		
		List<DBObject> content = this.diseaseService.findAllByQuery(query, page);
		Page<DBObject> result = new PageImpl<>(content, page, content.size());
		
		log.info("findDiseasesByQuery(" + query + "," + page + ") - end: " + result);
		return result;
	}
	
	/**
	 * @param page
	 * @return entities data collection
	 */
	@RequestMapping(value="/" + Disease.ENTITY_NAME +"/allsuggestions", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody 
	@RequiresRoles({ Roles.ADMIN })
	public Page<DBObject> findAllDiseasesForSuggestionAdmin(@PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page) {
		log.info("findAllDiseasesForSuggestionAdmin(" + page + ") - start");
		
		List<DBObject> content = this.diseaseService.findAllDiseasesWithTreatmentsForSuggestionAdmin(page);
		// calculate elapsed time from now for creation date of a review
		for (DBObject tr : content) {
			tr.put("lastChanged", DateTimeUtils.fmtElapsedTime((Date) tr.get(TreatmentReview.MODIFIED_ON)));
		}
		Page<DBObject> result = new PageImpl<>(content, page, content.size());
		
		log.info("findAllDiseasesForSuggestionAdmin(" + page + ") - end: " + result);
		return result;
	}
	
	/**
	 * @param page
	 * @param projection
	 * @return entities data collection
	 */
	@RequestMapping(value="/" + Disease.ENTITY_NAME, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public Page<Object> findDiseases(
			@PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page,
			@RequestParam(value = "proj", required = false) String projection) {
		log.info("findDiseases(" + page + ") - start");
		
		List<DBObject> entities = this.diseaseService.findAll(page, projection);
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Object> content = (List)entities;
		if (!StringUtils.isEmpty(projection)) {
			String[] properties = projection.split(",");
			if (properties.length == 1) {
				content = EntityUtils.transformToStringList(entities, projection);
			}	
		}
		
		if (content == null) {
			content = Collections.emptyList();
		}
		
		Page<Object> result = new PageImpl<Object>(content, page, content.size());
		log.info("findDiseases(" + page + ") - end: content.size=" + content.size());
		return result;
	}
	
	/**
	 * @param id
	 * @return response message
	 */
	@RequestMapping(value="/" + Disease.ENTITY_NAME + "/{id}", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> deleteEntity(@PathVariable String id) {
		log.info("deleteEntity(" + id + ") - start");
		
		DBObject error = this.diseaseService.delete(id);
		
		HttpStatus status = HttpStatus.OK;
		if (ErrorUtils.isError(error)) {
			status = HttpStatus.NOT_FOUND;
		}
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(error, status);
		log.info("deleteEntity(" + id + ") - end: " + response);
		return response;
	}
	
	/**
	 * @param object
	 * @return response message
	 */
	@RequestMapping(value = "/" + Disease.ENTITY_NAME, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> create(@RequestBody BasicDBObject object) {
		String type = Disease.ENTITY_NAME;
		log.info("create(" + type + "," + object + ") - start");
		DBObject result = this.diseaseService.create(type, object);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("end: " + response);
		return response;
	}
	
	/**
	 * @param id
	 * @param object
	 * @return response message
	 */
	@RequestMapping(value="/" + Disease.ENTITY_NAME + "/{id}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> updateName(@PathVariable String id, @RequestBody BasicDBObject object) {
		log.info("updateName(" + id + "," + object + ") - start");
		DBObject result = this.diseaseService.update(id, object);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("end: " + response);
		return response;
	}
}
