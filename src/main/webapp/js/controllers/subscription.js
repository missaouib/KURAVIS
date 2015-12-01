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

angular.module(appName).controller('SubscriptionCtrl',['$scope','$rootScope','$location','$routeParams','$timeout','Alerts', 'Subscription', function($scope, $rootScope, $location, $routeParams,$timeout, Alerts, Subscription) {
	Alerts.clear();
	$scope.invitationsCount = "0";
	$scope.isAdminLogged = false;
	$scope.getSubscriptions = function() {
		var loggedUser = $rootScope.userLogged;
		if ((loggedUser != null) && (loggedUser != 'undefined')) {
			Subscription.getAllSubscribers(null, null, null, null).then(function(result) {
				$scope.invitationsCount = "" + result.invitationCount;
				$scope.subscriptionsCount = result.length;
				$scope.subscriptions = result;
				if (loggedUser.roles.length > 0) {
					var role = loggedUser.roles[0]; 
					if (role === "admin") {
						$scope.isAdminLogged = true;
					}
				}
			});
		};
	};
	
	$timeout(function() { $scope.getSubscriptions(); }, 0);
	$scope.$on('userLogged', function() {	//init the data later, for the case when userLogged will be not available at the beginning (network latency)
		$scope.getSubscriptions();
	});
		
}]);
