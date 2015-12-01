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
package com.mobileman.kuravis.core.domain.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mobileman.kuravis.core.domain.Pair;
import com.mobileman.kuravis.core.ws.event.EventValidator;

public enum TreatmentCategory {
	PRESCRIPTION_MEDICINE(TreatmentCategoryGroup.TREATMENT_PLAN), 
	NON_PRESCRIPTION_MEDICINE(TreatmentCategoryGroup.TREATMENT_PLAN), 
	HOMEOPATHY(TreatmentCategoryGroup.TREATMENT_PLAN), 
	FOOD_SUPPLEMENTS(TreatmentCategoryGroup.TREATMENT_PLAN), 
	COMPLEMENTARY_MEDICINE(TreatmentCategoryGroup.TREATMENT_PLAN), 
	ALTERNATIVE_MEDICINE(TreatmentCategoryGroup.TREATMENT_PLAN), 
	PHYSIOTHERAPY(TreatmentCategoryGroup.THERAPIE), 
	PSYCHOTHERAPY(TreatmentCategoryGroup.THERAPIE), 
	MEDICAL_DEVICES(TreatmentCategoryGroup.DEVICES), 
	ELECTRONIC_DEVICES(TreatmentCategoryGroup.DEVICES), 
	OPERATION(TreatmentCategoryGroup.OPERATION), 
	OTHERS(TreatmentCategoryGroup.OTHERS);

	private final TreatmentCategoryGroup group;
	private static final Logger LOG = LoggerFactory.getLogger(TreatmentCategory.class);

	private static final Map<TreatmentCategoryGroup, List<Pair<String, Boolean>>> groupAttributesMap = new HashMap<>();
	static {
		//Pair contains attributeName and required
		List<Pair<String, Boolean>> commonAttributes = new ArrayList<>();
		commonAttributes.add(Pair.create(TreatmentEvent.START, true));
		//commonAttributes.add(Pair.create(TreatmentEvent.END, true));
		commonAttributes.add(Pair.create(TreatmentEvent.EVENT_TYPE, true));
		commonAttributes.add(Pair.create(TreatmentEvent.DISEASE, true));
		commonAttributes.add(Pair.create(TreatmentEvent.TREATMENT, true));
		commonAttributes.add(Pair.create(TreatmentEvent.CATEGORY, true));
		
		List<Pair<String, Boolean>> attributes = new ArrayList<>(commonAttributes);
		attributes.add(Pair.create(TreatmentEvent.FREQUENCY, true));
		attributes.add(Pair.create(TreatmentEvent.QUANTITY, true));
		attributes.add(Pair.create(TreatmentEvent.TREATMENT_TYPE, true));
		attributes.add(Pair.create(TreatmentEvent.DOSE, true));
		attributes.add(Pair.create(TreatmentEvent.UNIT, true));
		groupAttributesMap.put(TreatmentCategoryGroup.TREATMENT_PLAN, attributes);
		attributes = new ArrayList<>(commonAttributes);
		attributes.add(Pair.create(TreatmentEvent.FREQUENCY, true));
		attributes.add(Pair.create(TreatmentEvent.QUANTITY, true));
		attributes.add(Pair.create(TreatmentEvent.DURATION, true));
		attributes.add(Pair.create(TreatmentEvent.UNIT, true));
		groupAttributesMap.put(TreatmentCategoryGroup.THERAPIE, attributes);
		attributes = new ArrayList<>(commonAttributes);
		attributes.add(Pair.create(TreatmentEvent.START, true));
		groupAttributesMap.put(TreatmentCategoryGroup.DEVICES, attributes);
		groupAttributesMap.put(TreatmentCategoryGroup.OPERATION, attributes);
		attributes = new ArrayList<>(commonAttributes);
		attributes.add(Pair.create(TreatmentEvent.START, true));
		attributes.add(Pair.create(TreatmentEvent.TEXT, false));
		groupAttributesMap.put(TreatmentCategoryGroup.OTHERS, attributes);
	}

	private TreatmentCategory(TreatmentCategoryGroup group) {
		this.group = group;
	}
	
	public TreatmentCategoryGroup getGroup() {
		return group;
	}
	
	@JsonValue
	public String toJson() {
		return name().toUpperCase();
	}

	@JsonCreator
	public static TreatmentCategory fromJson(String text) {
		try {
			return valueOf(text.toUpperCase());
		} catch (Throwable e) {
			LOG.error("Error while converting '" + text + "' to enum value!", e);
			throw e;
		}
	}
	
	/**
	 * @param group the TreatmentCategoryGroup
	 * @return list of pairs (attributeName, requiredFlag)
	 */
	public static List<Pair<String, Boolean>> getGroupAttributes(TreatmentCategoryGroup group){
		return groupAttributesMap.get(group);
	}

	public enum TreatmentCategoryGroup {
		TREATMENT_PLAN, THERAPIE, DEVICES, OPERATION, OTHERS;
	}
}
