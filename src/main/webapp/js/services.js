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

var module = angular.module(appName + 'Services', [ 'ngResource' ]);

// var hostname = (window.location.hostname != '127.0.0.1') ? window.location.hostname : "dev-server url";

var storage = function(collection, $http, $q, $rootScope, $timeout, $location) {
	
	var result = {
		q : $q,
		rootScope : $rootScope,
		collection : collection,

		getUrl : function(query, collectionOverride, pageNumber, pageSize, sortBy, sortingOrder) {
			return '/api/v1/' 
			+ (collectionOverride || this.collection) 
			+ (query ? ("/" + query) : "") 
			+ (pageNumber != null ? ("?page.page=" + pageNumber) : "")
			+ ((pageNumber != null & pageSize != null) ? ("&page.size=" + pageSize) : "")
			+ (sortBy ? ("&page.sort=" + sortBy + "&page.sort.dir=" + sortingOrder) : "");
		},
		getUrlForTwoSortingColumns : function(query, collectionOverride, pageNumber, pageSize, sortBy1, sortingOrder1, sortBy2, sortingOrder2) {
			return '/api/v1/' 
			+ (collectionOverride || this.collection) 
			+ (query ? ("/" + query) : "") 
			+ (pageNumber ? ("?d_page.page=" + pageNumber) : "")
			+ ((pageNumber != null & pageSize != null) ? ("&d_page.size=" + pageSize) : "")
			+ (sortBy1 ? ("&d_page.sort=" + sortBy1 + "&d_page.sort.dir=" + sortingOrder1) : "")
			+ (sortBy2 ? ("&t_page.sort=" + sortBy2 + "&t_page.sort.dir=" + sortingOrder2) : "");			
		},
		
		execute : function(method, query, object, collectionOverride, pageNumber, pageSize, sortBy, sortingOrder, sortBy2, sortingOrder2) {
			this.checkSession();
			
			var url = (sortBy2 == null) ? this.getUrl(query, collectionOverride, pageNumber, pageSize, sortBy, (sortingOrder == null) ? "asc" : sortingOrder) 
					: this.getUrlForTwoSortingColumns(query, collectionOverride, pageNumber, pageSize, sortBy, (sortingOrder == null) ? "asc" : sortingOrder, sortBy2, (sortingOrder2 == null) ? "asc" : sortingOrder2);

			var deferred = this.q.defer();

			this.rootScope.workStatus = 'running';				
			if(!this.rootScope.readyCounter){
				this.rootScope.readyCounter = 1;
			}else{
				this.rootScope.readyCounter = this.rootScope.readyCounter + 1;
			}
			$http({
				method : method,
				url : url,
				data : object,
				headers: {'Cache-Control': 'no-cache', 'Pragma': 'no-cache'}
			}).success(function(data, status, headers, config) {
				deferred.resolve(data.content || data);
				$timeout(function(){
					$rootScope.readyCounter = $rootScope.readyCounter - 1;
					if($rootScope.readyCounter <=0 ){
						$rootScope.workStatus = 'ready';
					}					
				}, 10);
				return data;
			}).error(function(data, status, headers, config) {
				if ((data != null) && (data.errors != null)) {
					for(var i=0; i<data.errors.length; i++){
						var errorString = data.errors[i];
						console.log("Error: "+ errorString);
					}
				} else if (data.message != null) {
					console.log("Error: "+ data.message);
				}
				
				deferred.reject({
					data : data,
					status : status,
					headers : headers,
					config : config
				});
				$timeout(function(){
					$rootScope.readyCounter = $rootScope.readyCounter - 1;
					if($rootScope.readyCounter <=0 ){
						$rootScope.workStatus = 'ready';
					}
				}, 10);
			});
			
			return deferred.promise;
		},

		query : function() {
			return this.execute("GET");
		},
		queryById : function(id) {
			return this.execute("GET", id);
		},
		queryPage : function(page, pageNumber, pageSize, sortBy, sortingOrder) {
			return this.execute("GET", page, null, null, pageNumber, pageSize, sortBy, sortingOrder);
		},
		queryPageWithOverride: function(collectionOverride, page, pageNumber, pageSize, sortBy, sortingOrder) {
			return this.execute("GET", page, null, collectionOverride, pageNumber, pageSize, sortBy, sortingOrder);
		},
		find : function(condition, pageNumber, pageSize, sortBy, sortingOrder) {
			return this.execute("POST", "query", condition, null, pageNumber, pageSize, sortBy, sortingOrder);
		},
		findWithSecondarySortColumn : function(condition, pageNumber, pageSize, sortBy, sortingOrder, sortBy2, sortingOrder2) {
			return this.execute("POST", "query", condition, null, pageNumber, pageSize, sortBy, sortingOrder, sortBy2, sortingOrder2);
		},
		create : function(object) {
			return this.execute("POST", null, object);
		},
		update : function(object, id) {
			return this.execute("PUT", id, object);
		},
		remove : function(id) {
			return this.execute("DELETE", id, null);
		},

		newId : function(count) {
			if (!count) {
				count = 1;
			}
			return this.Execute("GET", count, null, "newid");
		},

		checkSession: function(){
			var deferred = this.q.defer();

			if(!this.rootScope.lastSessionCheck){
				this.rootScope.lastSessionCheck = 0;
			}
			var now = new Date().getTime();
			var diff = now - this.rootScope.lastSessionCheck;
			var maxAge = 60000;
			if(this.rootScope.userLogged) {
				if (this.rootScope.userLogged.state != 'unverified') {
					maxAge = 2000;		
				}
			}
			if(diff < maxAge){
				//console.log("get user from cache");
				deferred.resolve();
			}else{
				//console.log("get user from DB");
				this.rootScope.lastSessionCheck = now;
				var rootScope = this.rootScope;
				var url = this.getUrl("checksession", "user");
				$http({
					method : "GET",
					url : url,
					headers: {'Cache-Control': 'no-cache', 'Pragma': 'no-cache'}
				}).success(function(data, status, headers, config) {
					rootScope.userLogged = {
							email: data.email,
							name: data.name,
							gender: data.gender,
							yearOfBirth: data.yearOfBirth,
							//avatarColor:data.settings.profile.avatarColor,
							roles: data.roles,
							state:data.state,
							id: data._id,
							_id: data._id
					};
					if(data && data.settings && data.settings.profile) {
						rootScope.userLogged.avatarColor = data.settings.profile.avatarColor;
					}
					deferred.resolve();
					rootScope.signInMessage = null;
					rootScope.$broadcast("userLogged", rootScope.userLogged);
					return data;
				}).error(function(data, status, headers, config) {
					rootScope.userLogged = null;
					rootScope.$broadcast("userLogged", rootScope.userLogged);
//					$location.path("/");
					deferred.resolve();
				});			
			}
				
			return deferred.promise;
		},
		
		ensureUserLogged : function(context, func) {
			var deferred = $q.defer();

			if(this.rootScope.userLogged){
				func.call(context).then(function(result) {
					deferred.resolve(result);
				}, function(result) {
					deferred.reject(result);
				});
				return deferred.promise;
			}
			this.rootScope.signInLogin = null;
			this.rootScope.signInPassword = null;
			this.rootScope.doLogin = true;
			this.rootScope.signInMessage = null;
			
			this.rootScope.signFunc = func;
			this.rootScope.signContext = context;
			this.rootScope.signDeferred = deferred;
			return deferred.promise;
		}
	};
	$rootScope.logout = function(){
		$rootScope.userLogged = null;
		$rootScope.$broadcast("userLogged", $rootScope.userLogged);
		result.execute("POST", "signout", {
			d : 1
		}, "user");
		$location.path("/signedout");
	};
	$rootScope.logoutWithNoPathChange = function(){
		$rootScope.userLogged = null;
		$rootScope.$broadcast("userLogged", $rootScope.userLogged);
		result.execute("POST", "signout", {
			d : 1
		}, "user");
	};
	
	
	$rootScope.golbalResetPassword = function(){
		result.execute("POST", "resetpassword", {
			email : $rootScope.signInLogin}, "user").then(function (result){

			}, function (result){
			});
		$rootScope.resetPasswordSend = true;
	};
	
	$rootScope.globalSignIn = function(login, password, captcha, redirectToHomeScreen) {
//		var login = $rootScope.signInLogin;
//		var password = $rootScope.signInPassword;
//		var captcha = $rootScope.signInCaptcha;
		var rememberMe = $rootScope.signInRememberMe;

		result.execute.call(result, "POST", "signin", {
			login : login,
			password : password,
			captcha_answer: captcha,
			rememberMe: rememberMe
		}, "user").then(function(data) {
			// login success
			$rootScope.userLogged = {
					email: data.email,
					name: data.name,
					gender: data.gender,
					yearOfBirth: data.yearOfBirth,
					avatarColor:data.settings.profile.avatarColor,					
					id: data._id,
					state:data.state,
					roles: data.roles,
					_id: data._id,
			};
			$rootScope.$broadcast("userLogged", $rootScope.userLogged);
			$rootScope.doLogin = false;
			$rootScope.signInPassword = null;
			$rootScope.signInMessage = null;
			var func = $rootScope.signFunc;
			var context = $rootScope.signContext;
			var deferred = $rootScope.signDeferred;
			
			if(func){
				func.call(context).then(function(result1) {
					deferred.resolve(result1);
				}, function(result1) {
					deferred.reject(result1);
				});
			}
			
			if (!$rootScope.$$phase) {
				$rootScope.$apply();
			}
			$rootScope.$broadcast("userLogged", $rootScope.userLogged);
			if (redirectToHomeScreen) {
				$location.path("");
			}
		}, function(data) {
			// login error
			$rootScope.userLogged = null;
			$rootScope.$broadcast("userLogged", $rootScope.userLogged);
			$rootScope.signInMessage = "Error Sign In " + data.data.message;
			$rootScope.signInPassword = null;
			$rootScope.showCaptcha = data.data.show_captcha;
			$rootScope.updateCaptcha();
			//ctx.$apply();
		});
	};
	
	$rootScope.updateCaptcha = function() {
		var login = $rootScope.signInLogin;
		if($rootScope.showCaptcha){
			$rootScope.captchaImage = result.getUrl(encodeURI(login).replace(/\./g, '%2E').replace(/@/gi, '%40'), "captcha") + ".png?" + (new Date().getTime());
		}
	};	
	
	function fixModalWidth(){
		$timeout(function(){
			//jQuery('.modal').css({'width': '475px', 'margin-left': function () { return ($(this).width() / 2); }});
		}, 10 ,false);
	}
	
	$rootScope.startSignIn = function(){
		$rootScope.doLogin=true;
		$rootScope.doSignup = false;
		$rootScope.doResetPassword = false;
		$rootScope.doSendFeedback = false;
		$rootScope.resetPasswordSend = false;
		$rootScope.showCaptcha = false;
		$rootScope.signInLogin = null;
		$rootScope.signInPassword = null;
		$rootScope.signInRememberMe = null;
		$rootScope.signInMessage = null;

		fixModalWidth();
	};
	
	$rootScope.isFullWidthView = function(){
		var rc = $location.path() == "/" || $location.path() == "/home" || $location.path() == "/signedout" || $location.path().indexOf("/admin")==0; 
		return rc;
	};

	$rootScope.cancelSignUp = function() {
		$rootScope.doSignup=false;
		$rootScope.signUpName = null;
		$rootScope.signUpEmail = null;
		$rootScope.signUpPassword = null;
		$rootScope.signUpSubmitedError = false;
		$rootScope.signUpSubmited = false;
	};
	
	$rootScope.loading = function(show) {
		var loading_elem = $("#loading-indicator");
		if (show) {
			$(loading_elem).show();
		} else {
			$(loading_elem).hide();
		}
	};

	$rootScope.globalSignUp = function(userName, userEmail, pwd, pwd2) {
		if(pwd != pwd2){
			$rootScope.signUpSubmitedError = true;
			$rootScope.loading = false;
			return;
		}
		$rootScope.signUpName = userName;
		$rootScope.signUpPassword = pwd;
		$rootScope.signUpSubmitedError = false;
		$rootScope.signUpSubmited = false;
		
		result.execute("POST", "signup", {
			name : userName,
			email : userEmail,
			password : pwd
		}, "user").then(function (result){
			
			$rootScope.signInLogin = userEmail;
			$rootScope.signInPassword = pwd;
			$rootScope.signUpSubmited = result.activationUuid;
			$rootScope.signUpEmail = null;
			$rootScope.signUpName = null;
			$rootScope.invitationEmail = null;
			$rootScope.doSignup = false;
			$rootScope.signUpSubmited = true;
			$rootScope.globalSignIn(userEmail, pwd, null, null);
			//$timeout($rootScope.globalSignIn, 100);
		}, function (result){
			$rootScope.signUpSubmitedError = true;
		});
	};
	
	$rootScope.golbalIn2Up = function(){
		$rootScope.doLogin = false;
		$rootScope.doSignup = true;
		$rootScope.doResetPassword = false;
		$rootScope.doSendFeedback = false;
		$rootScope.resetPasswordSend = false;
		
		$rootScope.signInLogin = null;
		$rootScope.signInEmail = null;
		$rootScope.signInPassword = null;
		$rootScope.signInPassword2 = null;
		
		$rootScope.signUpSubmited = false;
		$rootScope.signUpSubmitedError = false;
		
		fixModalWidth();
	};
	
	$rootScope.globalSubscribe = function(){
		$rootScope.doLogin = false;
		$rootScope.doSignup = false;
		$rootScope.doSendFeedback = false;
		$rootScope.doSubscribe = true;
		$rootScope.doResetPassword = false;
		$rootScope.resetPasswordSend = false;
		
		$rootScope.signInLogin = null;
		$rootScope.signInEmail = null;
		$rootScope.signInPassword = null;
		$rootScope.signInPassword2 = null;
		$rootScope.subcribeEmail = null;
		
		$rootScope.signUpSubmited = false;
		$rootScope.signUpSubmitedError = false;
		$rootScope.subscribeEmailSend = false;
		
		fixModalWidth();
	};
	
	$rootScope.globalSubscribeNow = function(){
		result.execute("POST", "", {
			email : $rootScope.subcribeEmail}, "subscription");
		$rootScope.subscribeEmailSend = true;
	};
	
	
	$rootScope.golbalIn2Reset = function(){
		this.doLogin = false;
		this.doSignup = false;
		this.doSendFeedback = false;
		this.doResetPassword = true;
		this.resetPasswordSend = false;
		this.resetPasswordError = false;						
		fixModalWidth();
	};
	
	$rootScope.showFeebackModal = function(){
		this.doLogin = false;
		this.doSignup = false;
		this.doResetPassword = false;
		this.doSendFeedback = true;
		this.resetPasswordSend = false;
		this.resetPasswordError = false;						
		fixModalWidth();
	};
	
	$rootScope.globalSendFeedback = function() {
		var userEmail = "";
		if ($rootScope.userLogged != null) {
			userEmail = $rootScope.userLogged.email;
		}
		result.execute("POST", "feedback", {
			comment : $rootScope.feedbackText, email: userEmail}, "user");
		$rootScope.feedbackSent = true;
	};
	
	$rootScope.globalSendFeedbackModalHide = function() {
		$rootScope.feedbackText = "";
		$rootScope.feedbackSent = false;
		$rootScope.doSendFeedback = false;
	};
	
	
	return result;
};

