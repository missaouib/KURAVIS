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
 * UserAccountController.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 15.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.user;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.util.JsonUtil;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class UserAccountController extends AbstractHealtPlatformController {
	
	@Autowired
	private UserService userService;
	
	/**
	 * @param data
	 * @return response message
	 */
	@RequestMapping(value="/useraccount", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> createAccount(@RequestBody BasicDBObject data) {
		log.info("createAccount(" + data + ") - start");
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(new BasicDBObject(), new HttpHeaders(), HttpStatus.FORBIDDEN);
		log.info("createAccount(" + data + ") - end: " + response);
		return response;
	}
	
	/**
	 * @param data
	 * @param accountId 
	 * @return response message
	 */
	@RequestMapping(value="/useraccount/{accountId}", method = RequestMethod.GET, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> getAccount(@RequestBody BasicDBObject data, @PathVariable String accountId) {
		log.info("createAccount(" + data + ", " + accountId + ") - start");
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(new BasicDBObject(), new HttpHeaders(), HttpStatus.FORBIDDEN);
		log.info("createAccount(" + data + ", " + accountId + ") - end: " + response);
		return response;
	}
	
	/**
	 * @param data
	 * @param accountId 
	 * @return response message
	 */
	@RequestMapping(value="/useraccount/{accountId}", method = RequestMethod.PUT, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> updateAccount(@RequestBody BasicDBObject data, @PathVariable String accountId) {
		log.info("createAccount(" + data + ", " + accountId + ") - start");
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(new BasicDBObject(), new HttpHeaders(), HttpStatus.FORBIDDEN);
		log.info("createAccount(" + data + ", " + accountId + ") - end: " + response);
		return response;
	}
	
	/**
	 * @param accountId
	 * @return response message
	 */
	@RequestMapping(value="/useraccount/{accountId}", method = RequestMethod.DELETE, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value={ Roles.ADMIN })
	public ResponseEntity<DBObject> deleteAccount(@PathVariable String accountId) {
		log.info("createAccount(" + accountId + ") - start");
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(new BasicDBObject(), new HttpHeaders(), HttpStatus.FORBIDDEN);
		log.info("createAccount(" + accountId + ") - end: " + response);
		return response;
	}
}
