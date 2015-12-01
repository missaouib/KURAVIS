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

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.services.user.UserNotificationService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.util.JsonUtil;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */

@Controller
public class UserNotificationController extends AbstractHealtPlatformController {
	
	@Autowired
	private UserNotificationService userNotificationService;
	
	/**
	 * Process delete of user
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/unreadnotificationscount", method = RequestMethod.GET, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value = { Roles.USER, Roles.ADMIN }, logical = Logical.OR)
	public ResponseEntity<DBObject> getUnreadNotificationsCount() {
		log.info("unreadNotificationsCount() - start");
		
		DBObject result = this.userNotificationService.getUnreadNotificationsCount();
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("unreadNotificationsCount() - end: " + response);
		return response;
	}
	
	/**
	 * Process delete of user
	 * @param page 
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/notifications", method = RequestMethod.GET, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value = { Roles.USER, Roles.ADMIN }, logical = Logical.OR)
	public ResponseEntity<List<DBObject>> getNotifications(@PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page) {
		log.info("getNotifications(" + page + ") - start");
		
		List<DBObject> result = this.userNotificationService.getNotifications(page);
		ResponseEntity<List<DBObject>> response  = new ResponseEntity<List<DBObject>>(result, HttpStatus.OK);
		log.info("getNotifications() - end: " + response);
		return response;
	}
	
	/**
	 * Process delete of user
	 * @param page 
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/unreadnotifications", method = RequestMethod.GET, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value = { Roles.USER, Roles.ADMIN }, logical = Logical.OR)
	public ResponseEntity<List<DBObject>> getUnreadNotifications(@PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page) {
		log.info("getNotifications(" + page + ") - start");
		
		List<DBObject> result = this.userNotificationService.getUnreadNotifications(page);
		ResponseEntity<List<DBObject>> response  = new ResponseEntity<List<DBObject>>(result, HttpStatus.OK);
		log.info("getNotifications() - end: " + response);
		return response;
	}
}