module.factory('Disease', function($http, $q, $rootScope, $location, $timeout) {

	var d = storage("disease", $http, $q, $rootScope, $timeout, $location);
	
	d.deleteNames = function() {
		$rootScope.diseaseNames = null;
	};
	
	d.queryNames = function(update) {

		if (!$rootScope.diseaseNames) {
			var deferred = $q.defer();
			$rootScope.diseaseNames = deferred.promise;

			this.execute("GET", "?proj=name,treatmentReviewsCount").then(function(diseases) {
				diseases.sort(function(a, b) {
					if(a!= null && b!= null) {
						if (a.name.toLowerCase() < b.name.toLowerCase())
							return -1;
						if (a.name.toLowerCase() > b.name.toLowerCase())
							return 1;
					}
					return 0;
				});

				var diseaseNamesForSearch = {};

				$.each(diseases, function(index, value) {
					var disease = value;
					if(disease != null) {
						var name = disease.name.toLowerCase();
						var first = name.charAt(0);
	
						if (!diseaseNamesForSearch[first]) {
							diseaseNamesForSearch[first] = [];
						}
						diseaseNamesForSearch[first].push({
							id : 1,
							name : name
						});
					} else {
						diseases.splice(index, 1);
					}
				});

				$rootScope.diseaseNamesForSearch = diseaseNamesForSearch;
				deferred.resolve(diseases);
			});
		}
		return $rootScope.diseaseNames;
	};
	d.findByName = function(name) {
		return this.find({
			name : name
		});
	};
	return d;
});

