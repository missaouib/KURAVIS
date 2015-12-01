'use strict';

angular.module(appName).controller('SettingsCtrl',['$scope','$rootScope','$location','$routeParams','Alerts', 'User', 'Disease','Title','Follow', function($scope, $rootScope, $location, $routeParams, Alerts, User, Disease, Title, Follow) {
	Alerts.clear();
	Title.set(Title.defaultTitle());
	$scope.isLoggedUser = false;
	$scope.boxes = {};
	$scope.user = null;
	$scope.userAccount = null;
	$scope.loadingUser = false;
	$scope.gender = null;
	$scope.year = null;
	$scope.list_years = [];
	$scope.follows = [];
	
	$scope.disablePasswordChange = true;
	$scope.newPassword = "";
	$scope.confirmPassword = "";
	
	$scope.doDeleteAccount = false;
	$scope.doTreatmentHeardFrom = false;
	$scope.diseaseToTreatmentHeardFrom = null;
	Follow.getAllFollowedItems().then(function(response){
		$scope.follows = response;
	});
	$scope.initUser = function() {
		var result = false;
		var user = $rootScope.userLogged;
		if(user != null){
			result =  true;
			$scope.isLoggedUser = true;
				User.queryById($rootScope.userLogged._id).then(function(result) {
					$scope.user = result;
					if ($scope.user.settings.profile == null) {
						$scope.user.settings.profile = {};
						$scope.user.settings.profile.avatarColor = "#79ae3d";
					}
				});
		} else {
			$scope.isLoggedUser = false;
		}
		return result;
	};
	
	$scope.$on('userLogged', function() {	//init the data later, for the case when userLogged will be not available at the beginning (network latency)
		$scope.initUser();
	});
	
	
	$scope.initYears = function() {
		var actualYear = parseInt(new Date().getFullYear());
		for (var i = actualYear; i >= 1900; i--) {
			$scope.list_years.push({id:i, name:i});
		}
	};
	
	
	
	$scope.updateUserProfile = function() {

		User.updateUserProfile($scope.user).then(function(result) {
			$rootScope.userLogged.email = $scope.user.email;
			$rootScope.userLogged.aboutMe =  $scope.user.aboutMe;
			$rootScope.userLogged.name =  $scope.user.name;
			$rootScope.userLogged.gender = $scope.user.gender;
			$rootScope.userLogged.yearOfBirth = $scope.user.yearOfBirth;
			$rootScope.userLogged.avatarColor = $scope.user.settings.profile.avatarColor;
			$scope.initUser();
			//if ($rootScope.userLogged.settings) {
			//	$rootScope.userLogged.settings.profile.diaryPublic = $scope.user.settings.profile.diaryPublic;
			//}

			iosOverlay({
				text: "Aktualisiert",
				duration: 2e3,
				icon: "assets/iosOverlay/img/check.png"
			});
		});

	};
	
	$scope.changeEmail = function() {
		User.changeEmail($scope.user.email).then(function(result) {
			iosOverlay({
				text: "Aktualisiert",
				duration: 2e3,
				icon: "assets/iosOverlay/img/check.png"
			});
		});
	};
	
	$scope.validatePassword = function() {
	
		if($scope.newPassword.length >= 4 && $scope.newPassword.length <= 12 ){
			if($scope.newPassword == $scope.confirmPassword){
				$scope.disablePasswordChange = false;
			} else {
				$scope.disablePasswordChange = true;
			}
		} else {
			$scope.disablePasswordChange = true;
		}
	
	};
	
	$scope.changePassword = function() {
		User.changePassword($scope.newPassword, $scope.confirmPassword).then(function(result) {
			iosOverlay({
				text: "Geändert",
				duration: 2e3,
				icon: "assets/iosOverlay/img/check.png"
			});
		});
	};
	
	$scope.closeDeletion = function(){
		$scope.doDeleteAccount = false;
	};
	
	$scope.deleteAccount = function() {
		$scope.doDeleteAccount = false;
		User.deleteAccount($scope.user._id).then(function(result) {
			$rootScope.logoutWithNoPathChange();
			iosOverlay({
				text: "Gelöscht",
				duration: 2e3,
				icon: "assets/iosOverlay/img/check.png"
			});
			$location.path("");
		});
	};
	
	$scope.updatePrivacy = function() {
		User.updatePrivacy($scope.user.settings.privacySettings.emailNotification).then(function(result) {
			iosOverlay({
				text: "Aktualisiert",
				duration: 2e3,
				icon: "assets/iosOverlay/img/check.png"
			});
		});
	};
	
	$scope.unfollow = function(id, idx) {
		Follow.unfollow(id).then(function(idx) {
			var newFollows = [];
			for (var i = 0; i < $scope.follows.length; i++) {
				var item = $scope.follows[i];
				if (item._id != id) {
					newFollows.push(item);
				}
			}
			$scope.follows=newFollows;
		});
	};

	
}]);
