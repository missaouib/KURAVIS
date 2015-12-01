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

angular
		.module(appName)
		.controller(
				'DiseaseCtrl',
				[
						'$scope',
						'$rootScope',
						'$location',
						'$q',
						'$routeParams',
						'$timeout',
						'Disease',
						'TreatmentReviewSummary',
						'User',
						'Title',
						'Follow',
						'Alerts',
						
						function($scope, $rootScope, $location, $q,
								$routeParams, $timeout, Disease,
								TreatmentReviewSummary, User, Title, Follow, Alerts) {
							  
							  Alerts.clear();
							  $scope.diseaseName = $routeParams.diseaseName || "";
							  $scope.diseaseName = Title.urlDecode($scope.diseaseName);
							  if ($scope.diseaseName == null) {
								Title.set(Title.defaultTitle());
							  } else {
								  Title.set($scope.diseaseName);
							  }
							  $rootScope.followTooltip = 'Sie können ein Gesundheitsanliegen oder Behandlung folgen und erhalten eine E-Mail Notifikation, sobald es einen neuen Eintrag gibt. Um diesen Service zu nutzen, müssen Sie sich bei KURAVIS registrieren.';

							  
							  $scope.busy = false;
							  $scope.reachEnd = false;
							  $scope.after = 1;
							  $scope.selectedDiseaseId = null;
							  $scope.treatmentReviewSummary = [];
							  $scope.treatmentReviewsCount = 0;
							  $scope.users = [];
							  
							  $scope.initUsers = function(diseaseId) {
/*									User.findByDiseaseIdPageNumberPageSize(diseaseId, 1, 24).then(
											function(users){
												$scope.users = users;
											});
*/											
								};

							  $scope.initAllUsers = function() {
/*									if ($scope.notfound) {
										User.findByDiseaseIdPageNumberPageSize(null, 1, 24).then(
											function(users){
												$scope.users = users;
											});
									}
*/									
								};
							
								
							  $scope.treatmentReviewSummaryNextPage = function() {
								
							    if ($scope.busy) return;
							    if ($scope.selectedDiseaseId == null) {
							    	$scope.initAllUsers();
							    	return;
							    }
							    if ($scope.reachEnd) return;
							    
							    $scope.busy = true;
							    
							    if($scope.treatmentReviewSummary == null){
							    	$scope.treatmentReviewSummary = [];
							    }
							    TreatmentReviewSummary
								.findByDiseaseIdPageNumberPageSize( $scope.selectedDiseaseId, $scope.after, 10, "rating", "ASC")
								.then(
										function( data) {
											$scope.treatmentReviewsCount = data.totalElements;
											var items = data.page;
											for (var i = 0; i < items.length; i++) {
												if($scope.treatmentReviewSummary == null) {
													$scope.treatmentReviewSummary = [];
												}
										        $scope.treatmentReviewSummary.push(items[i]);
										      }
											if(items.length == 0){
												$scope.reachEnd = true;
											}
											
											if($scope.treatmentReviewSummary.length == 0){
												$scope.notfound = true;
												$scope.initAllUsers();
											}
											
											$scope.after++;
										    $scope.busy = false;
										});
							    
							  };
							
							
							var preload_data = [];
							
							$("#disease_selector")
									.select2(
											{
												placeholder : "Gesundheitsanliegen eingeben",
												matcher: function ( term , text, element ) {
										            var match = (text.toUpperCase()+element.attr("synonyms").toUpperCase()).indexOf(term.toUpperCase())>=0;
										            //console.log("matcher/ term", term, "text", text, "match" , match);
										            return match;
										        },
										        createSearchChoice:function(term, data) {
												    if ($(data).filter(function() {
												        return this.text.toUpperCase().localeCompare(term.toUpperCase()) === 0;
												    }).length === 0) {
												        return {id:term, text: term, isNew: true};
												    }
												},
												formatResult : function(result, container, query, escapeMarkup) {
													var matchString = query.term;
													var matchStringU = query.term.toUpperCase();
													var n = result.text.toUpperCase().indexOf(matchStringU);
													var preString = result.text.substring(0, n);
													var postString = result.text.substring(n + query.term.length);
													var newString = "";
													var returnString = "";
													if(!result.count){
														result.count = 0;
													}
													if(result.isNew){
														result.count = 0;
														newString = '<span class="label success">Neu</span> ';
													}
													returnString =  "<div class='select2-result-label'>"
															+ newString 
															+ preString
															+ "<span class='select2-match' style='font-weight:bold;'>"
															+ matchString
															+ "</span>";
													if (result.isNew) {
														returnString = returnString + "</div>";
													} else {
														returnString = returnString + postString
														+ "<span style='float:right'>"
														+ result.count
														+ " Bewertungen" + "</span></div>";
													}
													return returnString;
												},
												query : function(query) {
													var data = {
														results : []
													};
													$scope.suggestedDisease = query.term;
													$
															.each(
																	preload_data,
																	function() {
																		if (query.term.length == 0
																				|| this.text
																						.toUpperCase()
																						.indexOf(
																								query.term
																										.toUpperCase()) >= 0) {
																			if (data.results.length < 11) {
																				data.results
																						.push({
																							id : this.id,
																							text : this.text,
																							count : this.count
																						});
																			}
																		}
																	});

													data.results.sort(function(
															a, b) {
														if (typeof(a.count) ==  'undefined')
															return 1;
														if (typeof(b.count) ==  'undefined')
															return -1;
														if (a.count < b.count)
															return 1;
														if (a.count > b.count)
															return -1;
														return 0;
													});

													query.callback(data);
												}
											});

							$("div.select-drop").on("click", "button",
									function(event) {
										alert("Clicked the button!");
									});

							

							$scope.notfound = false;
							$scope.phrase = $rootScope.phrase;
							$rootScope.phrase = null;
//							if ($routeParams.diseaseName) {
//								$scope.diseaseName = $routeParams.diseaseName || "";
//							}
							$scope.disease_dropdown_source = [];

							if ($location.search().s) {
								doSearch();
							}
							// click on create new treatment
							$scope.newTreatmentReview = function() {
// TODO check if it's working
								$location.path("/reviewEdit/" + encodeURIComponent($scope.diseaseName)
										+ "/");
							};

							function doSearch(term) {

								if ($scope.searching === "true") return;
								$scope.searching = "true";
								
								$scope.notfound = false;
								$scope.treatments = null;
								$scope.treatmentReviewSummary = [];
								term = term || $scope.diseaseName;

								Disease
										.findByName(term)
										.then(
												function(diseases) {
													if (diseases
															&& diseases.length > 0) {
														var disease = diseases[0];
														// show results
														$scope.selectedDiseaseId = disease._id;
														Title.set(disease.name);
														$scope.busy = false;
														$scope.reachEnd = false;
														$scope.after = 1;
														
														Follow.getFollowItemId('disease',$scope.selectedDiseaseId).then(function(response){
															if(response && response.followItemId) {
																$scope.followItemId = response.followItemId;
															}
														});
														
														$timeout(function() { $scope.treatmentReviewSummaryNextPage(); }, 0);
														$scope.initUsers($scope.selectedDiseaseId);														
													} else {
														$scope.notfound = true;
														$scope.initAllUsers();
													}
													$scope.searching = "false";
												});
								
							}

							$scope.doSearch = doSearch;

							$scope.init = function() {
								Disease
										.queryNames(false)
										.then(
												function(diseases) {
													for ( var i = 0; i < diseases.length; i++) {
														var disease = diseases[i];
														preload_data
																.push({
																	id : i,
																	text : disease.name,
																	count : disease.treatmentReviewsCount
																});
													}
														
													if ($scope.phrase) {
														$("#disease_selector").select2("extSearch", $scope.phrase);
													}
													
													if($scope.diseaseName.length > 0){
														$("#disease_selector").select2("data", {id : null, text : $scope.diseaseName.toUpperCase(), count : "0"});
														doSearch($scope.diseaseName);
													}
													
												});
								
//								$scope.initAllUsers();
								
							};

							$("#disease_selector").on("change", function(e) {
								$timeout(function() {
									var newName = e.added.text;
									var encodedName = Title.urlEncode(newName);
									encodedName = encodeURIComponent(encodedName);
									$scope.diseaseName = newName;
									$scope.$apply( $location.path('/'+ encodedName));
								}, 10);
							});
							
							$scope.selectUser = function(user) {
								$location.path("/Nutzer/" + user._id);
							};
							
							$scope.doMakeReview = function(treatmentName) {
								var newPath = "/Nutzenbewertung/" + encodeURIComponent($scope.diseaseName);
								if (treatmentName) {
									newPath += "/" + encodeURIComponent(treatmentName);
								}
								$location.path(newPath);
							};

							$scope.follow = function(flag) {
								if (flag) {
									Follow.follow("disease", $scope.selectedDiseaseId, $scope.diseaseName)
										.then(function(response) { $scope.followItemId=response._id });
								} else {
									Follow.unfollow($scope.followItemId).then(function() { $scope.followItemId=null });
								}
							};

						
							// dictionary
							$scope.selectedLetter = "A";

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
						
						
						} ]);
