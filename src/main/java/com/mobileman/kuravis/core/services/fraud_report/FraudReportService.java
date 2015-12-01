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
 * ProblemReport.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 15.8.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.fraud_report;

import com.mobileman.kuravis.core.services.entity.EntityService;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public interface FraudReportService extends EntityService {

	/**
	 * @param entityName
	 * @param entityId
	 * @param data
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject reportEntity(String entityName, String entityId, DBObject data);
	
	/**
	 * @param entityName
	 * @param entityId
	 */
	void deleteFraudReportsForEntity(String entityName, String entityId);
	
	
	/**
	 * @param id
	 * @return DBObject - error message in case of error, related data if success
	 */
	DBObject deleteFraudReportItem(String id);


	/**
	 * @param userId
	 */
	void deleteFraudReportsOfUser(String userId);

}
