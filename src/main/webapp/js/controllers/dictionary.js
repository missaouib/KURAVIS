'use strict';

angular.module(appName).controller('DictionaryCtrl',
		[ '$scope', 'TreatmentReviewSummary', '$location', '$routeParams', function($scope, TreatmentReviewSummary, $location, $routeParams) {
			TreatmentReviewSummary.getAllDiseaseLetters().then(function(data) {
				$scope.letters = [];
				for ( var index = 0; index < data.length; index++) {
					$scope.letters.push(data[index]["index"]);
				}
			});

			$scope.objStyle = function(l) {
				if ($routeParams.id == l) {
					return true;
				}
				return false;
			};

			if ($routeParams.id) {
				TreatmentReviewSummary.getAllDiseaseTreatments($routeParams.id).then(function(data){
					$scope.content = data;
					$scope.tableData = [];
					for (var index = 0; index < data.length; index++) {
						var treatments = data[index].treatments;
						for (var subIndex = 0; subIndex < treatments.length; subIndex++) {
							var obj = {
									"disease":data[index].name,
									"treatment":treatments[subIndex].name
							};
							$scope.tableData.push(obj);
						}
					}
				});
			}
		} ]);
