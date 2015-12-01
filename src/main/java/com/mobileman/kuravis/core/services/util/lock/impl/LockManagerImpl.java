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
package com.mobileman.kuravis.core.services.util.lock.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.mobileman.kuravis.core.services.util.lock.LockManager;

/**
 * @author Jozef Bozek &lt;bozek.jozef@zoznam.sk&gt;
 *
 */
@Service
public class LockManagerImpl implements LockManager {
		
	//private Map<String, Lock> lockMap = new HashMap<>();
	
	private Lock lock = new ReentrantLock();

	@Override
	public void lock(String resourceType, String resourceId) {
		
		lock.lock();
	}

	@Override
	public synchronized void unlock(String resourceType, String resourceId) {
		lock.unlock();
	}

}
