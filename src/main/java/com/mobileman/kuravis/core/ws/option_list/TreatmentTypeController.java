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
 * OptionListController.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 19.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.option_list;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.option_list.treatment_type.TreatmentType;
import com.mobileman.kuravis.core.services.option_list.treatment_type.TreatmentTypeService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class TreatmentTypeController {

	private static final Logger log = LoggerFactory.getLogger(TreatmentTypeController.class);
	
	@Autowired
	private TreatmentTypeService treatmentTypeService;
	
	/**
	 * @param treatmentType
	 * @param errors
	 * @return operation result
	 */
	@RequestMapping(value = "/option_list/" + TreatmentType.ENTITY_NAME, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> create(@RequestBody TreatmentType treatmentType, Errors errors) {
		log.debug("create(" + treatmentType + ") - start");
		
		this.treatmentTypeService.create(treatmentType);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(ErrorUtils.success(), HttpStatus.OK);
		log.debug("create(" + treatmentType + ") - end");
		return response;
	}
	
	/**
	 * @param entity
	 * @return operation result
	 */
	@RequestMapping(value = "/option_list/" + TreatmentType.ENTITY_NAME, method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> update(@RequestBody BasicDBObject entity) {
		log.debug("update(" + entity + ") - start");
		
		this.treatmentTypeService.update(entity);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(ErrorUtils.success(), HttpStatus.OK);
		log.debug("update(" + entity + ") - end");
		return response;
	}
	
	/**
	 * @return operation result
	 */
	@RequestMapping(value = "/option_list/" + TreatmentType.ENTITY_NAME, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<List<DBObject>> findAllTreatmentTypes() {
		log.debug("findAllTreatmentTypes() - start");
		
		List<DBObject> result = this.treatmentTypeService.findAll();
		ResponseEntity<List<DBObject>> response = new ResponseEntity<List<DBObject>>(result, HttpStatus.OK);
		log.debug("findAllTreatmentTypes() - end");
		return response;
	}
	
	/**
	 * @param id 
	 * @return operation result
	 */
	@RequestMapping(value = "/option_list/" + TreatmentType.ENTITY_NAME + "/{id}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> findTreatmentTypeById(@PathVariable String id) {
		log.debug("findTreatmentTypeById(" + id + ") - start");
		DBObject result = this.treatmentTypeService.findById(id);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, HttpStatus.OK);
		log.debug("findTreatmentTypeById(" + id + ") - end");
		return response;
	}
}