module.factory('User', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("user", $http, $q, $rootScope, $timeout, $location);

	d.findByDiseaseId = function(diseaseId) {
		return this.find({
			"disease._id" : diseaseId
		});
	};
	d.findByDiseaseIdPageNumberPageSize = function(diseaseId, pageNumber, pageSize) {
		if(diseaseId != null){
			return this.execute("POST", "disease", {diseaseId:diseaseId}, null, pageNumber, pageSize);
		} else {
			return this.execute("POST", "disease", {}, null, pageNumber, pageSize);
		}
		
	};

	d.updateUserProfile = function(user) {
		return d.ensureUserLogged(this, function() {
			return this.update(user, user._id);
		});
	};

	d.activate = function(id){
		return this.execute("PUT", "activate/" + id, {});
	};
	
	d.changeEmail  = function(email){
		return this.execute("POST", "changeemail",{
			email : email
		});
	};
	d.changePassword = function(password, password2){
		return this.execute("POST", "changepassword",{
			password : password,
			password2 : password2
		});
	};
	
	
	d.getInvitations = function(userId){
		return this.execute("GET", "invitations/" + userId, {});
	};
	
	d.sendInvitation = function(emailAddress){
		return this.execute("POST", "invitations",{
			email : emailAddress
		});
	};
	
	d.canBeRegistered = function(registrationEmail){
			return d.execute("POST", null, {email : registrationEmail}, "user/invitations/canberegistered");
	};
 
	d.subscribe = function(subscriptionEmail) {
		return d.execute("POST", "", {email : subscriptionEmail}, "subscription");
	};
	
 	d.loadAccount = function(id){
		return this.execute("GET", "account/" + id, {});
	};
	
	d.deleteAccount = function(id){
		return this.execute("DELETE", "account/" + id, {});
	};
	
	//privacysettings
	d.updatePrivacy = function(emailNotification){
		return this.execute("POST", "privacysettings",{
			emailNotification : emailNotification
		});
	};

	d.loadNotifications = function(pageNumber, pageSize, sortBy, sortOrder){
		return this.queryPage("notifications", pageNumber, pageSize, sortBy, sortOrder);
	};
	
	d.loadUnreadNotifications = function(pageNumber, pageSize, sortBy, sortOrder){
		return this.queryPage("unreadnotifications", pageNumber, pageSize, sortBy, sortOrder);
	};
	
	d.loadNotificationsUnreadCount = function(){
		return this.execute("GET", "unreadnotificationscount", {});
	};

	d.resetPasswordRequest = function(email){
		return this.execute("POST", "resetpassword", { email: email});
	};

	
	d.resetPassword = function(id, password){
		return this.execute("POST", "changeresetedpassword", { resetPasswordUuid: id, password: password});
	};
	
	return d;
});

