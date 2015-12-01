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
 * ConfigurationServiceImpl.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 14.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.configuration.impl;

import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.services.configuration.ConfigurationService;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {

	private int minPasswordLength;

	private int maxPasswordLength;

	private int maxUnsuccessfulLoginsCount;

	private int resetPasswordLifetime;
	
	/**
	 *
	 * @return minPasswordLength
	 */
	@Override
	public int getMinPasswordLength() {
		return this.minPasswordLength;
	}

	/**
	 *
	 * @param minPasswordLength minPasswordLength
	 */
	public void setMinPasswordLength(int minPasswordLength) {
		this.minPasswordLength = minPasswordLength;
	}

	/**
	 *
	 * @return maxPasswordLength
	 */
	@Override
	public int getMaxPasswordLength() {
		return this.maxPasswordLength;
	}

	/**
	 *
	 * @param maxPasswordLength maxPasswordLength
	 */
	public void setMaxPasswordLength(int maxPasswordLength) {
		this.maxPasswordLength = maxPasswordLength;
	}

	/**
	 *
	 * @return maxUnsuccessfulLoginsCount
	 */
	@Override
	public int getMaxUnsuccessfulLoginsCount() {
		return this.maxUnsuccessfulLoginsCount;
	}

	/**
	 *
	 * @param maxUnsuccessfulLoginsCount maxUnsuccessfulLoginsCount
	 */
	public void setMaxUnsuccessfulLoginsCount(int maxUnsuccessfulLoginsCount) {
		this.maxUnsuccessfulLoginsCount = maxUnsuccessfulLoginsCount;
	}
	
	/**
	 *
	 * @param resetPasswordLifetime resetPasswordLifetime
	 */
	public void setResetPasswordLifetime(int resetPasswordLifetime) {
		this.resetPasswordLifetime = resetPasswordLifetime;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.configuration.ConfigurationService#getResetPasswordLifetime()
	 */
	@Override
	public int getResetPasswordLifetime() {
		return this.resetPasswordLifetime;
	}
	
	
}
