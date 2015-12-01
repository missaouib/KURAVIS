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
package com.mobileman.kuravis.core.ws.treatment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PageableDefaults;
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

import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.util.JsonUtil;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class TreatmentController extends AbstractHealtPlatformController {
	
	@Autowired
	private TreatmentService treatmentService;
	
	/**
	 * @param query
	 * @param d_page
	 * @param t_page 
	 * @return entities data collection
	 */
	@RequestMapping(value="/" + EntityUtils.TREATMENT + "/query", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody 
	public Page<DBObject> findEntitiesByQuery(@RequestBody BasicDBObject query,
			@PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) @Qualifier("d") Pageable d_page, 
			@PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) @Qualifier("t") Pageable t_page) {
		log.info("findEntities(" + query + "," + d_page + ") - start");
		
		if (t_page != null && t_page.getSort() != null) {
						
			List<Order> orders = new ArrayList<Sort.Order>();
			Iterator<Order> iter = t_page.getSort().iterator();
			while (iter.hasNext()) {
				orders.add(iter.next());
			}
			
			if (orders.size() > 0) {
				if (t_page.getSort() != null) {
					iter = t_page.getSort().iterator();
					while (iter.hasNext()) {
						orders.add(iter.next());
					}
				}
				
				Sort sort = new Sort(orders);
				d_page = new PageRequest(d_page.getPageNumber(), d_page.getPageSize(), sort);
			}
		}
		
		List<DBObject> content = this.treatmentService.findAllByQuery(query, d_page);
		Page<DBObject> result = new PageImpl<>(content, d_page, content.size());
		
		log.info("findEntities(" + query + "," + d_page + ") - end: " + result);
		return result;
	}
	
	/**
	 * @param page
	 * @param projection
	 * @return entities data collection
	 */
	@RequestMapping(value="/" + EntityUtils.TREATMENT, method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public Page<Object> findEntities(
			@PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page,
			@RequestParam(value = "proj", required = false) String projection) {
		log.info("findEntities(" + page + ") - start");
		
		List<DBObject> entities = this.treatmentService.findAll(page, projection);
		
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
	 * @param id
	 * @return response message
	 */
	@RequestMapping(value="/" + EntityUtils.TREATMENT + "/{id}", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> deleteEntity(@PathVariable String id) {
		log.info("deleteEntity(" + id + ") - start");
		
		DBObject error = this.treatmentService.delete(id);
		
		HttpStatus status = HttpStatus.OK;
		if (ErrorUtils.isError(error)) {
			status = HttpStatus.NOT_FOUND;
		}
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(error, status);
		log.info("deleteEntity(" + id + ") - end: " + response);
		return response;
	}
	
	/**
	 * @param type
	 * @param object
	 * @return response message
	 */
	@RequestMapping(value = "/" + EntityUtils.TREATMENT, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> create(@RequestBody BasicDBObject object) {
		log.info("create(" + EntityUtils.TREATMENT + "," + object + ") - start");
		DBObject result = this.treatmentService.create(EntityUtils.TREATMENT, object);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("end: " + response);
		return response;
	}

	/**
	 * @param id
	 * @param object
	 * @return response message
	 */
	@RequestMapping(value="/" + EntityUtils.TREATMENT + "/{id}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> updateName(@PathVariable String id, @RequestBody BasicDBObject object) {
		log.info("updateName(" + id + "," + object + ") - start");
		DBObject result = this.treatmentService.update(id, object);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("end: " + response);
		return response;
	}
}
