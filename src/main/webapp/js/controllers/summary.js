'use strict';

angular.module(appName).controller('SummaryCtrl', [ '$scope', '$location', '$routeParams', 'TreatmentReviewSummary', 'Title',
                                                   function($scope, $location, $routeParams, TreatmentReviewSummary, Title) {

	TreatmentReviewSummary
	.findBySummaryId($routeParams.summaryId)
	.then(
			function( data) {
				if (data.length > 0) {
					var diseaseName = data[0].disease.name;
					var treatmentName = data[0].treatment.name;
					var newPath = diseaseName + "/" + treatmentName;
					newPath = Title.urlEncode(newPath);
					newPath = encodeURI(newPath);
					$scope.createReviewSaving = null;
					$location.path(newPath);
				}
	});
}]);