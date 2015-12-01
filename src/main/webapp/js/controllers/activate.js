'use strict';

angular.module(appName).controller('ActivateCtrl', [ '$routeParams', '$location', 'User', 'Alerts', function($routeParams, $location, User, Alerts) {
		//Alerts.info(result.toSource());
		//Alerts.error("error");
	
		Alerts.clear();
		
		function error(){
			Alerts.error("Error activate account");
		}
		
		var id = $routeParams.id;
		
		if(id){
			User.activate(id).then(function(result){
				Alerts.info("Hurra! Ihr Konto ist nun aktiviert.");
				$location.path("/Behandlungen");
			}, function(result){
				error();
			});
			return;
		}
		
		error();
} ]);
