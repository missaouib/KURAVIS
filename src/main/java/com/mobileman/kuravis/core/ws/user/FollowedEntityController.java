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
 * UserNotificationController.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 19.10.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.user;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.user.FollowedEntity;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.UserUtils;
import com.mobileman.kuravis.core.services.user.FollowedEntityService;
import com.mobileman.kuravis.core.util.JsonUtil;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;

@Controller
public class FollowedEntityController extends AbstractHealtPlatformController {

	@Autowired
	private FollowedEntityService userFollowedEntityService;

	@Autowired
	private FollowedEntityValidator validator;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}

	@RequestMapping(value = "/user/follow/{entityType}/{entityId}", method = RequestMethod.GET, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<ModelMap> getFollowedEntityId(@PathVariable String entityType, @PathVariable String entityId) {
		String logMsg = null;
		if (log.isDebugEnabled()) {
			logMsg = "getFollowedEntityId(" + entityType + ", " + entityId + ")";
		}
		String userId = EntityUtils.getEntityId(UserUtils.getLoggedUser());
		FollowedEntity follow = userFollowedEntityService.getByEntity(userId, entityType, entityId);
		if (follow == null) {
			if (log.isDebugEnabled()) {
				log.debug(logMsg +" return " + HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<ModelMap>(HttpStatus.NOT_FOUND);
		}
		ResponseEntity<ModelMap> response = new ResponseEntity<ModelMap>(new ModelMap("followItemId", follow.get_id()), HttpStatus.OK);
		if (log.isDebugEnabled()) {
			log.debug(logMsg +" return " + follow.get_id());
		}
		return response;
	}

	@RequestMapping(value = "/user/follow", method = RequestMethod.POST, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<ModelMap> follow(@RequestBody @Validated FollowedEntity object, Errors errors) {
		if (log.isDebugEnabled()) {
			log.debug("follow() " + object);
		}
		if (errors.hasErrors()) {
			return getErrorResponse(errors);
		}
		EntityUtils.setBaseProperties(object);
		String id = userFollowedEntityService.follow(object);
		ResponseEntity<ModelMap> response = new ResponseEntity<ModelMap>(new ModelMap(FollowedEntity.ID, id), HttpStatus.CREATED);
		if (log.isDebugEnabled()) {
			log.debug("end: " + response);
		}
		return response;
	}

	@RequestMapping(value = "/user/follow/{id}", method = RequestMethod.DELETE, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<ModelMap> unfollow(@PathVariable(value = "id") String followItemId) {
		if (log.isDebugEnabled()) {
			log.debug("unfollow(" + followItemId + ")");
		}
		try {
			userFollowedEntityService.unfollow(followItemId);
		} catch (EmptyResultDataAccessException e) {
			log.debug("returns " + HttpStatus.NOT_FOUND + ", " + e.getMessage());
			return new ResponseEntity<ModelMap>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<ModelMap>(HttpStatus.OK);
	}

	@RequestMapping(value = "/user/follow", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<List<FollowedEntity>> getAllUserFollowedItems() {
		String userId = EntityUtils.getEntityId(UserUtils.getLoggedUser());
		log.debug("getAllUserFollowedItems(" + userId + ")");
		final List<FollowedEntity> entities = userFollowedEntityService.findByUserId(userId);
		log.debug("return " + entities.size() + " items.");
		return new ResponseEntity<List<FollowedEntity>>(entities, HttpStatus.OK);
	}

}
