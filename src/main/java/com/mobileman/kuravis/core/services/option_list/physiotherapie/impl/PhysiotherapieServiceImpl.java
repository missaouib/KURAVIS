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
 * PhysiotherapieServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 26.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.option_list.physiotherapie.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.option_list.physiotherapie.Physiotherapie;
import com.mobileman.kuravis.core.domain.option_list.unit.Unit;
import com.mobileman.kuravis.core.services.option_list.impl.OptionListServiceImpl;
import com.mobileman.kuravis.core.services.option_list.physiotherapie.PhysiotherapieService;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class PhysiotherapieServiceImpl extends OptionListServiceImpl<Physiotherapie> implements PhysiotherapieService {

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return Physiotherapie.ENTITY_NAME;
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#findById(java.lang.String)
	 */
	@Override
	@Cacheable(Physiotherapie.ENTITY_NAME)
	public DBObject findById(String id) {
		return super.findById(id);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#update(com.mongodb.DBObject)
	 */
	@CacheEvict(value=Physiotherapie.ENTITY_NAME, key="#object.get(\"_id\")", allEntries=false)
	@Override
	public DBObject update(DBObject object) {
		return super.update(object);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#update(java.lang.String, com.mongodb.DBObject)
	 */
	@CacheEvict(value=Physiotherapie.ENTITY_NAME, key="#id", allEntries=false)
	@Override
	public DBObject update(String id, DBObject object) {
		return super.update(id, object);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#update(java.lang.String, java.lang.String, com.mongodb.DBObject)
	 */
	@CacheEvict(value=Physiotherapie.ENTITY_NAME, key="#id", allEntries=false)
	@Override
	public DBObject update(String type, String id, DBObject object) {
		return super.update(type, id, object);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#save(com.mongodb.DBObject)
	 */
	@CacheEvict(value=Physiotherapie.ENTITY_NAME, key="#object.get(\"_id\")", allEntries=true)
	@Override
	public DBObject save(DBObject object) {
		return super.save(object);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#save(com.mobileman.kuravis.core.domain.Entity)
	 */
	@CacheEvict(value=Physiotherapie.ENTITY_NAME, key="#object.get_id()", allEntries=true)
	@Override
	public String save(Physiotherapie object) {
		return super.save(object);
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.option_list.impl.OptionListServiceImpl#isRefernced(java.lang.String)
	 */
	@Override
	public boolean isRefernced(String id) {
		return false;
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#delete(java.lang.String)
	 */
	@CacheEvict(value=Unit.ENTITY_NAME, key="#id", allEntries=false)
	@Override
	public DBObject delete(String id) {
		return super.delete(id);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.option_list.impl.OptionListServiceImpl#delete(java.lang.String, java.lang.String)
	 */
	@CacheEvict(value=Unit.ENTITY_NAME, key="#id", allEntries=false)
	@Override
	public DBObject delete(String type, String id) {
		return super.delete(type, id);
	}
}
