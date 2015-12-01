'use strict';

angular.module(appName).controller('UserCtrl',['$scope', '$rootScope','$location', '$window', 'User', 'Disease', 'Statistics', 'TreatmentReview', 'TreatmentReviewEvents', 'TreatmentReviewVotes','TreatmentReviewComment', '$routeParams', 'Alerts','Title','TmpTreatmentReview',  
                                               function($scope, $rootScope, $location, $window,User, Disease, Statistics, TreatmentReview, TreatmentReviewEvents, TreatmentReviewVotes, TreatmentReviewComment, $routeParams, Alerts,Title, TmpTreatmentReview) {
	
	Alerts.clear();

	Title.set(Title.defaultTitle());
	
	/*
	//Unread
	$scope.unreadNotifications = 0;
	$scope.loadNotificationsUnreadCount = function() {
		User.loadNotificationsUnreadCount()
		.then(function(data) {
			$scope.unreadNotifications = data.count;	
			if(data.count>0){
				
			}
		});
	};
	$scope.loadNotificationsUnreadCount();
	*/
	$scope.doDeleteReview = false;
	$scope.reviewToDelete = null;
	
	$scope.doMakeSettings = function() {
		$location.path("/settings/" + $scope.user._id);
	};
	
	function showError() {
//		hiding for production
	}
	
	$scope.loggedUserId = "";
	$scope.loggesUserState = "";
	$scope.userVerified = true;
	$scope.initLoggedUser = function() {
		var loggedUser = $rootScope.userLogged;
		if ((loggedUser != null) && (loggedUser != 'undefined')) {
			$scope.loggedUserId = loggedUser._id;
			$scope.loggedUserState = loggedUser.state;
			for (var i=0;i<loggedUser.roles.length;i++){
				if("nonverified_user" === loggedUser.roles[i]) {
					$scope.userVerified = false;
				}
			}
		}
	};

	$scope.$on('userLogged', function() {	//init the data later, for the case when userLogged will be not available at the beginning (network latency)
		$scope.initLoggedUser();
	});
	$scope.initLoggedUser();
	
	User.queryById($routeParams.userId).then(function(result) {
		$scope.user = result;
		getPageViewCount($scope.user._id);
		$scope.treatmentReviewNextPage();
	}, function() {
		//Alerts.error("Fehler beim Laden");
	});
	
	function getPageViewCount(userId){
		Statistics.create(userId, "userdetail").then(function(result){
			$scope.userPageViewCount = result["viewsCount"];
		}, function(){
			//Alerts.error("Fehler beim Laden");
		});
	};
	/*
	//Notifications
	$scope.notificationBusy = false;
	$scope.notificationReachEnd = false;
	$scope.notificationAfter = 1;
	$scope.notifications = [];
	
	$scope.notificationsNextPage = function() {
	
		if ($scope.notificationBusy) return;
		if ($scope.notificationReachEnd) return;
    
		$scope.notificationBusy = true;

		if($scope.notifications == null){
			$scope.notifications = [];
		}
    
		User.loadUnreadNotifications($scope.notificationAfter, 10, "createdOn", "desc")
		.then(function(data) {
									
			for (var i = 0; i < data.length; i++) {
				if($scope.notifications == null) {
					$scope.notifications = [];
				}
				$scope.notifications.push(data[i]);
			}
			if(data.length == 0){
				$scope.notificationReachEnd = true;
			}
			
			if($scope.notifications.length == 0){
				$scope.notfound = true;
			}
			
			$scope.notificationAfter++;
		    $scope.notificationBusy = false;
			
		}, function() {
			showError();
		});
	};
	*/
	
	//TreatmentReview
	$scope.busy = false;
	$scope.reachEnd = false;
	$scope.after = 1;
	$scope.selectedDiseaseId = null;
	$scope.reviews = [];
	$("#popover1").popover({placement:'top'});
	$scope.treatmentReviewNextPage = function() {
	
		if ($scope.busy) return;
		if ($scope.reachEnd) return;
    
		$scope.busy = true;

		if($scope.reviews == null){
			$scope.reviews = [];
		}
		
		if ($scope.loggedUserState  == "unverified") {
			TmpTreatmentReview.findUserReviews($scope.user._id, $scope.after, 100)
			.then(function(data) {
				$scope.reviews = data.reviews;
				$scope.currentCount = data.currentCount;
				$scope.curedCount = data.curedCount;			
				$rootScope.loading(false);
			}, function() {
				showError();
			});
		} else {
			TreatmentReview.findUserReviews($scope.user._id, $scope.after, 100)
			.then(function(data) {
				$scope.reviews = data.reviews;
				$scope.currentCount = data.currentCount;
				$scope.curedCount = data.curedCount;
				$rootScope.loading(false);
			}, function() {
				showError();
			});
		}
	};
	
	//TreatmentReviewEvents
	$scope.userEventsBusy = false;
	$scope.userEventsReachEnd = false;
	$scope.userEventsAfter = 1;
	$scope.userEvents = [];
  
	$scope.userEventsNextPage = function() {
	
		if ($scope.userEventsBusy) return;
		if ($scope.userEventsReachEnd) return;
    
		$scope.userEventsBusy = true;

		if($scope.userEvents == null){
			$scope.userEvents = [];
		}
    
		TreatmentReviewEvents.findByUserPageNumberPageSize($routeParams.userId, $scope.userEventsAfter, 10)
		.then(function(data) {
			for (var i = 0; i < data.length; i++) {
				if($scope.userEvents == null) {
					$scope.userEvents = [];
				}
				$scope.userEvents.push(data[i]);
			}
			if(data.length == 0){
				$scope.userEventsReachEnd = true;
			}
			
			if($scope.userEvents.length == 0){
				$scope.reviewNotfound = true;
			}
			
			$scope.userEventsAfter++;
		    $scope.userEventsBusy = false;
		}, function() {
			showError();
		});
	};
	
	//TreatmentReviewComment
	$scope.userDetailsReviewsBusy = false;
	$scope.userDetailReviewsReachEnd = false;
	$scope.userDetailReviewReviewAfter = 1;
	$scope.userDetailReviewComments = [];
  
	$scope.userDetailReviewCommentsNextPage = function() {
	
		if ($scope.userDetailsReviewsBusy) return;
		if ($scope.userDetailReviewsReachEnd) return;
    
		$scope.userDetailsReviewsBusy = true;

		if($scope.userDetailReviewComments == null){
			$scope.userDetailReviewComments = [];
		}
    
		TreatmentReviewComment.findByUserPageNumberPageSize($routeParams.userId, $scope.userDetailReviewReviewAfter, 10)
		.then(function(data) {
			for (var i = 0; i < data.length; i++) {
				if($scope.userDetailReviewComments == null) {
					$scope.userDetailReviewComments = [];
				}
				$scope.userDetailReviewComments.push(data[i]);
			}
			if(data.length == 0){
				$scope.userDetailReviewsReachEnd = true;
			}
			
			if($scope.userDetailReviewComments.length == 0){
				$scope.reviewNotfound = true;
			}
			
			$scope.userDetailReviewReviewAfter++;
		    $scope.userDetailsReviewsBusy = false;
		}, function() {
			showError();
		});
	};
	
	//TreatmentReviewVotes
	$scope.userDetailsVotesBusy = false;
	$scope.userDetailVotesReachEnd = false;
	$scope.userDetailVotesAfter = 1;
	$scope.userDetailVotes = [];
  
	$scope.userDetailVotesNextPage = function() {
	
		if ($scope.userDetailsVotesBusy) return;
		if ($scope.userDetailVotesReachEnd) return;
    
		$scope.userDetailsVotesBusy = true;

		if($scope.userDetailVotes == null){
			$scope.userDetailVotes = [];
		}
		TreatmentReviewVotes.findByUserIdPageNumberPageSize($routeParams.userId, $scope.userDetailVotesAfter, 10)
		.then(function(data) {
			for (var i = 0; i < data.length; i++) {
				if($scope.userDetailVotes == null) {
					$scope.userDetailVotes = [];
				}
				$scope.userDetailVotes.push(data[i]);
			}
			if(data.length == 0){
				$scope.userDetailVotesReachEnd = true;
			}
			
			if($scope.userDetailVotes.length == 0){
				$scope.votesNotfound = true;
			}
			
			$scope.userDetailVotesAfter++;
		    $scope.userDetailsVotesBusy = false;
		}, function() {
			showError();
		});
	};
	
	$scope.isSelectedDiseaseState = function(disease, aState) {
		var result = false;
		if(disease.state == aState) {
			result = true;
		}
		return result;
	};
	
	$scope.selectReview = function(review) {
		$location.path("/Bewertung/" + review._id);
	};
	
	$scope.editReview = function(review) {
		//$location.path("edit").search({reviewId: review._id, diseaseName:review.disease.name , treatmentName:review.treatment.name});
		var newPath = "/Nutzenbewertung/" + review.disease.name + "/" + review.treatment.name + "/";
		newPath = encodeURI(newPath);
		newPath = encodeURI(newPath);	//WHY do I have to call it twice??
		$location.path(newPath).search({reviewId: review._id});
	};
	
	$scope.deleteReview = function() {
		$scope.doDeleteReview = false;
		//action
		TreatmentReview.deleteReview($scope.reviewToDelete._id)
		.then(function(data) {
			$rootScope.loading(true);
			$scope.busy = false;
			$scope.treatmentReviewNextPage();
			/*
			for(var i = $scope.reviews.length; i--;) {
		          if($scope.reviews[i] === $scope.reviewToDelete) {
		        	  $scope.reviews.splice(i, 1);
		        	  $scope.reviewToDelete = null;
		          }
		      }*/
		}, function() {
			showError();
		});
	};
	
	$scope.openDeletion = function(review){
		$scope.reviewToDelete = review;
		$scope.doDeleteReview = true;
	};
	
	$scope.closeDeletion = function(){
		$scope.reviewToDelete = null;
		$scope.doDeleteReview = false;
	};
	
	$scope.newDisease = "";
	var diseaseIndex = 0;
	var preload_disease_data = [];
	
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
							
						});
		
		
		
	};
	
	$("#disease_selector")
	.select2(
			{
				placeholder : "Gesundheitsanliegen",
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
					if(!result.count){
						result.count = 0;
					}
					if(result.isNew){
						result.count = 0;
						newString = '<span class="label success">New</span> ';
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
		$scope.newDisease = e.added.text;
	});
	
	function isDiseaseAlreadyAdded() {
		var result = false;
		
		for(var i=0; i<$scope.user.diseases.length; i++){
			var d = $scope.user.diseases[i];
			if(d.disease.name == $scope.newDisease){
				result = true;
				iosOverlay({
					text: "Bereits vorhanden",
					duration: 2e3,
					icon: "assets/iosOverlay/img/cross.png"
				});
				return result;
			}
		}
		return result;
	};
	
	$scope.addDisease = function() {
		$("#disease_selector").select2("val", "");
		if($scope.newDisease.length == 0){
			return;
		}

		if(isDiseaseAlreadyAdded()) {
			return;
		}
		
		if($scope.user.diseases ==  null){
			$scope.user.diseases = [];
		}
		$scope.user.diseases.push({
			//createdOn:0,
			state:1,
			treatmentHeardFrom: "kuravis",
			disease:{
				_id:diseaseIndex.toString(), 
				name:$scope.newDisease, 
			}
		});
		
		User.updateUserProfile($scope.user).then(function(result) {
			diseaseIndex++;
			$scope.newDisease = "";
		});
		
		
	};
	
	$scope.removeDisease = function(disease) {
		for(var i=0; i<$scope.user.diseases.length; i++){
			var d = $scope.user.diseases[i];
			if(d.disease._id == disease.disease._id){
				$scope.user.diseases.splice(i, 1);
				User.updateUserProfile($scope.user).then(function(result) {
					
				});
			}
		}
		
	};
	
	$scope.setDiseaseState = function(disease, aState) {
		disease.state = aState;
		if(aState == '2'){
			$scope.diseaseToTreatmentHeardFrom = disease;
			$scope.doTreatmentHeardFrom = true;
		}
	};
	
	$scope.isSelectedDiseaseState = function(disease, aState) {
		var result = false;
		if(disease.state == aState) {
			result = true;
		}
		return result;
	};
	
	$scope.closeTreatmentHeardFrom = function() {
		User.updateUserProfile($scope.user).then(function(result) {
			alert("USER PROFILE UPDATED");
			$scope.doTreatmentHeardFrom = false;
			$scope.diseaseToTreatmentHeardFrom = null;
		});
	};
	
	$scope.selectedDiseaseHeardFrom = function(){
		var result = $scope.diseaseToTreatmentHeardFrom.treatmentHeardFrom;
		return result;
	};
	
}]);
