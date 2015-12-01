'use strict';

angular.module(appName).controller('ResetPasswordCtrl', [ '$scope', '$routeParams', 'User', 'Alerts', function($scope, $routeParams, User, Alerts) {
		//Alerts.info(result.toSource());
		//Alerts.error("error");
	
		Alerts.clear();
		
		function error(){
			Alerts.error("Fehler. Ihr Passwort konnte nicht geändert werden.");
		}
		$scope.pwdChanged = false;
		
		$scope.change = function(){
			$scope.pwdChanged = false;
			var id = $routeParams.id;
			if($scope.password1 == $scope.password2){
				
				if(id){
					User.resetPassword(id, $scope.password1).then(function(result){
						$scope.pwdChanged = true;
						Alerts.info("Ihr Passwort wurde erfolgreich geändert");
					}, function(result){
						error();
					});
					return;
				}
				error();
			}
		};
} ]);
