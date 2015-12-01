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
package com.mobileman.kuravis.core.ws.entity;

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

import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.entity.CommonEntityService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class EntityRestController extends AbstractHealtPlatformController {
		
	private static final Logger LOG = LoggerFactory.getLogger(EntityRestController.class);
	
	@Autowired
	private CommonEntityService entityService;
	
	
	/**
	 * @return new UID
	 */
	@RequestMapping(value="/newid", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@RequiresAuthentication
	public List<String> getNewId() {
		LOG.info("getNewId() - start");
		
		List<String> ids = this.entityService.getNewIds(1);
		
		LOG.info("getNewId() - end: " + ids);
		return ids;
	}
	
	/**
	 * @param count
	 * @return new UIDs
	 */
	@RequestMapping(value="/newid/{count}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@RequiresAuthentication
	public List<String> getNewId(@PathVariable Integer count) {
		LOG.info("getNewId(" + count + ") - start");
		
		List<String> ids = this.entityService.getNewIds(count);
		
		LOG.info("getNewId(" + count + ") - end: " + ids);
		return ids;
	}
	
	/**
	 * @param type
	 * @param id
	 * @return entity data
	 */
	@RequestMapping(value="/{type}/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public ResponseEntity<DBObject> findEntityById(@PathVariable String type, @PathVariable String id) {
		LOG.info("findEntityById(" + type + "," + id + ") - start");
		
		DBObject entity = this.entityService.findById(type, id);
		LOG.info("findEntityById(" + type + "," + id + ") - end: " + entity);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(entity, new HttpHeaders(), entity == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
		return response;
	}
	
	/**
	 * @param type
	 * @param page
	 * @param projection
	 * @return entities data collection
	 */
	@RequestMapping(value="/{type}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@RequiresRoles(value={ Roles.ADMIN })
	public Page<Object> findEntities(
			@PathVariable String type,
			@PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page,
			@RequestParam(value = "proj", required = false) String projection) {
		LOG.info("findEntities(" + type + "," + page + ") - start");
		
		List<DBObject> entities = this.entityService.findAll(type, projection, page);
		
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
		LOG.info("findEntities(" + type + "," + page + ") - end: content.size=" + content.size());
		return result;
	}
	
	/**
	 * @param type
	 * @param query
	 * @param page
	 * @return entities data collection
	 */
	@RequestMapping(value="/{type}/query", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody 
	@RequiresRoles(value={ Roles.ADMIN })
	public Page<DBObject> findEntitiesByQuery(@PathVariable String type, @RequestBody BasicDBObject query, @PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page) {
		LOG.info("findEntities(" + type + "," + query + "," + page + ") - start");
		
		List<DBObject> content = this.entityService.findAllByQuery(type, query, page);
		Page<DBObject> result = new PageImpl<>(content, page, content.size());
		
		LOG.info("findEntities(" + type + "," + query + "," + page + ") - end: " + result);
		return result;
	}
	
	/**
	 * @param type
	 * @param id
	 * @param object
	 * @return response message
	 */
	@RequestMapping(value="/{type}/{id}", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> updateEntity(@PathVariable String type, @PathVariable String id, @RequestBody BasicDBObject object) {
		LOG.info("updateEntity(" + type + "," + id + "," + object + ") - start");
		
		DBObject result = this.entityService.update(type, id, object);
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		LOG.info("updateEntity(" + type + "," + id + "," + object + ") - end: " + response);
		return response;
	}
	
	/**
	 * @param type
	 * @param id
	 * @return response message
	 */
	@RequestMapping(value="/{type}/{id}", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> deleteEntity(@PathVariable String type, @PathVariable String id) {
		LOG.info("deleteEntity(" + type + "," + id + ") - start");
		
		DBObject error = this.entityService.delete(type, id);
		
		HttpStatus status = HttpStatus.OK;
		if (ErrorUtils.isError(error)) {
			status = HttpStatus.NOT_FOUND;
		}
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(error, status);
		LOG.info("deleteEntity(" + type + "," + id + ") - end: " + response);
		return response;
	}
}
