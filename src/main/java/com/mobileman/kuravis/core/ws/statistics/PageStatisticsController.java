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
 * PageStatisticsController.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 16.8.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.services.statistics.PageStatisticsService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.util.JsonUtil;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class PageStatisticsController extends AbstractHealtPlatformController {

	private static final Logger log = LoggerFactory.getLogger(PageStatisticsController.class);
	
	@Autowired
	private PageStatisticsService pageStatisticsService;

	/**
	 * @param pageId 
	 * @param parameter 
	 * @return response message
	 */
	@RequestMapping(value="/page/statistics/{pageId}/{parameter}", method = RequestMethod.GET, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> getPageStatistics(@PathVariable String pageId, @PathVariable String parameter) {
		log.info("getPageStatistics(" + pageId + ", " + parameter + ") - start");
		
		DBObject result = this.pageStatisticsService.computeStatistics(pageId, parameter);
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("getPageStatistics(...) - end: " + response);
		return response;
	}
	
	/**
	 * @param pageId 
	 * @param parameter 
	 * @return response message
	 */
	@RequestMapping(value="/page/statistics/{pageId}/{parameter}", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> savePageStatistics(@PathVariable String pageId, @PathVariable String parameter) {
		log.info("pageStatistics(" + pageId + ", " + parameter + ") - start");
		
		DBObject result = this.pageStatisticsService.save(pageId, parameter);
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("savePageStatistics(...) - end: " + response);
		return response;
	}
}
