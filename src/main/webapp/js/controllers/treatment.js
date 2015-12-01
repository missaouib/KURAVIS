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

angular.module(appName).controller(
		'TreatmentCtrl',
		[ '$scope', '$filter','$location', '$routeParams', 'TreatmentReviewSummary', 'Alerts', 'TreatmentReview','Title', 'Follow',
				function($scope, $filter, $location, $routeParams, TreatmentReviewSummary, Alerts, TreatmentReview, Title, Follow) {
					
					Alerts.clear();
					$scope.diseaseName = Title.urlDecode($routeParams.diseaseName);
					$scope.treatmentName = Title.urlDecode($routeParams.treatmentName);
					Title.set($scope.diseaseName + " > " + $scope.treatmentName);
					$scope.summary = null;
					$scope.buttonText = null;
					$scope.reviewId = null;

					$scope.doMakeReview = function() {
						$location.path("/Nutzenbewertung/" + encodeURIComponent($scope.diseaseName) + "/" + encodeURIComponent($scope.treatmentName) + "/");
					};
					
					$('html').on('mouseup', function(e) {		// dismiss popover when user clicks outside of the popover
					    if(!$(e.target).closest('.popover').length) {
					        $('.popover').each(function(){
					           $(this.previousSibling).popover().click();
					        });
					    }	    
					});
					
					var isMinimalized = false;
					
					function showError() {
//						Alerts.error("Bewertung fehlgeschlagen für " + $scope.diseaseName + " " + $scope.treatmentName);
					}
					
					$scope.createReview = function() {
						var newPath = "/Nutzenbewertung/" + encodeURIComponent($scope.diseaseName) + "/" + encodeURIComponent($scope.treatmentName);
						if($scope.reviewId){
							$location.path(newPath).search({reviewId: $scope.reviewId});
						} else {
							$location.path(newPath);
						}
					};
										
					if($scope.diseaseName.length > 0) {
						$("#s2id_disease_selector").select2("data", {id : null, text : $scope.diseaseName, count : "0"});
					}
					
					$scope.maxAgeCount = 0;
					
					//sort
					$scope.sortOptions = ["Ranking", "Vote", "Latest update"];
					$scope.sortModel = "Ranking";
					$scope.selectedSort = "rank";
					$scope.$watch('sortModel', function(v){
						if(v == "Ranking"){
							$scope.selectedSort = "rank";
						} else if(v == "Vote"){
							$scope.selectedSort = "vote";
						} else if(v == "Latest update"){
							$scope.selectedSort = "last_update";
						} else {
							$scope.selectedSort = "rank";
						}
						
						$scope.busy = false;
						$scope.reachEnd = false;
						$scope.after = 1;
						$scope.reviews = [];
						$scope.treatmentReviewNextPage();
					});
					  
					//infinite scroll
					$scope.busy = false;
					$scope.reachEnd = false;
					$scope.after = 1;
					$scope.selectedDiseaseId = null;
					$scope.reviews = [];
				  
					$scope.treatmentReviewNextPage = function() {
					
						if ($scope.busy) return;
						if ($scope.summary == null) return;
						if ($scope.reachEnd) return;
				    
						$scope.busy = true;

						if($scope.reviews == null){
							$scope.reviews = [];
						}
				    
						TreatmentReview.findByDiseaseIdTreatmentIdPageNumberPageSize($scope.summary.disease._id, $scope.summary.treatment._id, $scope.after, 10, $scope.selectedSort)
						.then(function(data) {
													
							for (var i = 0; i < data.length; i++) {
								if($scope.reviews == null) {
									$scope.reviews = [];
								}
								$scope.reviews.push(data[i]);
							}
							if(data.length == 0){
								$scope.reachEnd = true;
							}
							
							if($scope.reviews.length == 0){
								$scope.notfound = true;
							}
							
							$scope.after++;
						    $scope.busy = false;
							
						}, function() {
							showError();
						});
					};
					
					$scope.char1data = true;
					$scope.char2data = true;
					$scope.charGenderData = true;
					$scope.char3data = true;
					
					$scope.costsStatisticsDataAvailable = true;
					$scope.treatmentDurationStatisticsDataAvailable = true;
					
					$scope.numberOfRatings = 0;
					$scope.numberOfReviews = 0;
					var ratingData = [];
					$scope.genderData = {};
					var sideEffectsCount = 0;
					var sideEffectsCategories = [];
					var sideEffectsData = [];
					var reviewsCount = 0;
					var severities = {};
					var severityData = [];
					var severityNoneCount = 0;
					var ageWomanData = [];
					var ageManData = [];
					
					var costsStatisticsData = [];
					var treatmentDurationStatisticsData = [];

					//rating chart popover
					$("#popover1").popover({placement:'left'});
					$("#popover2").popover({placement:'bottom'});
					
					//charts
					function doCHarts() {
						//rating
						$('#infographic11 .chart, #infographic12 .chart').each(function(){
							$(this).highcharts({
							colors: ['#d24e43'],
							chart: {
								type: 'bar',
//				                margin: [ 20, 0, 20, 20],
				                backgroundColor:'transparent',
				                
				            },
				            credits: {
				                enabled: false
				            },
				            title: {
				                text: ''
				            },
				            xAxis: {
				                categories: [
				                    'Sehr gut',
				                    'Gut',
				                    'Befriedigend',
				                    'Mangelhaft',
				                    'Ungenügend'
				                ],
				                   
				                //lineWidth: 0,
				                
				                minorGridLineWidth: 0,
				                lineColor: 'transparent',     
				                labels: {
				                    enabled: true,
				                    style: {
				                        fontSize: '11px',
				                        fontFamily: 'Verdana, sans-serif',
				                        color: '#000',
				                        
				                    },
				                    
				                    
				                },
				                minorTickLength: 0,
				                tickLength: 0
				            },
				            yAxis: {
				                min: 0,
				                title: {
				                	enabled: false
				                },
				                labels: {
				                    enabled: false
				                },
				                gridLineColor: 'transparent'
				            },
				            plotOptions: {
				                bar: {
				                    dataLabels: {
				                        enabled: true
				                    }
				                }
				            },
				            legend: {
				                enabled: false
				            },
				            tooltip: {
				            	enabled: false,
				            	followPointer: false
				            },
				            series: [{
				                name: 'Rating',
				                data: ratingData,
				                dataLabels: {
				                    enabled: true,
				                    color: '#666',
				                    align: 'left',
				                    x: 5,
				                    //y: -0.5,
				                    formatter: function() {
				                        return  this.y;
				                    },
				                    /*
				                    style: {
				                        fontSize: '13px',
				                        fontFamily: 'Verdana, sans-serif',
				                  
				                    }*/
				                },
				                pointWidth: 16,
				                minPointLength: 2,
				                pointPadding: 0
				            }]
					    });
						});
						
						// costs
						$('#infographic41 .chart, #infographic42 .chart').each(function(){						
							$(this).highcharts({
							colors: ['#d24e43'],
							chart: {
								type: 'column',
//				                margin: [ 20, 0, 20, 20],
				                backgroundColor:'transparent',
				                zoomType: 'x',
				                //width: 355,
				                //height: null
				                
				            },
				            credits: {
				                enabled: false
				            },
				            title: {
				                text: ''
				            },
				            xAxis: {
				                categories: [
				                    '< €25',
				                    '€ 25-50',
				                    '€ 51-100',
				                    '€ 101-200',
				                    '+ €200'
				                ],
				                minorGridLineWidth: 0,
				                lineColor: '#d24e43', 
				                minorTickLength: 0,
				                tickLength: 0
/*				                title: {
				                    enabled: false
				                },
 
				                lineWidth: 1,
				                
				                minorGridLineWidth: 0,
				                lineColor: 'transparent',     
				                labels: {
				                    enabled: true,

				                    style: {
				                        fontSize: '12px',
				                        fontFamily: 'Verdana, sans-serif',
				                        color: '#000'
				                    }
				                    
				                },
				                minorTickLength: 0,
				                tickLength: 0
*/				                
				            },
				            yAxis: {
				                min: 0,
				                title: {
				                	enabled: false
				                },
				                labels: {
				                    enabled: false
				                },
				                gridLineColor: 'transparent'
				            },
				            plotOptions: {
				                bar: {
				                    dataLabels: {
				                        enabled: true
				                    }
				                }
				            },
				            legend: {
				                enabled: false
				            },
				            tooltip: {
				            	enabled: false,
				            	followPointer: false
				            },
				            series: [{
				                name: 'Rating',
				                data: costsStatisticsData,
				                dataLabels: {
				                    enabled: true,
				                    color: '#666',
				                    align: 'center',
				                    //x: 5,
				                    y: -0.5,
				                    formatter: function() {
				                        return  this.y;
				                    },
				                    /*
				                    style: {
				                        fontSize: '13px',
				                        fontFamily: 'Verdana, sans-serif',
				                  
				                    }*/
				                },
				                pointWidth: 30,
				                minPointLength: 2,
				                pointPadding: 0
				            }]
					    });
						});
						// treatment duration
						$('#infographic51 .chart, #infographic52 .chart').each(function(){
							$(this).highcharts({
							colors: ['#d24e43'],
							chart: {
								type: 'areaspline',
//				                margin: [ 20, 0, 20, 20],
				                backgroundColor:'transparent',
				                //width: 355,
				                //height: null				                
				                
				            },
				            credits: {
				                enabled: false
				            },
				            title: {
				                text: ''
				            },
				            legend: {
				                enabled: false
				            },
				            tooltip: {
				            	enabled: false,
				            	followPointer: false
				            },
				            xAxis: {
				                categories: ['< 1 Tag', '< 1 Woche', '< 1 Monat', '< 1 Jahr', 'über 1 Jahr'],
				                minorGridLineWidth: 0,
				                lineColor: '#d24e43', 
				                minorTickLength: 0,
				                tickLength: 0
				            },
				            yAxis: {
				                min: 0,
				                title: {
				                	enabled: false
				                },
				                labels: {
				                    enabled: false
				                },
				                gridLineColor: 'transparent'
				            },
				            
				            series: [{
				                name: '',
				                data: treatmentDurationStatisticsData,
				                step: 'left',
				                dataLabels: {
				                    enabled: true,
				                    color: '#000000',
				                    align: 'center',
//				                    x: -5,
				                    y: -5,
//				                    formatter: function() {
//				                        return  this.y;
//				                    },				                    
				                },
				                pointWidth: 30,
				                minPointLength: 2,
				                pointPadding: 0
				            }]
					    });
						});						
						
						
						//side effects
						$('#infographic21 .chart, #infographic22 .chart').each(function(){
							
							var $reporting = $('#reporting');

							$(this).highcharts({
							colors: ['#d24e43', '#fdb3ad', '#f79c88', '#fc8376', '#dd6c7d'],
							chart: {
								type: 'pie',
				                margin: [ 0, 0, 0, 0],
				                backgroundColor:'transparent'
				            },
				            credits: {
				                enabled: false
				            },
				            
				            title: {
				                text: ''
				            },
				            legend: {
				                enabled: false
				            },
				            tooltip: {
				            	enabled: true,
				            	hideDelay: 10,
				            	formatter: function () {
				                    /*return '<b>' + this.point.name +'</b><br>' + this.point.data;*/
				            		
				                    if(this.point.showData){
				                    	return this.point.data;
				                    } else {
				                    	return false;
				                    }
				                }
				            },
				            plotOptions: {
				                pie: {
				                    allowPointSelect: true,
				                    cursor: 'pointer',
				                    dataLabels: {
				                        enabled: true,
				                        color: '#666',
				                        connectorColor: 'transparent',
				                        connectorPadding: -20,
				                        format: '{point.name}',
				                        /*
				                        style: {
					                        fontSize: '13px',
				                        }*/
				                    }
				                }
				            },
				            series: [{
				                name: 'Rating',
				                data: sideEffectsData,
				                dataLabels: {
				                    enabled: true,
				                    
				                },
				                /*
				                point:{
				                    events:{
				                    	click: function (event) {
				                    		$reporting.html(this.data);
				                        }
				                    }
				                } */
				            }]
					    });
						});
						
						//age
						$('#infographic31 .chart, #infographic32 .chart').each(function(){
							$(this).highcharts({
							colors: ['#d24e43', '#33627c'],
							chart: {
				                type: 'bar',
				                margin: [ 20, 80, 0, 80],
				                backgroundColor:'transparent'
				            },
				            credits: {
				                enabled: false
				            },
				            title: {
				                text: ''
				            },
				            xAxis: {
				            	categories: [ 
				                    '≥70',
				                    '60-59',
				                    '50-59',
				                    '40-49',
				                    '30-39',
				                    '19-29',
				                    '≤18'
				                ],
				                //lineWidth: 0,
				                offset: 30,
				                opposite: true,
				                minorGridLineWidth: 0,
				                lineColor: 'transparent',     
				                labels: {
				                    enabled: true,
				                    //overflow: 'justify',
				                    formatter: function(){
				                    	return this.value;
				                    },
				                    /*
				                    style: {
				                        fontSize: '13px',
				                        //fontFamily: 'Verdana, sans-serif',
				                        color: '#000',
				                        width: 100,
				                    }*/
				                },
				                minorTickLength: 0,
				                tickLength: 0
				            },
				            yAxis: {
				            	title: {
				                	enabled: false
				                },
				                labels: {
				                    enabled: false
				                },
				                gridLineColor: 'transparent',
				                min: -$scope.maxAgeCount,
				                max: $scope.maxAgeCount
				            },
				            plotOptions: {
				                bar: {
				                    dataLabels: {
				                        enabled: true
				                    }
				                },
				                series: {
				                    stacking: 'normal'
				                }
				            },
				            legend: {
				                enabled: false
				            },
/*				            tooltip: {
				            	enabled: false,
				            	followPointer: false
				            },
	*/			            series: [{
				                name: 'Weiblich',
				                data: ageWomanData,
				                dataLabels: {
				                    enabled: true,
				                    color: '#666',
				                    align: 'left',
				                    x: -20,
				                    y: -0.5,
				                    formatter: function() {
				                    	var result = Math.abs(this.y);
				                    	if(result==0) {
				                    		return null;
				                    	}
				                        return result; 
				                    },
				                    /*
				                    style: {
				                        fontSize: '13px',
				                        //fontFamily: 'Verdana, sans-serif',
				                  
				                    }*/
				                },
				                pointWidth: 15,
				                minPointLength: 2,
				                pointPadding: 0
				            }, {
				                name: 'Männlich',
				                data: ageManData,
				                dataLabels: {
				                    enabled: true,
				                    color: '#666',
				                    align: 'right',
				                    x: 20,
				                    //y: -0.5,
				                    formatter: function() {
				                    	var result = Math.abs(this.y);
				                    	/*
				                    	if(result==0) {
				                    		return null;
				                    	}
				                    	*/
				                        return result; 
				                    },
				                    /*
				                    style: {
				                        fontSize: '13px',
				                        //fontFamily: 'Verdana, sans-serif',
				                  
				                    }*/
				                },
				                pointWidth: 16,
				                minPointLength: 2,
				                pointPadding: 0,
				                groupPadding: 200
				            }]
				            
						}, function(chart) {
					        /*
					        var box = chart.plotWidth;
					        chart.xAxis[0].update({
					           // offset: -box*0.5
					        });
					        */
					    });		
						});
						
					}
					
					TreatmentReviewSummary.findBy($scope.diseaseName, $scope.treatmentName).then(function(result) {
						
						$scope.char1data = false;
						$scope.char2data = false;
						$scope.charGenderData = false;
						$scope.char3data = false;
						
						$scope.costsStatisticsDataAvailable = false;
						$scope.treatmentDurationStatisticsDataAvailable = false;
						
						if (!angular.isArray(result)) {
							showError();
							return;
						}
						var summary = result[0];						
						$scope.summary = summary;
						if(summary.rating) {
							$scope.rating = parseFloat(summary.rating);
						}
						Follow.getFollowItemId('treatmentreviewsummary',$scope.summary._id).then(function(response){
							if(response && response.followItemId) {
								$scope.followItemId = response.followItemId;
							}
						});
						TreatmentReview.existForUser($scope.summary.disease._id, $scope.summary.treatment._id).then(function(result) {
							if(result.exists == true){
								$scope.reviewId = result.reviewId;
								$scope.buttonText = "Bearbeiten";
							} else{
								$scope.buttonText = "Bewerten";
							}
						});
						
						reviewsCount = summary.reviewsCount;
						$scope.numberOfReviews = reviewsCount;
						var rating1 = 0;
						var rating2 = 0;
						var rating3 = 0;
						var rating4 = 0;
						var rating5 = 0;
						
						//ratings
						if (summary.ratings && summary.ratings.length > 0) {
							$scope.char1data = true;
						}
						if ($scope.char1data) {
							for (var i = 0; i < summary.ratings.length; i++) {
								var rating = summary.ratings[i];
								var ratingName = $filter('ratingSingleValue')(rating.name);
								var ratingCount = rating.count;
								$scope.numberOfRatings += ratingCount;
								switch (parseInt(ratingName)) {
								case 1:
									rating1 = ratingCount;
									break;
								case 2:
									rating2 = ratingCount;
									break;
								case 3:
									rating3 = ratingCount;
									break;
								case 4:
									rating4 = ratingCount;
									break;
								case 5:
									rating5 = ratingCount;
									break;
								} 
							}
						}
						ratingData = [rating5, rating4, rating3, rating2, rating1];
						
						//sideEffect
						if (summary.sideEffects.length > 0) {
							$scope.char2data = true;
						}
						severityNoneCount = reviewsCount;
						for (var i = 0; i < Math.min(summary.sideEffects.length, 10); i++) {
							
							var sideEffect = summary.sideEffects[i];
							//var severity = sideEffect.counts;
							//var sideEffectCount = severity.count;
							var sideEffectName = sideEffect.name;
							var sideEffectCounts = sideEffect.counts;
							
							
							var sideEffectShowData = true;
							var allSideEffectSeverityCount = 0;
							for (var j = 0; j < sideEffectCounts.length; j++) {
								var severity = sideEffectCounts[j];
								var severityCount = severity.count;
								allSideEffectSeverityCount += severityCount;
							}
							var sideEffectCount = 0;
							var severityDataString = "";
							for (var j = 0; j < sideEffectCounts.length; j++) {
								var severity = sideEffectCounts[j];
								var severityName = severity.name;
								var severityCount = severity.count;
								
								
								sideEffectCount += severityCount;
								
								var existingSeverity = severities[severityName];
								if(existingSeverity !== undefined){
									severities[severityName] = severityCount + existingSeverity;
								} else {
									severities[severityName] = severityCount;
								}
								if(severityDataString.length > 0) {
									severityDataString += "<br>"
								}
								var percent = ((severityCount/allSideEffectSeverityCount) * 100).toFixed(1).toString()+"%";
								severityDataString += "<p>" + $filter('getSeverityName')(severityName) + ": <b>" + percent + "</b></p>";
								
							}
							
							sideEffectsCategories.push(sideEffectName);
							if(sideEffect.noSideEffect == true) {
								sideEffectShowData = false;
								sideEffectName = "Keine Nebenwirkungen";
							}
							sideEffectsData.push({name:sideEffectName, y:sideEffectCount, showData: sideEffectShowData, data:severityDataString});
							sideEffectsCount += sideEffectCount;
							severityNoneCount -= sideEffectCount;
							
						}
						
						//gender
						
						var maleCount = 0;
						var femaleCount = 0;
						var unknownCount = 0;
						
						if(summary.genderStatistics.male != null){
							$scope.charGenderData = true;
							maleCount = summary.genderStatistics.male;
						}
						if(summary.genderStatistics.female != null){
							$scope.charGenderData = true;
							femaleCount = summary.genderStatistics.female;
						}
						if(summary.genderStatistics.unknown != null){
							$scope.charGenderData = true;
							unknownCount = summary.genderStatistics.unknown;
						}
												
						var minScale = 0;
						
						var allCount = maleCount + femaleCount + unknownCount;
						$scope.manScale = (maleCount* 200) / allCount;
						$scope.womanScale = (femaleCount * 200) /allCount;
												
						$scope.genderData.man = ((maleCount/allCount) * 100).toFixed(0).toString();
						$scope.genderData.woman = ((femaleCount/allCount) * 100).toFixed(0).toString();
						$scope.genderData.unknown = ((unknownCount/allCount) * 100).toFixed(0).toString();
												
						if(maleCount == 0){
							$scope.manScale = minScale;
							$scope.genderData.man = "0";
						}
						if(femaleCount == 0){
							$scope.womanScale = minScale;
							$scope.genderData.woman = "0";
						}
						if(unknownCount == 0){
							$scope.genderData.unknown = "0";
						}
						
/*						$('#womanGenderImage1,#womanGenderImage2').css({
						    "-webkit-transform":"scale("+womanScale+")",
						    "-moz-transform":"scale("+womanScale+")",
						    "-ms-transform":"scale("+womanScale+")",
						    "-o-transform":"scale("+womanScale+")",
						    "transform":"scale("+womanScale+")",
						});
						
						$('#manGenderImage1,#manGenderImage2').css({
						    "-webkit-transform":"scale("+manScale+")",
						    "-moz-transform":"scale("+manScale+")",
						    "-ms-transform":"scale("+manScale+")",
						    "-o-transform":"scale("+manScale+")",
						    "transform":"scale("+manScale+")",
						});
*/
						//age
						var ageMap = {};
						/*
						summary.ageStatistics.female = [
											{
												name:1974,
												count:4
											},
											{
												name:1934,
												count:1
											},
											{
												name:1985,
												count:12
											},
											{
												name:1992,
												count:11
											},
										];
					
						summary.ageStatistics.male = [
														{
															name:1974,
															count:3
														},
														{
															name:1934,
															count:5
														},
														{
															name:1985,
															count:12
														}
													];
						
						*/
						
						var age7 = 0; //0-18
						var age6 = 0; //18-30
						var age5 = 0; //30-40
						var age4 = 0; //40-50
						var age3 = 0; //50-60
						var age2 = 0; //60-70
						var age1 = 0; //70+
										
						if(summary.ageStatistics.female != undefined || summary.ageStatistics.male != undefined) {
							$scope.char3data = true;
						}
						
						if(summary.ageStatistics.female != undefined){
							for (var i = 0; i < summary.ageStatistics.female.length; i++) {
								var age = summary.ageStatistics.female[i];
								var ageKey = age.name;
								var ageCount = age.count;
								//ageMap
								var existingAge = ageMap[ageKey];
								if(existingAge !== undefined){
									ageMap[ageKey] = ageCount + existingAge;
								} else {
									ageMap[ageKey] = ageCount;
								}
							}
							
							for (var k in ageMap) {
								var actualYear = parseInt(new Date().getFullYear());
								var yearOfBird = parseInt(k);
								var yearsOld = actualYear - yearOfBird;
								//console.log(yearsOld);
								
								if(yearsOld >= 70){
									age1 -= ageMap[k];
								} else if(yearsOld >= 60){
									age2 -= ageMap[k];
								} else if(yearsOld >= 50){
									age3 -= ageMap[k];
								} else if(yearsOld >= 40){
									age4 -= ageMap[k];
								} else if(yearsOld >= 30){
									age5 -= ageMap[k];
								} else if(yearsOld >= 18){
									age6 -= ageMap[k];
								} else {
									age7 -= ageMap[k];
								}
							}
						}
						
						ageWomanData = [age1, age2, age3, age4, age5, age6, age7];
						$scope.maxAgeCount = Math.max($scope.maxAgeCount, Math.abs(age1), Math.abs(age2), Math.abs(age3), Math.abs(age4), Math.abs(age5), Math.abs(age6), Math.abs(age7));
						
						ageMap = {};
						
						age7 = 0; //0-18
						age6 = 0; //18-30
						age5 = 0; //30-40
						age4 = 0; //40-50
						age3 = 0; //50-60
						age2 = 0; //60-70
						age1 = 0; //70+
									
						if(summary.ageStatistics.male != undefined){
							for (var i = 0; i < summary.ageStatistics.male.length; i++) {
								var age = summary.ageStatistics.male[i];
								var ageKey = age.name;
								var ageCount = age.count;
								//ageMap
								var existingAge = ageMap[ageKey];
								if(existingAge !== undefined){
									ageMap[ageKey] = ageCount + existingAge;
								} else {
									ageMap[ageKey] = ageCount;
								}
							}
							
							for (var k in ageMap) {
								var actualYear = parseInt(new Date().getFullYear());
								var yearOfBird = parseInt(k);
								var yearsOld = actualYear - yearOfBird;
								//console.log(yearsOld);
								
								if(yearsOld >= 70){
									age1 += ageMap[k];
								} else if(yearsOld >= 60){
									age2 += ageMap[k];
								} else if(yearsOld >= 50){
									age3 += ageMap[k];
								} else if(yearsOld >= 40){
									age4 += ageMap[k];
								} else if(yearsOld >= 30){
									age5 += ageMap[k];
								} else if(yearsOld >= 18){
									age6 += ageMap[k];
								} else {
									age7 += ageMap[k];
								}
							}
						}
						
						
						ageManData = [age1, age2, age3, age4, age5, age6, age7];
						$scope.maxAgeCount = Math.max($scope.maxAgeCount, age1, age2, age3, age4, age5, age6, age7);
						
						
						
						if(summary.costsStatistics) {
							costsStatisticsData = [];
							for (var i = 0; i < summary.costsStatistics.length; i++) {
								var value = summary.costsStatistics[i];
								
								if ((value.count > 0) && !$scope.costsStatisticsDataAvailable) {
									$scope.costsStatisticsDataAvailable = true;
								}
								
								costsStatisticsData.push(value.count);
							}
						}
						
						if(summary.treatmentDurationStatistics) {
							treatmentDurationStatisticsData = [];
							for (var i = 0; i < summary.treatmentDurationStatistics.length; i++) {
								var value = summary.treatmentDurationStatistics[i];
								if ((value.count > 0) && !$scope.treatmentDurationStatisticsDataAvailable) {
									$scope.treatmentDurationStatisticsDataAvailable = true;
								}
								treatmentDurationStatisticsData.push(value.count);
							}
						}

						
						doCHarts();
						
						$scope.treatmentReviewNextPage();
						/*
						TreatmentReview.findBy(summary.disease._id, summary.treatment._id).then(function(result) {
							$scope.reviews = result;							
							
						}, function() {
							showError();
						});*/
					}, function(error) {
						showError();
					});
					
					var infographics = document.querySelector( '#infographics');
					var infographicsHelper = document.querySelector( '#infographicsHelper');
					
					$scope.hideHeader = function(scrolled) {
						if(scrolled == true){
							
							if(isMinimalized == true) {
								return;
							}
							classie.remove( infographics, 'maximalize' );
							classie.add( infographics, 'minimalize' );
							
							isMinimalized = true;
							//doCHarts();
						} else {
							
							if(isMinimalized == false) {
								return;
							}
							
							classie.remove( infographics, 'minimalize' );
							classie.add( infographics, 'maximalize' );
							
							isMinimalized = false;
							//doCHarts();
						}
					};
					
					$scope.follow = function(flag) {
						if (flag) {
							Follow.follow("treatmentreviewsummary", $scope.summary._id, $scope.treatmentName)
								.then(function(response) { 
									$scope.followItemId=response._id
								});
						} else {
							Follow.unfollow($scope.followItemId).then(function() { $scope.followItemId=null });
						}
					};
					
				} ]);
	
