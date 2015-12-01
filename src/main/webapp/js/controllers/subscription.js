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