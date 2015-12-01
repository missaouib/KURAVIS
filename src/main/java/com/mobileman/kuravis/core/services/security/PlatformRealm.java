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
package com.mobileman.kuravis.core.services.security;

import java.util.HashSet;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.util.security.SecurityUtils;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
public class PlatformRealm extends AuthorizingRealm {
	
	@Autowired
	private UserService userService;
	
	/**
	 * 
	 */
	public PlatformRealm() {
		super();
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		final DBObject user = (DBObject) principals.getPrimaryPrincipal();
        final List<String> roles = userService.getUserRoles((String)user.get("accountId"));
        return new SimpleAuthorizationInfo(new HashSet<>(roles));
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		
		UsernamePasswordToken credentials = (UsernamePasswordToken)token;
	    String email = credentials.getUsername();
	    String password = new String(credentials.getPassword());
	    
	    User user = this.userService.findUserByEmail(email);
	    if (user == null) {
			throw new UnknownAccountException("Unknown email: " + email);
		}
	    
	    try {			
			if (SecurityUtils.check(password, user.getAccount().getPassword())) {				
				DBObject dbUser = this.userService.findDBUserByEmail(email);
				
				DBObject account = this.userService.findDBUserAccountByEmail(email);
				dbUser.put("account", account);
			    SimpleAuthenticationInfo authInfo = new SimpleAuthenticationInfo(dbUser, password, getName());
			    return authInfo;
			} else {
				throw new IncorrectCredentialsException();
			}
			
		} catch (Exception e) {
			throw new AuthenticationException(e);
		}
	    
	}

}
