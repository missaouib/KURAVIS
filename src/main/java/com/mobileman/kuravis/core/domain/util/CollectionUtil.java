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
 * CollectionUtil.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 27.3.2014
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.domain.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author MobileMan GmbH
 *
 */
public class CollectionUtil {

	/**
	 * @param list
	 * @param splitSize
	 * @return splited collection
	 */
	public static <T> List<List<T>> split(List<T> list, int splitSize){
		if (list == null || list.isEmpty() || splitSize <= 0) {
			return Collections.emptyList();
		}
		
		List<List<T>> result = new ArrayList<List<T>>();
		if (list.size() > splitSize) {
			int fromIndex = 0;
			while (true) {
				int toIndex = fromIndex + splitSize;
				if (toIndex > list.size()) {
					toIndex = list.size();
				}
				result.add(list.subList(fromIndex, toIndex));
				fromIndex += splitSize;
				if (fromIndex >= list.size()) {
					break;
				}				
			}
		} else {
			result.add(list);
		}
		
		return result;
	}
}
