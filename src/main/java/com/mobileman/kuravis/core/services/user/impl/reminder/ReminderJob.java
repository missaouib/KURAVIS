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
 * ReminderJob.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 23.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.user.impl.reminder;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.user.reminder.Reminder;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.event.EventService;
import com.mobileman.kuravis.core.services.messaging.mail.MailService;
import com.mobileman.kuravis.core.services.user.ReminderService;
import com.mobileman.kuravis.core.services.user.UserService;

/**
 * @author MobileMan GmbH
 *
 */
public class ReminderJob extends QuartzJobBean {

	/**
	 * 
	 */
	public static final String GROUP = "reminders";

	/**
	 * 
	 */
	public ReminderJob() {
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
			//MongoTemplate mongoTemplate = appCtx.getBean("mongoTemplate", MongoTemplate.class);
			UserService userService = appCtx.getBean("userService", UserService.class);
			EventService eventService = appCtx.getBean("eventService", EventService.class);
			ReminderService reminderService = appCtx.getBean("reminderService", ReminderService.class);
			MailService mailService = appCtx.getBean("mailService", MailService.class);
			String reminderId = context.getJobDetail().getJobDataMap().getString(EntityUtils.ID);
			
			Reminder reminder = reminderService.getById(reminderId);
			TreatmentEvent event = (TreatmentEvent)eventService.getById(reminder.getEventId(), TreatmentEvent.class);
			User user = userService.getById(reminder.getUser().get_id());
			mailService.sendTreatmentEventRemminder(user, event);
			
		} catch (Exception e) {
			throw new JobExecutionException(e);
		}
	}
}
