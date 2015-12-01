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
 * BaseControllerTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 20.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws;

import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ServletWebArgumentResolverAdapter;

import com.mobileman.kuravis.core.domain.Entity;
import com.mobileman.kuravis.core.domain.NamedEntity;
import com.mobileman.kuravis.core.domain.disease.Disease;
import com.mobileman.kuravis.core.domain.event.NoteEvent;
import com.mobileman.kuravis.core.domain.treatment.Treatment;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.disease.DiseaseService;
import com.mobileman.kuravis.core.services.treatment.TreatmentService;
import com.mobileman.kuravis.core.ws.user.UserController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 * 
 */
public abstract class BaseControllerTest {

	@Autowired
	protected ObjectMapper objectMapper;
	
	@Autowired
	private DiseaseService diseaseService;

	@Autowired
	private TreatmentService treatmentService;

	private Disease disease;
	private Treatment treatment;

	protected MockMvc mockMvc;

	protected MockMvc mockUserMvc;

	private static boolean userLogged = false;
	protected User loggedUser;
	
	@Autowired
	protected UserController userController;
	

	/**
	 * @throws Exception
	 */
	protected void signinAuthorizedUser() throws Exception {
		String userEmail = getUserEmail();
		String password = StringUtils.split(userEmail, "@")[0];
		DBObject body = new BasicDBObject("login", userEmail).append("password", password);
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/user/signin").content(body.toString()).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		MvcResult andReturn = this.mockUserMvc.perform(requestBuilder).andReturn();
		assertNull(andReturn.getResolvedException());
		setId(getLoggedUser(), andReturn);
	}
	
	protected User getLoggedUser() {
		if (loggedUser == null) {
			loggedUser = new User();
			loggedUser.setEmail(getUserEmail());
		}
		return loggedUser;
	}

	protected String getUserEmail() {
		return "james.thomas@test.com";//"peter.novak1@test.com";
	}
	
	/**
	 * @throws Exception
	 */
	protected void signinAdminUser() throws Exception {
		String login = "admin@kuravis.com";
		String password = "password";
		DBObject body = new BasicDBObject("login", login).append("password", password);
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/user/signin").content(body.toString()).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		MvcResult andReturn = this.mockUserMvc.perform(requestBuilder).andReturn();
		assertNull(andReturn.getResolvedException());
	}

	/**
	 * @param controllers
	 * @throws Exception
	 */
	public void setUp(Object... controllers) throws Exception {
		mockUserMvc = MockMvcBuilders.standaloneSetup(userController).setCustomArgumentResolvers(new ServletWebArgumentResolverAdapter(new PageableArgumentResolver())).build();
		mockMvc = MockMvcBuilders.standaloneSetup(controllers).setCustomArgumentResolvers(new ServletWebArgumentResolverAdapter(new PageableArgumentResolver())).build();
		if (!userLogged) {
			signinAuthorizedUser();
			userLogged = true;
		}
	}

	protected MvcResult doPost(String url, Object content) throws Exception {
		RequestBuilder rb = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(toJson(content));
		return this.mockMvc.perform(rb).andReturn();
	}

	protected MvcResult doUpdate(String url, Object content) throws Exception {
		RequestBuilder rb = MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(toJson(content));
		return this.mockMvc.perform(rb).andReturn();
	}

	private String toJson(Object content) throws IOException, JsonGenerationException, JsonMappingException {
		if (content instanceof String) {
			return (String) content;
		} else {
			return objectMapper.writeValueAsString(content);
		}
	}

	protected MvcResult doDelete(String id, String url) throws Exception {
		if (StringUtils.isEmpty(id)) {
			return null;
		}
		url = StringUtils.endsWith(url, "/") ? url : url + "/";
		url += id;
		RequestBuilder rb = MockMvcRequestBuilders.delete(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		MvcResult result = this.mockMvc.perform(rb).andReturn();
		assertNull(result.getResolvedException());
		Assert.assertEquals(HttpStatus.OK, HttpStatus.valueOf(result.getResponse().getStatus()));
		return result;
	}

	public Disease getDisease() {
		if (disease == null) {
			DBObject item = diseaseService.findAll().get(0);
			disease = new Disease();
			setIdAndName(disease, item);
		}
		return disease;
	}

	public Treatment getTreatment() {
		if (treatment == null) {
			DBObject item = treatmentService.findAll().get(0);
			treatment = new Treatment();
			setIdAndName(treatment, item);
		}
		return treatment;
	}

	private void setIdAndName(NamedEntity ne, DBObject item) {
		ne.set_id(EntityUtils.getEntityId(item));
		ne.setName(EntityUtils.getEntityName(item));
	}


	protected void setId(Entity entity, MvcResult andReturn) throws Exception {
		JsonNode node = objectMapper.readTree(andReturn.getResponse().getContentAsString());
		JsonNode idNode = node.get(NoteEvent.ID);
		if (idNode != null) {
			entity.set_id(idNode.asText());
		}
	}

}
