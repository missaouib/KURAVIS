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

var appName = "kuravis";

/* App Module */
var app = angular.module(appName, ['ngGrid', 'ngRoute', 'ui.bootstrap', 'angularFileUpload', appName + 'Services' ]);

app.config([ '$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $locationProvider.html5Mode(true).hashPrefix("!");
    
	$routeProvider.when('/', {
		templateUrl : '/partials/start.html',
		controller : 'StartCtrl'
	}).
	when('/home', {
		templateUrl : '/partials/start.html',
		controller : 'StartCtrl'
	}).
	when('/Behandlungen', {
		templateUrl : '/partials/disease.html',
		controller : 'DiseaseCtrl'	
	}).
	when('/Nutzenbewertung', {
		templateUrl : '/partials/edit.html',
		controller : 'EditCtrl'
	}).
	
	when('/Nutzenbewertung/:diseaseName', {
		templateUrl : '/partials/edit.html',
		controller : 'EditCtrl'
	})
	
	.when('/Nutzenbewertung/:diseaseName/:treatmentName', {
		templateUrl : '/partials/edit.html',
		controller : 'EditCtrl'
	}).
	when('/Bewertung', {
		templateUrl : '/partials/review.html',
		controller : 'ReviewCtrl'
	}).
	
	when('/Bewertung/:reviewId', {		// THIS MUST be still available for creating reviews by user that have not activated their accounts yet, therefore the review(and treatment/disease)is not visible in the system yet
		templateUrl : '/partials/review.html',
		controller : 'ReviewCtrl'
	}).
	when('/review/:reviewId', {		// THIS MUST be still available for creating reviews by user that have not activated their accounts yet, therefore the review(and treatment/disease)is not visible in the system yet
		templateUrl : '/partials/review.html',
		controller : 'ReviewCtrl'
	}).
	when('/summary/:summaryId', {
		templateUrl : '/partials/activate.html',
		controller : 'SummaryCtrl'
	}).
	when('/Nutzer', {
		templateUrl : '/partials/user.html',
		controller : 'UserCtrl'
	}).
	when('/Nutzer/:userId', {
		templateUrl : '/partials/user.html',
		controller : 'UserCtrl'
	}).
/*when('/votes/:reviewId', {
		templateUrl : '/partials/votes.html',
		controller : 'VotesCtrl'
	}).
*/	
	when('/Tagebuch/:userId/:year/:month/:day', {
		templateUrl : '/partials/diary.html',
		controller : 'DiaryCtrl'
	}).
	when('/Tagebuch/:userId/:year/:month', {
		templateUrl : '/partials/diary.html',
		controller : 'DiaryCtrl'
	}).
	when('/Tagebuch/:userId/:year/', {
		templateUrl : '/partials/diary.html',
		controller : 'DiaryCtrl'
	}).
	when('/Tagebuch/:userId', {
		templateUrl : '/partials/diary.html',
		controller : 'DiaryCtrl'
	}).
	when('/Tagebuch', {
		templateUrl : '/partials/diary.html',
		controller : 'DiaryCtrl'
	}).
	when('/activate/:id', {
		templateUrl : '/partials/activate.html',
		controller : 'ActivateCtrl'
	}).
	when('/admin', {
		templateUrl : '/partials/admin.html',
		controller : 'AdminCtrl'
	}).	
	when('/admin/:id', {
		templateUrl : '/partials/admin.html',
		controller : 'AdminCtrl'
	}).
	when('/Einstellungen', {
		templateUrl : '/partials/settings.html',
		controller : 'SettingsCtrl'
	}).
	when('/notifications', {
		templateUrl : '/partials/notifications.html',
		controller : 'NotificationsCtrl'
	}).
	when('/Einladung', {
		templateUrl : '/partials/invitation.html',
		controller : 'InvitationCtrl'
	}).
	when('/Einladung/:email', {
		templateUrl : '/partials/edit.html',
		controller : 'EditCtrl'
	}).		
	when('/invitation/:email', {
		templateUrl : '/partials/edit.html',
		controller : 'EditCtrl'
	}).		
	when('/test', {
		templateUrl : '/partials/test.html',
		controller : 'TestCtrl'
	}).
	when('/resetpassword/:id', {
		templateUrl : '/partials/resetpassword.html',
		controller : 'ResetPasswordCtrl'
	}).
	when('/signedout', {
		templateUrl : '/partials/signedout.html',
		controller : 'EmptyCtrl'
	}).
	when('/text/tour', {
		templateUrl : '/partials/text/tour.html',
		controller : 'EmptyCtrl'
	}).
	when('/text/treatment', {
		templateUrl : '/partials/text/treatment.html',
		controller : 'EmptyCtrl'
	}).
	when('/text/blog', {
		templateUrl : '/partials/text/blog.html',
		controller : 'EmptyCtrl'
	}).
	when('/About', {
		templateUrl : '/partials/text/about.html',
		controller : 'EmptyCtrl'
	}).
	when('/text/privacy', {
		templateUrl : '/partials/text/privacy.html',
		controller : 'EmptyCtrl'
	}).
	when('/text/codex', {
		templateUrl : '/partials/text/codex.html',
		controller : 'EmptyCtrl'
	}).
	when('/subscriptions', {
		templateUrl : '/partials/subscription.html',
		controller : 'SubscriptionCtrl'
	}).	
	when('/Gesundheitsverzeichnis', {
		templateUrl : '/partials/dictionary.html',
		controller : 'DictionaryCtrl'
	}).
	when('/dictionary/:id', {
		templateUrl : '/partials/dictionary.html',
		controller : 'DictionaryCtrl'
	}).
	when('/:diseaseName', {
		templateUrl : '/partials/disease.html',
		controller : 'DiseaseCtrl'
	}).
	when('/:diseaseName/:treatmentName', {
		templateUrl : '/partials/treatment.html',
		controller : 'TreatmentCtrl'
	}).
	when('/:diseaseName/:treatmentName/:reviewId', {
		templateUrl : '/partials/review.html',
		controller : 'ReviewCtrl'
	}).
	when('/:diseaseName/:treatmentName/:reviewId/Avis', {
		templateUrl : '/partials/votes.html',
		controller : 'VotesCtrl'
	}).
	otherwise({
		redirectTo : '/'
	});
} ]);

