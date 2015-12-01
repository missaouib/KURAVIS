'use strict';

angular.module(appName).controller('ReviewCtrl', [ '$scope', '$rootScope', '$location', 'TreatmentReview', 'TreatmentEvent', 'User', '$routeParams','Report', 'Statistics', 'TreatmentReviewEvents', 'TreatmentReviewComment', 'Disease', 'Alerts', 'Title','TmpTreatmentReview','frequencyOptions','treatmentCategoryOptions','getNameForId',
                                                   function($scope, $rootScope, $location, TreatmentReview, TreatmentEvent, User, $routeParams,Report, Statistics, TreatmentReviewEvents, TreatmentReviewComment, Disease, Alerts, Title, TmpTreatmentReview,frequencyOptions,treatmentCategoryOptions, getNameForId) {
	Alerts.clear();
	$scope.diseaseName = "";
	$scope.buttonText = null;
	$scope.isMyReview = false;
	$scope.frequencyOptions = frequencyOptions; 
	$scope.treatmentCategoryOptions = treatmentCategoryOptions; 	
    //  the following simple make the textbox "Auto-Expand" as it is typed in
    $("#reviewtextarea").keyup(function(e) {
        //  the following will help the text expand as typing takes place
        while($(this).outerHeight() < this.scrollHeight + parseFloat($(this).css("borderTopWidth")) + parseFloat($(this).css("borderBottomWidth"))) {
            $(this).height($(this).height()+15);
        };
    });

    function afterLoad(review){
    	$scope.urlPrefix = review.disease.name + "/" + review.treatment.name;
		checkIfReviewIsMy();
    }
    
    TreatmentReview.queryById($routeParams.reviewId).then(function(result) {
		$scope.review = result;
		$scope.diseaseName = $scope.review.disease.name;
		afterLoad(result);
		Title.set($scope.review.disease.name + " > " + $scope.review.treatment.name + " > " + $scope.review.text);		
		$scope.votesCount = result.votesCount;
		$scope.reviewCommentsCount = $scope.review.reviewCommentsCount;
		checkIfUserVoted($routeParams.reviewId, ($scope.userLogged ? $scope.userLogged._id : null));
		getPageViewCount($routeParams.reviewId); 
		loadAuthor($scope.review.author._id);
		$scope.loadAllTreatmentEvents();
		if($scope.diseaseName.length > 0) {
			$(".searchbar").select2("data", {id : null, text : $scope.diseaseName, count : "0"});
		}
		
	}, function(){
		TmpTreatmentReview.queryById($routeParams.reviewId).then(function(result) {
			$scope.review = result;
			$scope.diseaseName = $scope.review.disease.name;
			afterLoad(result);
			Title.set($scope.review.disease.name + " > " + $scope.review.treatment.name + " > " + $scope.review.text);		
			$scope.votesCount = result.votesCount;
			$scope.reviewCommentsCount = $scope.review.reviewCommentsCount;
			checkIfUserVoted($routeParams.reviewId, ($scope.userLogged ? $scope.userLogged._id : null));
			getPageViewCount($routeParams.reviewId); 
			loadAuthor($scope.review.author._id);	
		}, function(){
			Alerts.error("Fehler beim Laden");
		});
	});	
	
	$scope.$on('userLogged', function() {	
		checkIfUserVoted($routeParams.reviewId, ($scope.userLogged ? $scope.userLogged._id : null));
		checkIfReviewIsMy();
	});
	
	function loadAuthor(userId) {
		User.queryById(userId).then(function(result) {
			$scope.author = result;
		}, function() {
			Alerts.error("Fehler beim Laden");
		});
	};	
	
	$scope.loadAllTreatmentEvents = function() {
		if ($scope.review != null) {
			 
			var userId = $scope.review.author._id;

			TreatmentEvent.findAllForReview(userId, $scope.review.disease._id, $scope.review.treatment._id, 0, 0, 20, "start", "desc").then(
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
							var categoryName = getNameForId($scope.treatmentCategoryOptions, tempTreatmentEvent.category);
							tempTreatmentEvent.category = categoryName;			
							
							var frequencyName = getNameForId($scope.frequencyOptions, tempTreatmentEvent.frequency);
							tempTreatmentEvent.frequency = frequencyName;			
						}
					} 
					$scope.treatmentEvents = result;
				}
			);
		}
	};
	
	function checkIfReviewIsMy() {
		var loggedUserId = null;
		if (($rootScope.userLogged != null) && ($rootScope.userLogged != 'undefined')) {
			loggedUserId = $rootScope.userLogged._id;
		}
		if ($scope.review && $scope.review.author._id == loggedUserId) {
			// edit my review
			$scope.isMyReview = true;
		}
		if($scope.isMyReview){
			$scope.buttonText = "Bearbeiten";
		} else {
			$scope.buttonText = "Bewerten";
			if ($scope.review) {
				$scope.buttonText = $scope.review.treatment.name + " " + $scope.buttonText;
			}
		}
	};

	$scope.doMakeReview = function() {
		var newPath = "/Nutzenbewertung/" + encodeURIComponent($scope.review.disease.name) + "/" + encodeURIComponent($scope.review.treatment.name);
		if ($scope.isMyReview) {
			// edit review
			$location.path(newPath).search({reviewId: $scope.review._id});
		} else {
			// create review
			$location.path(newPath);
		}
	};
	
	$scope.vote = function(){
		if($scope.review){
			if (($scope.userLogged != null) && ($scope.userLogged.state == "unverified")) {
				$scope.doActivateAccountText = "Um die Avis Funktion zu verwenden, bestätigen Sie bitte zuerst Ihr Konto. Sie haben hierzu eine E-Mail mit einem Aktivierungslink erhalten.";
				$scope.doActivateAccount = true;
			} else { 
				TreatmentReview.voteFor($scope.review._id).then(function(result){
					$scope.votesCount =(result.count ? result.count : 0);
					checkIfUserVoted($routeParams.reviewId, ($scope.userLogged ? $scope.userLogged._id : null));
				}, function(data, result){
					Alerts.error(data.toSource());
					Alerts.error(status);
				});
			}
		}
	};
	
	$scope.createReview = function() {
		
		TreatmentReview.existForUser($scope.review.disease._id, $scope.review.treatment._id).then(function(result) {
			if(result.exists == true){
				Alerts.error("Upps, Sie haben bereits eine Behandlungsbewertung für "+ $scope.diseaseName +" und "+ $scope.treatmentName +" erstellt. Wenn Sie Ihre bisherige Bewertung ändern möchten, gehen Sie einfach auf Ihr Profil und klicken Sie auf bearbeiten.");
			} else {
				var newPath = "/Nutzenbewertung/" + $scope.review.disease.name + "/" + $scope.review.treatment.name + "/";
				newPath = encodeURI(newPath);
				newPath = encodeURI(newPath);
				if ($scope.userLogged != null) {
					if ($scope.userLogged._id == $scope.review.author._id) {
						$location.path(newPath).search({reviewId: $scope.review._id});
					} else {
						$location.path(newPath);
					}
				} else {
					$location.path(newPath);
				} 
			}
			
		}, function(){
			var newPath = "/Nutzenbewertung/" + $scope.review.disease.name + "/" + $scope.review.treatment.name + "/";
			newPath = encodeURI(newPath);
			newPath = encodeURI(newPath);
			if ($scope.userLogged != null) {
				if ($scope.userLogged._id == $scope.review.author._id) {
					$location.path(newPath).search({reviewId: $scope.review._id});
				} else {
					$location.path(newPath);
				}
			} else {
				$location.path(newPath);
			} 
		});
	};
	
	function checkIfUserVoted(reviewId, userId){
		if(($scope.review) && (userId)) {
			TreatmentReviewEvents.findVotesByReviewIdUserId(reviewId, userId).then(function(result){
				$scope.userVotedForCurrentReview = result.length;
			}, function(data, result){
				Alerts.error(data.toSource());
				Alerts.error(status);
			});
		}
	};
	
	function getPageViewCount(reviewId){
		Statistics.create(reviewId, "treatmentreviewdetail").then(function(result){
			$scope.reviewPageViewCount = result["viewsCount"];
		});
	};
	
	$scope.addComment = function(){
		//show modal
		TreatmentReviewComment.create($routeParams.reviewId, $scope.modalText).then(function(){
			loadAllComments();
			$("#reviewtextarea").height(25);
		});
		$scope.showingModalText = false;
		$scope.modalText = "";
	};
	
	$scope.reportTreatmentReview = function(){
		//show modal
		Report.create($scope.selectedTreatmentReportId, $scope.selectedReportClass, $scope.reportModalText, $scope.reportedText, $scope.reportedUserId,	$scope.reportedUserName, 
				$routeParams.reviewId, $scope.reportType).then(function(){
				});
		$scope.showingModalText = false;
		$scope.reportModalText = "";
	};
	
	$scope.loadAllComments = function(){
		loadAllComments();
	};
	
	$scope.showReportModalView = function(treatmentReportId, title, selectedReportClass, reportedText, reportedUserId, reportedUserName, reportingItemLocalizedString){
		$scope.showingModalText = true;
		$scope.reportingTreatmentReview = true;
		$scope.selectedReportClass = selectedReportClass;
		$scope.selectedTreatmentReportId = treatmentReportId;
		$scope.reportedText = reportedText;
		$scope.reportedUserId = reportedUserId;
		$scope.reportedUserName = reportedUserName;
		$scope.modalTextTitle = title;
		$scope.reportingItemLocalizedString = reportingItemLocalizedString;
	};
	
	$scope.showAddCommentModalView = function(){
		$scope.modalTextTitle = "Add new comment";
		$scope.showingModalText = true;
		$scope.addingComment = true;
	};
	
	$scope.modalTextConfirmed = function() {
		if ($scope.reportingTreatmentReview) {
			$scope.reportTreatmentReview();
		} else {
			if (($scope.userLogged != null) && ($scope.userLogged.state == "unverified")) {
				$scope.doActivateAccountText = "Um die Kommentar Funktion zu verwenden, bestätigen Sie bitte zuerst Ihr Konto.\nSie haben hierzu eine E-Mail mit einem Aktivierungslink erhalten. ";
				$scope.doActivateAccount = true;
			} else {
				$scope.addComment();
			}
		}

		$scope.addingComment = false;
		$scope.reportingTreatmentReview = false;
	};
	
	function loadAllComments(){
		TreatmentReviewEvents.findCommentsByReview($routeParams.reviewId).then(function(result){
			$scope.userEvents = result;
			$scope.reviewCommentsCount = result.length;
		});
	};
	
	//TreatmentReviewEvents - Comments
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
    
		TreatmentReviewEvents.findCommentsByReviewPageNumberPageSize($routeParams.reviewId, $scope.userEventsAfter, 10)
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
		
	$scope.reportReview = function(reviewId) {
		$scope.showReportModalView(reviewId, 'Melden Sie bitte verdächtige Behandlungsbewertungen und helfen Sie uns, die Qualität bei KURAVIS sicherzustellen.',
				'treatmentreview', $scope.review.text, $scope.review.author._id, $scope.review.author.name, 'diese Bewertung');
	};
} ]);