module.factory('Treatment', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("treatment", $http, $q, $rootScope, $timeout, $location);

	d.deleteNames = function() {
		$rootScope.treatmentNames = null;
	};
	
	d.queryNames = function(update) {

		if (!$rootScope.treatmentNames) {
			var deferred = $q.defer();
			$rootScope.treatmentNames = deferred.promise;

			this.execute("GET", "?proj=name").then(function(treatments) {
				treatments.sort(function(a, b) {
					if(a!= null && b!= null) {
						if (a.toLowerCase() < b.toLowerCase()) 
							return -1;
						if (a.toLowerCase() > b.toLowerCase())
							return 1;
					}
					return 0;
				});

				var treatmentNamesForSearch = {};

				$.each(treatments, function(index, value) {
					var treatment = value;
					if(treatment != null) {
						var name = treatment.toLowerCase();
						var first = name.charAt(0);

						if (!treatmentNamesForSearch[first]) {
							treatmentNamesForSearch[first] = [];
						}
						treatmentNamesForSearch[first].push({id:1, name:name});
					} else {
						treatments.splice(index, 1);
					}
					
				});

				$rootScope.treatmentNamesForSearch = treatmentNamesForSearch;
				deferred.resolve(treatments);
			});
		}
		return $rootScope.treatmentNames;
	};
	
	d.findByName = function(name) {
		return this.find({
			name : name
		});
	};
	
	return d;
});