app.config([ '$httpProvider', function($httpProvider) {
	$httpProvider.defaults.useXDomain = true;
	// delete $httpProvider.defaults.headers.common['X-Requested-With'];
} ]);

app.controller('EmptyCtrl', [ function() {}]);

app.filter('ratingSingleValue', function() {
	return function(rating, decimalPlaces) {
		if (typeof(rating) !== 'undefined') {
			if(rating == null || (rating !== 0 && rating==""))	{//note strict equality
				return "";
			}
			//0 -> 5
			//0.25 -> 4
			//0.5 -> 3
			//0.75 -> 2
			//1 -> 1
			var r = 1;
			for(var i=0;i<decimalPlaces;i++){
				r *= 10;
			}
			return Math.round((5 + rating * (-4)) * r) / r;
		} else return ""; 
	};
});

app.filter('calcRating',  [ '$filter', function(filter) {
	var ratingFilter = filter('ratingSingleValue');
	return function(ratings) {
		if (typeof(ratings) !== 'undefined' && ratings.length > 0) {
			var sum = 0;
			var count = 0;
			for(var i=0;i<ratings.length;i++){
				var r = ratings[i]; 
				sum += r.count * parseFloat(r.name);
				count += r.count;
			}
			return sum / count;
		} else return "";
	};
}]);

app.filter('ratingNameFromSingleRatingValue', [ '$filter', function(filter) {
	var ratingFilter = filter('ratingSingleValue'); 
    return function(input) {
    	if (typeof(input) !== 'undefined') {
			if (input != null) {
				var rating = ratingFilter(input, 1);
				if (rating >= 4.5) {
		            return "Sehr gut";
		        } else if (rating >= 3.5) {
		            return "Gut";
		        } else if (rating >= 2.5) {
		            return "Befriedigend";
		        } else if (rating >= 1.5) {
		            return "Mangelhaft";
		        } else if (rating >= 1) {
		            return "Ungenügend";
		        } else {
		            return "";
		        }				
			} else return "";
		} else return ""; 	 
    };
}]);

