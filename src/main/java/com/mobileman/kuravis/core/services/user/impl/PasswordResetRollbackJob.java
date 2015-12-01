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
 * PasswordResetRollbackJob.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 29.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.user.impl;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mongodb.BasicDBObject;

/**
 * @author MobileMan GmbH
 *
 */
public class PasswordResetRollbackJob extends QuartzJobBean  {
	
	/**
	 * Job group
	 */
	public static final String GROUP = "reset-credentials";

	/**
	 * 
	 */
	public PasswordResetRollbackJob() {
		super();
	}

	/** 
	 * {@inheritDoc}
	 * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
	 */
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		try {
			ApplicationContext appCtx = (ApplicationContext)context.getScheduler().getContext().get("applicationContext");
			MongoTemplate mongoTemplate = appCtx.getBean("mongoTemplate", MongoTemplate.class);
			String accountId = context.getJobDetail().getJobDataMap().getString("accountId");
			mongoTemplate.getCollection(EntityUtils.USERACCOUNT).update(new BasicDBObject(EntityUtils.ID, accountId), 
					new BasicDBObject("$unset", new BasicDBObject("resetPasswordUuid", "")));
			
		} catch (SchedulerException e) {
			throw new JobExecutionException(e);
		}
	}

}