module.factory('TreatmentReviewSummary', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("treatmentreviewsummary", $http, $q, $rootScope, $timeout, $location);

	d.findBy = function(diseaseName, treatmentName) {
		var deferred = $q.defer();
		this.find({
			"disease.name" : diseaseName,
			"treatment.name" : treatmentName
		}).then(function(result) {
			if (result.length == 0) {
				deferred.reject("not found");
				return;
			}

			deferred.resolve(result);
		}, function(result) {
			deferred.reject(result);
		});
		return deferred.promise;
	};
	
	d.findBySummaryId = function(summaryId) {
		return this.find({
			"_id" : summaryId
		});
	};
	
	d.findByDiseaseId = function(diseaseId) {
		return this.find({
			"disease._id" : diseaseId
		});
	}; 
	d.findByDiseaseIdPageNumberPageSize = function(diseaseId, pageNumber, pageSize, sortBy, sortingOrder) {
		return this.queryPageWithOverride("treatmentreviewsummary/bydisease", diseaseId, pageNumber, pageSize, sortBy, sortingOrder);
	};
	d.getAllDiseaseLetters = function(){
		return this.queryById("diseaseindex");
	};
	
	d.getAllDiseaseTreatments = function(letter){
		return this.queryById("bydiseasenameprefix/" + letter);
	};
	
	return d;
});

module.factory('TreatmentReview', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("treatmentreview", $http, $q, $rootScope, $timeout, $location);
	
	d.createReview = function(id, reviewObject) {
		return d.ensureUserLogged(this, function() {
			if(id.length > 0){
				reviewObject._id = id;
				return this.update(reviewObject, id);
			} else {
				return this.create(reviewObject);
			}
		});
	};

	d.createSubscriptionReview = function(id, diseaseName, treatmentName, rating, reviewText, sideEffects, yearOfBirth, gender, subscriptionEmail) {
			
		var tr = {};
		tr.disease = {
			name : diseaseName
		};
		tr.treatment = {
			name : treatmentName
		};
		tr.text = reviewText;
		tr.rating = rating;
		tr.sideEffects = sideEffects;
		if(yearOfBirth){
			tr.yearOfBirth = yearOfBirth;
		}
		if(gender){
			tr.gender = gender;
		}
		var treatmentReview = {treatmentReview: tr};
		var body = {"email":subscriptionEmail,
					"treatmentReview":treatmentReview};
		return this.execute("POST", "forsubscription", body);
	};
	
	d.deleteReview = function(id) {
		return d.ensureUserLogged(this, function() {
			return this.execute("DELETE", id, {});
		});
	};
	d.findBy = function(diseaseId, treatmentId) {
		return this.find({
			"disease._id" : diseaseId,
			"treatment._id" : treatmentId
		});
	};
	d.findByDiseaseIdTreatmentIdPageNumberPageSize = function(diseaseId, treatmentId, pageNumber, pageSize, sortBy) {
		return this.findWithSecondarySortColumn({
			"disease._id" : diseaseId,
			"treatment._id" : treatmentId
		}, pageNumber, pageSize, "rating","ASC", "rating", "ASC");
	};
	d.findByAuthorPageNumberPageSize = function(authorId, pageNumber, pageSize) {
		return this.findWithSecondarySortColumn({
			"author._id" : authorId
		}, pageNumber, pageSize, "disease.name","ASC", "treatment.name", "ASC");
	};
	d.findUserReviews = function(authorId, pageNumber, pageSize) {
		return this.execute("GET", "user/" + authorId);
	};
	d.voteFor = function(reviewId) {
		return d.ensureUserLogged(this, function() {
			return this.execute("PUT", reviewId, null, "vote/" + this.collection);
		});
	};
	
	d.existForUser = function(diseaseId, treatmentId) {
		return this.execute("POST", "existsforuser", { diseaseId: diseaseId, treatmentId: treatmentId});
	};

	return d;
});

module.factory('TmpTreatmentReview', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("tmptreatmentreview ", $http, $q, $rootScope, $timeout, $location);
	
	d.findBy = function(authorId,pageNumber, pageSize) {
		return this.find({
			"author._id" : authorId
		}, pageNumber, pageSize);
	};
	
	d.findUserReviews = function(authorId, pageNumber, pageSize) {
		return this.execute("GET", "user/" + authorId);
	};

	return d;
});
			    
