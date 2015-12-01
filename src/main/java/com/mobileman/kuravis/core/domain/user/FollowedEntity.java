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
package com.mobileman.kuravis.core.domain.user;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mobileman.kuravis.core.domain.Entity;

@JsonSerialize(include = Inclusion.NON_NULL)
@Document(collection = "followedentity")
public class FollowedEntity extends Entity implements FollowedEntityAttributes {

	/**
	 * entity collection name {@link Document#collection()} e.g. disease, treatment, ...
	 */
	private String entityType;
	private String entityId;

	/**
	 * only for GUI. In case of disease text=disease.name, if entityType is treatmentreviewsummary, than text=disease.name treatment.name
	 */
	@Transient
	private String text;

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityName) {
		this.entityType = entityName;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
