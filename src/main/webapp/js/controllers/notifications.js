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