module.factory('TreatmentEvent', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("treatmentevent", $http, $q, $rootScope, $timeout, $location);

	d.upsert = function(treatmentEvent) {
		return d.ensureUserLogged(this, function() {
			if (treatmentEvent._id) {
				return d.execute("PUT", null, treatmentEvent, "event/treatment");
			} else {
				return d.execute("POST", null, treatmentEvent, "event/treatment");
			}
		});
	};
	
	d.findAllForReview = function(userId, diseaseId, treatmentId, page, pageNumber, pageSize, sortBy, sortingOrder) {
		var parameters = "event/treatment?userId=" + userId + "&diseaseId=" + diseaseId + "&treatmentId=" + treatmentId;
		return this.queryPageWithOverride(parameters, null, null, null, null, null); 
	};
	
	d.commit = function(){
		var defered = $q.defer();
		defered.resolve(null);
		return defered.promise;
	};
	
	return d;
});

module.factory('TreatmentEventMemory', ['$q', 'TreatmentEvent', 'frequencyOptions','treatmentCategoryOptions' , 'getIdForName', function($q, TreatmentEvent, frequencyOptions, treatmentCategoryOptions, getIdForName) {
	var d = {};
	d.events = [];
	d.counter = 0;
	
	d.init = function(){
		this.events = [];
	};
	
	d.commit = function(){
		var promises = [];

	    angular.forEach(this.events , function(e) {

	        var deferred = $q.defer();

			delete e._id;
			//set domain values for category and frequency
			var categoryName = e.category;
			var categoryId = getIdForName(treatmentCategoryOptions, categoryName);
			if (!isStringEmpty(categoryId)) {
				e.category = categoryId;
			}
			var frequencyName = e.frequency;
			if (e.frequency) {
				var frequencyId = getIdForName(frequencyOptions, frequencyName);
				if (!isStringEmpty(categoryId)) {
					e.frequency = frequencyId;
				}
			} else {
				e.frequency = null;
			}
			
			TreatmentEvent.upsert(e).then(function(){
				deferred.resolve(null);
			}, function(){
				deferred.resolve(null);
			});
	        promises.push(deferred.promise);
	    });

	    return $q.all(promises);
	};
	
	d.upsert = function(treatmentEvent) {
		var defered = $q.defer();
		d.events.push(treatmentEvent);
		var rc = treatmentEvent._id;
		if(!treatmentEvent._id){
			d.counter = d.counter + 1;
			rc = d.counter;
		}
		defered.resolve({_id:rc});
		return defered.promise;
	};
	
	d.findAllForReview = function(userId, diseaseId, treatmentId, page, pageNumber, pageSize, sortBy, sortingOrder) {
		var defered = $q.defer();
		var result = [];
		for(var i = 0; i<this.events.length; i++ ){
			var e = this.events[i];
			result.push(e);
		}
		defered.resolve(result);
		return defered.promise;
	};

	d.deleteEvent = function(id){
		var defered = $q.defer();
		for(var i = 0; i<this.events.length; i++ ){
			var e = this.events[i];
			if(e._id==id){
				this.events.splice(i, 1);
				break;
			}
		}
		defered.resolve(null);
		return defered.promise;
	};
	
	return d;
}]);


module.factory('OptionList', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("option_list", $http, $q, $rootScope, $timeout, $location);
	
	var getCollection = function(type){
		return "option_list/" + type;
	};
	
	d.findAllUnits=  function() {
		return this.queryPageWithOverride(getCollection("unit"), null, null, null, null, null); 
	};
	d.findAllOptionLists =  function() {
		return this.queryPageWithOverride(getCollection("treatmenttype"), null, null, null, null, null); 
	};
	
	d.query = function(type){
		return this.execute("GET", null, null, getCollection(type));
	};

	d.create = function(type, object){
		return this.execute("POST", null, object, getCollection(type));
	};

	d.update = function(type, id, object){
		return this.execute("PUT", id, object, getCollection(type));
	};
	
	d.remove = function(type, id){
		return this.execute("DELETE", id, null, getCollection(type));
	};
	
	return d;
});

module.factory('TreatmentReviewEvents', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("treatmentreviewevent", $http, $q, $rootScope, $timeout, $location);

	d.create = function(reviewId, text) {
		return d.ensureUserLogged(this, function() {
			return d.execute("PUT", reviewId, {text : text}, "event/treatmentreview");
		});
	};

	d.findByReview = function(reviewId) {
		return this.find({
			"treatmentReviewId" : reviewId
		});
	};
	d.findByReviewPageNumberPageSize = function(reviewId, pageNumber, pageSize) {
		return this.find({
			"treatmentReviewId" : reviewId
		}, pageNumber, pageSize);
	};
	d.findByUserPageNumberPageSize = function(userId, pageNumber, pageSize) {
		return this.find({
			"user._id" : userId
		}, pageNumber, pageSize, "createdOn", "desc");
	};
	
	d.findCommentsByReview = function(reviewId) {
		return this.find({
			"type" : "comment",
			"treatmentReviewId" : reviewId
		});
	};
	d.findCommentsByReviewPageNumberPageSize = function(reviewId, pageNumber, pageSize) {
		return this.find({
			"type" : "comment",
			"treatmentReviewId" : reviewId
		}, pageNumber, pageSize);
	};
	d.findCommentsByUserPageNumberPageSize = function(userId, pageNumber, pageSize) {
		return this.find({
			"type" : "comment",
			"user._id" : userId
		}, pageNumber, pageSize, "createdOn", "desc");
	};

	d.findVotesByReview = function(reviewId) {
		return this.find({
			"type" : "vote",
			"treatmentReviewId" : reviewId
		});
	};
	
	d.findVotesByReviewIdUserId = function(reviewId, userId) {
		return this.find({
			"type" : "vote",
			"treatmentReviewId" : reviewId,
			"user._id" : userId
		});
	};
	
	return d;
});

