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
package com.mobileman.kuravis.core.ws.treatment_review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
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
import com.mobileman.kuravis.core.domain.util.UserUtils;
import com.mobileman.kuravis.core.services.entity.CommonEntityService;
import com.mobileman.kuravis.core.services.fraud_report.FraudReportService;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewService;
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
public class TreatmentReviewController extends AbstractHealtPlatformController {

	@Autowired
	private TreatmentReviewService treatmentReviewService;

	@Autowired
	private CommonEntityService commonEntityService;

	@Autowired
	private FraudReportService fraudReportService;

	/**
	 * @param review
	 * @return error messahe in case of error
	 */
	@RequestMapping(value = "/" + TreatmentReview.ENTITY_NAME, method = RequestMethod.POST, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> createTreatmentReview(@RequestBody BasicDBObject review) {
		log.info("createTreatmentReview(" + review + ") - start");

		DBObject result = treatmentReviewService.createTreatmentReview(review);

		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("createTreatmentReview(" + review + ") - end: " + response);
		return response;
	}

	/**
	 * @param data
	 * @return error message in case of error
	 */
	@RequestMapping(value = "/" + TreatmentReview.ENTITY_NAME + "/forsubscription", method = RequestMethod.POST, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresGuest
	public ResponseEntity<DBObject> createTreatmentReviewForSubscription(@RequestBody BasicDBObject data) {
		log.info("createTreatmentReviewForSubscription(" + data + ") - start");

		DBObject result = treatmentReviewService.createTreatmentReviewForSubscription(data);

		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("createTreatmentReviewForSubscription(" + data + ") - end: " + response);
		return response;
	}

	/**
	 * @param id
	 * @param review
	 * @return error messahe in case of error
	 */
	@RequestMapping(value = "/" + TreatmentReview.ENTITY_NAME + "/{id}", method = RequestMethod.PUT, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> updateTreatmentReview(@PathVariable String id, @RequestBody BasicDBObject review) {
		log.info("editTreatmentReview(" + review + ") - start");

		DBObject result = treatmentReviewService.update(review);

		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("editTreatmentReview(" + review + ") - end: " + response);
		return response;
	}

	/**
	 * @param entityId
	 * @return error message in case of error
	 */
	@RequestMapping(value = "/" + TreatmentReview.ENTITY_NAME + "/{entityId}", method = RequestMethod.DELETE, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> deleteTreatmentReview(@PathVariable String entityId) {
		log.info("deleteTreatmentReview(" + entityId + ") - start");

		final DBObject result = treatmentReviewService.delete(entityId);

		HttpStatus status = HttpStatus.OK;
		if (result == null || result.get("result") == null || ErrorUtils.isError(result)) {
			status = HttpStatus.NOT_FOUND;
		}

		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, status);
		log.info("deleteTreatmentReview(" + entityId + ") - end: " + response);
		return response;
	}

	/**
	 * @param entityId
	 * @return result message
	 */
	@RequestMapping(value = "/vote/" + TreatmentReview.ENTITY_NAME + "/{entityId}", method = RequestMethod.PUT, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> voteForTreatmentReview(@PathVariable String entityId) {
		log.info("voteForEntity(" + entityId + ") - start");

		DBObject result = this.treatmentReviewService.voteForTreatmentReview(entityId);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, new HttpHeaders(), ErrorUtils.getStatus(result));
		log.info("voteForEntity(" + entityId + ") - end: " + response);
		return response;
	}

	/**
	 * @param entityId
	 * @param data
	 * @return result message
	 */
	@RequestMapping(value = "/comment/" + TreatmentReview.ENTITY_NAME + "/{entityId}", method = RequestMethod.PUT, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> commentTreatmentReview(@PathVariable String entityId, @RequestBody BasicDBObject data) {
		log.info("commentTreatmentReview(" + entityId + ", " + data + ") - start");

		DBObject result = this.treatmentReviewService.commentTreatmentReview(entityId, data);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, new HttpHeaders(), ErrorUtils.getStatus(result));
		log.info("commentTreatmentReview(" + entityId + ", " + data + ") - end: " + response);
		return response;
	}

	/**
	 * @param commentId
	 * @return result message
	 */
	@RequestMapping(value = "/comment/{commentId}", method = RequestMethod.DELETE, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> deleteTreatmentReviewComment(@PathVariable String commentId) {
		log.info("deleteTreatmentReviewComment(" + commentId + ") - start");

		this.treatmentReviewService.deleteTreatmentReviewComment(commentId);
		DBObject result = ErrorUtils.success();
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, new HttpHeaders(), ErrorUtils.getStatus(result));
		log.info("deleteTreatmentReviewComment(" + commentId + ") - end: " + response);
		return response;
	}

	/**
	 * @param entityId
	 * @param data
	 * @return result message
	 */
	@RequestMapping(value = "/report/treatmentreviewcomment/{entityId}", method = RequestMethod.POST, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> reportTreatmentReviewComment(@PathVariable String entityId, @RequestBody BasicDBObject data) {
		log.info("reportTreatmentReviewComment(" + entityId + ", " + data + ") - start");

		DBObject result = this.fraudReportService.reportEntity(EntityUtils.TREATMENT_REVIEW_EVENT, entityId, data);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("reportTreatmentReviewComment(" + entityId + ", " + data + ") - end: " + response);
		return response;
	}

	/**
	 * @param entityId
	 * @param data
	 * @return result message
	 */
	@RequestMapping(value = "/report/" + TreatmentReview.ENTITY_NAME + "/{entityId}", method = RequestMethod.POST, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> reportTreatmentReview(@PathVariable String entityId, @RequestBody BasicDBObject data) {
		log.info("reportTreatmentReview(" + entityId + ", " + data + ") - start");

		DBObject result = this.fraudReportService.reportEntity(TreatmentReview.ENTITY_NAME, entityId, data);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("reportTreatmentReview(" + entityId + ", " + data + ") - end: " + response);
		return response;
	}

	/**
	 * @param id
	 * @return entity data
	 */
	@RequestMapping(value = "/" + TreatmentReview.ENTITY_NAME + "/{id}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<DBObject> findEntityById(@PathVariable String id) {
		log.debug("findEntityById(" + id + ")");
		DBObject entity = this.treatmentReviewService.findById(id);
		Object currency = entity.get(TreatmentReview.CURRENCY);
		if (StringUtils.isEmpty(currency)) {
			// default currency is EUR
			entity.put(TreatmentReview.CURRENCY, "€");
		}
		log.debug("return: " + entity);
		return new ResponseEntity<DBObject>(entity, new HttpHeaders(), entity == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
	}

	/**
	 * @param query
	 * @param d_page
	 * @param t_page
	 * @return entities data collection
	 */
	@RequestMapping(value = "/" + TreatmentReview.ENTITY_NAME + "/query", method = RequestMethod.POST, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	public Page<DBObject> findEntitiesByQuery(@RequestBody BasicDBObject query, @PageableDefault(page = 0, size = Integer.MAX_VALUE) @Qualifier("d") Pageable d_page,
			@PageableDefault(page = 0, size = Integer.MAX_VALUE) @Qualifier("t") Pageable t_page) {
		log.info("findEntities(" + query + "," + d_page.getPageSize() + ")");

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

		List<DBObject> content = this.treatmentReviewService.findAllByQuery(query, d_page);
		Page<DBObject> result = new PageImpl<>(content, d_page, content.size());

		log.info("return " + content.size() + " items.");
		return result;
	}

	@RequestMapping(value = "/" + TreatmentReview.ENTITY_NAME + "/user/{authorId}", method = RequestMethod.GET, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	public ResponseEntity<ModelMap> findUserReviews(@PathVariable String authorId) {
		List<DBObject> result = treatmentReviewService.findUserReviewsGoupByDisease(authorId);
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
	 * @param page
	 * @param projection
	 * @return entities data collection
	 */
	@RequestMapping(value = "/" + TreatmentReview.ENTITY_NAME, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public Page<Object> findEntities(@PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable page, @RequestParam(value = "proj", required = false) String projection) {
		log.info("findEntities(" + page + ") - start");

		List<DBObject> entities = null;
		if (page.getPageSize() == Integer.MAX_VALUE) {
			entities = this.treatmentReviewService.findAll(projection);
		} else {
			entities = this.treatmentReviewService.findAll(page, projection);
		}

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
	 * @param body
	 * @return entities data collection
	 */
	@RequestMapping(value = "/" + TreatmentReview.ENTITY_NAME + "/existsforuser", method = RequestMethod.POST, produces = { JsonUtil.MEDIA_TYPE_APPLICATION_JSON })
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> reviewExistsForUser(@RequestBody BasicDBObject body) {
		log.debug("reviewExistsForUser(" + body + ")");
		String diseaseId = body.getString("diseaseId");
		String treatmentId = body.getString("treatmentId");
		String reviewId = treatmentReviewService.getReviewId(UserUtils.getLoggedUserId(), diseaseId, treatmentId);
		boolean exists = !StringUtils.isEmpty(reviewId);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(new BasicDBObject("exists", exists).append("reviewId", reviewId), HttpStatus.OK);
		log.debug("end: " + response);
		return response;
	}
}
