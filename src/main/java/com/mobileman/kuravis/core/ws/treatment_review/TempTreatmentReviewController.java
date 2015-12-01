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
 * TempTreatmentReviewController.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 18.11.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.treatment_review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.treatment_review.TempTreatmentReviewService;
import com.mobileman.kuravis.core.util.JsonUtil;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 * 
 */
@Controller
public class TempTreatmentReviewController extends AbstractHealtPlatformController {

	@Autowired
	private TempTreatmentReviewService tempTreatmentReviewService;

	/**
	 * @param page
	 * @param projection
	 * @return entities data collection
	 */
	@RequestMapping(value = "/" + EntityUtils.TEMP_TREATMENT_REVIEW, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public Page<Object> findEntities(@PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable page, @RequestParam(value = "proj", required = false) String projection) {
		log.info("findEntities(" + page + ") - start");

		List<DBObject> entities = this.tempTreatmentReviewService.findAll(page, projection);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Object> content = (List) entities;
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
	 * @param query
	 * @param d_page
	 * @param t_page
	 * @return entities data collection
	 */
	@RequestMapping(value = "/" + EntityUtils.TEMP_TREATMENT_REVIEW + "/query", method = RequestMethod.POST, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	public Page<DBObject> findEntitiesByQuery(@RequestBody BasicDBObject query, @PageableDefault(page = 0, size = Integer.MAX_VALUE) @Qualifier("d") Pageable d_page,
			@PageableDefault(page = 0, size = Integer.MAX_VALUE) @Qualifier("t") Pageable t_page) {
		log.info("findEntities(" + query + "," + d_page + ") - start");

		if (t_page != null && t_page.getSort() != null) {

			List<Order> torders = new ArrayList<Sort.Order>();
			Iterator<Order> iter = t_page.getSort().iterator();
			while (iter.hasNext()) {
				torders.add(iter.next());
			}

			if (torders.size() > 0) {
				List<Order> orders = new ArrayList<Sort.Order>();
				if (d_page.getSort() != null) {
					iter = d_page.getSort().iterator();
					while (iter.hasNext()) {
						orders.add(iter.next());
					}
				}

				orders.addAll(torders);

				Sort sort = new Sort(orders);
				d_page = new PageRequest(d_page.getPageNumber(), d_page.getPageSize(), sort);
			}
		}

		List<DBObject> content = this.tempTreatmentReviewService.findAllByQuery(query, d_page);
		Page<DBObject> result = new PageImpl<>(content, d_page, content.size());

		log.info("findEntities(" + query + "," + d_page + ") - end: " + result);
		return result;
	}

	@RequestMapping(value = "/" + EntityUtils.TEMP_TREATMENT_REVIEW + "/user/{authorId}", method = RequestMethod.GET, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<ModelMap> findUserReviews(@PathVariable String authorId) {
		List<DBObject> result = tempTreatmentReviewService.findUserReviewsGoupByDisease(authorId);
		int curedCount = 0;
		int currentCount = 0;
		for (DBObject d : result) {
			boolean curen = EntityUtils.getBoolean(TreatmentReview.CURED, d);
			List<?> trs = (List<?>) d.get(TreatmentReview.ENTITY_NAME + "s");
			if (curen) {
				curedCount += trs.size();
			} else {
				currentCount += trs.size();
			}
		}
		ModelMap modelMap = new ModelMap();
		modelMap.put("currentCount", currentCount);
		modelMap.put("curedCount", curedCount);
		modelMap.put("reviews", result);
		if (log.isDebugEnabled()) {
			log.debug("findUserReviews() return " + (currentCount + curedCount) + " items.");
		}
		return new ResponseEntity<ModelMap>(modelMap, HttpStatus.OK);
	}

	/**
	 * @param id
	 * @return entity data
	 */
	@RequestMapping(value = "/" + EntityUtils.TEMP_TREATMENT_REVIEW + "/{id}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<DBObject> findEntityById(@PathVariable String id) {
		log.info("findEntityById(" + id + ") - start");

		DBObject entity = this.tempTreatmentReviewService.findById(id);
		log.info("findEntityById(" + id + ") - end: " + entity);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(entity, new HttpHeaders(), entity == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
		return response;
	}

}
