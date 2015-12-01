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

angular.module(appName).controller('NotificationsCtrl',['$scope','$rootScope','$location','$routeParams','Alerts', 'User', 'Disease', function($scope, $rootScope, $location, $routeParams, Alerts, User, Disease) {
	
	//Notifications
	$scope.busy = false;
	$scope.reachEnd = false;
	$scope.after = 1;
	$scope.notifications = [];
	Alerts.clear();
	$scope.notificationsNextPage = function() {
	
		if ($scope.busy) return;
		if ($scope.reachEnd) return;
    
		$scope.busy = true;

		if($scope.notifications == null){
			$scope.notifications = [];
		}
    
		User.loadNotifications($scope.after, 20, "createdOn", "desc")
		.then(function(data) {
									
			for (var i = 0; i < data.length; i++) {
				if($scope.notifications == null) {
					$scope.notifications = [];
				}
				$scope.notifications.push(data[i]);
			}
			if(data.length == 0){
				$scope.reachEnd = true;
			}
			
			if($scope.notifications.length == 0){
				$scope.notfound = true;
			}
			
			$scope.after++;
		    $scope.busy = false;
			
		}, function() {
			showError();
		});
	};
	
	
}]);
