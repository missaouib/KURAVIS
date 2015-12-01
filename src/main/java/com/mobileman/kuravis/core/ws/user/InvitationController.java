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
 * InvitationController.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 6.11.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.user;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.services.user.InvitationService;
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
public class InvitationController extends AbstractHealtPlatformController {

	@Autowired
	private InvitationService invitationService;
	
	/**
	 * @param id 
	 * @return All users invoitations
	 */
	@RequestMapping(value="/user/invitations/{id}", method = RequestMethod.GET, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value = { Roles.USER, Roles.ADMIN }, logical = Logical.OR)
	public ResponseEntity<DBObject> getInvitations(@PathVariable String id) {
		log.info("getInvitations(" + id + ") - start");
		
		DBObject result = this.invitationService.findAllForUser(id);
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, HttpStatus.OK);
		
		log.info("getInvitations(" + id + ") - end: " + response);
		return response;
	}
	
	/**
	 * @param body 
	 * @return send the invitation
	 */
	@RequestMapping(value="/user/invitations", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value = { Roles.USER, Roles.ADMIN }, logical = Logical.OR)
	public ResponseEntity<DBObject> sendInvitation(@RequestBody BasicDBObject body) {
		log.info("sendInvitation(" + body + ") - start");
		
		this.invitationService.sendInvitation((String)body.get("email"));
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(ErrorUtils.success(), HttpStatus.OK);
		
		log.info("sendInvitation() - end: " + response);
		return response;
	}
	
	/**
	 * User existence check
	 * @param body
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/invitations/canberegistered", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> canBeRegistered(@RequestBody BasicDBObject body) {
		log.info("canBeRegistered(" + body + ") - start");
		
		DBObject result = this.invitationService.inviteeData(body.getString("email"));
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, HttpStatus.OK);
		log.info("canBeRegistered(...) - end: " + response);
		return response;
	}
}
