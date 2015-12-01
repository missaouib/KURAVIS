'use strict';

angular.module(appName).controller('VotesCtrl',['$scope','$location','TreatmentReviewEvents','$routeParams','Alerts', 'TreatmentReview', 'User','Title', 'TmpTreatmentReview', function($scope, $location, TreatmentReviewEvents, $routeParams, Alerts, TreatmentReview, User, Title, TmpTreatmentReview) {
	Title.set(Title.defaultTitle());
	
	Alerts.clear();
	
	$scope.user = null;
	$scope.diseaseName = "";
	
	TreatmentReview.queryById($routeParams.reviewId).then(function(result){
		$scope.review = result;
		$scope.diseaseName = $scope.review.disease.name;
		Title.set($scope.review.disease.name + " > " + $scope.review.treatment.name + " > " + $scope.review.text + " > AVIS Stimmen");
		if($scope.diseaseName.length > 0) {
			$(".searchbar").select2("data", {id : null, text : $scope.diseaseName, count : "0"});
		}
		$scope.votesCount = result.votesCount;
		checkIfUserVoted($scope.review._id, ($scope.userLogged ? $scope.userLogged._id : null));
		
		User.queryById($scope.review.author._id).then(function(result) {
			$scope.user = result;
		}, function() {
			Alerts.error("Fehler beim Laden");
		});
	}, function() {
		TmpTreatmentReview.queryById($routeParams.reviewId).then(function(result) {
			$scope.review = result;
			$scope.diseaseName = $scope.review.disease.name;
			if($scope.diseaseName.length > 0) {
				$(".searchbar").select2("data", {id : null, text : $scope.diseaseName, count : "0"});
			}			
			User.queryById($scope.review.author._id).then(function(result) {
				$scope.user = result;
			}, function() {
				Alerts.error("Fehler beim Laden");
			});			
		}, function(){
			Alerts.error("Fehler beim Laden");
		});
		
	});
	
	$scope.vote = function(){
		if($scope.review){
			if (($scope.userLogged != null) && ($scope.userLogged.state == "unverified")) {
				$scope.doActivateAccountText = "Um die Avis Funktion zu verwenden, bestätigen Sie bitte zuerst Ihr Konto. Sie haben hierzu eine E-Mail mit einem Aktivierungslink erhalten.";
				$scope.doActivateAccount = true;
			} else { 
				TreatmentReview.voteFor($scope.review._id).then(function(result){
					$scope.votesCount =(result.count ? result.count : 0);
					checkIfUserVoted($scope.review._id, ($scope.userLogged ? $scope.userLogged._id : null));
				}, function(data, result){
					Alerts.error(data.toSource());
					Alerts.error(status);
				});
			}
		}
	};
	
	function checkIfUserVoted(reviewId, userId){
		if(($scope.review) && (userId)) {
			TreatmentReviewEvents.findVotesByReviewIdUserId(reviewId, userId).then(function(result){
				$scope.userVotedForCurrentReview = result.length;
				findVotesByReview(reviewId);
			}, function(data, result){
				Alerts.error(data.toSource());
				Alerts.error(status);
			});
		} else if(!userId){
			findVotesByReview(reviewId);
		}
		
	};

	$scope.$on('userLogged', function() {	
		checkIfUserVoted($routeParams.reviewId, ($scope.userLogged ? $scope.userLogged._id : null));
	});
	
	
	function findVotesByReview(reviewId){
		TreatmentReviewEvents.findVotesByReview(reviewId).then(function(result){
			$scope.votes = result;
			
		},function(){Alerts.error("Fehler beim Laden");});
	};
	
}]);
