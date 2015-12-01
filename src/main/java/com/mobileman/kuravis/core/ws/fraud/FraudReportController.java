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
 * FraudReportController.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 29.8.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.fraud;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.fraud_report.FraudReportService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.util.JsonUtil;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class FraudReportController extends AbstractHealtPlatformController {
	
	private static final Logger log = LoggerFactory.getLogger(FraudReportController.class);
	
	@Autowired
	private FraudReportService fraudReportService;

	/**
	 * @param entityId
	 * @return error message in case of error
	 */
	@RequestMapping(value="/" + EntityUtils.FRAUD_REPORT + "/{entityId}", method = RequestMethod.DELETE, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> deleteFraudReport(@PathVariable String entityId) {
		log.info("deleteFraudReport(" + entityId + ") - start");
		
		final DBObject result = fraudReportService.delete(entityId);
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("deleteFraudReport(" + entityId + ") - end: " + response);
		return response;
	}
	
	/**
	 * @param entityId
	 * @return error message in case of error
	 */
	@RequestMapping(value="/" + EntityUtils.FRAUD_REPORT_ITEM + "/{entityId}", method = RequestMethod.DELETE, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> deleteFraudReportItem(@PathVariable String entityId) {
		log.info("deleteFraudReportItem(" + entityId + ") - start");
		
		final DBObject result = fraudReportService.deleteFraudReportItem(entityId);
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("deleteFraudReportItem(" + entityId + ") - end: " + response);
		return response;
	}
}
