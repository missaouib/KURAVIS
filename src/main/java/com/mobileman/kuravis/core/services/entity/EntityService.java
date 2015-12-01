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
package com.mobileman.kuravis.core.services.entity;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.mobileman.kuravis.core.domain.Entity;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

/**
 * @author MobileMan GmbH
 * @param <T>
 * 
 */
public interface EntityService<T extends Entity> {

	/**
	 * @param query
	 * @return count of docments by query
	 */
	long count(DBObject query);

	/**
	 * @param entityProperty
	 * @param entity
	 * @return error message in case of error
	 */
	DBObject findOrInsertByProperty(String entityProperty, DBObject entity);

	/**
	 * @return list of all documents of specified type
	 */
	List<DBObject> findAll();

	/**
	 * @param projection
	 * @return list of all documents properties of specified type
	 */
	List<DBObject> findAll(String projection);

	/**
	 * @param page
	 * @return paginated list of all documents of specified type
	 */
	List<DBObject> findAll(Pageable page);

	/**
	 * @param entityName
	 * @param page
	 * @return paginated list of all documents of specified type
	 */
	List<DBObject> findAll(String entityName, Pageable page);

	/**
	 * @param entityName
	 * @param query
	 * @return list of all documents by query
	 */
	List<DBObject> findAllByQuery(String entityName, DBObject query);
	
	/**
	 * @param entityName
	 * @param query
	 * @param projection 
	 * @return list of all documents by query
	 */
	List<DBObject> findAllByQuery(String entityName, DBObject query, DBObject projection);

	/**
	 * @param entityName
	 * @param projection
	 * @param page
	 * @return paginated list of all partial documents (projected attributes) fo specified type
	 */
	List<DBObject> findAll(String entityName, String projection, Pageable page);

	/**
	 * @param projection
	 * @param page
	 * @return paginated list of all partial documents (projected attributes) fo specified type
	 */
	List<DBObject> findAll(Pageable page, String projection);

	/**
	 * @param projection
	 * @param query
	 * @param page
	 * @return paginated list of all partial documents (projected attributes) by query
	 */
	List<DBObject> findAllByQuery(DBObject query, String projection, Pageable page);

	/**
	 * @param entityName
	 * @param query
	 * @param page
	 * @return paginated list of documents of specified type - by query
	 */
	List<DBObject> findAllByQuery(String entityName, DBObject query, Pageable page);

	/**
	 * @param query
	 * @return list of all documents of specified type - by query
	 */
	List<DBObject> findAllByQuery(DBObject query);

	/**
	 * @param query
	 * @param page
	 * @return paginated list of documents of specified type - by query
	 */
	List<DBObject> findAllByQuery(DBObject query, Pageable page);

	/**
	 * @param id
	 * @return entity by given id
	 */
	DBObject findById(String id);
	
	T getById(Object id);
	

	/**
	 * @param id
	 * @param entityType
	 * @return T
	 */
	T getById(Object id, Class<? extends T>entityType);

	/**
	 * @param entityName
	 * @param id
	 * @return entity of given type by given id
	 */
	DBObject findById(String entityName, String id);

	/**
	 * @param propertyName
	 * @param propertyValue
	 * @return entity by given property value
	 */
	DBObject findByProperty(String propertyName, Object propertyValue);
	
	/**
	 * @param propertyName
	 * @param propertyValue
	 * @return entity by given property value
	 */
	T findEntityByProperty(String propertyName, Object propertyValue);


	/**
	 * @param count
	 * @return list of newly generated uids with specified size
	 */
	List<String> getNewIds(int count);

	/**
	 * @param object
	 * @return error message in case of error
	 */
	DBObject save(DBObject object);

	/**
	 * 
	 * @param object
	 * @return object id
	 */
	String save(T object);

	/**
	 * 
	 * @param object
	 * @return object id
	 */
	String create(T object);

	/**
	 * @param type
	 * @param object
	 * @return error message in case of error
	 */
	DBObject saveWithUserData(String type, DBObject object);

	/**
	 * @param object
	 * @return error message in case of error
	 */
	DBObject update(DBObject object);

	/**
	 * @param type
	 * @param id
	 * @param object
	 * @return error message in case of error
	 */
	DBObject update(String type, String id, DBObject object);

	/**
	 * @param id
	 * @param object
	 * @return error message in case of error
	 */
	DBObject update(String id, DBObject object);

	WriteResult updateAll(DBObject filter, DBObject command);

	/**
	 * @param id
	 * @return result data
	 */
	DBObject delete(String id);

	/**
	 * @param type
	 * @param id
	 * @return result data
	 */
	DBObject delete(String type, String id);

	/**
	 * Creates new object if doesn't exist by name
	 * 
	 * @param type
	 * @param object
	 * @return
	 */
	DBObject create(String type, DBObject object);

	DBObject createOrFindByName(Map<String, Object> entity);

	T createOrFindByName(T entity);
}
