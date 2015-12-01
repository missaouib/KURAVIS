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

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mobileman.kuravis.core.domain.Entity;

/**
 * @author MobileMan GmbH
 *
 */
@JsonSerialize(include=Inclusion.NON_NULL)
@Document(collection="user")
@XmlRootElement
public class User extends Entity implements UserAttributes, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @author MobileMan GmbH
	 *
	 */
	public static enum DiseaseState {
		/**
		 * 
		 */
		CURED("cured"),
		
		/**
		 * 
		 */
		IN_THERAPY("in_therapy");
		
		private final String value;
		
		DiseaseState(String val) {
			this.value = val;
		}
		
		/**
		 * @return value
		 */
		public String getValue() {
			return this.value;
		}
	}
	
	@Indexed(unique = false)
	private String name;
	
	@Indexed(unique = false)
	private String email;
	
	private UserAccount account;
	
	@Transient
	private List<FollowedEntity> followedEntities;

	/**
	 * @return name
	 */
	@XmlAttribute
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return user account
	 */
	public UserAccount getAccount() {
		return account;
	}

	/**
	 * @param account
	 */
	public void setAccount(UserAccount account) {
		this.account = account;
	}

	/**
	 *
	 * @return email
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 *
	 * @param email email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 *
	 * @return followedEntities
	 */
	public List<FollowedEntity> getFollowedEntities() {
		return this.followedEntities;
	}

	/**
	 *
	 * @param followedEntities followedEntities
	 */
	public void setFollowedEntities(List<FollowedEntity> followedEntities) {
		this.followedEntities = followedEntities;
	}
	
		
}
