'use strict';

angular.module(appName).controller(
		'StartCtrl',
		[ '$scope', '$rootScope', '$location', 'Disease', 'User', '$timeout', 'Title', '$q', 'TreatmentReviewSummary',
				function($scope, $rootScope, $location, Disease, User, $timeout, Title, $ql, TreatmentReviewSummary) {
					$("#search").focus();
					// $("#wrap").addClass("backgroundNormal");
					$("#wrap1").addClass("startLogo");

					$scope.mPer = 0.5;
					$scope.fPer = 0.5;

					var normalColor = "#000";
					var inactiveColor ="#888";
					
					$scope.mColor = inactiveColor;
					$scope.fColor = inactiveColor;
					
					$timeout(function() {
						Disease.queryNames(false);
					}, 2000);

					$scope.initSearch = function() {
						if ($scope.phrase.length > 0) {// FIX for IE
							$rootScope.phrase = $scope.phrase;
							$location.path("/Behandlungen");
						}
					};
					/*
					 * $scope.users = [];
					 * 
					 * User.findByDiseaseIdPageNumberPageSize(null, 1, 21).then( function(users){ $scope.users = users; });
					 */

					$timeout(function() {
						Title.set(Title.defaultTitle());
					}, 200);

					// menu
					var prevScrollY = 0;
					jQuery(window).scrollTop(0);

					var prevTime = new Date().getTime();
					var scrolling = false;
					var currSection = 1;

					var bigMove = null;

					jQuery(window).keydown(function(event) {
						if (event.keyCode == 36 /* "Home" */|| event.keyCode == 35 /* "End" */) {
							bigMove = new Date().getTime();
						}
					});

					jQuery(window).unbind("scroll");		
					//console.log("unbind scroll init");	
					

					jQuery(window).keyup(function(event){
						if(jQuery(".modal").length>0){
							return;
						}
						var key = event.keyCode;
						var ranges = [[65, 90, 97], [48, 57, 48], [96, 105, 48]]; //A-Z,0-9,0-9 on num block
						for(var i =0;i<ranges.length;i++){
							var r = ranges[i];
							if(key >= r[0] && key <=r[1]){
								var idx = key - r[0] + r[2];
								var ch = String.fromCharCode(idx);
								//console.log("Pressed: " + key + " " + ch);
								if(!$scope.phrase){
									$scope.phrase = ch;
									$scope.initSearch();
								}
								return;
							}
						}
					});
					
					$scope.$on("$destroy", function(){
						jQuery(window).unbind("scroll");		
						jQuery(window).unbind("keyup");
						//console.log("unbind scope destroy");	
				    });
					
					$scope.scroll = function(section){
						currSection = section;
						scrolling = true;
						//console.log("scroll " + newScroll + " " + prevScrollY + " " + diff + " " + currSection + " time:" + (now - prevTime));
						var height = jQuery("#wrap" + currSection).offset().top;
						if(currSection == 1){
							height -= 50;
						}
						$('html,body').animate({
							scrollTop: height
						}, 200, null, function() {
							scrolling = false;
							prevScrollY = jQuery(window).scrollTop();
						});						
					};
					
					jQuery(window).scroll(function(data) {
						if(jQuery("#wrap1").length == 0){
							//other view as start
							jQuery(window).unbind("scroll");		
							//console.log("unbind scroll");	
							return;
						}
						if (scrolling) {
							//console.log("scrolling " + jQuery(window).scrollTop());
							return;
						}
						if (bigMove && (new Date().getTime() - bigMove) < 1000) {
							//console.log("sc big move");
							return;
						}
						var now = new Date().getTime();
						var dt = now - prevTime;
						prevTime=now;
						if(dt < 250){//Apple mouse and trackpad - bouncing effect on ent and start
							return;
						}
						
						bigMove = null;
						var newScroll = jQuery(window).scrollTop();
						var diff = newScroll - prevScrollY;

						var prevSection = currSection;

						if (diff > 0) {
							currSection++;
							if (currSection > 4) {
								currSection = 4;
							}
						} else if (diff < 0) {
							currSection--;
							if (currSection < 1) {
								currSection = 1;
							}
						}
						if (prevSection != currSection) {
							$scope.scroll(currSection);
						}else{
							//console.log("sc same sections");
						}
						prevScrollY = newScroll;
					});

					// dictionary
					$scope.selectedLetter = "A";
					//var testL = "SCROLLtest";
					var testL = null;

					$scope.steps = null;
					$scope.currentStep = 1;
					$scope.stepClassInfo = function(step) {
						var result = "wizardInfo " + ($scope.currentStep == step ? "wizardInfoActive" : "");
						return result;
					};

					$scope.stepProgressClassInfo = function(step) {
						var result = "progressInfo " + (($scope.currentStep >= step) ? "progressInfoActive" : "");
						return result;
					};

					var pagesize = 10;

					$scope.toStep = function(step) {
						$scope.currentStep = step;
						var index = (step - 1) * pagesize;
						$scope.tableData = [];

						for ( var i = 0; i < pagesize && index < $scope.tableData1.length; i++, index++) {
							$scope.tableData.push($scope.tableData1[index]);
						}
					};

					function prepareData() {

						$scope.tableData = [];
						var len = $scope.tableData1.length < pagesize ? $scope.tableData1.length : pagesize;

						for ( var i = 0; i < len; i++) {
							$scope.tableData.push($scope.tableData1[i]);
						}

						var numpages = Math.ceil($scope.tableData1.length / pagesize);
						if (numpages > 1) {
							$scope.steps = [];
							for ( var i = 1; i <= numpages; i++) {
								$scope.steps.push(i);
							}
						}
					}

					$scope.reloadLetter = function(l) {
						// $location.hash("");
						$scope.currentStep = 1;
						$scope.steps = null;
						$scope.selectedLetter = l;
						$scope.tableData1 = [];
						if(testL){
							if (l == testL) {
								for ( var index = 0; index < 10; index++) {
									for ( var subIndex = 0; subIndex < 7; subIndex++) {
										var obj = {
											"disease" : "test disease " + (index + 1),
											"treatment" : "test treatment " + (subIndex + 1)
										};
										$scope.tableData1.push(obj);
									}
								}
								prepareData();
								return;
							}
						}
						TreatmentReviewSummary.getAllDiseaseTreatments(l).then(function(data) {
							for ( var index = 0; index < data.length; index++) {
								var treatments = data[index].treatments;
								for ( var subIndex = 0; subIndex < treatments.length; subIndex++) {
									var obj = {
										"disease" : data[index].name,
										"treatment" : treatments[subIndex].name,
										"suggestion" : data[index].suggestion
									};
									$scope.tableData1.push(obj);
								}
							}
							prepareData();
						});
					};

					TreatmentReviewSummary.getAllDiseaseLetters().then(function(data) {
						$scope.letters = [];
						for ( var index = 0; index < data.length; index++) {
							if (index == 0) {
								$scope.selectedLetter = data[index]["index"];
							}
							$scope.letters.push(data[index]["index"]);
						}
						$scope.reloadLetter($scope.selectedLetter);
						if (testL) {
							$scope.letters.push(testL);
						}
					});

					$scope.objStyle = function(l) {
						if ($scope.selectedLetter == l) {
							return true;
						}
						return false;
					};

					$scope.signup = function() {
						$rootScope.doSignup = true;
					};
					
					function makeSum(data){
						var sum = 0;
						for(var i=0;i<data.length;i++){
							sum += data[i].count;
						}
						return sum;
					}
					
					//statistics
					TreatmentReviewSummary.find({}, 0, 5, "modifiedOn", "desc").then(function(data){
						if(data.length > 0){
							var index = Math.floor(Math.random()*data.length);
							$scope.trss = data;
							$scope.trs0 = data[0];
							/*
							$scope.trs = data[index];

							$scope.mCount = 0;
							$scope.fCount = 0;
							if($scope.trs.ageStatistics.male){
								$scope.mCount = makeSum($scope.trs.ageStatistics.male);
							}
							if($scope.trs.ageStatistics.female){
								$scope.fCount = makeSum($scope.trs.ageStatistics.female);
							}
							var sum = $scope.mCount + $scope.fCount;
							if(sum > 0){
								$scope.mPer = $scope.mCount / sum;
								$scope.fPer = $scope.fCount / sum;

								if($scope.mPer > $scope.fPer){
									$scope.mColor = normalColor;
								}
								if($scope.fPer > $scope.mPer){
									$scope.fColor = normalColor;
								}
							}
							*/
						}
					});
				} ]);
