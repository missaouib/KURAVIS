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
 * SystemController.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 13.1.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.ws.system;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.domain.system.VersionInfo;
import com.mobileman.kuravis.core.domain.util.EntityUtils;
import com.mobileman.kuravis.core.services.system.HealthPlatformSystemService;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class SystemController extends AbstractHealtPlatformController {
	
	private static final Logger log = LoggerFactory.getLogger(SystemController.class);
	
	@Autowired
	private HealthPlatformSystemService healtPlatformSystemService;

	/**
	 * @return response message
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	@RequestMapping(value="/" + EntityUtils.SYSTEM, method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public ResponseEntity<String> getVersion() throws JsonGenerationException, JsonMappingException, IOException {
		log.info("getVersion() - start");
		
		VersionInfo versionInfo = this.healtPlatformSystemService.getVersion();
		String versionString = new ObjectMapper().writerWithType(VersionInfo.class).writeValueAsString(versionInfo);
		ResponseEntity<String> response  = new ResponseEntity<String>(versionString, HttpStatus.OK);
		log.info("getVersion() - end: " + response);
		return response;
	}
}
