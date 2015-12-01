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
 * SubscriptionController.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 8.11.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.subscription;

import java.util.List;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.user.Roles;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.subscription.SubscriptionService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.util.JsonUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class SubscriptionController {
	
	private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);
	
	@Autowired
	private SubscriptionService subscriptionService; 

	/**
	 * @param page 
	 * @param projection 
	 * @return All subscriptions paginated
	 */
	@RequestMapping(value="/subscription/{id}", method = RequestMethod.GET, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	@RequiresRoles(value = { Roles.USER, Roles.ADMIN }, logical = Logical.OR)
	public ResponseEntity<List<DBObject>> getSubscriptions(@PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page,
			@RequestParam(value = "proj", required = false) String projection) {
		log.info("getSubscriptions(" + page + ", " + projection + ") - start");
		
		List<DBObject> result = this.subscriptionService.findAll(page, projection);
		
		ResponseEntity<List<DBObject>> response  = new ResponseEntity<List<DBObject>>(result, HttpStatus.OK);
		
		log.info("getSubscriptions(" + page + ", " + projection + ") - end: " + response);
		return response;
	}
	
	/**
	 * @param subscription 
	 * @return created a subscription
	 */
	@RequestMapping(value="/" + EntityUtils.SUBSCRIPTION + "", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@RequiresGuest
	public ResponseEntity<DBObject> createSubscription(@RequestBody BasicDBObject subscription) {
		log.info("createSubscription(" + subscription + ") - start");
		
		this.subscriptionService.subscribe(subscription);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(ErrorUtils.success(), HttpStatus.OK);
		
		log.info("createSubscription() - end: " + response);
		return response;
	}
}
