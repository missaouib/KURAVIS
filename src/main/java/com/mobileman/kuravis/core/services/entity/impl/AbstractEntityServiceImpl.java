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
package com.mobileman.kuravis.core.services.entity.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import com.mobileman.kuravis.core.domain.Entity;
import com.mobileman.kuravis.core.domain.NamedEntity;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.domain.util.RoleUtils;
import com.mobileman.kuravis.core.domain.util.UserUtils;
import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.exception.HealtPlatformException;
import com.mobileman.kuravis.core.services.MongoDbUtils;
import com.mobileman.kuravis.core.services.entity.EntityService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException.DuplicateKey;
import com.mongodb.WriteResult;

/**
 * @author MobileMan GmbH
 * @param <T> 
 * 
 */
public abstract class AbstractEntityServiceImpl<T extends Entity> implements EntityService<T> {
	@Autowired
	protected ObjectMapper objectMapper;


	@Autowired
	private MongoTemplate mongoTemplate;
	
	protected final Class<T> entityClass;
	
	/**
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public AbstractEntityServiceImpl() {
		super();
		if (ParameterizedType.class.isInstance(getClass().getGenericSuperclass())) {
			java.lang.reflect.Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
			if (java.lang.reflect.TypeVariable.class.isInstance(type)) {
				TypeVariable typeVariable = TypeVariable.class.cast(type);
				this.entityClass = (Class<T>)typeVariable.getBounds()[0];
			} else {
				this.entityClass = (Class<T>)type;
			}
		} else {
			this.entityClass = null;
		}
	}
	
	/**
	 *
	 * @return entityClass
	 */
	public Class<T> getEntityClass() {
		return this.entityClass;
	}

