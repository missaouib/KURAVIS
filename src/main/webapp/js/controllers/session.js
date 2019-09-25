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

angular.module(appName).controller('SessionCtrl',['$scope','$rootScope','User', '$timeout', '$location', '$window',function($scope, $rootScope, User, $timeout, $location, $window) {
	//user check on page reload
	$rootScope.workStatus = 'start';
	User.checkSession();

	function isStartPage(){
		return $location.path() == "/" || $location.path() == "/home";
	}
	
	//main window resizing
	function checkWindowContent(){
		$timeout(function () {
			var wrap = jQuery("#wrap");
			if(isStartPage()){
				wrap = jQuery("#wrap1");
			}
			
			var footer = jQuery("#footer");
			var wh = jQuery(window).height();
			var ww = wrap.height();
			var fh = footer.height();
			var diff = wh - ww;
			var minHeight = 0;
			if(diff > 0){
				minHeight = wh - fh - 5;
			}
			
			if(isStartPage()){
				jQuery(".start_wrap").css("min-height", wh);
				jQuery("#wrap1").css("min-height", wh - 50); //50 == .wrap > container padding
				jQuery("#wrap4").css("min-height", wh - 80); //50 == .wrap > container padding
			}else{
				wrap.css("min-height", minHeight);
			}
		}, 0, false);
	}
	
	jQuery(window).resize(function() {
		checkWindowContent();
	});
	
	$rootScope.$on('$viewContentLoaded', function () {
		//Google Analytics
		if ($window._gaq) {
			$window._gaq.push(['_trackPageview', $location.path()]);
		}
		
		checkWindowContent();
		if(!isStartPage()){
			//$("#wrap").removeClass("startLogo");
			//$(".start_wrap").removeClass("startLogo");
			$("#wrap1").removeClass("startLogo");
		}
		if($location.path() == "/signedout"){
			$("#wrap").addClass("signedOutBg");
		}else{
			$("#wrap").removeClass("signedOutBg");	
		}

	  });
	
	//following is important for phantom.js - don't remove 
	$timeout(function(){
		if($rootScope.workStatus=='start'){
			$rootScope.workStatus = 'ready-timeout';
		}
	} , 150);
}]);
