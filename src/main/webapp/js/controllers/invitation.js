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

'use strict';

angular.module(appName).controller('InvitationCtrl',['$scope','$rootScope','$location','$routeParams','$timeout','Alerts', 'User', function($scope, $rootScope, $location, $routeParams,$timeout, Alerts, User) {
	Alerts.clear();
	$scope.invitationsCount = "0";
	$scope.isAdminLogged = false;
	$scope.getInvitations = function() {
		var loggedUser = $rootScope.userLogged;
		if ((loggedUser != null) && (loggedUser != 'undefined')) {
			User.getInvitations($rootScope.userLogged._id).then(function(result) {
				$scope.invitationsCount = "" + result.invitationCount;
				$scope.invitations = result.invitations;
				if (loggedUser.roles.length > 0) {
					var role = loggedUser.roles[0]; 
					if (role === "admin") {
						$scope.isAdminLogged = true;
					}
				}
			});
		};
	};
	$scope.sendInvitation = function() {
		if ((!($scope.invitationEmail)) || ($scope.invitationEmail.length == 0)) {
			iosOverlay({
				text: "Bitte E-Mail Adresse angeben",
				duration: 2e3,
				icon: "assets/iosOverlay/img/cross.png"
			});
			
		} else {
			User.sendInvitation($scope.invitationEmail).then(function(result){
				$scope.invitationsCount = $scope.invitationsCount - 1;
				$scope.invitationEmail = null;
				$scope.getInvitations();
			}, function(data, result){
				Alerts.error(data.toSource());
				Alerts.error(status);
			});

			iosOverlay({
				text: "Gesendet",
				duration: 2e3,
				icon: "assets/iosOverlay/img/check.png"
			});
		}
	};

	$scope.resendInvitation = function(emailAddress) {
		User.sendInvitation(emailAddress).then(function(result){
			iosOverlay({
				text: "Erneut versandt",
				duration: 2e3,
				icon: "assets/iosOverlay/img/check.png"
			});
		}, function(data, result){
			Alerts.error(data.toSource());
			Alerts.error(status);
		});
	};
	$timeout(function() { $scope.getInvitations(); }, 0);
	$scope.$on('userLogged', function() {	//init the data later, for the case when userLogged will be not available at the beginning (network latency)
		$scope.getInvitations();
	});
		
}]);
