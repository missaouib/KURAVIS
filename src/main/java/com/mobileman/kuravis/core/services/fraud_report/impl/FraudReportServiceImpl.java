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
 * ProblemReportImpl.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 15.8.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.fraud_report.impl;

import java.util.Date;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.fraud_report.FraudReport;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.RoleUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.fraud_report.FraudReportService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

/**
 * @author MobileMan GmbH
 * 
 */
@Service
public class FraudReportServiceImpl extends AbstractEntityServiceImpl implements FraudReportService {
	
	@Autowired
    private MongoTemplate mongoTemplate;

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return EntityUtils.FRAUD_REPORT;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public DBObject reportEntity(String entityName, String entityId, DBObject data) {
		if (entityName == null || entityName.trim().length() == 0) {
			return ErrorUtils.error("entityName is empty", ErrorCodes.INCORRECT_PARAMETER);
		}

		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser == null || !currentUser.isAuthenticated()) {
			return ErrorUtils.error("Not authenticated: currentUser=" + currentUser, ErrorCodes.USER_NOT_AUTHENTICATED);
		}

		DBObject user = (DBObject) currentUser.getPrincipal();
		if (user == null) {
			return ErrorUtils.error("Not authenticated: currentUser=" + currentUser, ErrorCodes.UNAUTHORIZED);
		}

		if (!data.containsField("fraudReportCategory")) {
			throw ErrorUtils.exception("Missing fraudReportCategory", ErrorCodes.INCORRECT_PARAMETER);
		}

		DBObject fraudReport = getCollection().findOne(new BasicDBObject(FraudReport.ATTR_ENTITY_NAME, entityName).append(FraudReport.ATTR_ENTITY_ID, entityId));
		if (fraudReport == null) {
			fraudReport = EntityUtils.createDBObjectId(EntityUtils.newId());
			fraudReport.put(EntityUtils.CREATED_ON, new Date());
			fraudReport.put(EntityUtils.MODIFIED_ON, fraudReport.get(EntityUtils.CREATED_ON));
			fraudReport.put(FraudReport.ATTR_ENTITY_NAME, entityName);
			fraudReport.put(FraudReport.ATTR_ENTITY_ID, entityId);
			fraudReport.put("text", data.get("text"));
			fraudReport.put("sourceId", data.get("sourceId"));
			fraudReport.put(EntityUtils.ATTR_USER, data.get(EntityUtils.ATTR_USER));
			getCollection().save(fraudReport);
		}

		DBObject fraudReportItem = EntityUtils.createDBObjectId(EntityUtils.newId());
		fraudReportItem.put(EntityUtils.ATTR_USER, EntityUtils.createBaseUser(user));
		fraudReportItem.put("fraudReportCategory", data.get("fraudReportCategory"));
		fraudReportItem.put("fraudReportId", fraudReport.get(EntityUtils.ID));
		fraudReportItem.put(FraudReport.ATTR_ENTITY_NAME, entityName);
		fraudReportItem.put(FraudReport.ATTR_ENTITY_ID, entityId);
		fraudReportItem.put(EntityUtils.CREATED_ON, new Date());
		fraudReportItem.put(EntityUtils.MODIFIED_ON, fraudReportItem.get(EntityUtils.CREATED_ON));
		fraudReportItem.put("note", data == null ? null : data.get("note"));

		save(EntityUtils.FRAUD_REPORT_ITEM, fraudReportItem);

		return ErrorUtils.success();
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#delete(java.lang.String, java.lang.String)
	 */
	@Override
	public DBObject delete(String type, String id) {
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser == null || !currentUser.isAuthenticated()) {
			return ErrorUtils.error("Not authenticated: currentUser=" + currentUser, ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		
		DBObject user = (DBObject) currentUser.getPrincipal();
		if (!RoleUtils.isAdminAccount((DBObject) user.get("account"))) {
			return ErrorUtils.error("Unknown account: " + user, ErrorCodes.UNAUTHORIZED);
		}
		
		WriteResult result = getCollection(EntityUtils.FRAUD_REPORT).remove(new BasicDBObject(EntityUtils.ID, id));
		if (ErrorUtils.isError(result)) {
			return ErrorUtils.error(result);
		}
		
		result = getCollection(EntityUtils.FRAUD_REPORT_ITEM).remove(new BasicDBObject("fraudReportId", id));
		if (ErrorUtils.isError(result)) {
			return ErrorUtils.error(result);
		}
		
		return ErrorUtils.success();
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.fraud_report.FraudReportService#deleteFraudReportItem(java.lang.String)
	 */
	@Override
	public DBObject deleteFraudReportItem(String id) {
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser == null || !currentUser.isAuthenticated()) {
			return ErrorUtils.error("Not authenticated: currentUser=" + currentUser, ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		
		DBObject user = (DBObject) currentUser.getPrincipal();
		if (user == null) {
			return ErrorUtils.error("Not authenticated: currentUser=" + currentUser, ErrorCodes.UNAUTHORIZED);
		}
		
		WriteResult result = getCollection(EntityUtils.FRAUD_REPORT_ITEM).remove(new BasicDBObject(EntityUtils.ID, id));
		if (ErrorUtils.isError(result)) {
			return ErrorUtils.error(result);
		}
		
		return ErrorUtils.success();
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.fraud_report.FraudReportService#deleteFraudReportsOfUser(java.lang.String)
	 */
	@Override
	public void deleteFraudReportsOfUser(String userId) {
		getCollection(EntityUtils.FRAUD_REPORT_ITEM).remove(new BasicDBObject("user._id", userId));
		getCollection().remove(new BasicDBObject("user._id", userId));
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.fraud_report.FraudReportService#deleteFraudReportsForEntity(java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteFraudReportsForEntity(String entityName, String entityId) {
		getCollection(EntityUtils.FRAUD_REPORT_ITEM).remove(new BasicDBObject(FraudReport.ATTR_ENTITY_NAME, entityName).append(FraudReport.ATTR_ENTITY_ID, entityId));
	}
}