	/**
	 * @return mongo template
	 */
	public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}

	protected String getEntityName() {
		return EntityUtils.getDocumentCollectionName(entityClass);
	}

	protected DBCollection getCollection() {
		return this.mongoTemplate.getDb().getCollection(getEntityName());
	}

	protected DBCollection getCollection(String collection) {
		return this.mongoTemplate.getDb().getCollection(collection);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.entity.EntityService#count(DBObject)
	 */
	@Override
	public long count(DBObject query) {
		return getCollection().count(query);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.entity.EntityService#findAll(org.springframework.data.domain.Pageable)
	 */
	@Override
	public List<DBObject> findAll(Pageable page) {
		return findAll(getEntityName(), page);
	}

	@Override
	public List<DBObject> findAll(String entityName, Pageable page) {
		return findAll(entityName, (String) null, page);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.entity.EntityService#findAllByQuery(com.mongodb.DBObject)
	 */
	@Override
	public List<DBObject> findAllByQuery(DBObject query) {
		return findAllByQuery(query, new PageRequest(0, Integer.MAX_VALUE));
	}

	@Override
	public List<DBObject> findAll(String entityName, String projection, Pageable page) {
		if (page == null) {
			page = new PageRequest(0, Integer.MAX_VALUE);
		}

		DBCursor cursor = null;
		if (StringUtils.isEmpty(projection)) {
			cursor = getCollection(entityName).find(new BasicDBObject());
		} else {
			String[] properties = projection.split(",");
			BasicDBObjectBuilder projectionBuilder = BasicDBObjectBuilder.start();
			boolean idWanted = false;
			for (String property : properties) {
				property = property.trim();
				if (!StringUtils.isEmpty(property)) {
					if (property.equals(EntityUtils.ID)) {
						idWanted = true;
					}
					
					projectionBuilder.add(property.trim(), true);
				}
			}
			
			if (idWanted == false) {
				projectionBuilder.append("_id", false);
			}

			cursor = getCollection(entityName).find(new BasicDBObject(), projectionBuilder.get()).sort(new BasicDBObject(projection, 1));
		}

		if (page.getSort() != null) {
			cursor = cursor.sort(createSort(page));
		} else if (projection != null) {
			cursor = cursor.sort(new BasicDBObject(projection, 1));
		}

		cursor = cursor.skip(page.getOffset()).limit(page.getPageSize());

		List<DBObject> result = cursor.toArray();
		return result;
	}

	@Override
	public List<DBObject> findAll(Pageable page, String projection) {
		return findAll(getEntityName(), projection, page);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public List<DBObject> findAllByQuery(DBObject query, String projection, Pageable page) {
		DBCursor cursor = null;
		if (StringUtils.isEmpty(projection)) {
			cursor = getCollection().find(query);
		} else {
			cursor = getCollection().find(query, new BasicDBObject(projection, true).append("_id", false));
		}

		if (page.getSort() != null) {
			cursor = cursor.sort(createSort(page));
		} else if (projection != null) {
			cursor = cursor.sort(new BasicDBObject(projection, 1));
		}

		cursor = cursor.skip(page.getOffset()).limit(page.getPageSize());

		List<DBObject> result = cursor.toArray();
		return result;
	}

	/**
	 * use {@link UserUtils#getLoggedUser()}
	 * @return UserUtils.getLoggedUser()
	 */
	@Deprecated 
	protected DBObject getLoggedUser() {
		return UserUtils.getLoggedUser();
	}

	/**
	 * @param page
	 * @return sort DBObject
	 */
	protected DBObject createSort(Pageable page) {
		if (page.getSort() == null) {
			return null;
		}

		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		Iterator<Order> orderIter = page.getSort().iterator();
		while (orderIter.hasNext()) {
			Order order = orderIter.next();
			builder.add(order.getProperty(), order.getDirection().equals(Direction.ASC) ? 1 : -1);
		}

		return builder.get();
	}

	@Override
	public List<DBObject> findAll() {
		return findAll((String) null);
	}

	@Override
	public List<DBObject> findAll(String projection) {
		return findAll(new PageRequest(0, Integer.MAX_VALUE), projection);
	}

	@Override
	public List<DBObject> findAllByQuery(String entityName, DBObject query) {
		if (query.containsField(EntityUtils.NAME)) {
			String name = (String) query.get(EntityUtils.NAME);
			query.put(EntityUtils.NAME, new BasicDBObject("$regex", "^" + name.toLowerCase() + "$").append("$options", "i"));
		}

		DBCursor cursor = getCollection(entityName).find(query);
		List<DBObject> result = cursor.toArray();
		return result;
	}
	
	/**
	 * @param entityName
	 * @param query
	 * @return list of all documents by query
	 */
	@Override
	public List<DBObject> findAllByQuery(String entityName, DBObject query, DBObject projection) {
		if (query.containsField(EntityUtils.NAME)) {
			String name = (String) query.get(EntityUtils.NAME);
			query.put(EntityUtils.NAME, new BasicDBObject("$regex", "^" + name.toLowerCase() + "$").append("$options", "i"));
		}
		
		final DBCursor cursor;
		if (projection != null && projection.keySet().size() > 0) {
			cursor = getCollection(entityName).find(query, projection);
		} else {
			cursor = getCollection(entityName).find(query);
		}

		
		List<DBObject> result = cursor.toArray();
		return result;
	}

	@Override
	public List<DBObject> findAllByQuery(String entityName, DBObject query, Pageable page) {

		if (query.containsField(EntityUtils.NAME)) {
			String name = (String) query.get(EntityUtils.NAME);
			query.put(EntityUtils.NAME, new BasicDBObject("$regex", "^" + name.toLowerCase() + "$").append("$options", "i"));
		}

		DBCursor cursor = null;
		if (page.getSort() != null) {
			cursor = getCollection(entityName).find(query).sort(createSort(page)).skip(page.getOffset()).limit(page.getPageSize());
		} else {
			cursor = getCollection(entityName).find(query).skip(page.getOffset()).limit(page.getPageSize());
		}

		List<DBObject> result = cursor.toArray();
		return result;
	}

	@Override
	public List<DBObject> findAllByQuery(DBObject query, Pageable page) {
		return findAllByQuery(getEntityName(), query, page);
	}

	@Override
	public DBObject findById(String entityName, String id) {
		DBObject object = getCollection(entityName).findOne(id);
		return object;
	}

	@Override
	public DBObject findById(String id) {
		return findById(getEntityName(), id);
	}

	@Override
	public List<String> getNewIds(int count) {
		List<String> result = new ArrayList<String>(count);
		for (int i = 0; i < count; i++) {
			result.add(UUID.randomUUID().toString());
		}

		return result;
	}
	
	/**
	 * @param type
	 * @param object
	 * @return save result
	 */
	protected DBObject save(String type, DBObject object) {
		if (!object.containsField(EntityUtils.ID)) {
			object.put(EntityUtils.ID, EntityUtils.newId());
		}
		if (!object.containsField(EntityUtils.CREATED_ON)) {
			object.put(EntityUtils.CREATED_ON, new Date());
		}
		if (!object.containsField(EntityUtils.MODIFIED_ON)) {
			object.put(EntityUtils.MODIFIED_ON, new Date());
		}

		if (!object.containsField(EntityUtils.ATTR_USER)) {
			DBObject currentUser = getLoggedUser();
			if (currentUser != null) {
				object.put(EntityUtils.ATTR_USER, EntityUtils.createBaseUser(currentUser));
			}
		}

		WriteResult result = getCollection(type).save(object);
		if (result.getError() != null) {
			return ErrorUtils.error(result.getError());
		}

		DBObject success = ErrorUtils.success(HttpStatus.CREATED);
		success.put(EntityUtils.ID, object.get(EntityUtils.ID));
		return success;
	}

	@Override
	public DBObject save(DBObject object) {
		return save(getEntityName(), object);
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.EntityService#save(com.mobileman.kuravis.core.domain.Entity)
	 */
	@Override
	public String save(T object) {
		EntityUtils.setBaseProperties(object);
		this.mongoTemplate.save(object);
		return object.get_id();
	}
	
	@Override
	public String create(T object) {
		EntityUtils.setBaseProperties(object);
		mongoTemplate.insert(object);
		return object.get_id();
	}
	
	@Override
	public T getById(Object id) {
		return getById(id, getEntityClass());
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.entity.EntityService#getById(java.lang.Object, java.lang.Class)
	 */
	@Override
	public T getById(Object id, Class<? extends T> entityType) {
		return (T) mongoTemplate.findById(id, entityType);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.entity.EntityService#saveWithUserData(java.lang.String, com.mongodb.DBObject)
	 */
	@Override
	public DBObject saveWithUserData(String type, DBObject object) {
		object.put(EntityUtils.ID, EntityUtils.newId());
		object.put(EntityUtils.CREATED_ON, new Date());
		object.put(EntityUtils.MODIFIED_ON, new Date());
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser == null || !currentUser.isAuthenticated() || currentUser.getPrincipal() == null) {
			return ErrorUtils.error("Not authenticated: currentUser=" + currentUser, ErrorCodes.USER_NOT_AUTHENTICATED);
		}

		DBObject user = (DBObject) currentUser.getPrincipal();
		object.put(EntityUtils.ATTR_USER, EntityUtils.createBaseUser(user));
		WriteResult result = getCollection(type).save(object);
		if (result.getError() != null) {
			return ErrorUtils.error(result.getError());
		}

		return ErrorUtils.success(HttpStatus.CREATED);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public DBObject update(String type, String id, DBObject object) {
		DBObject currentUser = getLoggedUser();

		if (object.containsField("$set") && object.get("$set") != null) {
			if (Map.class.isInstance(object.get("$set"))) {
				Map<String, Object> setCommand = (Map<String, Object>) object.get("$set");
				setCommand.put(EntityUtils.MODIFIED_ON, new Date());
				setCommand.put(EntityUtils.ATTR_USER, EntityUtils.createBaseUser(currentUser));
			} else {
				DBObject setCommand = (DBObject) object.get("$set");
				setCommand.put(EntityUtils.MODIFIED_ON, new Date());
				setCommand.put(EntityUtils.ATTR_USER, EntityUtils.createBaseUser(currentUser));
			}

		} else {
			object.put(EntityUtils.MODIFIED_ON, new Date());
			object.put(EntityUtils.ATTR_USER, EntityUtils.createBaseUser(currentUser));
		}

		WriteResult result = getCollection(type).update(new BasicDBObject("_id", id), object);
		if (result.getError() != null) {
			return ErrorUtils.error(result.getError());
		}

		return ErrorUtils.success();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mobileman.kuravis.core.services.entity.EntityService#update(com.mongodb.DBObject)
	 */
	@Override
	public DBObject update(DBObject object) {
		if (StringUtils.isEmpty(object.get(EntityUtils.ID))) {
			throw ErrorUtils.exception("ID is missing", ErrorCodes.INCORRECT_PARAMETER);
		}

		return update((String) object.get(EntityUtils.ID), object);
	}

	@Override
	public DBObject update(String id, DBObject object) {
		return update(getEntityName(), id, object);
	}
	
	@Override
	public WriteResult updateAll(DBObject filter, DBObject command) {
		return getCollection().update(filter, command, false, true);
	}

	/**
	 * @param id
	 * @return error message in case of error
	 */
	@Override
	public DBObject delete(String id) {
		return delete(getEntityName(), id);
	}

	@Override
	public DBObject delete(String type, String id) {
		WriteResult result = getCollection(type).remove(new BasicDBObject("_id", id));
		if (result.getError() != null) {
			return ErrorUtils.error(result.getError());
		}

		return ErrorUtils.success();
	}

	protected DBObject findOrInsertByProperty(String entityName, String entityProperty, DBObject entity) {
		Object propertyValue = entity.get(entityProperty);
		if (propertyValue == null) {
			throw new HealtPlatformException(ErrorUtils.error("Entity '" + entityName + "' property '" + entityProperty + "' value is missing",
					ErrorCodes.INCORRECT_PARAMETER));
		}
		DBObject foundEntity = findOneByProperty(entityName, entityProperty, propertyValue);
		if (foundEntity == null) {
			try {
				foundEntity = new BasicDBObject(entityProperty, propertyValue);
				DBObject result = save(entityName, foundEntity);
				if (ErrorUtils.isError(result)) {
					throw new HealtPlatformException(result);
				}
				entity.put(EntityUtils.ID, foundEntity.get(EntityUtils.ID));
			} catch (DuplicateKey e) {
				foundEntity = findOneByProperty(entityName, entityProperty, propertyValue);
			}
		}
		return foundEntity;
	}

	@Override
	public DBObject findOrInsertByProperty(String entityProperty, DBObject entity) {
		return findOrInsertByProperty(getEntityName(), entityProperty, entity);
	}

	/**
	 * @param entityName
	 * @param propertyName
	 * @param propertyVallue
	 * @return collection of documents by property value
	 */
	public List<DBObject> findByProperty(String entityName, String propertyName, Object propertyVallue) {
		DBCursor cursor = getCollection(entityName).find(new BasicDBObject(propertyName, propertyVallue));
		List<DBObject> result = new ArrayList<DBObject>();
		while (cursor.hasNext()) {
			result.add(cursor.next());
		}

		return result;
	}

	@Override
	public DBObject findByProperty(String propertyName, Object propertyValue) {
		return findOneByProperty(getEntityName(), propertyName, propertyValue);
	}

	/**
	 * @param entityName
	 * @param propertyName
	 * @param propertyValue
	 * @return document by property value
	 */
	public DBObject findOneByProperty(String entityName, String propertyName, Object propertyValue) {
		final Query query;
		if (String.class.isInstance(propertyValue)) {
			propertyValue = String.class.cast(propertyValue).toLowerCase();
			query = MongoDbUtils.getQueryByStringCaseInsensitive(propertyName, (String) propertyValue);
		} else {
			query = new Query(Criteria.where(propertyName).is(propertyValue));
		}

		DBObject object = getCollection(entityName).findOne(query.getQueryObject());
		return object;
	}
	
	/**
	 * @param propertyName
	 * @param propertyValue
	 * @return document by property value
	 */
	public T findEntityByProperty(String propertyName, Object propertyValue) {
		final Query query;
		if (String.class.isInstance(propertyValue)) {
			propertyValue = String.class.cast(propertyValue).toLowerCase();
			query = MongoDbUtils.getQueryByStringCaseInsensitive(propertyName, (String) propertyValue);
		} else {
			query = new Query(Criteria.where(propertyName).is(propertyValue));
		}

		T obj = getMongoTemplate().findOne(query, getEntityClass());
		return obj;
	}
	
	@Override
	public DBObject create(String type, DBObject object) {
		String name = EntityUtils.getEntityName(object);
		if (existsByName(type, name)) {
			//TODO localize = error.create.alreadyExistsByName, args 
			throw new HealtPlatformException("'" + name + "' already exists!", ErrorCodes.ENTITY_BYNAME_EXISTS);
		}
		return save(type, object);
	}
	
	@Override
	public DBObject createOrFindByName(Map<String, Object> entity) {
		if (entity == null) {
			throw ErrorUtils.exception("Entity parameter '" + getEntityName() + "' is missing", ErrorCodes.INCORRECT_PARAMETER);
		}
		final String name = EntityUtils.NAME;
		if (entity.get(EntityUtils.ID) != null) {
			return new BasicDBObject(EntityUtils.ID, entity.get(EntityUtils.ID)).append(name, entity.get(name));
		}
		DBObject obj = new BasicDBObject(EntityUtils.ID, entity.get(EntityUtils.ID));
		obj.put(name, entity.get(name));
		DBObject result = findOrInsertByProperty(getEntityName(), name, obj);
		return new BasicDBObject(EntityUtils.ID, result.get(EntityUtils.ID)).append(name, result.get(name));
	}
	
	@SuppressWarnings("unchecked")
	public T createOrFindByName(T entity) {
		if (StringUtils.hasText(entity.get_id())) {
			return entity;
		}
		DBObject obj = new BasicDBObject(Entity.ID, entity.get_id());
		obj.put(Entity.NAME, ((NamedEntity)entity).getName());
		DBObject result = findOrInsertByProperty(getEntityName(), Entity.NAME, obj);
		obj = new BasicDBObject(Entity.ID, result.get(Entity.ID)).append(Entity.NAME, result.get(Entity.NAME));
		try {
			entity = (T) objectMapper.readValue(obj.toString(), entity.getClass());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entity;
	}
	
	/**
	 * @param type
	 * @param name
	 * @return return true if entity with given name already exist. Query is case insensitive
	 */
	protected boolean existsByName(String type, String name) {
		if (org.apache.commons.lang.StringUtils.isEmpty(name)) {
			return false;
		}
		return getCollection(type).count(MongoDbUtils.getQueryByStringCaseInsensitive(EntityUtils.NAME, name).getQueryObject()) > 0;
	}

	protected DBObject validateName(String type, String id, DBObject entity) {
		requiresAdminRole();

		List<DBObject> items = findAllByQuery(new BasicDBObject(EntityUtils.ID, id), EntityUtils.NAME, new PageRequest(0, 1));
		if (items.isEmpty()) {
			throw new HealtPlatformException(getEntityName() + " with id: " + id + " does not exists", ErrorCodes.ENTITY_NOT_FOUND);
		}
		DBObject dbEntity = items.get(0);
		String dbName = EntityUtils.getEntityName(dbEntity);
		String name = EntityUtils.getEntityName(entity);
		if (!dbName.equalsIgnoreCase(name) && existsByName(type, name)) {
			throw new HealtPlatformException("'" + name + "' already exists!", ErrorCodes.ENTITY_BYNAME_EXISTS);
		}
		return dbEntity;
	}

	protected void requiresAdminRole() {
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser == null || !currentUser.isAuthenticated()) {
			throw new HealtPlatformException("Not authenticated user: " + currentUser, ErrorCodes.USER_NOT_AUTHENTICATED);
		}
		DBObject user = (DBObject) currentUser.getPrincipal();
		if (!RoleUtils.isAdminAccount((DBObject) user.get("account"))) {
			throw new HealtPlatformException("Not an admin account: " + user, ErrorCodes.UNAUTHORIZED);
		}
	}
	
}
