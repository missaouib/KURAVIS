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
package com.mobileman.kuravis.core.ws.user;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobileman.kuravis.core.exception.ErrorCodes;
import com.mobileman.kuravis.core.services.user.UserService;
import com.mobileman.kuravis.core.util.ErrorUtils;
import com.mobileman.kuravis.core.util.JsonUtil;
import com.mobileman.kuravis.core.ws.AbstractHealtPlatformController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Controller
public class UserController extends AbstractHealtPlatformController {
	
	@Autowired
	private UserService userService;

	/**
	 * @param id
	 * @return user by id
	 */
	@RequestMapping(value="/user/{id}", method = RequestMethod.GET, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> getById(@PathVariable String id) {
		DBObject result = this.userService.findById(id);
		HttpStatus status = HttpStatus.OK;
		if (result == null) {
			status = HttpStatus.NOT_FOUND;
		}
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, status);
		return response;
	}
	
	/**
	 * process sign-in
	 * @param body sign-in data
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/signin", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> signin(@RequestBody BasicDBObject body) {
		if (log.isDebugEnabled()) {
			log.debug("signin(" + body + ") - start");
		}
		String userName = body.getString("login");
		String password = body.getString("password");
		String captcha_answer = body.getString("captcha_answer");
		boolean rememberMe = body.getBoolean("rememberMe");
		DBObject result = this.userService.signin(userName, password, captcha_answer, rememberMe);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, new HttpHeaders(), ErrorUtils.getStatus(result));
		if (log.isDebugEnabled()) {
			log.debug("return " + response);
		}
		return response;
	}
	
	/**
	 * process sign-out
	 * @param body sign-out data
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/signout", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> signout(@RequestBody BasicDBObject body) {
		log.info("signout(" + body + ") - start");
		
		String error = this.userService.signout();
		HttpStatus status = HttpStatus.OK;
		if (!StringUtils.isEmpty(error)) {
			status = HttpStatus.NOT_FOUND;
		}
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(new BasicDBObject("result", error), new HttpHeaders(), status);
		log.info("signout(...) - end: " + response);
		return response;
	}
	
	/**
	 * process sign-out
	 * @param body sign-out data
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/signup", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> signup(@RequestBody BasicDBObject body) {
		log.debug("signup(" + body + ")");
		DBObject result = this.userService.signup(body);
		ResponseEntity<DBObject> response = new ResponseEntity<DBObject>(result, new HttpHeaders(), ErrorUtils.getStatus(result));
		log.debug("return " + response);
		return response;
	}
	
	/**
	 * process sign-out
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/checksession", method = RequestMethod.GET, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> checkSession() {
		DBObject result = this.userService.checkSession();
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.debug("checkSession() return: " + response);
		return response;
	}
	
	/**
	 * process sign-out
	 * @param activationUuid
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/activate/{activationUuid}", method = RequestMethod.PUT, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> activateAccount(@PathVariable String activationUuid) {
		log.info("activateAccount(" + activationUuid + ") - start");
		
		DBObject result = this.userService.activateAccount(activationUuid);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("activateAccount(...) - end: " + response);
		return response;
	}
	
	/**
	 * User existence check
	 * @param email
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/exists/{email}", method = RequestMethod.GET, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> userExists(@PathVariable String email) {
		log.info("userExists(" + email + ") - start");
		
		try {
			email = URLDecoder.decode(email, "UTF-8");
		} catch (UnsupportedEncodingException e) {}
		
		boolean exists = this.userService.userExistsWithEmail(email);
		DBObject result = ErrorUtils.success();
		result.put("exists", exists);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, HttpStatus.OK);
		log.info("userExists(...) - end: " + response);
		return response;
	}
	
	/**
	 * Process delete of user
	 * @param userId id of user to delete
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/account/{userId}", method = RequestMethod.DELETE, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> deleteUserAccount(@PathVariable String userId) {
		log.info("deleteAccount(" + userId + ") - start");
		
		DBObject result = this.userService.deleteUserAccount(userId);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, new HttpHeaders(), ErrorUtils.getStatus(result));
		log.info("deleteAccount(" + userId + ") - end: " + response);
		return response;
	}
	
	/**
	 * Process delete of user
	 * @param userId id of user to delete
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/{userId}", method = RequestMethod.DELETE, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> deleteUser(@PathVariable String userId) {
		log.info("deleteAccount(" + userId + ") - start");
		
		DBObject result = this.userService.delete(userId);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, new HttpHeaders(), ErrorUtils.getStatus(result));
		log.info("deleteAccount(" + userId + ") - end: " + response);
		return response;
	}
	
	/**
	 * process sign-out
	 * @param userId id of user to delete
	 * @param body 
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/{userId}", method = RequestMethod.PUT, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> updateUser(@PathVariable String userId, @RequestBody BasicDBObject body) {
		log.debug("updateUser(" + userId + ")");
		
		DBObject result = this.userService.updateUser(userId, body);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, new HttpHeaders(), ErrorUtils.getStatus(result));
		log.debug("" + response);
		return response;
	}
	
	/**
	 * UC17 Forgot password - reset
	 * 
	 * @param body
	 * @return result ok if password was successfuly recreated, error
	 */
	@RequestMapping(value="/user/resetpassword", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> resetPassword(@RequestBody BasicDBObject body) {
		log.info("resetPassword(" + body + ") - start");
		
		DBObject result = null;		
		String email = body.getString("email");
		if (StringUtils.isEmpty(email)) {
			result = ErrorUtils.error("Email unknown", ErrorCodes.UNKNOWN_EMAIL);
		} else {
			result = userService.resetCredentials(email);
		}
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, new HttpHeaders(), ErrorUtils.getStatus(result));
		log.info("resetPassword(...) - end: " + response);
		return response;
	}
	