module.factory('TreatmentReviewComment', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("treatmentreviewcomment", $http, $q, $rootScope, $timeout, $location);

	d.create = function(reviewId, text) {
		return d.ensureUserLogged(this, function() {
			return d.execute("PUT", reviewId, {text : text}, "comment/treatmentreview");
		});
	};
	
	d.deleteComment = function(commentId){
		return d.ensureUserLogged(this, function() {
			return d.execute("DELETE", commentId, {}, "comment");
		});
	};
	return d;
});

module.factory('Report', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("report", $http, $q, $rootScope, $timeout, $location);

	d.create = function(entityId, entityName, note, text, createdByUserId, createdByUserName, source, reportType) {
		return d.ensureUserLogged(this, function() {
			return d.execute("POST", entityId, {note : note, fraudReportCategory: reportType, text: text, sourceId:source, user: { _id:createdByUserId, 	name: createdByUserName}}, "report/" + entityName);
		});
	};

	return d;
});

module.factory('Statistics', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("statistics", $http, $q, $rootScope, $timeout, $location);

	d.create = function(entityId, entityName) {
		return d.execute("GET", entityId, {}, "page/statistics/" + entityName);
	};

	return d;
});

module.factory('TreatmentReviewVotes', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("treatmentreviewvotes", $http, $q, $rootScope, $timeout, $location);
	d.findByReviewId = function(reviewId) {
		return this.find({
			treatmentReviewId : reviewId
		});
	};

	d.findByReviewIdUserId = function(reviewId, userId) {
		return this.find({
			"treatmentReviewId" : reviewId,
			"user._id" : userId
		});
	};

	d.findByUserIdPageNumberPageSize = function(userId, pageNumber, pageSize) {
		return this.find({
			"user._id" : userId
		}, pageNumber, pageSize, "createdOn", "desc");
	};
	return d;
});

module.factory('SideEffect', function($http, $q, $rootScope, $location, $timeout) {

	var d = storage("treatmentsideeffect", $http, $q, $rootScope, $timeout, $location);
	
	d.deleteNames = function() {
		$rootScope.sideEffectNames = null;
	};
	
	d.queryNames = function(update) {

		if (!$rootScope.sideEffectNames) {
			var deferred = $q.defer();
			$rootScope.sideEffectNames = deferred.promise;

			this.execute("GET", "?proj=name").then(function(sideEffects) {
				sideEffects.sort(function(a, b) {
					if(a!= null && b!= null) {
						if (a.toLowerCase() < b.toLowerCase())
							return -1;
						if (a.toLowerCase() > b.toLowerCase())
							return 1;
					}
					return 0;
				});

				var sideEffectNamesForSearch = {};

				$.each(sideEffects, function(index, value) {
					var sideEffect = value;
					if(sideEffect!= null) {
						var name = sideEffect.toLowerCase();
						var first = name.charAt(0);
	
						if (!sideEffectNamesForSearch[first]) {
							sideEffectNamesForSearch[first] = [];
						}
						sideEffectNamesForSearch[first].push({id:1, name:name});
					} else {
						sideEffects.splice(index, 1);
					}
				});

				$rootScope.sideEffectNamesForSearch = sideEffectNamesForSearch;
				deferred.resolve(sideEffects);
			});
		}
		return $rootScope.sideEffectNames;
	};
	d.findByName = function(name) {
		return this.find({
			name : name
		});
	};
	
	d.normalize = function(value) {
		return severityHlp.normalize(value);
	};
	
	d.denormalize = function(value) {
		return severityHlp.denormalize(value);
	};
	
	return d;
});

module.factory('Fraud', function($http, $q, $rootScope, $timeout, $location, $filter) {
	var d = storage("fraudreportitem", $http, $q, $rootScope, $timeout, $location);
	
	d.query = function(){
			var fri = storage("fraudreportitem", $http, $q, $rootScope, $timeout, $location);
			var fr = storage("fraudreport", $http, $q, $rootScope, $timeout, $location);
			
			var defered = $q.defer();
			
			$q.all([fri.query(), fr.query()]).then(function(adata){
				var reportItems = adata[0];
				var reports = adata[1];
				
				var result = [];
				var df = $filter('toLocaleString');
				
				for(var index = 0; index < reportItems.length;index++){
					var ri = reportItems[index];
					
					var entityName = '*** unknown entity ' + ri.fraudReportId + " ***";
					var entityTarget= "";
					var createdByUser = "";
					var createdByUserId = "";
					for(var i2 = 0; i2 < reports.length; i2++){
						var r = reports[i2];
						if(r._id == ri.fraudReportId){
							//TODO - depending on entityname change url
							entityTarget = 'Bewertung/' + r.sourceId;
							entityName =  r.text;
							entityId = r.entityId;
							if(r.user){
								createdByUser = r.user.name;
								createdByUserId = r.user._id;
							}
							break;
						}
					}
					
					result.push({_id:ri._id, date:df(ri.createdOn), dateRaw: ri.createdOn, note:ri.note, entityName:entityName, entityTarget:entityTarget, entityId: ri.entityId,
						entityType:ri.entityName, fraudReportCategory:ri.fraudReportCategory, user:ri.user.name, userId:ri.user._id, createdByUser:createdByUser, createdByUserId:createdByUserId});
				}
				
				result.sort(function(a,b){return b.dateRaw - a.dateRaw;});
				defered.resolve(result);
			}, function(err){
				defered.reject(err);
			});
			return defered.promise;
		};
	return d;
});

