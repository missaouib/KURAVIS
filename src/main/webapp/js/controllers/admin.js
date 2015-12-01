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
		'AdminCtrl',
		[ 'Loading', '$scope', '$rootScope', 'User', 'Alerts', 'Disease', 'Treatment', 'SideEffect', 'Title', 'Fraud', '$routeParams', '$q', '$timeout', 'Subscription',
		  'TreatmentReviewComment', 'TreatmentReview', 'TreatmentReviewSummary','OptionList',
				function(Loading, $scope, $rootScope, User, Alerts, Disease, Treatment, SideEffect, Title, Fraud, $routeParams, $q, $timeout, Subscription, TreatmentReviewComment, TreatmentReview, TreatmentReviewSummary, OptionList) {

					Title.set(Title.defaultTitle());
					Alerts.clear();
					$scope.tab = 0;
					if ($routeParams.id) {
						$scope.tab = parseInt($routeParams.id);
					}

					var cmpDates = function(a,b){
						var x = a.constructor === Date;
						return (a>b)-(a<b);
					};

					var cmpDates1 = function(a,b){
						return -1 * cmpDatesNormal(a, b);
					};
					
					$scope.delColumnTemplate = '<button class="btn btn-danger bdelete" ng-click="deleteRow(row)" >Delete</button>';

					$scope.columnDefsAll = {
						cols : [ {
							field : '_id',
							displayName : 'Id',
							enableCellEdit : false
						}, {
							field : 'name',
							displayName : 'Name',
							enableCellEdit : true
						}, {
							displayName : '',
							cellTemplate : $scope.delColumnTemplate
						} ]
					};

					$scope.columnDefsInv = {
						cols : [ {
							field : 'email',
							displayName : 'E-mail',
							enableCellEdit : false
						}, {
							displayName : '',
							cellTemplate : '<button class="btn btn-danger" ng-click="resendInvitation(row)" >Resend</button>'
						} ]
					};

					$scope.columnDefsUser = {
						cols : [ {
							field : '_id',
							displayName : 'Id',
							enableCellEdit : false
						},{
							displayName : 'User Name',
							cellTemplate: '<a target="_blank" href="/Nutzer/{{row.entity[\'_id\']}}">{{row.getProperty(\'name\')}}</a>'
						}, {
							displayName : '',
							cellTemplate : $scope.delColumnTemplate
						} ]
					};
					$scope.columnDefsSubscription = {
							cols : [ {
								field : 'createdOn',
								displayName : 'Date',
								cellTemplate: '<span>{{row.entity[\'createdOn\'] | toLocaleString}}</span>',
								sortFn: cmpDates
							}, {
								field : 'email',
								displayName : 'Email',
								enableCellEdit : false
							} ]
						};
					$scope.columnDefsFraud = {
						cols : [ {
							field : 'dateRaw',
							displayName : 'Date reported',
							enableCellEdit : false,
							cellTemplate: '<p> {{row.getProperty(\'date\')}}</p>',
							sortFn: cmpDates
						}, {
							field : 'note',
							displayName : 'Note',
							enableCellEdit : false
						}, {
							field : 'entityType',
							displayName : 'Type',
							enableCellEdit : false,
							cellTemplate: '<p> {{row.getProperty(\'entityType\') | fraudReportEntityType}}</p>'
						}, {
							displayName : 'Category',
							cellTemplate: '<p> {{row.getProperty(\'fraudReportCategory\') | localizedReportReason}}</p>'
						}, {
							displayName : 'Text',
							cellTemplate: '<a target="_blank" href="/{{row.entity[\'entityTarget\']}}">{{row.getProperty(\'entityName\')}}</a>'
						}, {
							displayName : 'User Created Entity',
							cellTemplate: '<a target="_blank" href="/Nutzer/{{row.entity[\'createdByUserId\']}}">{{row.getProperty(\'createdByUser\')}}</a>'
						}, {
							displayName : 'User Reported',
							cellTemplate: '<a target="_blank" href="/Nutzer/{{row.entity[\'userId\']}}">{{row.getProperty(\'user\')}}</a>'
						}, {
							displayName : 'Delete Report',
							cellTemplate : '<button class="btn btn-danger bdelete" ng-click="deleteReport(row)" >Delete</button>'
						},  {
							displayName : 'Delete Reported Item',
							cellTemplate : '<button class="btn btn-danger bdelete" ng-show="row.entity[\'entityType\']" ng-click="deleteReportItem(row)" >Delete</button>'
						}, {
							displayName : 'Delete User Created Entity',
							cellTemplate : '<button class="btn btn-danger bdelete" ng-click="deleteUser(row)" >Delete user</button>'
						} ]
					};

					$scope.columnOptions = {
							cols : [ {
								field : '_id',
								displayName : 'Id',
								enableCellEdit : false
							}, {
								field : 'typeName',
								displayName : 'Type',
								enableCellEdit : false
							},{
								field : 'name',
								displayName : 'Name',
								enableCellEdit : true
							},{
								isplayName : '',
								cellTemplate : $scope.delColumnTemplate
							} ]
						};
					
					$scope.columnDisease = {
							cols : [ {
								field : '_id',
								displayName : 'Id',
								enableCellEdit : false
							}, {
								field : 'name',
								displayName : 'Name',
								enableCellEdit : true
							},{
								field : 'summariesCount',
								displayName : 'different user treatments count',
								enableCellEdit : false
							},{
								field : 'sugestions',
								displayName : 'Suggestions',
								cellTemplate :  '<button ng-hide="editSuggestions" ng-click="startEditSuggestions(row.entity)">Edit suggestions</button> &nbsp; <span ng-repeat="treatment in row.entity.suggestedTreatments">{{treatment.name}}&nbsp;</span>'
							},
							{
								displayName : '',
								cellTemplate : $scope.delColumnTemplate
							} ]
						};
					$scope.startEditSuggestions = function(entity){
						$scope.editSuggestions = entity;
					};
					
					$scope.addSuggestion = function(entity){
						//check for existing object
						if($scope.selectedTreatment.originalObject && entity.suggestedTreatments){
							var id = $scope.selectedTreatment.originalObject._id;
							for(var i =0;i<entity.suggestedTreatments.length;i++){
								var o = entity.suggestedTreatments[i];
								if(o._id==id){
									return;//same object
								}
							}
						}
						
						var obj = {
							    disease:{
							        _id:entity._id
							    },
							    treatment:{
							        _id:$scope.selectedTreatment.originalObject._id
							    },
							    suggestion:true
							};
						TreatmentReviewSummary.create(obj).then(function(data){
							if(!entity.suggestedTreatments){
								entity.suggestedTreatments = [];
							}
							var tr = {
									_id: $scope.selectedTreatment.originalObject._id,
									name: $scope.selectedTreatment.originalObject.name,
									treatmentReviewSummaryId: data._id
							};
							entity.suggestedTreatments.push(tr);
						}, function(){
							Alerts.error("Error create suggestion");
						});
					};
					
					$scope.removeSuggestion = function(entity, index, treatmentReviewSummaryId){
						TreatmentReviewSummary.remove(treatmentReviewSummaryId).then(function(){
							entity.suggestedTreatments.splice(index, 1);
						}, function(){
							Alerts.error("Error delete suggestion");
						});
					};
					
					$scope.optionTypes = [{'physiotherapie':'Physiotherapie'},
					                      {'psychotherapy':'Psychotherapy'},
					                      {'treatmenttype':'Treatement Type'},
					                      {'unit':'Unit'}];
					
					$scope.optionTypeName = function(obj){
						for(var otype in obj){
							return obj[otype];
						}
					};
					$scope.optionTypeValue = function(obj){
						for(var otype in obj){
							return otype;
						}
					};
					
					function optionsLoader(){
						var defered = $q.defer();
						var aggregatedData = [];

						var promises = [];
						
						for(var i=0;i<$scope.optionTypes.length;i++){
							var obj = $scope.optionTypes[i];
							(function(obj){ //mp note: this is IIFE (immediately-invoked function expression)
								for(var otype in obj){
									var p = OptionList.query(otype);
									promises.push(p);
									p.then(function(results){
										angular.forEach(results, function (result) {
											result.type = otype;
											result.typeName = obj[otype];
						                    aggregatedData.push(result);
						                });
									});
									break;
								}
							})(obj);
						}

						$q.all(promises).then(function(){
							defered.resolve(aggregatedData);
						});
						return defered.promise;
					};
					
					$scope.columnDefs = $scope.columnDefsAll;

					$scope.tabs = [ {
						name : 'Disease',
						id : 'disease',
						service : Disease,
						add : true,
						columns : 'columnDisease',
						loader: function(){
							var defered = $q.defer();
							
							Treatment.query().then(function(treatments){
								$scope.allTreatments = treatments;
								Disease.queryById("allsuggestions").then(function(data){
									defered.resolve(data);
								});
							});
							
							return defered.promise;
						}
					}, {
						name : 'Treatment',
						id : 'treatment',
						service : Treatment,
						add : true,
						columns : 'columnDefsAll'
					}, {
						name : 'Side Effect',
						id : 'sideEffect',
						service : SideEffect,
						add : true,
						columns : 'columnDefsAll'
					}, {
						name : 'Fraud',
						id : 'fraud',
						service : Fraud,
						columns : 'columnDefsFraud'
					}, {
						name : 'User',
						id : 'user',
						service : User,
						columns : 'columnDefsUser'
					}, {
						name : 'Invitiation',
						id : 'invitation',
						service : null,
						loader : function() {
							var defered = $q.defer();
							if ($rootScope.userLogged) {
								User.getInvitations($rootScope.userLogged._id).then(function(data) {
									defered.resolve(data.invitations);
								}, function(err) {
									defered.reject(err);
								});
							} else {
								defered.resolve([]);
							}
							return defered.promise;
						},
						inv : true,
						columns : 'columnDefsInv'
					}, {
						name : 'Subscription',
						id : 'subscription',
						service : Subscription,
						columns : 'columnDefsSubscription'
					} ,{
						name : 'Options',
						id : 'options',
						service : null,
						loader : optionsLoader,
						add : true,
						optionTypes: true,
						columns : 'columnOptions',
						addFunc: function(){
							if($scope.listType && $scope.name){
								OptionList.create($scope.listType, {name: $scope.name}).then(function() {
									$scope.name = "";
									$scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);
								}, function(arg) {
									Alerts.error("Error create new entity! " + arg.data.message);
								});
							}
						},
						deleteFunc: function(entity){
							OptionList.remove(entity.type, entity._id).then(function() {
								Alerts.success("Item deleted");
								$scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);						
							}, function() {
								Alerts.error("Error delete item");
							});			
						},
						updateFunc: function(entity){
							var data = '{"$set": {"name":"' + entity.name + '"} }';
							OptionList.update(entity.type, entity._id, data).then(function() {
							}, function(arg) {
								Alerts.error("Error update entity! " + arg.data.message);
								$scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);
							});
						}
					}];

					var getService = function() {
						return $scope.tabs[$scope.tab].service;
					};

					$scope.filterOptions = {
						filterText : "",
						useExternalFilter : true
					};
					$scope.totalServerItems = 0;
					$scope.pagingOptions = {
						pageSizes : [ 250, 500, 1000 ],
						pageSize : 250,
						currentPage : 1
					};
					$scope.setPagingData = function(data, page, pageSize) {
						var pagedData = data.slice((page - 1) * pageSize, page * pageSize);
						$scope.myData = pagedData;
						$scope.totalServerItems = data.length;
						if (!$scope.$$phase) {
							$scope.$apply();
						}
						;
					};
					$scope.getPagedDataAsync = function(pageSize, page, searchText) {
						var service = getService();
						var loader = $scope.tabs[$scope.tab].loader;

						if (loader) {
							Loading.start();
							loader().then(function(result) {
								$scope.setPagingData(result, page, pageSize);
							}).then(function() {
								Loading.stop();
							});
						} else {
							if (service) {
								Loading.start();
								service.query().then(function(result) {
									$scope.setPagingData(result, page, pageSize);
								}).then(function() {
									Loading.stop();
								});
							} else {
								$scope.setPagingData([], page, pageSize);
							}
						}
					};
					$scope.$watch('pagingOptions', function(newVal, oldVal) {
						if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage) {
							$scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);
						}
					}, true);
					$scope.$watch('filterOptions', function(newVal, oldVal) {
						if (newVal !== oldVal) {
							$scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);
						}
					}, true);

					$scope.gridOptions = {
						data : 'myData',
						enablePaging : true,
						showFooter : true,
						totalServerItems : 'totalServerItems',
						pagingOptions : $scope.pagingOptions,
						filterOptions : $scope.filterOptions,
						enableCellSelection : true,
						enableRowSelection : false,
						enableCellEdit : true,
						columnDefs : 'columnDefs.cols'
					};

					$scope.columnDefs = angular.copy($scope[$scope.tabs[$scope.tab].columns], {});
					$scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);

					$scope.$on('userLogged', function() { // init the data later, for the case when userLogged will be not available at the beginning (network
															// latency)
						$scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);
					});

					function remove(array, property, value) {
						$.each(array, function(index, result) {
							if (result && result[property] == value) {
								array.splice(index, 1);
							}
						});
					};

					$scope.deleteRow = function(row) {
						if (!confirm("Sind Sie sicher?")) {
							return;
						}

						var service = getService();
						if (service) {
							service.remove(row.entity._id).then(function() {
								remove($scope.myData, "_id", row.entity._id);
							}, function() {
								Alerts.error("Error delete entity");
							});
						}else {
							var deleteFunc = $scope.tabs[$scope.tab].deleteFunc;
							if(deleteFunc){
								deleteFunc(row.entity);
							}
						}
					};

					$scope.deleteReport = function(row) {
						if (!confirm("Sind Sie sicher?")) {
							return;
						}

						Fraud.remove(row.entity._id).then(function() {
							remove($scope.myData, "_id", row.entity._id);
						}, function() {
							Alerts.error("Error delete entity");
						});
					};
					
					$scope.deleteReportItem = function(row) {
						if (!confirm("Sind Sie sicher?")) {
							return;
						}
						
						var etype = row.entity.entityType;
						var result = null;
						if(etype=='treatmentreviewevent'){
							result = TreatmentReviewComment.deleteComment(row.entity.entityId);
						}else if(etype=='treatmentreview'){
							result = TreatmentReview.deleteReview(row.entity.entityId);
						}

						if(result){
							result.then(function() {
								Alerts.success("Item deleted");
								$scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);						
							}, function() {
								Alerts.error("Error delete item");
							});							
						}
					};
					
					$scope.deleteUser = function(row) {
						if (!confirm("Sind Sie sicher?")) {
							return;
						}

						User.service.remove(row.entity.userId).then(function() {
							remove($scope.myData, "createdByUserId", row.entity.createdByUserId);
						}, function() {
							Alerts.error("Error delete entity");
						});
					};

					$scope.$on('ngGridEventEndCellEdit', function(evt) {
						var obj = evt.targetScope.row.entity;
						var service = getService();
						if (service) {
							var data = '{"$set": {"name":"' + obj.name + '"} }';
							service.update(data, obj._id).then(function() {
							}, function(arg) {
								Alerts.error("Error update entity! " + arg.data.message);
								$scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);
							});
						}else{
							var updateFunc = $scope.tabs[$scope.tab].updateFunc;
							if(updateFunc){
								updateFunc(obj);
							}							
						}
					});

					$scope.addNew = function() {
						var service = getService();
						if (service) {
							service.create({
								name : $scope.name
							}).then(function() {
								$scope.name = "";
								$scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);
							}, function(arg) {
								Alerts.error("Error create new entity! " + arg.data.message);
							});
						}else{
							var addFunc = $scope.tabs[$scope.tab].addFunc;
							if(addFunc){
								addFunc();
							}
						}
					};

					function sendInternal(emailAddress) {
						User.sendInvitation(emailAddress).then(function(result) {
							$scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);
							iosOverlay({
								text : "Erneut versandt",
								duration : 2e3,
								icon : "assets/iosOverlay/img/check.png"
							});
						}, function(data, result) {
							Alerts.error(data.toSource());
							Alerts.error(status);
						});
					}

					$scope.sendInvitation = function() {
						sendInternal($scope.email);
					};

					$scope.resendInvitation = function(row) {
						sendInternal(row.entity.email);
					};

				} ]);
