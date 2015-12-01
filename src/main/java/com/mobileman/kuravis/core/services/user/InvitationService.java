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
 * InvitationService.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 6.11.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.user;

import com.mobileman.kuravis.core.services.entity.EntityService;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public interface InvitationService extends EntityService {

	/**
	 * @param email
	 */
	void sendInvitation(String email);

	/**
	 * @param userId
	 * @return all invitations sent by given user
	 */
	DBObject findAllForUser(String userId);

	/**
	 * @param email
	 * @return data for an invitee (if user with given email has been invited and account has not been created for him)
	 */
	DBObject inviteeData(String email);

}
