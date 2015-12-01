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
 * TreatmentTypeServiceImpl.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 19.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.option_list.treatment_type.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.option_list.treatment_type.TreatmentType;
import com.mobileman.kuravis.core.domain.option_list.unit.Unit;
import com.mobileman.kuravis.core.services.event.EventService;
import com.mobileman.kuravis.core.services.option_list.impl.OptionListServiceImpl;
import com.mobileman.kuravis.core.services.option_list.treatment_type.TreatmentTypeService;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service
public class TreatmentTypeServiceImpl extends OptionListServiceImpl<TreatmentType> implements TreatmentTypeService {
	
	@Autowired
	private EventService eventService;

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#getEntityName()
	 */
	@Override
	protected String getEntityName() {
		return TreatmentType.ENTITY_NAME;
	}

	@Override
	@Cacheable(TreatmentType.ENTITY_NAME)
	public List<DBObject> findAll() {
		return super.findAll();
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#findById(java.lang.String)
	 */
	@Override
	@Cacheable(TreatmentType.ENTITY_NAME)
	public DBObject findById(String id) {
		return super.findById(id);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#update(com.mongodb.DBObject)
	 */
	@CacheEvict(value=TreatmentType.ENTITY_NAME, key="#object.get(\"_id\")", allEntries=false)
	@Override
	public DBObject update(DBObject object) {
		return super.update(object);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#update(java.lang.String, com.mongodb.DBObject)
	 */
	@CacheEvict(value=TreatmentType.ENTITY_NAME, key="#id", allEntries=false)
	@Override
	public DBObject update(String id, DBObject object) {
		return super.update(id, object);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#update(java.lang.String, java.lang.String, com.mongodb.DBObject)
	 */
	@CacheEvict(value=TreatmentType.ENTITY_NAME, key="#id", allEntries=false)
	@Override
	public DBObject update(String type, String id, DBObject object) {
		return super.update(type, id, object);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#save(com.mongodb.DBObject)
	 */
	@CacheEvict(value=TreatmentType.ENTITY_NAME, key="#object.get(\"_id\")", allEntries=true)
	@Override
	public DBObject save(DBObject object) {
		return super.save(object);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.impl.AbstractEntityServiceImpl#save(com.mobileman.kuravis.core.domain.Entity)
	 */
	@CacheEvict(value=TreatmentType.ENTITY_NAME, key="#object.get_id()", allEntries=true)
	@Override
	public String save(TreatmentType object) {
		return super.save(object);
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.option_list.impl.OptionListServiceImpl#isRefernced(java.lang.String)
	 */
	@Override
	public boolean isRefernced(String id) {
		Query query = Query.query(Criteria.where(TreatmentEvent.TREATMENT_TYPE + "." + TreatmentEvent.ID).is(id));
		if (eventService.count(query.getQueryObject()) > 0) {
			return true;
		}
		
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
