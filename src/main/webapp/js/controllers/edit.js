'use strict';

angular.module(appName).controller('EditCtrl',['$scope', '$rootScope', '$location','$timeout','$routeParams', 'Disease', 'Treatment', 'TreatmentReview', 'User', 'SideEffect',
                                               'Alerts','Title','Event','TreatmentEvent', 'OptionList', 'frequencyOptions','treatmentCategoryOptions','getIdForName','getNameForId','eventTypeOptions', 'EventMemory', 'TreatmentEventMemory',
                         function($scope, $rootScope, $location, $timeout, $routeParams, Disease, Treatment, TreatmentReview, User, SideEffect, Alerts, 
                        		 Title, Event, TreatmentEvent, OptionList, frequencyOptions, treatmentCategoryOptions, getIdForName, getNameForId, eventTypeOptions, EventMemory, TreatmentEventMemory) {
	Alerts.clear();
	$scope.reviewId = $location.search().reviewId || "";
	$scope.diseaseName = $routeParams.diseaseName || "";
	$scope.treatmentName = $routeParams.treatmentName || "";
	$scope.editMode = false;
	if(($scope.diseaseName && $scope.treatmentName) && $scope.reviewId) {
		$scope.editMode = true;
	}
	$scope.diseaseId = null;
	$scope.treatmentId = null;
	$scope.loginTabSelected = true;
	$scope.treatmentCategoryOptions = treatmentCategoryOptions;
	$scope.frequencyOptions = frequencyOptions;
	
	$scope.treatmentEvents = [];
	$scope.treatmentTypeOptions = [];
	$scope.unitOptions = [];
	$scope.selectedTreatmentEvent = {};
	$scope.selectedTreatmentEvent.category = "";	
	$scope.createReviewSaving = null;
	$scope.review = {};
	$scope.wizardBreadcrumb = ["Gesundheitsanliegen"];
	
	$('[rel=popover1]').popover({ 
		placement:'bottom',
		html : true, 
        content: $('#popover_content_wrapper1').html(),
    });
	$('[rel=popover2]').popover({ 
		placement:'bottom',
		html : true, 
        content: $('#popover_content_wrapper2').html(),
    });
	$('[rel=popover3]').popover({ 
		placement:'bottom',
		html : true, 
        content: $('#popover_content_wrapper3').html(),
    });

	// first time initialization
	$('.datepicker').datepicker({ 
		format: "dd-mm-yyyy", 
		language: "de",
		autoclose: true,
		todayHighlight: true,
		weekStart: 1,
		});
	
	$('#timepicker3').timepicker({
		 minuteStep: 1,
		 showMeridian: false
		 });
	
	$scope.safeApply = function(fn) {
		  var phase = this.$root.$$phase;
		  if(phase == '$apply' || phase == '$digest') {
		    if(fn && (typeof(fn) === 'function')) {
		      fn();
		    }
		  } else {
		    this.$apply(fn);
		  }
		};
	
	$('#dateOfDiagnosisId').datepicker()
		.on('changeDate', function(ev){
			 $scope.safeApply(function() {
				$scope.review.dateOfDiagnosis = new Date(ev.date);
			});
    });
	
	$('#dateOfFirstSymptomsId').datepicker()
	.on('changeDate', function(ev){
		 $scope.safeApply(function() {
			$scope.review.dateOfFirstSymptoms = new Date(ev.date);
		});
	});
	
	
	// step 2 date fields initialization
	$('#treatmentEntryStartDateId').datepicker()
	.on('changeDate', function(ev){
		if (!$scope.dontUpdateStartDate) {
			$scope.safeApply(function() {
				 $scope.selectedTreatmentEvent.start = new Date(ev.date);
				 var time = $scope.selectedTreatmentEvent.start.getHours() + ":" + $scope.selectedTreatmentEvent.start.getMinutes();				 
				 $('#timepicker3').timepicker('setTime', time);
			});
		}
	});
	
	$('#treatmentEntryEndDateId').datepicker()
		.on('changeDate', function(ev) {
		 $scope.selectedTreatmentEvent.end = new Date(ev.date);
	});


	 $('#timepicker3').timepicker().on('changeTime.timepicker', function(e) {
		 $scope.dontUpdateStartDate = true;
		 if (!$scope.selectedTreatmentEvent.start) {
			 $scope.selectedTreatmentEvent.start = new Date();
		 }
		 $scope.safeApply(function() {
			 $scope.selectedTreatmentEvent.start.setHours(e.time.hours);
			 $scope.selectedTreatmentEvent.start.setMinutes(e.time.minutes);
		 });
		 $scope.dontUpdateStartDate = null;
	});

	function eventSvc(svc, svcMem){
		if($scope.review._id){
			return svc;
		}else{
			return svcMem;
		}
	}
	
	TreatmentEventMemory.init();
	
	// wizard "Costs" step initialization	
	$scope.currencyOptions = [
		                       { _id: 'EUR', name: '€' },
		                     ];
	
	$scope.durationUnitOptions = [];
	
	$scope.amountOptions = ['1','2','3','4','5','6','7','8','9','10',
	                        '11','12','13','14','15','16','17','18','19','20'
	                        ];
	$scope.amountOptionsWithId = [
		                       { id: '1', name: '1' },
		                       { id: '2', name: '2' },
		                       { id: '3', name: '3' },
		                       { id: '4', name: '4' },
		                       { id: '5', name: '5' },
		                       { id: '6', name: '6' },
		                       { id: '7', name: '7' },
		                       { id: '8', name: '8' },
		                       { id: '9', name: '9' },
		                       { id: '10', name: '10' },
		                       { id: '11', name: '11' },
		                       { id: '12', name: '12' },
		                       { id: '13', name: '13' },
		                       { id: '14', name: '14' },
		                       { id: '15', name: '15' },
		                       { id: '16', name: '16' },
		                       { id: '17', name: '17' },
		                       { id: '18', name: '18' },
		                       { id: '19', name: '19' },
		                       { id: '20', name: '20' }
		                     ];
	
	
	$scope.dateOfFirstSymptomsValid = function() {
		var now = new Date();
		if ($scope.review.dateOfFirstSymptoms && $scope.review.dateOfFirstSymptoms > now) {
			Alerts.error("Zeitpunkt der ersten Symptome kann nicht in der Zukunft liegen.");
			return false;
		}
		return true;
	};

	$scope.dateOfDiagnosisValid = function() {
		var now = new Date();
		if ($scope.review.dateOfDiagnosis) {
			if ($scope.review.dateOfDiagnosis > now) {
				Alerts.error("Zeitpunkt der Diagnose kann nicht in der Zukunft liegen");
				return false;
			} else if($scope.review.dateOfFirstSymptoms && $scope.review.dateOfDiagnosis < $scope.review.dateOfFirstSymptoms){
				Alerts.error("Zeitpunkt der Diagnose kann nicht vor dem Zeitpunkt der ersten Symptome sein.");
				return false;
			}
		}
		return true;
	};

	$scope.isStartEnabled = function() {
		var result = false;
		if (!$scope.selectedTreatmentEvent.category) {
			return false;
		}
		if(($scope.isTreatmentPlanCategorySelected() || $scope.isPhysiotherapyCategorySelected() || $scope.isPsychotherapyCategorySelected()) && $scope.selectedTreatmentEvent.frequency){
			result = true;
		} else if ($scope.isMedicalElectronicDeviceCategorySelected()  || $scope.isOperationCategorySelected() || $scope.isOthersCategorySelected()) {
			result = true;
		}
		return result;
	};

	$scope.isEndEnabled = function() {
		var result = false;
		if (!$scope.selectedTreatmentEvent.category) {
			return false;
		}
		if ($scope.selectedTreatmentEvent.frequency && !$scope.isOneTimeFrequencySelected() && ($scope.isTreatmentPlanCategorySelected()  || $scope.isPhysiotherapyCategorySelected() || $scope.isPsychotherapyCategorySelected())) {
			result = true;
		}
		return result;
	};

	$scope.isOneTimeFrequencySelected = function() {
		var result = false;
		if ($scope.selectedTreatmentEvent.frequency && ($scope.selectedTreatmentEvent.frequency === frequencyOptions[0].name)
		 ){
			result = true;
		}
		return result;
	};
	
	$scope.isTreatmentPlanCategorySelected = function() {
		var result = false;
		if (($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[0].name)
		 || ($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[1].name)
		 || ($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[2].name)
		 || ($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[3].name)		 
		 || ($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[4].name)
		 || ($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[5].name)
		 ){
			result = true;
		}
		return result;
	};
	
	$scope.isPsychotherapyCategorySelected = function() {
		var result = false;
		if (($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[7].name)
		 ){
			result = true;
		}
		return result;
	};
	
	$scope.isPhysiotherapyCategorySelected = function() {
		var result = false;
		if (($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[6].name)
		 ){
			result = true;
		}
		return result;
	};

	$scope.isMedicalElectronicDeviceCategorySelected = function() {
		var result = false;
		if (($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[8].name)
		 || ($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[9].name)				
		 ){
			result = true;
		}
		return result;
	};
	
	$scope.isOperationCategorySelected = function() {
		var result = false;
		if (($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[10].name)				
		 ){
			result = true;
		}
		return result;
	};
	
	$scope.isOthersCategorySelected = function() {
		var result = false;
		if (($scope.selectedTreatmentEvent.category === treatmentCategoryOptions[11].name)				
		 ){
			result = true;
		}
		return result;
	};	
	
	OptionList.findAllOptionLists().then(function(result) {
		if(result) {
			var count = result.length;
			for(var i=0; i< count; i++){
				var tempTreatmentEvent = result[i];
				tempTreatmentEvent.id = tempTreatmentEvent._id;
			}			
			$scope.treatmentTypeOptions = result;
		}		
	}, function(error){
		if (error.data != null) {
			Alerts.error(error.data.message);
		}
	});
	OptionList.findAllUnits().then(function(result) {
		if(result) {
			var count = result.length;
			for(var i=0; i< count; i++){
				var unit = result[i];
				unit.id = unit._id;
				// minutes & hours
				if(unit._id === "1f6d8b80-4dc8-4443-b348-5f5c03f57a15" || unit._id === "a3c1db9c-55fb-44c9-a209-eeb96cfb7acb"){
					$scope.durationUnitOptions.push(unit);
				}
			}
			$scope.unitOptions = result;
		}		
	}, function(error){
		if (error.data != null) {
			Alerts.error(error.data.message);
		}
	});
	
	$scope.totalAmount = new Number("0");
    $scope.updateTotalAmount = function() {
		if (!$scope.review.treatmentQuantity) {
			//default is 1
			$scope.review.treatmentQuantity = $scope.amountOptions[0];
		}
		if ((!isNaN($scope.review.doctorCosts))  && ($scope.review.doctorCosts)) {
			$scope.totalAmount = $scope.review.doctorCosts;
		} else {
			$scope.totalAmount = "0";
		}
		if ((!isNaN($scope.review.treatmentPrice)) && ($scope.review.treatmentPrice) && (!isNaN($scope.review.treatmentQuantity))) {
			$scope.totalAmount = $scope.totalAmount + ($scope.review.treatmentQuantity * $scope.review.treatmentPrice);
		} else if (!$scope.totalAmount) {
			$scope.totalAmount = "0";
		}
		$scope.totalAmount = new Number($scope.totalAmount);
		$scope.computeInsuranceCoverage();
	};
	
	$scope.computeInsuranceCoverage = function() {
		if ((!isNaN($scope.review.coinsurance))  && ($scope.review.coinsurance)) {
			$scope.review.insuranceCoverage = $scope.totalAmount - $scope.review.coinsurance;
		}
		if(!$scope.review.coinsurance){
			$scope.review.insuranceCoverage = null;
		}
	};
	
	$scope.rejectEmpty = function(fieldName) {
		Alerts.error(fieldName+" ist erforderlich!");
	};
	function isDate(obj) {
		return Object.prototype.toString.call(obj) === '[object Date]' && isFinite(obj);
	};
	
	$scope.validateTreatmentEvent = function() {
		Alerts.clear();
		var result = true;
		if(!$scope.diseaseId && !$scope.diseaseName){ 
			result = false;
			$scope.rejectEmpty("Krankheit");
		}
		if(!$scope.treatmentId && !$scope.treatmentName) {
			result = false;
			$scope.rejectEmpty("Behandlung");
		}
		if ($scope.isTreatmentPlanCategorySelected()) {
			if (!$scope.selectedTreatmentEvent.frequency) {
				result = false; 
				$scope.rejectEmpty("Häufigkeit");
			} else if (!$scope.selectedTreatmentEvent.quantity) {
				result = false;
				$scope.rejectEmpty("Menge");
			} else if (!$scope.selectedTreatmentEvent.type) {
				result = false;
				$scope.rejectEmpty("Typ der Menge");
			} else	if (!$scope.selectedTreatmentEvent.dose) {
				result = false;
				$scope.rejectEmpty("Dosis");
			} else if (!$scope.selectedTreatmentEvent.unit) {
				result = false;
				$scope.rejectEmpty("Einheit");
			} else if (!isDate($scope.selectedTreatmentEvent.start)) {
				result = false;
				if ($scope.selectedTreatmentEvent.frequency == frequencyOptions[0].name) {
					$scope.rejectEmpty("Datum");
				} else {
					$scope.rejectEmpty("Beginn der Behandlung");
				}
			} else if ($scope.selectedTreatmentEvent.frequency != frequencyOptions[0].name && !isDate($scope.selectedTreatmentEvent.end)) {
				result = false;
				$scope.rejectEmpty("Ende der Behandlung");
			}			
		} else if ($scope.isPhysiotherapyCategorySelected() || $scope.isPsychotherapyCategorySelected()) {
			if (!$scope.selectedTreatmentEvent.frequency) {
				$scope.rejectEmpty("Häufigkeit");
				result = false;
			/*
			} else if (!$scope.selectedTreatmentEvent.quantity) {
				$scope.rejectEmpty("Menge");
				result = false;
			*/
			} else  if (!isDate($scope.selectedTreatmentEvent.start)) {
				result = false;
				if ($scope.selectedTreatmentEvent.frequency == frequencyOptions[0].name) {
					$scope.rejectEmpty("Datum");
				} else {
					$scope.rejectEmpty("Beginn der Behandlung");
				}
			} else if ($scope.selectedTreatmentEvent.frequency != frequencyOptions[0].name && !isDate($scope.selectedTreatmentEvent.end)) {
				result = false;
				$scope.rejectEmpty("Ende der Behandlung");
			} else if (!$scope.selectedTreatmentEvent.unit) {
				$scope.rejectEmpty("Dauereinheit");
				result = false;
			} else if (!$scope.selectedTreatmentEvent.duration) {
				$scope.rejectEmpty("Behandlungsdauer");
				result = false;
			}
		} else if ($scope.isMedicalElectronicDeviceCategorySelected()) {
			if (!isDate($scope.selectedTreatmentEvent.start)) {
				$scope.rejectEmpty("Zeitpunkt der Anwendung");
				result = false;
			}
		} else if ($scope.isOperationCategorySelected()) {
			if (!isDate($scope.selectedTreatmentEvent.start)) {
				$scope.rejectEmpty("Zeitpunkt der Operation");
				result = false;
			}
		} else if ($scope.isOthersCategorySelected()) {
			if (!isDate($scope.selectedTreatmentEvent.start)) {
				$scope.rejectEmpty("Zeitpunkt der Behandlung");
				result = false;
			}
		}
		if (isDate($scope.selectedTreatmentEvent.start) && isDate($scope.selectedTreatmentEvent.end)) {
			if($scope.selectedTreatmentEvent.end < $scope.selectedTreatmentEvent.start){
				Alerts.error("Ende der Behandlung kann nicht vor dem Beginn der Behandlung sein.");
				result = false;
			}
		}
		
		if ($scope.selectedTreatmentEvent.quantity) {
			var num = new Number($scope.selectedTreatmentEvent.quantity);
			if (num < 0) {
				Alerts.error("Menge muss grösser als 0 sein.");
				result = false;
			}
			//$scope.selectedTreatmentEvent.quantity = num;
		}
		if ($scope.selectedTreatmentEvent.dose) {
			var num = new Number($scope.selectedTreatmentEvent.dose);
			if (num < 0) {
				Alerts.error("Dosis muss grösser als 0 sein.");
				result = false;
			}
			//$scope.selectedTreatmentEvent.dose = num;
		}
		if ($scope.selectedTreatmentEvent.duration) {
			var num = new Number($scope.selectedTreatmentEvent.duration);
			if (num < 0) {
				Alerts.error("Behandlungsdauer muss grösser als 0 sein.");
				result = false;
			}
			//$scope.selectedTreatmentEvent.duration = num;
		}
		return result;
	};
	
	$scope.selectTreatmentEvent = function(treatmentEvent) {
			$scope.selectedTreatmentEvent = treatmentEvent;
			$('#treatmentEntryEndDateId').datepicker('setDate', $scope.selectedTreatmentEvent.end);
			$('#treatmentEntryStartDateId').datepicker('setDate', $scope.selectedTreatmentEvent.start);
	};
	
	$scope.resetSelectedTreatmentEvent = function() {
		var categoryName = $scope.selectedTreatmentEvent.category;
		$scope.selectedTreatmentEvent = {};
		$scope.selectedTreatmentEvent.category = categoryName;
		$('#treatmentEntryEndDateId').datepicker('setDate', $scope.selectedTreatmentEvent.end);
		$('#treatmentEntryStartDateId').datepicker('setDate', $scope.selectedTreatmentEvent.start);
	};
	
	$scope.cancelTreatmentEvent = function() {
		$scope.selectedTreatmentEvent = {};
		$('#treatmentEntryEndDateId').datepicker('setDate', $scope.selectedTreatmentEvent.end);
		$('#treatmentEntryStartDateId').datepicker('setDate', $scope.selectedTreatmentEvent.start);
		Alerts.clear();
	};

	$scope.fmtTreatmentCategory = function(event) {
		var category = event.category;
		var text = getNameForId(treatmentCategoryOptions, category);
		if (!isStringEmpty(text)) {
			return text;
		}else{
			return category;
		}
	};
	
	$scope.saveTreatmentEvent = function() {
		if ($scope.validateTreatmentEvent()) {
			var newEntry = ($scope.selectedTreatmentEvent._id);
			$scope.selectedTreatmentEvent.disease = {"_id" : $scope.diseaseId, "name": $scope.diseaseName};
			$scope.selectedTreatmentEvent.treatment = {"_id" : $scope.treatmentId, "name" : $scope.treatmentName};
			if ($rootScope.userLogged != null) {
				$scope.selectedTreatmentEvent.user = {"_id" : $rootScope.userLogged._id, "name": $rootScope.userLogged.name};
			}
			$scope.selectedTreatmentEvent.eventType = eventTypeOptions[3].id;
			if ($scope.selectedTreatmentEvent.frequency == frequencyOptions[0].name) {
				// for ONCE delete end date
				$scope.selectedTreatmentEvent.end = null;
			}
			
			var categoryName = $scope.selectedTreatmentEvent.category;
			var categoryId = getIdForName(treatmentCategoryOptions, categoryName);
			$scope.selectedTreatmentEvent.category = categoryId;

			var frequencyName = $scope.selectedTreatmentEvent.frequency;
			if ($scope.selectedTreatmentEvent.frequency) {
				var frequencyId = getIdForName(frequencyOptions, frequencyName);
				$scope.selectedTreatmentEvent.frequency = frequencyId;
			} else $scope.selectedTreatmentEvent.frequency = null;
			if (($scope.selectedTreatmentEvent.type != null) && ($scope.selectedTreatmentEvent.type.name != null)) {
				var typeId = getIdForName($scope.treatmentTypeOptions, $scope.selectedTreatmentEvent.type.name);
				$scope.selectedTreatmentEvent.type._id = typeId;
			}
			if (($scope.selectedTreatmentEvent.unit != null) && ($scope.selectedTreatmentEvent.unit.name != null)) {
				var unitId = getIdForName($scope.unitOptions, $scope.selectedTreatmentEvent.unit.name);
				$scope.selectedTreatmentEvent.unit._id = unitId;
			}
			
			eventSvc(TreatmentEvent, TreatmentEventMemory).upsert($scope.selectedTreatmentEvent).then(function(result) {
				// translate category from enum to text
				$scope.selectedTreatmentEvent.category = categoryName;				
				$scope.selectedTreatmentEvent.frequency = frequencyName;
				if(result._id != null) {
						$scope.selectedTreatmentEvent._id = result._id;
						if (!newEntry) {
							$scope.treatmentEvents.push($scope.selectedTreatmentEvent);
						}
				}
				$scope.cancelTreatmentEvent();
			}, function(result){
				if ((result.data != null) && (result.data.errors != null)) {
					for(var i=0; i<result.data.errors.length; i++){
						var errorString = result.data.errors[i];
						Alerts.error(errorString);
					}
				}
				$scope.selectedTreatmentEvent.category = categoryName;
				$scope.selectedTreatmentEvent.frequency = frequencyName;
			});
			return true;
		} else {
			Alerts.error("Bitte füllen Sie alle Felder aus, die Ihnen angezeigt werden.");
			return false;
		}
	};
	
	$scope.deleteTreatmentEvent = function(treatmentEventItem) {
		eventSvc(Event, EventMemory).deleteEvent(treatmentEventItem._id).then(function(result) {
			for(var i=0; i< $scope.treatmentEvents.length; i++){
				var tempTreatmentEvent = $scope.treatmentEvents[i];
				if(tempTreatmentEvent === treatmentEventItem){
					$scope.treatmentEvents.splice(i, 1);
				}
			}
		}, function(result){
			if ((result.data != null) && (result.data.errors != null)) {
				for(var i=0; i<result.data.errors.length; i++){
					var errorString = result.data.errors[i];
					Alerts.error(errorString);
				}
			}
		});
	};
	
	$scope.loadAllTreatmentEvents = function() {
		if (($scope.review != null) || ($rootScope.userLogged != null)) {
			var userId = "";
			if ($scope.review._id != null) { 
				userId = $scope.review.author._id;
			} else if ($rootScope.userLogged != null) {
				userId = $rootScope.userLogged.id;
			}
			eventSvc(TreatmentEvent, TreatmentEventMemory).findAllForReview(userId, $scope.diseaseId, $scope.treatmentId, 0, 20, "start", "desc").then(
				function(result) {
					if (result) {
						var count = result.length;
						for(var i=0; i< count; i++){
							var tempTreatmentEvent = result[i];
							if (tempTreatmentEvent.start != null) {
								var date = new Date(tempTreatmentEvent.start);
								tempTreatmentEvent.start = date;
							}
							if (tempTreatmentEvent.end != null) {
								var date = new Date(tempTreatmentEvent.end);
								tempTreatmentEvent.end = date;
							}
							var categoryName = getNameForId(treatmentCategoryOptions, tempTreatmentEvent.category);
							tempTreatmentEvent.category = categoryName;			
							
							var frequencyName = getNameForId(frequencyOptions, tempTreatmentEvent.frequency);
							tempTreatmentEvent.frequency = frequencyName;			
						}
					} 
					$scope.treatmentEvents = result;
				}
			);
		}
	};
	
// hide seems not to work properly - after hiding it's still "catching" the clicks (therefore click event)
	$('html').on('mouseup', function(e) {
	    if(!$(e.target).closest('.popover').length) {
	        $('.popover').each(function(){
	           $(this.previousSibling).popover().click();
	        });
	    }
	});
	
	$scope.initializeReviewVariables = function() {
		var firstSymptomsDate = new Date($scope.review.dateOfFirstSymptoms);
		if (firstSymptomsDate && (!isNaN(firstSymptomsDate.getTime()))) {
			$('#dateOfFirstSymptomsId').datepicker('setDate', firstSymptomsDate);
		}
		var dateOfDiagnosisDate = new Date($scope.review.dateOfDiagnosis);
		if (dateOfDiagnosisDate && (!isNaN(dateOfDiagnosisDate.getTime()))) {
			$('#dateOfDiagnosisId').datepicker('setDate', dateOfDiagnosisDate);
		}
		$scope.diseaseId = $scope.review.disease._id;
		$scope.diseaseName = $scope.review.disease.name;
		$scope.treatmentName = $scope.review.treatment.name;
		$scope.treatmentId = $scope.review.treatment._id;
		$scope.gender = $scope.review.gender;
		$scope.yearOfBirth = $scope.review.yearOfBirth;
		$scope.reviewText = $scope.review.text;
		$scope.sideEffects = $scope.review.sideEffects;

		$("#disease_selector").select2("data", {id : $scope.diseaseId, text : $scope.diseaseName, count : 0});
		$("#treatment_selector").select2("data", {id : $scope.treatmentId, text : $scope.treatmentName});
		
		$scope.loadAllTreatmentEvents();
		$scope.updateTotalAmount();
	};
	
	Title.set(Title.defaultTitle());
	console.log("id: "+$scope.reviewId+" dname: "+$scope.diseaseName+" tname: "+$scope.treatmentName);
	
	$scope.diseaseId = null;
	$scope.reviewText = "";
	$scope.sideEffects = [];
	$scope.noSideEffect = false;
	
	
	
	if($scope.reviewId.length > 0){
		TreatmentReview.queryById($scope.reviewId).then(function(result) {
			$scope.review = result;
			$scope.initializeReviewVariables();
		}, function(){
			//Alerts.error("Fehler beim Laden");
		});
	}	
	
	$scope.gender = null;
	$scope.yearOfBirth = null;
	$scope.userLoggedState = "";
	$scope.initYearGenderDefaultValues = function() {
		var user = $rootScope.userLogged;
		if(user != null){
			$scope.yearOfBirth = user.yearOfBirth;
			$scope.gender = user.gender;
			$scope.userLoggedState = user.state;
		}
		
	};
	
	$scope.hideLoggedUser = function() {
		var result = false;
		var user = $rootScope.userLogged;
		if(user != null){
			var yearOfBirth = user.yearOfBirth;
			var gender = user.gender;
			result =  ((gender != null) && (yearOfBirth != null));
			if (($scope.reviewId != null) && ($scope.reviewId.length > 0)) {
				result = true;
			}
		}
		return result;
	};
	
	$scope.hideLoginStep = function() {
		var result = false;
		var user = $rootScope.userLogged;
		if(user != null){
			result = true;
		}
		return result;
	};

	$scope.$on('userLogged', function() {	
		if ($rootScope.userLogged != null) {
			$scope.initYearGenderDefaultValues();
			$scope.userLoggedState = $rootScope.userLogged.state;
			if (($scope.signInPassword != null) || ($scope.signUpPassword != null)) { 	// we're at the login step, finish the creation of the review since the user is now successfully logged in
				var maxStepsCount = $scope.getStepsMaxNumber();
				if($scope.step >= maxStepsCount) {
					$scope.step = maxStepsCount;
				}
				if (!$scope.createReviewSaving) {
					$scope.save();				
				}
			}
		}
	});
	$scope.initYearGenderDefaultValues();
	
	$scope.list_years = [];
	
	$scope.newSideEffect = "";
	
		
	$scope.users = {};
	
	var sideEffectIndex = 0;
	
	var preload_disease_data = [];
	var preload_treatment_data = [];
	var preload_side_effect_data = [];
	
	
	
	$scope.hideLoggedUserAge = function() {
		var result = false;
		var user = $rootScope.userLogged;
		if(user != null){
			var yearOfBirth = user.yearOfBirth;
			if(yearOfBirth != null){
				result =  true;
			}
		}
		return result;
	};
	
	$scope.hideLoggedUserGender = function() {
		var result = false;
		var user = $rootScope.userLogged;
		if(user != null){
			var gender = user.gender;
			if(gender != null){
				result =  true;
			}
		}
		return result;
	};
	
	$scope.getStepsMaxNumber = function() {
		var result = 7;

		if ($scope.hideLoginStep()){
			result -= 1;
		}
		
		if($scope.hideLoggedUser()){
			result -= 1;
		}
				
		return result;
	};
	
	$scope.initUsers = function() {
		User.findByDiseaseIdPageNumberPageSize($scope.diseaseId, 1, 24).then(
				function(users){
					$scope.users = users;
				});
	};
	
	$scope.initDiseases = function() {		
		Disease
				.queryNames(false)
				.then(
						function(diseases) {
							for ( var i = 0; i < diseases.length; i++) {
								var disease = diseases[i];
								preload_disease_data
										.push({
											id : i,
											text : disease.name,
											count : disease.treatmentReviewsCount
										});
							}
							
							var disease = getDisease($scope.diseaseName);
							if(disease == null) {
								if($scope.diseaseName.length > 0) {
									$("#disease_selector").select2("data", {id : null, text : $scope.diseaseName, count : "0"});
								}
								
								$scope.initUsers();
								return;
							} 
							
							$("#disease_selector").select2("data", {id : disease.id, text : disease.text, count : disease.count});
							
							Disease
							.findByName($scope.diseaseName)
							.then(
									function(diseases) {
										if (diseases && diseases.length > 0) {
											var disease = diseases[0];
											$scope.diseaseId = disease._id;
											$scope.initUsers();
											$scope.loadAllTreatmentEvents();
										}
										
										
									}, function() {
										$scope.initUsers();
									}
							);
						});
		
		
		
	};
	
	$scope.initTreatments = function() {

		Treatment.queryNames(false)
		.then(
				function(treatments) {
					for ( var i = 0; i < treatments.length; i++) {
						var treatment = treatments[i];
						
						preload_treatment_data
								.push({
									id : i,
									text : treatment
								});
					}
					
					var treatment = getTreatment($scope.treatmentName);
					if(treatment == null) {
						return;
					}
					$("#treatment_selector").select2("data", {id : treatment.id, text : treatment.text});
					
				});
	};
	
	$scope.initSideEffects = function() {
		SideEffect.queryNames(false)
		.then(
				function(sideEffects) {
					for ( var i = 0; i < sideEffects.length; i++) {
						var sideEffect = sideEffects[i];
						
						preload_side_effect_data
								.push({
									id : i,
									name : sideEffect
								});
					}
					
				});
	};
	
	function getDisease(diseaseName) {
		var result = null;
		
		for(var i=0; i<preload_disease_data.length; i++){
			var disease = preload_disease_data[i];
			if(disease.text.toUpperCase() == diseaseName.toUpperCase()){
				result = disease;
				return result;
			}
		}
	};
	
	function getTreatment(treatmentName) {
		var result = null;
		
		for(var i=0; i<preload_treatment_data.length; i++){
			var treatment = preload_treatment_data[i];
			if(treatment.text == treatmentName){
				result = treatment;
				return result;
			}
		}
		
		return result;
	};
	
	function getSideEffect(sideEffectName) {
		var result = null;
		
		for(var i=0; i<preload_side_effect_data.length; i++){
			var sideEffect = preload_side_effect_data[i];
			if(sideEffect == sideEffectName){
				result = sideEffect;
				return result;
			}
		}
		
		return result;
	};
	
	$("#disease_selector")
	.select2(
			{
				maximumInputLength: 60,
				placeholder : "Um welches Gesundheitsanliegen handelt es sich?",
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
					if (query.term.length == 1) {
						matchString = query.term.toUpperCase();
					} else {
						matchString = query.term.charAt(0).toUpperCase() +  query.term.slice(1);
					}
					
					var matchStringU = query.term.toUpperCase();
					var n = result.text.toUpperCase().indexOf(matchStringU);
					var preString = result.text.substring(0, n);
					var postString = result.text.substring(n + query.term.length);
					var newString = "";
					if(!result.count){
						result.count = 0;
					}
					if(result.isNew){
						result.count = 0;
						newString = '<span class="label success">Neu</span> ';
					}
					return "<div class='select2-result-label'>"
							+ newString 
							+ preString
							+ "<span class='select2-match' style='font-weight:bold'>"
							+ matchString
							+ "</span>"
							+ postString
							+ "<span style='float:right'>"
							+ result.count
							+ " reviews</span></div>";

				},
				query : function(query) {
					var data = {
						results : []
					};
					$scope.suggestedDisease = query.term;
					$
							.each(
									preload_disease_data,
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
	
	$("#disease_selector").on("change", function(e) {
		$timeout(function() {
			$scope.diseaseName = e.added.text;
			if ($scope.diseaseName.length == 1) {
				$scope.diseaseName = $scope.diseaseName.toUpperCase();
			} else {
				$scope.diseaseName = $scope.diseaseName.charAt(0).toUpperCase() +  e.added.text.slice(1);
			}
			validateStep($scope.step);
			
			Disease
			.findByName($scope.diseaseName)
			.then(
					function(diseases) {
						if (diseases
								&& diseases.length > 0) {
							var disease = diseases[0];
							$scope.diseaseId = disease._id;
							$scope.initUsers();
						} 
						else {
							$scope.users = null;
						}
					});
			
		}, 0);
	});
		
	$("#treatment_selector")
	.select2(
			{
				maximumInputLength: 60,
				placeholder : "Wie haben Sie es behandelt?",
				createSearchChoice:function(term, data) {
				    if ($(data).filter(function() {
				        return this.text.toUpperCase().localeCompare(term.toUpperCase()) === 0;
				    }).length === 0) {
				        return {id:term, text: term, isNew: true};
				    }
				},
				formatResult: function(term) {
				    var newTerm = term.text;
				    if (newTerm.length == 1) {
				    	newTerm = newTerm.toUpperCase();
					} else {
						newTerm = newTerm.charAt(0).toUpperCase() +  term.text.slice(1);
					}
					if (term.isNew) {
				        return '<span class="label success">Neu</span> ' + newTerm;
				    }
				    else {
				        return newTerm;
				    }
				},
				query : function(query) {
					var data = {
						results : []
					};
					$scope.suggestedDisease = query.term;
					$
							.each(
									preload_treatment_data,
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
						if (a.count < b.count)
							return 1;
						if (a.count > b.count)
							return -1;
						return 0;
					});

					query.callback(data);
				},
			});
	
	
	$("#treatment_selector").on("change", function(e) {
		$timeout(function() {
			$scope.treatmentName = e.added.text;
			if ($scope.diseaseName.length == 1) {
				$scope.treatmentName = $scope.treatmentName.toUpperCase();
			} else {
				$scope.treatmentName = $scope.treatmentName.charAt(0).toUpperCase() +  e.added.text.slice(1);
			}
			Alerts.clear();
			
			
				Treatment
				.findByName($scope.treatmentName)
				.then(
				function(treatments) {
					if (treatments && treatments.length > 0) {
						var treatment = treatments[0];
						$scope.treatmentId = treatment._id;
						if($rootScope.userLogged != null) {
							$scope.loadAllTreatmentEvents();
							if ($scope.diseaseId != null) {
								if($scope.diseaseId.length > 0 && $scope.treatmentId.length > 0){
									TreatmentReview.existForUser($scope.diseaseId, $scope.treatmentId).then(function(result) {
										if(result.exists == true){
											/*
											iosOverlay({
												text: "Already created",
												duration: 2e3,
												icon: "assets/iosOverlay/img/cross.png"
											});
											*/
											Alerts.error("Upps, Sie haben bereits eine Behandlungsbewertung für "+ $scope.diseaseName +" und "+ $scope.treatmentName +" erstellt. Wenn Sie Ihre bisherige Bewertung ändern möchten, gehen Sie einfach auf Ihr Profil und klicken Sie auf bearbeiten.");
										}
										validateStep($scope.step);
									}, function(){
										
										validateStep($scope.step);
									});
								} else {
									
									validateStep($scope.step);
								}
							}
						} else {
							validateStep($scope.step);
						}
					} else {
						
						validateStep($scope.step);
					}
				});
		}, 0);
	});
	var noSideEffectText = "Keine Nebenwirkungen";
	
	$("#side_effect_selector")
	.select2(
			{
				maximumInputLength: 60,
				placeholder : "Welche Nebenwirkungen haben sich gezeigt?",
				createSearchChoice:function(term, data) {
				    if ($(data).filter(function() {
				        return this.text.toUpperCase().localeCompare(term.toUpperCase()) === 0;
				    }).length === 0) {
				        return {id:term, text: term, isNew: true};
				    }
				},
				formatResult: function(term) {
				    if (term.isNew) {
				        return '<span class="label success">Neu</span> ' + term.text;
				    }
				    else {
				    	if(term.noSideEffect){
				    		return '<hr style="margin-top: 2px;margin-bottom: 2px;"/><span style="font-weight:bold">' + term.text + '</span>';
				    	}
				        return term.text;
				    }
				},
				query : function(query) {
					var data = {
						results : []
					};
					$scope.suggestedDisease = query.term;
					jQuery.each(
									preload_side_effect_data,
									function(count) {
										if (query.term.length == 0
												|| this.name
														.toUpperCase()
														.indexOf(
																query.term
																		.toUpperCase()) >= 0) {
											if (data.results.length < 11) {
												
												if(!isSideEffectAlreadyAdded(this.name)){
													data.results
													.push({
														id : this.id,
														text : this.name,
														count : count
													});
												}
											}
										}
									});

					data.results.sort(function(
							a, b) {
						if (a.count < b.count)
							return 1;
						if (a.count > b.count)
							return -1;
						return 0;
					});

					data.results.push({
						id : null,//data.results.length,
						text : noSideEffectText,
						count : 0,
						noSideEffect: true
					});
					query.callback(data);
				},
			});
	
	//$('#side_effect_selector').select2('disable');
	
	$("#side_effect_selector").on("change", function(e) {
		
		$timeout(function() {
			$scope.newSideEffect = e.added.text;
			$scope.addSideEffect();
		}, 0);
	});
	
	$scope.validateAndNext = function(isValid){
		Alerts.clear();
		//make sure valid
		// step 1; $scope.step;
		isValid = $scope.validateSetp1();
		if (isValid) {
			$scope.chgStep(1);
		} else {
			return false;
		}
	};
	
	$scope.validateSetp1 = function(){
		var valid = $scope.dateOfFirstSymptomsValid();
		if (valid) {
			valid = $scope.dateOfDiagnosisValid();
		} else {
			$scope.dateOfDiagnosisValid();
		}
		return valid;
	};

	$scope.finish = function() {
		var valid = true;
		if ($scope.step == 1) {
			valid = $scope.validateSetp1();
		} else if ($scope.step == 2) {
			if($rootScope.userLogged && $scope.selectedTreatmentEvent.category){
				valid = $scope.saveTreatmentEvent();
			}
		}
		if (!valid) {
			return false;
		}
		$scope.save();
	};
	
	$scope.save = function() {
		var maxStepsCount = $scope.getStepsMaxNumber();
		if(!$scope.editMode && $scope.step != maxStepsCount){
			return;
		}
		$scope.create();
	};
	
	$scope.dismissSubscribeDialog = function() {
		if ($scope.subscribeEmailSend) {
			$scope.subscribeUserDialog = false;
			$rootScope.newReviewPending = false;			
			$location.path("");
		} else {
			$scope.subscribeUserDialog = false;
			$rootScope.newReviewPending = false;			
		}
	};
	
	$scope.subscribeEmail = function() {
		Alerts.clear();
		
		var error = false;
		if(isStringEmpty($scope.diseaseName)){
			error = true;
			Alerts.error("Mandatory disease name");
		}
		if(isStringEmpty($scope.treatmentName)){
			error = true;
			Alerts.error("Mandatory treatment name");
		}
		
		if(error){
			return;
		}

		User.subscribe($scope.subcribeEmail).then(function(result){
			TreatmentReview.createSubscriptionReview($scope.reviewId, $scope.diseaseName, $scope.treatmentName, $scope.review.rating, $scope.reviewText, $scope.sideEffects, $scope.yearOfBirth, $scope.gender, $scope.subcribeEmail)
				.then(function(result){
				}, function(error){
					//show error
					Alerts.error("Ein Fehler ist aufgetreten");
				});
			$scope.subscribeEmailSend = true;
		}, function(error){
			//show error
			Alerts.error("Fehler ");
		});
	};
	
	
	$scope.create = function() {
		$scope.subscribeUserDialog = false;
		$rootScope.newReviewPending = false;
		
		Alerts.clear();
		
		var error = false;
		if(isStringEmpty($scope.diseaseName)){
			error = true;
			Alerts.error("Mandatory disease name");
		}
		if(isStringEmpty($scope.treatmentName)){
			error = true;
			Alerts.error("Mandatory treatment name");
		}
		
		if(error){
			return;
		}
		$scope.createReviewSaving = true;
			
		$scope.review.disease = {
			name : $scope.diseaseName
		};
		$scope.review.treatment = {
			name : $scope.treatmentName
		};
		$scope.review.text = $scope.reviewText;
		$scope.review.sideEffects = $scope.sideEffects;
		
		if($scope.yearOfBirth){
			$scope.review.yearOfBirth = $scope.yearOfBirth;
		}
		if($scope.gender){
			$scope.review.gender = $scope.gender;
		}			
		
		TreatmentReview.createReview($scope.reviewId, $scope.review)
			.then(function(result){
				Disease.deleteNames();
				Treatment.deleteNames();
				SideEffect.deleteNames();
				var newReviewId = result._id;
				
				eventSvc(TreatmentEvent, TreatmentEventMemory).commit().then(function(){
					if($scope.userLoggedState == "unverified") {
						var newPath = "/Bewertung/" + newReviewId;
						$location.path(newPath);
					} else {
						var newPath = $scope.diseaseName + "/" + $scope.treatmentName;
						newPath = Title.urlEncode(newPath);
						newPath = encodeURI(newPath);
						$scope.createReviewSaving = null;
						$location.path(newPath);
					}
				});				
		}, function(error){
			//show error
			$scope.createReviewSaving = null;
			Alerts.error("Fehler in Behandlungsbewertung");
		});
	};
		
	$scope.register = function() {
		alert("adding after registration process");
		$scope.createReviewSaving = true;
		
		$scope.review.disease = {
			name : $scope.diseaseName
		};
		$scope.review.treatment = {
			name : $scope.treatmentName
		};
		$scope.review.text = $scope.reviewText;
		$scope.review.sideEffects = $scope.sideEffects;
		if($scope.yearOfBirth){
			$scope.review.yearOfBirth = $scope.yearOfBirth;
		}
		if($scope.gender){
			$scope.review.gender = $scope.gender;
		}		
		
		TreatmentReview.create($scope.reviewId, $scope.review)
			.then(function(){
				$scope.closeRegistration();
				Disease.deleteNames();
				Treatment.deleteNames();
				SideEffect.deleteNames();

				eventSvc(TreatmentEvent, TreatmentEventMemory).commit().then(function(){
					$scope.createReviewSaving = null;
					$location.path(encodeURIComponent($scope.diseaseName) + "/" + encodeURIComponent($scope.treatmentName));
				});
		}, function(error){
			$scope.closeRegistration();
			$scope.createReviewSaving = null;
			//show error
			Alerts.error("Fehler in Behandlungsbewertung");
		});
	};
	
	$scope.opts = {
		backdropFade : true,
		dialogFade : true
	};
	
	$("#treatmentName").focus();
	
	$scope.selectMale = function() {
		$scope.gender = "male";
		$("#male").removeClass("semitransparent");
		$("#female").addClass("semitransparent");
	};
	
	$scope.selectFemale = function() {
		$scope.gender = "female";
		$("#male").addClass("semitransparent");
		$("#female").removeClass("semitransparent");
	};
	
	$scope.initYears = function() {
		var actualYear = parseInt(new Date().getFullYear());
		for (var i = actualYear; i >= 1900; i--) {
			$scope.list_years.push({id:i, name:i});
		}
	};
	
	
	$('input[name="year"]').keyup(function(e)
			{
			    if (/\D/g.test(this.value))
			    {
			        // Filter non-digits from input value.
			        this.value = this.value.replace(/\D/g, '');
			    }
			    
			    if (this.value.length>4)
			    {
			        // Filter non-digits from input value.
			        this.value = this.value.substring(0,4);
			    }
			});
	
	function isSideEffectAlreadyAdded(newSideEffect, notify) {
		var result = false;
		
		for(var i=0; i<$scope.sideEffects.length; i++){
			var se = $scope.sideEffects[i];
			if(se.sideEffect.name.toUpperCase() == newSideEffect.toUpperCase()){
				result = true;
				if(notify){
					iosOverlay({
						text: "Bereits vorhanden",
						duration: 2e3,
						icon: "assets/iosOverlay/img/cross.png"
					});
				}
				return result;
			}
		}
		return result;
	};
	
	function removeProposedSideEffect(sideEffectName) {
		/*
		$("#side_effect_selector").select2(
			{
				query : function(query) {
					var data = {
						results : []
					};
					
					//TODO
					
					query.callback(data);
				},
			}
		);
		*/
	};
	
	$scope.addSideEffect = function() {
		$scope.noSideEffect = false;

		$("#side_effect_selector").select2("val", "");
		if($scope.newSideEffect.length == 0){
			return;
		}

		if(isSideEffectAlreadyAdded($scope.newSideEffect, true)) {
			return;
		}
		
		if($scope.newSideEffect == noSideEffectText){
			$scope.sideEffects = [];
			$scope.noSideEffect = true;
			return;
		}
		
		$scope.sideEffects.push({
			severity:0,
			sideEffect:{
				id:sideEffectIndex, 
				name:$scope.newSideEffect, 
			}
		});
		
		removeProposedSideEffect($scope.newSideEffect);
		
		sideEffectIndex++;
		$scope.newSideEffect = "";
	};
	
	$scope.removeSideEffect = function(sideEffect) {
		for(var i=0; i<$scope.sideEffects.length; i++){
			var se = $scope.sideEffects[i];
			if(se.sideEffect.id == sideEffect.sideEffect.id){
				$scope.sideEffects.splice(i, 1);
			}
		}
		
	};
	
	$scope.setSeverity = function(sideEffect, aSeverity) {
		sideEffect.severity = SideEffect.normalize(aSeverity);
	};
	
	$scope.isSelectedSeverity = function(sideEffect, aSeverity) {
		var result = false;
		if(sideEffect.severity == SideEffect.normalize(aSeverity)) {
			result = true;
		}
		return result;
	};
	
	//rating
	$scope.$watch('review.rating', function(v){
		validateStep($scope.step);
	});
	
	//////////////////////////////////////////////////////////////////////
	// steps
	
	$scope.step = 1;
	$scope.minLengthDescription = 50;
	$scope.maxLengthDescription = 5000;
	
	function isValidStep(step) {
		var result = false;
		
		switch(step)
		{
		case 1:
			if($scope.diseaseName.length > 0) {
				result = true;
			}
		  break;
		case 2:
			if($scope.treatmentName.length > 0) {
				result = true;
			}
		  break;
		case 3:
//			if($scope.reviewText.length >= $scope.minLengthDescription) {
				result = true;
//			}
		  break;
		case 5:
			result = true;
		  break;
		default:
			result = true;
		}
		
		return result;
	}
	
	function getStepTitle(step){
		var title = "";
		if (step == 1) {
			title = "Gesundheitsanliegen";
		} else if (step == 2) {
			title = "Behandlung";
		} else if (step == 3) {
			title = "Bewertung";
		} else if (step == 4) {
			title = "Nebenwirkungen";
		} else if (step == 5) {
			title = "Kosten";
		} else if (step == 6) {
			title = "Persönliche Angaben";
		} else if (step == 7) {
			title = "Anmeldung/Registrierung";
		}
		return title;
	}
	
	function validateStep(step) {
		
		var maxStepsCount = $scope.getStepsMaxNumber();
		
		var prevBtn = $(".btn_prev_step");
		var nextBtn = $(".btn_next_step");
		var nextStep = $(".wizardNextButton");
		
		if (isValidStep(step)) {
			
			$(nextStep).removeClass("disabled");
			
			if(step == maxStepsCount){
				$(nextBtn).addClass("disabled");
			} else {
				$(nextBtn).removeClass("disabled");
			}
			
		} else {
			$(nextBtn).addClass("disabled");
			$(nextStep).addClass("disabled");
		}
		
		if(step == 1)	{
			$(prevBtn).addClass("disabled");
		} else {
			$(prevBtn).removeClass("disabled");
		}
		
		var nextStepTitle = $(".wizardTitle");
		
		$(nextStepTitle).text(step + ". " + getStepTitle(step)); 
		
		$scope.wizardBreadcrumb = [];
		for(var i = 1;i<=step;i++){
			$scope.wizardBreadcrumb.push(getStepTitle(i));
		}
	}
	
	function validateToStep(step) {
		for(var i=1;i<=step;i++){
			if(!isValidStep(i)){
				return false;
			}
		}
		return true;
	}

	$scope.chgStep = function(how){
		if ($scope.step == 2) {
			if($rootScope.userLogged && $scope.selectedTreatmentEvent.category){
				var valid = $scope.saveTreatmentEvent();
				if (!valid) {
					return false;
				}
			}
		}
		if (how == -1){
			var step = $scope.step;
			step += how;
			if(step < 1)	{
				step=1;
			}
			$scope.step = step;
			validateStep($scope.step);
			return;
		}
		
		if (isValidStep($scope.step)) {
			var maxStepsCount = $scope.getStepsMaxNumber();
			var step = $scope.step;
			step += how;
			if(step < 1)	{
				step=1;
			}
			if(step > maxStepsCount){
				step = maxStepsCount;
			}
			$scope.step = step;
			validateStep($scope.step);
		}
	};
	
	$scope.toStep = function(step){
		var maxStepsCount = $scope.getStepsMaxNumber();
		
		if(step < 1)	{
			step=1;
		}
		if(step > maxStepsCount){
			step = maxStepsCount;
		}
		if(validateToStep(step - 1)){
			$scope.step = step;
			validateStep($scope.step);
		}
	};
	
	$scope.validateStepInternal = function (){
		validateStep($scope.step);
	};
	
	$scope.stepClass = function(step){
		return "wizardPage " + ($scope.step == step?"wizardPageActive":"");
	};
	
	$scope.stepClassInfo = function(step){
		var result =  "wizardInfo " + ($scope.step == step?"wizardInfoActive":"");
		
		if($scope.step != step && validateToStep(step - 1)){
			result = result + " makePointer";
		}
		
		return result;
	};
	
	$scope.stepProgressClassInfo = function(step){
		var result =  "progressInfo " + (($scope.step >= step)?"progressInfoActive":"");
				
		return result;
	};
	
	
	$scope.selectedTabClassInfo = function(loginTab) {
		var result =  "wizardInfo " + ($scope.loginTabSelected ? "wizardInfoActive":"");
		return result;
	};
	
	validateStep($scope.step);
	
	$scope.isQuestionsPanel1Collapsed = function(){
		var r = $scope.review;
		if(r.dateOfFirstSymptoms!=null || r.dateOfDiagnosis!=null || r.cured!=null){
			return false;
		} else {
			return true;
		}
	};

	$scope.isQuestionsPanel2Collapsed = function(){
		return $scope.treatmentEvents.length == 0;
	};

	$('#questionsPanel1').on('show.bs.collapse', function () {
		var elem = $("#questionsPanel1Icon");
		$(elem).addClass("collapse-icon-rotated");
	});
	
	$('#questionsPanel1').on('hide.bs.collapse', function () {
		var elem = $("#questionsPanel1Icon");
		$(elem).removeClass("collapse-icon-rotated");
	});

	$('#questionsPanel2').on('show.bs.collapse', function () {
		var elem = $("#questionsPanel2Icon");
		$(elem).addClass("collapse-icon-rotated");
	});
	
	$('#questionsPanel2').on('hide.bs.collapse', function () {
		var elem = $("#questionsPanel2Icon");
		$(elem).removeClass("collapse-icon-rotated");
	});
	
}]);
