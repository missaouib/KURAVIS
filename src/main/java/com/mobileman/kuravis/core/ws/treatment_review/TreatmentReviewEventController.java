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
 * TreatmentReviewEventController.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 25.10.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.treatment_review;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewEventService;
import com.mobileman.kuravis.core.util.JsonUtil;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class TreatmentReviewEventController extends AbstractHealtPlatformController {
	
	@Autowired
	private TreatmentReviewEventService treatmentReviewEventService;
	
	/**
	 * @param query
	 * @param page
	 * @return entities data collection
	 */
	@RequestMapping(value="/" + EntityUtils.TREATMENT_REVIEW_EVENT +"/query", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody 
	public Page<DBObject> findAllByQuery(@RequestBody BasicDBObject query, @PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page) {
		log.info("findAllByQuery(" + query + "," + page + ") - start");
		
		List<DBObject> content = this.treatmentReviewEventService.findAllByQuery(query, page);
		Page<DBObject> result = new PageImpl<>(content, page, content.size());
		
		log.info("findAllByQuery(" + query + "," + page + ") - end: " + result);
		return result;
	}
}
