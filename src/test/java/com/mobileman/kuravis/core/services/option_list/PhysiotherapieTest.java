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
 * PhysiotherapieTest.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 26.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.option_list;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import com.mobileman.kuravis.core.domain.option_list.physiotherapie.Physiotherapie;

/**
 * @author MobileMan GmbH
 *
 */
public class PhysiotherapieTest {
	
	private static final List<Physiotherapie> ENTITIES;
	
	static {
		URL url = UnitServiceTest.class.getResource("/data/physiotherapy.json");
		Physiotherapie[] array = {};
		try {
			array = new ObjectMapper().readValue(new File(url.getFile()), Physiotherapie[].class);
			ENTITIES = Arrays.asList(array);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	/**
	 * @return ENTITIES
	 */
	public static List<Physiotherapie> getPsychotherapies() {
		return ENTITIES;
	}

}
