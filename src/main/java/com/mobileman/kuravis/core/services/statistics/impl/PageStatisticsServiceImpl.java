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
 * StatisticsServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 27.8.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.statistics.impl;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.statistics.PageStatisticsService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class PageStatisticsServiceImpl extends AbstractEntityServiceImpl implements PageStatisticsService {

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return EntityUtils.PAGE_STATISTICS;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.statistics.PageStatisticsService#save(java.lang.String, java.lang.String)
	 */
	@Override
	public DBObject save(String pageId, String parameter) {
		
		if (StringUtils.isEmpty(pageId)) {
			return ErrorUtils.error("pageId is empty", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		if (StringUtils.isEmpty(parameter)) {
			return ErrorUtils.error("parameter is empty", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		DBObject pageStat = new BasicDBObject();
		pageStat.put(EntityUtils.ID, EntityUtils.newId());
		pageStat.put(EntityUtils.CREATED_ON, new Date());
		pageStat.put(EntityUtils.MODIFIED_ON, new Date());
		pageStat.put("pageId", pageId);
		pageStat.put("parameter", parameter);
		WriteResult result = getCollection().save(pageStat);
		
		if (result.getError() != null) {
			return ErrorUtils.error(result.getError());
		}	
	
		return ErrorUtils.success();
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.statistics.PageStatisticsService#computeStatistics(java.lang.String, java.lang.String)
	 */
	@Override
	public DBObject computeStatistics(String pageId, String parameter) {
		long count = getCollection().count(new BasicDBObject("pageId", pageId).append("parameter", parameter));
		return new BasicDBObject("viewsCount", count);
	}

}