app.filter('age', function() {
	return function(input) {
		var ah = ageHlp; // TODO change
		return ah.denormalizeToDouble(input);
	};
});

app.filter('severity', function() {
	return function(input) {
		var sh = severityHlp; // TODO change
		return sh.denormalizeToDouble(input);
	};
});

function isStringEmpty(str) {
	return (!str || 0 === str.length);
}

app.filter('formatDate', function() {
	return function(input) {
		if (!input) {
			return "";
		}
		var d = new Date(input);
		return d.toUTCString();
	};
});

app.filter('formatAsIsoDate', function() {
	return function(input) {
		if (!input) {
			return "";
		}
		var d = new Date(input);
		return d.toISOString();
	};
});

app.filter('toLocaleDateString', function() {
    return function(input) {
        if (!input) {
            return "";
        }
        var d = new Date(input);
        return d.toLocaleDateString();
    };
});

app.filter('toLocaleString', function() {
    return function(input) {
        if (!input) {
            return "";
        }
        var d = new Date(input);
        return d.toLocaleString();
    };
});

app.filter('curedStateLocalization', function() {
    return function(input) {
        if (input) {
            return "Kuriert";
        } else return "Nicht kuriert";
    };
});

app.filter('treatmentEventDescription', function() {
    return function treatmentEventDescription(treatmentEvent) {
        if (!treatmentEvent) {
            return "";
        }
        var result = "";
        if (treatmentEvent.duration != null) {
        	//Psychotherapie, Physiotherapie
        	result += treatmentEvent.duration;
        	if(treatmentEvent.unit) {
        		result += " " +  treatmentEvent.unit.name;
        	}
        } else if (treatmentEvent.quantity != null) {
        	//treatment plan
        	result += treatmentEvent.quantity;
        	if(treatmentEvent.type) {
        		result += " " +  treatmentEvent.type.name;
        	}
        }
        if (treatmentEvent.dose != null) {
        	result = result + " á " + treatmentEvent.dose + " " +  treatmentEvent.unit.name;
        }
        if ((treatmentEvent.end) && (!isNaN( treatmentEvent.end.getTime()))) {
        	result += " vom " + treatmentEvent.start.toLocaleDateString() + " - " +  treatmentEvent.end.toLocaleDateString() + " um " + treatmentEvent.start.toLocaleTimeString() ;
        } else if (treatmentEvent.start != null) {
        	result += " am " + treatmentEvent.start.toLocaleDateString();
        }
        return result;
    };
});

app.filter('fmtTreatmentEvent', function() {
    return function fmtTreatmentEvent(treatmentEvent) {
        if (!treatmentEvent) {
            return "";
        }
        var result = "";
        if (treatmentEvent.type != null) {
        	result = treatmentEvent.type.name;
        }
        if (treatmentEvent.dose != null) {
        	result = result + " " + treatmentEvent.dose + " " +  treatmentEvent.unit.name;
        }
        return result;
    };
});

app.filter('getAgeFromYear', function() {
	return function getAge(dateString) {
	    if (!dateString || (dateString === "Unknown")) {
	        return "";
	    }
	    
	    var todayDate = new Date();
	    var usersBirthDate = new Date("01 Jan " + dateString);
	    var usersAge = todayDate.getFullYear() - usersBirthDate.getFullYear();
	    var m = todayDate.getMonth() - usersBirthDate.getMonth();
	    if (m < 0 || (m === 0 && todayDate.getDate() < usersBirthDate.getDate())) {
	    	usersAge--;
	    }
	    return usersAge;
	};
});

