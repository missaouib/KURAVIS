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
 * TreatmentReviewEventServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 25.10.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.treatment_review.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.treatment_review.TreatmentReviewEventService;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class TreatmentReviewEventServiceImpl extends AbstractEntityServiceImpl implements TreatmentReviewEventService {
	
	@Autowired
	private UserService userService;

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return EntityUtils.TREATMENT_REVIEW_EVENT;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public List<DBObject> findAllByQuery(String type, DBObject query, Pageable page) {
		List<DBObject> events = super.findAllByQuery(type, query, page);
		Set<String> usersId = new HashSet<String>();
		for (DBObject event : events) {
			String userId = EntityUtils.getEntityId(event.get("user"));
			usersId.add(userId);
		}
		
		if (!usersId.isEmpty()) {
			Map<String, DBObject> settingsMap = userService.findUsersData(usersId, "settings", User.ATTR_GENDER, User.ATTR_YEAR_OF_BIRTH);
			for (DBObject event : events) {
				DBObject user = (DBObject) event.get("user");
				String userId = EntityUtils.getEntityId(user);
				if (settingsMap.containsKey(userId)) {
					user.put("settings", settingsMap.get(userId).get("settings"));
					user.put(User.ATTR_GENDER, settingsMap.get(userId).get(User.ATTR_GENDER));
					user.put(User.ATTR_YEAR_OF_BIRTH, settingsMap.get(userId).get(User.ATTR_YEAR_OF_BIRTH));
				}
			}
		}
		
		return events;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#findById(java.lang.String, java.lang.String)
	 */
	@Override
	public DBObject findById(String entityName, String id) {
		DBObject event = super.findById(entityName, id);
		if (event != null) {
			DBObject user = (DBObject) event.get("user");
			DBObject data = this.userService.findUserSettings(EntityUtils.getEntityId(user));
			user.put("settings", data.get("settings"));
			user.put(User.ATTR_GENDER, data.get(User.ATTR_GENDER));
			user.put(User.ATTR_YEAR_OF_BIRTH, data.get(User.ATTR_YEAR_OF_BIRTH));
		}
		
		return event;
	}
}
