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
 * OptionListServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 19.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.option_list.impl;

import com.mobileman.kuravis.core.domain.option_list.OptionList;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl;
import com.mobileman.kuravis.core.services.option_list.OptionListService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 * @param <T> 
 *
 */
public abstract class OptionListServiceImpl<T extends OptionList> extends AbstractEntityServiceImpl<T> implements OptionListService<T> {

	/**
	 * @param id
	 * @return true if option list is refernced
	 */
	public abstract boolean isRefernced(String id);
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#delete(java.lang.String, java.lang.String)
	 */
	@Override
	public DBObject delete(String type, String id) {
		if (isRefernced(id)) {
			throw ErrorUtils.exception("Option list " + getEntityName() + " is still refernced: " + id, ErrorCodes.ENTITY_IS_REFERENCED);
		}
		
		return super.delete(type, id);
	}
}