app.filter('fmtElapsedTimeFromNow', function() {
    return function fmtElapsedTimeFromNow(dateValue) {
    	var numberValue = 0;
        if (dateValue) {
            var today = new Date();
            var age = today - dateValue;
            numberValue = Math.floor((age / (60)) / 1000);
        }
        var result = numberValue.toString();
        if (numberValue == 0) {
            result = " 1 Minute";
        } else if (numberValue < 60) {
            result = result + ((numberValue == 1) ? (" Minute") : (" Minuten"));
        } else  if (numberValue < (24 * 60)) {
            var hours = Math.floor(numberValue / 60);
            result = hours + ((hours == 1) ? (" Stunde") : (" Stunden"));        	
        } else  if (numberValue < (30 * 24 * 60)) {
            var days =  Math.floor(numberValue / (24 * 60));
            result = days + ((days == 1) ? (" Tag") : (" Tagen"));        	
        } else {
            var months =  Math.floor(numberValue / (30 * 24 * 60));
            result = months + ((months == 1) ? (" Monat") : (" Monaten"));        	
        } 
        return "vor " + result;
    };
});

app.filter('fmtFollowEntityType', function() {
    return function fmtEntityType(entityType) {
    	if (entityType == "disease") {
    		return "Krankheit";
    	} else if (entityType == "treatmentreviewsummary") {
    		return "Behandlung";
    	} else if (entityType == "treatment") {
    		return "Behandlung";
    	} else return entityType;
    	
    };
});

app.filter('getSeverityName', function() {
    return function getSeverityDefaultValue(input) {
    	var sh = severityHlp;
		var severityNumber = sh.denormalizeToDouble(input);
        var severityName = "Keine";
    	if (severityNumber == 3) {
            severityName = "Extrem";
        } else if (severityNumber == 2) {
            severityName = "Schwer";
        } else if (severityNumber == 1) {
            severityName = "Mittel";
        } else if (severityNumber == 0) {
            severityName = "Leicht";
        }
        return severityName;
    };
});

 app.filter('getSeverityDefaultValue', function() {
    return function getSeverityDefaultValue(severityNumber, severityValueName) {
        var severityName = "Keine";
        if (severityNumber >= 0.75) {
            severityName = "Schwer";
        } else if (severityNumber >= 0.50) {
            severityName = "Mittel";
        } else if (severityNumber >= 0.25) {
            severityName = "Leicht";
        }
        if (severityName == severityValueName)
        {
            return " btn-primary";
        } else return "";
    };
});
 
 app.filter('formatNumberToString', function() {
    return function getSeverityDefaultValue(number, baseString) {
        if (number == 1) {
            return baseString;
        } else return baseString + "en";
    };
});
 
app.filter('getLocalizationForStatus', function() {
	    return function getLocalizationForStatus(statusString) {
	    	if (statusString == "active") {
	    		return "Aktiv";
	    	} else if (statusString == "inactive") {
	    		return "Inaktiv";
	    	} else if (statusString == "unverified"){
	    		return "Nicht verifiziert, ";
	    	} else return statusString;
 	    };
});
	 
 
 app.filter('formatNumberToStringWithE', function() {
    return function getSeverityDefaultValue(number, baseString) {
        if (number == 1) {
            return baseString;
        } else return baseString + "e";
    };
});
  
 
 
 app.filter('formatAgeAndGender', function() {
	    return function formatAgeAndGender(age, gender) {
	    	if (gender == "male") {	//localization
	    		gender = "männlich";
	    	} else if (gender == "female") {
	    		gender = "weiblich";
	    	}
	    	
	        if ((!age)  && (!gender)) { // (typeof(input) !== 'undefined')
	        	return "";
	        } else if ((!age) && (gender)) {
	        	return "(" + gender + ")";
	        } else if ((age) && (!gender)) { 
	        	return "(" + age + " Jahre alt)";
	        } else {
	        	return "(" + age + " Jahre alt, " + gender + ")";
	        }
	    };
	});
 
 app.filter('formatGender', function() {
	    return function formatGender(gender) {
	    	if (gender == "male") {	//localization
	    		gender = "männlich";
	    	} else if (gender == "female") {
	    		gender = "weiblich";
	    	} else return "";
	    	return gender;
	    };
	});

 app.filter('localizedReportReason', function() {
	    return function(input) {

    	if (typeof(input) !== 'undefined') {
			if (input != null) {
				if (input == 1 ) {
		            return "Spam";
		        } if (input == 2 ) {
		            return "Schmähkritik";
		        } if (input == 3 ) {
		            return "Unsachliche Bewertung";
		        } if (input == 4 ) {
		            return "Sonstiges";
		        } else {
		            return "";
		        } 
			} else return "";
    	} else return "";
	    };
	});

 app.filter('fraudReportEntityType', function() {
	    return function fmtEntityType(val) {
	    	if (val == "treatmentreview") {	//localization
	    		val = "Bewertung";
	    	} else if (val == "treatmentreviewevent") {
	    		val = "Kommentar";
	    	} else return "";
	    	return val;
	    };
	});
 
