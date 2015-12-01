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