module.factory('Subscription', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("subscription", $http, $q, $rootScope, $timeout, $location);

	
//	queryPageWithOverride: function(collectionOverride, page, pageNumber, pageSize, sortBy, sortingOrder) {
//		return this.execute("GET", page, null, collectionOverride, pageNumber, pageSize, sortBy, sortingOrder);
//	},
	d.getAllSubscribers = function(pageNumber, pageSize, sortBy, sortOrder){
		return this.queryPageWithOverride("subscription", pageNumber, pageSize, sortBy, sortOrder);
	};
	
	d.query = function(){
		var defered = $q.defer();
		
		d.queryPageWithOverride("subscription", null, null, null, null).then(function(result){
			result.sort(function(a,b){return b.createdOn - a.createdOn;});
			defered.resolve(result);
		}, function(err){
			defered.reject(err);
		});
		return defered.promise;
	};
	
	return d;
});

module.factory('Event', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("event", $http, $q, $rootScope, $timeout, $location);
	
	d.getById = function(type, id){
		return this.queryById("/" + type + "/" + id);
	};

	d.findByUserByStartByEnd = function(userId, start, end, pageNumber, pageSize) {			
			return this.execute("GET", "?userId="+userId+"&start="+start+"&end="+end+"&page.page=0&page.size=" + pageSize + "&page.sort=start&page.sort.dir=desc", {}, null);
	};
	
	d.createEventWeight = function(weight, start){
		return this.execute("POST", "weight", { eventType:"WEIGHT", weight: weight, start: start});
	};
	
	d.createEventNote = function(start, title, text){
		return this.execute("POST", "note", { eventType:"NOTE", start: start, title: title, text: text});
	};
	
	d.updateEvent = function(event) {
		var type = event.eventType.toLowerCase();
		return this.execute("PUT", type, event);
	};

	d.deleteEvent = function(id){
		if (!confirm("Sind Sie sicher?")) {
			return;
		}
		return this.execute("DELETE", "/" + id, {});
	};

	
	return d;
});

module.factory('EventMemory', ['TreatmentEventMemory', function(TreatmentEventMemory) {
	var d = {};
	d.deleteEvent = function(id){
		if (!confirm("Sind Sie sicher?")) {
			return;
		}
		return TreatmentEventMemory.deleteEvent(id);
	};
	return d;
}]);

var ageHlp = {
	min : 1,
	max : 6,
	normalize : function(value) {
		return (value - this.min) / (this.max - this.min);
	},
	denormalize : function(value) {
		return value * (this.max - this.min) + this.min;
	},
	denormalizeToInt : function(value) {
		return Math.round(this.denormalize(value));
	},
	denormalizeToDouble : function(value, digitsAfterPoint) {
		return this.denormalize(value).toFixed(digitsAfterPoint);
	}
};

var severityHlp = {
		min : 0,
		max : 3,
		normalize : function(value) {
			return (value - this.min) / (this.max - this.min);
		},
		denormalize : function(value) {
			return value * (this.max - this.min) + this.min;
		},
		denormalizeToInt : function(value) {
			return Math.round(this.denormalize(value));
		},
		denormalizeToDouble : function(value, digitsAfterPoint) {
			return this.denormalize(value).toFixed(digitsAfterPoint);
		}
};

module.factory('Alerts', function($rootScope){
	function add($rootScope, type, text) {
		if (!$rootScope.alerts) {
			$rootScope.alerts = [];
		}
		$rootScope.alerts.push({
			type : type,
			msg : text
		});
	}
	$rootScope.closeAlert = function(index) {
		$rootScope.alerts.splice(index, 1);
	};
	return {
		error : function(text) {
			add($rootScope, "error", text);
		},
		success : function(text) {
			add($rootScope, "success", text);
		},
		info : function(text) {
			add($rootScope, "info", text);
		},
		clear : function() {
			$rootScope.alerts = [];
		}
	};
});

module.factory('Loading', function($rootScope){
	return {
		start : function() {
			$rootScope.loading = true;
		},
		stop : function(text) {
			$rootScope.loading = false;
		}
	};
});

module.factory('Follow', function($http, $q, $rootScope, $timeout, $location) {
	var d = storage("user/follow", $http, $q, $rootScope, $timeout, $location);

	d.follow = function(type, id, name){
		return d.ensureUserLogged(this, function() {
			return this.create({entityType:type, entityId : id});
		});
	};
	
	d.unfollow = function(id){
		//if (!confirm("Wirklich nicht mehr folgen?")) {
			//return;
		//}
		return this.remove(id);
	};
	d.getFollowItemId = function(type, id){
		return this.execute("GET", type + "/" +  id);
	};
	d.getAllFollowedItems = function(){
		return this.execute("GET");
	};
	return d;
});