app.directive('ngEnter', function() {
		return function(scope, element, attrs) {
			element.bind("keydown keypress", function(event) {
				if (event.which === 13) {
					scope.$apply(function() {
						scope.$eval(attrs.onEnter);
					});

					event.preventDefault();
				}
			});
		};
});


app.filter('urlEncode', function() {
    return function(url) {
        if (!url) {
            return "";
        }
        var result = url;
        result = result.replace(/ä/g, '-ae-');
        result = result.replace(/Ä/g, '-Ae-');
        result = result.replace(/Ö/g, '-Oe-');
        result = result.replace(/ö/g, '-oe-');
        result = result.replace(/Ü/g, '-Ue-');
        result = result.replace(/ü/g, '-ue-');
        result = result.replace(/ß/g, '-ss-');
        result = result.replace(/ /g, '__');
        if (encodeURIComponent) {
        	result = encodeURIComponent(result);
        	result = encodeURIComponent(result);
        }
        
        return result;
    };
});

app.value('frequencyOptions', [
	       		                       { id: 'ONCE', name: 'Einmalig' },
	    		                       { id: 'DAILY', name: 'Täglich' },
	    		                       { id: 'WEEKLY', name: 'Wöchentlich' },
	    		                       { id: 'MONTHLY', name: 'Monatlich' }
	    		                       ]);

app.value('treatmentCategoryOptions', [
	                                   { id: 'PRESCRIPTION_MEDICINE', name: 'Verschreibungspflichtiges Medikament' },
	    		                       { id: 'NON_PRESCRIPTION_MEDICINE', name: 'Nicht verschreibungspflichtiges Medikament' },
	    		                       { id: 'HOMEOPATHY', name: 'Homöopathie' },
	    		                       { id: 'FOOD_SUPPLEMENTS', name: 'Nahrungsergänzung' },
	    		                       { id: 'COMPLEMENTARY_MEDICINE', name: 'Komplementäre Medizin' },
	    		                       { id: 'ALTERNATIVE_MEDICINE', name: 'Alternative Medizin' },
	    		                       { id: 'PHYSIOTHERAPY', name: 'Physiotherapie' },
	    		                       { id: 'PSYCHOTHERAPY', name: 'Psychotherapie' },
	    		                       { id: 'MEDICAL_DEVICES', name: 'Medizinisches Gerät' },
	    		                       { id: 'ELECTRONIC_DEVICES', name: 'Elektronisches Gerät' },
	    		                       { id: 'OPERATION', name: 'Operation' },	    		                       	    		                       
	    		                       { id: 'OTHERS', name: 'Anderes' }
	                         ]);

app.value('eventTypeOptions', [
   		                       { id: 'REVIEW', name: 'Review' },
		                       { id: 'REVIEW_VOTE', name: 'Review vote' },
		                       { id: 'NOTE', name: 'Note' },
		                       { id: 'TREATMENT', name: 'Treatment' },
		                       { id: 'WEIGHT', name: 'Weight' },
		                       ]);


app.value('getIdForName', function(array, categoryName) {
	var arrayLength = array.length;
	for (var i = 0; i < arrayLength; i++) {
	    if (array[i].name === categoryName) {
	    	return array[i].id;
	    }
	}
	return "";
});

app.value('getNameForId', function(array, categoryId) {
	var arrayLength = array.length;
	for (var i = 0; i < arrayLength; i++) {
	    if (array[i].id === categoryId) {
	    	return array[i].name;
	    }
	}
	return "";
});
