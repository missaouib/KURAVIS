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
 * StringToEnumConverter.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 24.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.util.mongo.converter;

import org.springframework.core.convert.converter.Converter;

/**
 * @author MobileMan GmbH
 * @param <T> 
 *
 */
public final class StringToEnumConverter<T extends Enum> implements Converter<String, T> {

    private Class<T> enumType;

    /**
	 * 
	 */
	public StringToEnumConverter() {
		super();
	}

	/**
     * @param enumType
     */
    public StringToEnumConverter(Class<T> enumType) {
        this.enumType = enumType;
    }

    @SuppressWarnings("unchecked")
	public T convert(String source) {
        return (T) Enum.valueOf(this.enumType, source.trim().toUpperCase());
    }
}