	/**
	 * UC18 Change Password
	 * 
	 * @param body
	 * @return result ok if password was successfuly recreated, error
	 */
	@RequestMapping(value="/user/changepassword", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> changePassword(@RequestBody BasicDBObject body) {
		log.info("changePassword(" + body + ") - start");
		
		DBObject result = null;		
		String password = body.getString("password");
		String password2 = body.getString("password2");
		if (StringUtils.isEmpty(password) || StringUtils.isEmpty(password2)) {
			result = ErrorUtils.error("Incorrect parameter", ErrorCodes.INCORRECT_PARAMETER);
		} else {
			result = userService.changePassword(password, password2);
		}
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, new HttpHeaders(), ErrorUtils.getStatus(result));
		log.info("changePassword(...) - end: " + response);
		return response;
	}
	
	/**
	 * UC1020 Email - update email
	 * 
	 * @param body
	 * @return result ok if password was successfuly recreated, error
	 */
	@RequestMapping(value="/user/changeemail", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> updateEmail(@RequestBody BasicDBObject body) {
		log.info("updateEmail(" + body + ") - start");
		
		DBObject result = null;		
		String email = body.getString("email");
		if (StringUtils.isEmpty(email) ) {
			result = ErrorUtils.error("Incorrect parameter", ErrorCodes.INCORRECT_PARAMETER);
		} else {
			result = userService.updateEmail(email);
		}
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("updateEmail(...) - end: " + response);
		return response;
	}
	
	/**
	 * UC1100 Privacy Settings - update Privacy Settings
	 * 
	 * @param body
	 * @return result ok if password was successfuly recreated, error
	 */
	@RequestMapping(value="/user/privacysettings", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	@SuppressWarnings("unchecked")
	public ResponseEntity<DBObject> updatePrivacySettings(@RequestBody BasicDBObject body) {
		log.info("updatePrivacySettings(" + body + ") - start");
		
		userService.updatePrivacySettings(body.toMap());
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(ErrorUtils.success(), HttpStatus.OK);
		log.info("updatePrivacySettings(...) - end: " + response);
		return response;
	}
	
	/**
	 * UC18 Change Password
	 * 
	 * @param body
	 * @return result ok if password was successfuly recreated, error
	 */
	@RequestMapping(value="/user/changeresetedpassword", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> changeResetedPassword(@RequestBody BasicDBObject body) {
		log.info("changeResetedPassword(" + body + ") - start");
		
		String resetPasswordUuid = body.getString("resetPasswordUuid");
		String password = body.getString("password");
		DBObject result = userService.changeResetedPassword(resetPasswordUuid, password);
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("changeResetedPassword(...) - end: " + response);
		return response;
	}
	
	/**
	 * Updates users diseases
	 * @param data 
	 * @return error message in case of error
	 */
	@RequestMapping(value="/user/disease", method = RequestMethod.PUT, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	@RequiresAuthentication
	public ResponseEntity<DBObject> updateDiseases(@RequestBody ArrayList<BasicDBObject> data) {
		log.info("updateDiseases(" + data +  ") - start");
		
		DBObject result = this.userService.updateDiseases(data);
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(result, ErrorUtils.getStatus(result));
		log.info("updateDiseases(" + data +  ") - end: " + response);
		return response;
	}
	
	/**
	 * @param data
	 * @param page 
	 * @return users by disease
	 */
	@RequestMapping(value="/user/disease", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<List<DBObject>> findAllByDisease(@RequestBody BasicDBObject data, @PageableDefaults(pageNumber = 0, value = Integer.MAX_VALUE) Pageable page) {
		log.info("findAllByDisease(" + data +  ", " + page + ") - start");
		
		List<DBObject> result = this.userService.findUsersByDiseaseAndTreatment(data, page);
		ResponseEntity<List<DBObject>> response  = new ResponseEntity<List<DBObject>>(result, HttpStatus.OK);
		log.info("findAllByDisease(" + data +  ") - end: " + response);
		
		return response;
	}
	
	/**
	 * process sign-out
	 * @param email id of user to delete
	 * @return error message in case of error
	 */
	@RequestMapping(value="/captcha/{email}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<byte[]> generateCaptcha(@PathVariable String email) {
		log.info("generateCaptcha(" + email + ") - start");
		
		try {
			email = URLDecoder.decode(email, "UTF-8");
		} catch (UnsupportedEncodingException e) {}
		
		byte[] image = this.userService.generateCaptcha(email);
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.IMAGE_PNG);
		
		ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(image, responseHeaders, HttpStatus.OK);

		log.info("generateCaptcha(" + email + ") - end: " + response);
		return response;
	}
	
	/**
	 * Feedback from the user on the page
	 * 
	 * @param body
	 * @return result ok if email was sent
	 */
	@RequestMapping(value="/user/feedback", method = RequestMethod.POST, produces = {JsonUtil.MEDIA_TYPE_APPLICATION_JSON})
	@ResponseBody
	public ResponseEntity<DBObject> userFeedback(@RequestBody BasicDBObject body) {
		log.info("resetPassword(" + body + ") - start");
		
		String result = null;		
		String userComment = body.getString("comment");
		String email = body.getString("email");
		result = userService.userFeedback(userComment, email);
		HttpStatus status = HttpStatus.OK;
		
		ResponseEntity<DBObject> response  = new ResponseEntity<DBObject>(new BasicDBObject("result", result), new HttpHeaders(), status);
		log.info("resetPassword(...) - end: " + response);
		return response;
	}
	
}
