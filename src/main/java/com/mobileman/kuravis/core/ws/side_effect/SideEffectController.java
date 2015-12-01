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
 * SideEffectController.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 17.11.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.side_effect;

import java.util.Collections;
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

import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.treatment_side_effect.TreatmentSideEffectService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class SideEffectController extends AbstractHealtPlatformController {

	private static final Logger log = LoggerFactory.getLogger(SideEffectController.class);
	
	@Autowired
	private TreatmentSideEffectService treatmentSideEffectService;
	

	/**
	 * @param page
	 * @param projection
	 * @return entities data collection
	 */
	@RequestMapping(value="/" + EntityUtils.TREATMENT_SIDE_EFFECT, method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public Page<Object> findEntities(
			@PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page,
			@RequestParam(value = "proj", required = false) String projection) {
		log.info("findEntities(" + page + ") - start");
		
		List<DBObject> entities = this.treatmentSideEffectService.findAll(page, projection);
		
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
		log.info("findEntities(" + page + ") - end: content.size=" + content.size());
		return result;
	}
	
	/**
	 * @param type
	 * @param object
	 * @return response message
	 */
	@RequestMapping(value = "/" + EntityUtils.TREATMENT_SIDE_EFFECT, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> create(@RequestBody BasicDBObject object) {
		String type = EntityUtils.TREATMENT_SIDE_EFFECT;
		log.info("create(" + type + "," + object + ") - start");
		DBObject result = this.treatmentSideEffectService.create(type, object);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("end: " + response);
		return response;
	}

	/**
	 * @param id
	 * @param object
	 * @return response message
	 */
	@RequestMapping(value="/" + EntityUtils.TREATMENT_SIDE_EFFECT + "/{id}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> updateName(@PathVariable String id, @RequestBody BasicDBObject object) {
		log.info("updateName(" + id + "," + object + ") - start");
		DBObject result = this.treatmentSideEffectService.update(id, object);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("end: " + response);
		return response;
	}

}
