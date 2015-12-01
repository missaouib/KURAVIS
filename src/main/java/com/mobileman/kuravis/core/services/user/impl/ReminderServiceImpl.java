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
 * ReminderServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 23.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.user.impl;

import static org.quartz.DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule;

import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TimeOfDay;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.user.reminder.Reminder;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.user.ReminderService;
import com.mobileman.kuravis.core.services.user.impl.reminder.ReminderJob;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

/**
 * @author MobileMan GmbH
 *
 */
@Service("reminderService")
public class ReminderServiceImpl extends AbstractEntityServiceImpl<Reminder> implements ReminderService {
	
	@Autowired
	private SchedulerFactoryBean schedulerFactoryBean;
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return Reminder.ENTITY_NAME;
	}

	/**
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.ReminderService#createReminderForEvent(com.mobileman.kuravis.core.domain.event.TreatmentEvent)
	 */
	@Override
	public String createReminderForEvent(TreatmentEvent event) {
		if (event.getStart() == null) {
			throw ErrorUtils.exception("TreatmentEvent.start.required", ErrorCodes.INCORRECT_PARAMETER);
		}
		
		Reminder reminder = new Reminder();
		reminder.set_id(EntityUtils.newId());
		reminder.setEventId(event.get_id());
		reminder.setQuartzJobId("reminder-job-" + reminder.get_id());
		String _id = create(reminder);
		
		try {
			
			JobDetail jobDetail = JobBuilder.newJob(ReminderJob.class)
					.withIdentity(reminder.getQuartzJobId(), ReminderJob.GROUP)
					.storeDurably(false)
					.requestRecovery(true)
					.usingJobData(EntityUtils.ID, _id)
					.build();
			
			TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
					.withIdentity("reminder-trigger-" + reminder.get_id(), ReminderJob.GROUP)
					.forJob(jobDetail)
					.withDescription("Reminder job trigger - " + _id)
					.startAt(event.getStart());
			
			if (event.getEnd() != null) {
				triggerBuilder.endAt(event.getEnd());
			}
			
			// if frequency is not defined - nothing to do with schedule - it will be fired once automatically
			if (event.getFrequency() != null) {
				switch (event.getFrequency()) {
				case ONCE: {
					// nothing to do with schedule - it will be fired once automatically
				}
					break;
				case DAILY: {
					TimeOfDay timeOfStartDay = TimeOfDay.hourAndMinuteFromDate(event.getStart());
					triggerBuilder
							.withSchedule(dailyTimeIntervalSchedule()
							.onEveryDay()
							.startingDailyAt(timeOfStartDay));
				}
					break;
				case WEEKLY: {
					triggerBuilder.withSchedule(CalendarIntervalScheduleBuilder
							.calendarIntervalSchedule()
							.withIntervalInWeeks(1));
				}
					break;
				case BIWEEKLY: {
					triggerBuilder.withSchedule(CalendarIntervalScheduleBuilder
							.calendarIntervalSchedule()
							.withIntervalInWeeks(2));
				}
					
					break;
				case MONTHLY:
					triggerBuilder.withSchedule(CalendarIntervalScheduleBuilder
							.calendarIntervalSchedule()
							.withIntervalInMonths(1));
					break;
				default:
					throw ErrorUtils.exception("Unsuported medication entry frequency " + event.getFrequency());
				}
			}
			
			

			
			Trigger trigger = triggerBuilder.build();
			this.schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
			
			
		} catch (SchedulerException e) {
			throw ErrorUtils.exception("Can not create reminder", e);
		}
		
		return _id;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.user.ReminderService#deleteAllRemindersOfEvent(java.lang.String)
	 */
	@Override
	public void deleteAllRemindersOfEvent(String eventId) {
		QueryBuilder builder = QueryBuilder.start().put(Reminder.EVENT_ID).is(eventId);
		
		for (DBObject reminderData : findAllByQuery(builder.get())) {
			String quartzJobId = (String) reminderData.get(Reminder.QUARTZ_JOB_ID);
			try {
				this.schedulerFactoryBean.getScheduler().deleteJob(JobKey.jobKey(quartzJobId, ReminderJob.GROUP));
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			super.delete((String) reminderData.get(Reminder.ID));
		}
	}

}
