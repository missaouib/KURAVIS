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

angular.module(appName).controller('DiaryCtrl',
[ '$scope', '$rootScope', '$location', '$timeout', '$routeParams', 'User', 'Event', 'Alerts', '$upload','eventTypeOptions',
  function($scope, $rootScope, $location, $timeout, $routeParams, User, Event, Alerts, $upload, eventTypeOptions) {
	
	Alerts.clear();
	$scope.user = null;
	$scope.userId = null;
	$scope.year = null;
	$scope.month = null;
	$scope.day = null;
	$scope.date = null;
	$scope.eventTypeOptions = eventTypeOptions;
	
	$scope.isYearlyView = false;
	$scope.isMonthlyView = false;
	$scope.isDailyView = false;
	
	$scope.eventsSummary = [];
	
	$scope.selectedDate = new Date();	
	$scope.day = $scope.selectedDate.getDate();
	$scope.month = $scope.selectedDate.getMonth()+1; //January is 0!
	$scope.year = $scope.selectedDate.getFullYear();
	
	$scope.userId = $routeParams.userId;
	
	$scope.newWeight = null;
	$scope.selectedDate = null;
	
	$scope.newTitle = null;
	$scope.newText = null;
	$scope.maxLengthText = 5000;

	$scope.isDiaryEditable = false;
	document.isDiaryEditable = $scope.isDiaryEditable;

	$scope.initLoggedUser = function() {
		var loggedUser = $rootScope.userLogged;
		if ((loggedUser != null) && (loggedUser != 'undefined')) {
			if(($scope.userId == loggedUser._id) && (loggedUser.state != 'unverified')) {
				$scope.isDiaryEditable = true;
				document.isDiaryEditable = $scope.isDiaryEditable;
				//first time visit for my diary
				  var cookieName = "firstTimeDiary3";
				  var cookies = document.cookie; 
				  if(cookies && cookies.indexOf(cookieName) < 0){
					  $scope.firstTimeInfo = true;
					  
					  var d = new Date();
					  d.setTime(d.getTime()+(3650*24*60*60*1000));// 10 years
					  var expires = "expires="+d.toGMTString();
					  document.cookie = cookieName + "=1" + "; " + expires;
				  }
			}
		}
	};
	
	$scope.$on('userLogged', function() {	
		$scope.initLoggedUser();
	});
	
	if ($routeParams.year) {
		$scope.year = $routeParams.year;
	}
	if ($routeParams.month) {
		$scope.month = $routeParams.month;
	}
	if ($routeParams.day) {
		$scope.day = $routeParams.day;
	}
	
	Date.prototype.yyyymmdd = function() {        
        var yyyy = this.getFullYear().toString();                                    
        var mm = (this.getMonth()+1).toString(); // getMonth() is zero-based         
        var dd  = this.getDate().toString();             
                            
        return yyyy + '-' + (mm[1]?mm:"0"+mm[0]) + '-' + (dd[1]?dd:"0"+dd[0]);
	}; 
   
	$scope.createDateFromDateParts = function() {
	   var date = new Date();
	   date.setYear($scope.year);
	   date.setMonth($scope.month - 1);
	   date.setDate($scope.day);
	   return date;
	};
   
	User.queryById($scope.userId).then(function(result) {
		$scope.user = result;
	}, function() {
		
	});
	

	$scope.viewTypeDay = "day";
	$scope.viewTypeMonth = "month";
	$scope.viewTypeYear = "year";
	//$rootScope.selectedView = $scope.viewTypeYear;
	$scope.isDayViewSelected = function() {
		return ($rootScope.selectedView == $scope.viewTypeDay);
	};
	$scope.isMonthViewSelected = function() {
		return ($rootScope.selectedView == $scope.viewTypeMonth);
	};
	$scope.isYearViewSelected = function() {
		return ($rootScope.selectedView == $scope.viewTypeYear);
	};
	
	if(!$rootScope.selectedView){
		if($routeParams.year && $routeParams.month && $routeParams.day){
			$scope.selectedDate = new Date($scope.year, $scope.month - 1, $scope.day);
			$rootScope.selectedView = $scope.viewTypeDay;
		} else if($routeParams.year && $routeParams.month){		
			$scope.selectedDate = new Date($scope.year, $scope.month - 1, 1);
			$rootScope.selectedView = $scope.viewTypeMonth;
		} else if($routeParams.year){
			$scope.selectedDate = new Date($scope.year, 0, 1);
			$rootScope.selectedView = $scope.viewTypeYear;		
		} else {
			$rootScope.selectedView = $scope.viewTypeYear;
		}
	}
	//Diary
	var calendar;
	document.isDiaryEditable = $scope.isDiaryEditable;
	
	$('.datepicker').datepicker({ 
		format: "dd-mm-yyyy", 
		language: "de",
		autoclose: true,
		todayHighlight: true,
		weekStart: 1,
	});
	
	$scope.reloadCalendarData = function() {
		var calendarView = null;
		if (calendar) {
			$rootScope.selectedView = calendar.getView();
		}
		$scope.selectedDate = $scope.createDateFromDateParts();					
		Event.findByUserByStartByEnd($scope.userId, 0, 88881415176945000, 0, 20000).then(function(result) {
				var options = {
						//events_source: './assets/bootstrap-calendar-master/calendar_data.json',
						//view: targetView,
						language : 'de-DE', //navigator.language
						events_source: result,
						view: $rootScope.selectedView,
						tmpl_path: '/partials/diary/',
						tmpl_cache: false,
						day: $scope.selectedDate.yyyymmdd(),
						first_day: 1,
						onAfterEventsLoad: function(events) {
							if(!events) {
								return;
							}
							var list = $('#eventlist');
							list.html('');
	
							$.each(events, function(key, val) {
								$(document.createElement('li'))
									.html('<a href="' + val.url + '">' + val.title + '</a>')
									.appendTo(list);
							});
						},
						onAfterViewLoad: function(view) {
							$('#subtitle').text(this.getTitle());
							$('button').removeClass('active');
							$('button[data-calendar-view="' + view + '"]').addClass('active');
							
							var currentView = this.getView();
							var currentViewString = '';
							if(currentView == 'year'){
								currentViewString = 'Jahresansicht';
							} else if(currentView == 'month') {
								currentViewString = 'Monatsansicht';
							} else if(currentView == 'day') {
								currentViewString = 'Tagesansicht';
							}
							$('#breadCrumbView').text(currentViewString);
						},
						classes: {
							months: {
								general: 'label'
							}
						}
					};
				calendar = $('#calendar').calendar(options);
		});
	};
	
	$scope.reloadCalendarData();


	$('button[data-calendar-nav]').each(function() {
		var $this = $(this);
		$this.click(function() {
			calendar.navigate($this.data('calendar-nav'));
		});
	});

	$('button[data-calendar-view]').each(function() {
		var $this = $(this);
		$this.click(function() {
			calendar.view($this.data('calendar-view'));
		});
	});

	$('#first_day').change(function(){
		var value = $(this).val();
		value = value.length ? parseInt(value) : null;
		calendar.setOptions({first_day: value});
		calendar.view();
	});

	$('#language').change(function(){
		calendar.setLanguage($(this).val());
		calendar.view();
	});

	$('#events-in-modal').change(function(){
		var val = $(this).is(':checked') ? $(this).val() : null;
		calendar.setOptions({modal: val});
	});
	$('#events-modal .modal-header, #events-modal .modal-footer').click(function(e){
		//e.preventDefault();
		//e.stopPropagation();
	});
	
	
	///////////// modals start ////////////////
	$scope.go = function(path){
		$location.path(path);
	};
	
	$scope.showNewEventModal = function() {
		$scope.selectEvent=true;
		$scope.eventToEdit=null;
		$scope.newTitle = null;
		$scope.newText = null;
		$scope.newWeight = null;
		$scope.selectedDate = new Date();
	};

	$scope.initDatepickerModal = function(){
		function f1(){
			$('.datepicker').datepicker({ 
				format: "dd-mm-yyyy", 
				language: "de",
				autoclose: true,
				todayHighlight: true,
				weekStart: 1,
			});
			$('#timepicker').timepicker({
				 minuteStep: 1,
				 showMeridian: false
			});
			if(!$scope.selectedDate) {
				$scope.selectedDate = new Date();
			}
			$('.datepicker').datepicker('setDate', $scope.selectedDate);
			$('.datepicker').datepicker().on('changeDate', function(ev){
				$scope.selectedDate = new Date(ev.date);
				//set current hours
				var time = new Date();
				$scope.selectedDate.setHours(time.getHours(), time.getMinutes(), time.getSeconds());
			});
			$('#timepicker').timepicker().on('changeTime.timepicker', function(e) {
				$scope.selectedDate.setHours(e.time.hours, e.time.minutes);
			});
			var time = $scope.selectedDate.getHours() + ":" + $scope.selectedDate.getMinutes();				 
			$('#timepicker').timepicker('setTime', time);
		}
		$timeout(f1, 100);
	};
    
	$scope.showNoteModal = function() {
		$scope.addDiaryEntry = true;
		$scope.addDiaryEntryFiles = false;
		$scope.initDatepickerModal();
	};
	$scope.hideNoteModal = function() {
		$scope.addDiaryEntry = false;
		$scope.addDiaryEntryFiles = false;
	};
	
	$scope.saveNoteEvent = function() {
		$scope.hideNoteModal();
		if($scope.eventToEdit){
			$scope.eventToEdit.start = $scope.selectedDate;
			$scope.eventToEdit.title = $scope.newTitle;
			$scope.eventToEdit.text = $scope.newText;
			Event.updateEvent($scope.eventToEdit).then(function(result) {
				$scope.reloadCalendarData();
			}, function(result){
				$scope.showErrors(result);
			});
		} else {
			Event.createEventNote($scope.selectedDate, $scope.newTitle, $scope.newText).then(function(result) {
				$scope.reloadCalendarData();
			}, function(result){
				$scope.showErrors(result);
			});
		}
	};
	
	$scope.showWeightModal = function() {
		$scope.addWeight = true; 
		$scope.initDatepickerModal();
	};
	  
	$scope.hideWeightModal = function() {
		  $scope.addWeight = false;
	};
	
	$scope.showErrors = function(response) {
		if ((response.data != null) && (response.data.errors != null)) {
			for(var i=0; i<response.data.errors.length; i++){
				var errorString = response.data.errors[i];
				Alerts.error(errorString);
			}
		}
	};
	  
	$scope.saveWeightEvent = function() {
		$scope.hideWeightModal();
		if($scope.eventToEdit){
			$scope.eventToEdit.weight = $scope.newWeight;
			$scope.eventToEdit.start = $scope.selectedDate;
			Event.updateEvent($scope.eventToEdit).then(function(result) {
				$scope.reloadCalendarData();
			}, function(result){
				$scope.showErrors(result);
			});
		} else {
			Event.createEventWeight($scope.newWeight, $scope.selectedDate).then(function(result) {
				$scope.reloadCalendarData();
			}, function(result){
				$scope.showErrors(result);
			});
		}
	};

	$scope.step = 1;
	$scope.toStep = function(step){
		var maxStepsCount = 2;
		if(step < 1)	{
			step=1;
		}
		if(step > maxStepsCount){
			step = maxStepsCount;
		}
		$scope.step = step;
		if(step==1){
			$scope.addDiaryEntry = true;
			$scope.addDiaryEntryFiles = false;
		} else if(step==2){
			$scope.addDiaryEntry = false;
			$scope.addDiaryEntryFiles = true;
		} else {
			$scope.addDiaryEntry = true;
			$scope.addDiaryEntryFiles = false;
		}
	};
	$scope.stepClassInfo = function(step){
		var result =  "wizardInfo " + ($scope.step == step?"wizardInfoActive":"");
		return result;
	};
	$scope.stepProgressClassInfo = function(step){
		var result =  "progressInfo " + (($scope.step >= step)?"progressInfoActive":"");
		return result;
	};
	
	document.deleteEvent = function deleteEvent(eventId) {
		if(!$scope.isDiaryEditable) {
			return;
		}
		Event.deleteEvent(eventId).then(function() {
			$scope.reloadCalendarData();
		});
	};
	
	document.changeView = function changeView(view) {
		calendar.view(view);
	};
	
	document.toEditEvent = function toEditEvent(eventType, eventId) {
		if(!$scope.isDiaryEditable) {
			return;
		}
		$scope.eventToEdit = null;
		Event.getById(eventType, eventId).then(function(result) {
			$scope.eventToEdit=result;
			if($scope.eventToEdit.eventType == 'WEIGHT') {
				$scope.newWeight = $scope.eventToEdit.weight;
				$scope.selectedDate = new Date($scope.eventToEdit.start);
				$scope.showWeightModal();
			} else if ($scope.eventToEdit.eventType == 'NOTE') {
				$scope.selectedDate = new Date($scope.eventToEdit.start);
				$scope.newTitle = $scope.eventToEdit.title;
				$scope.newText = $scope.eventToEdit.text;
				$scope.showNoteModal();
			} else if ($scope.eventToEdit.eventType == 'TREATMENT') {
				var path = '/Nutzenbewertung/'+encodeURIComponent(result.disease.name)+'/'+encodeURIComponent(result.treatment.name);
				$location.search("reviewId", result.reviewId);
				$location.path(path);
			}
		});
	};

	$scope.onFileSelect = function($files) {
	    //$files: an array of files selected, each file has name, size, and type.
	    for (var i = 0; i < $files.length; i++) {
	      var file = $files[i];
	      $scope.upload = $upload.upload({
	        url: 'server/upload/url', //upload.php script, node.js route, or servlet url
	        // method: POST or PUT,
	        // headers: {'headerKey': 'headerValue'},
	        // withCredentials: true,
	        data: {myObj: $scope.myModelObj},
	        file: file,
	        // file: $files, //upload multiple files, this feature only works in HTML5 FromData browsers
	        /* set file formData name for 'Content-Desposition' header. Default: 'file' */
	        //fileFormDataName: myFile, //OR for HTML5 multiple upload only a list: ['name1', 'name2', ...]
	        /* customize how data is added to formData. See #40#issuecomment-28612000 for example */
	        //formDataAppender: function(formData, key, val){} //#40#issuecomment-28612000
	      }).progress(function(evt) {
	        console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
	      }).success(function(data, status, headers, config) {
	        // file is uploaded successfully
	        console.log(data);
	      });
	      //.error(...)
	      //.then(success, error, progress); 
	    }
	    // $scope.upload = $upload.upload({...}) alternative way of uploading, sends the the file content directly with the same content-type of the file. Could be used to upload files to CouchDB, imgur, etc... for HTML5 FileReader browsers. 
	  };
	  ///////////// modals end ////////////////
	  
	 
} 
]);
