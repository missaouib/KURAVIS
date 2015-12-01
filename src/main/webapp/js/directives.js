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

var app = angular.module(appName);

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


app.directive('infiniteScroll', [ '$rootScope', '$window', '$timeout', function($rootScope, $window, $timeout) {
return {
	link : function(scope, elem, attrs) {
		var checkWhenEnabled, handler, scrollDistance, scrollEnabled;
		$window = angular.element($window);
		scrollDistance = 0;
		if (attrs.infiniteScrollDistance != null) {
			scope.$watch(attrs.infiniteScrollDistance, function(value) {
				return scrollDistance = parseInt(value, 10);
			});
		}
		scrollEnabled = true;
		checkWhenEnabled = false;
		if (attrs.infiniteScrollDisabled != null) {
			scope.$watch(attrs.infiniteScrollDisabled, function(value) {
				scrollEnabled = !value;
				if (scrollEnabled && checkWhenEnabled) {
					checkWhenEnabled = false;
					return handler();
				}
			});
		}
		handler = function() {
			var elementBottom, remaining, shouldScroll, windowBottom;
			windowBottom = $window.height() + $window.scrollTop();
			elementBottom = elem.offset().top + elem.height();
			remaining = elementBottom - windowBottom;
			shouldScroll = remaining <= $window.height() * scrollDistance;
			if (shouldScroll && scrollEnabled) {
				if ($rootScope.$$phase) {
					return scope.$eval(attrs.infiniteScroll);
				} else {
					return scope.$apply(attrs.infiniteScroll);
				}
			} else if (shouldScroll) {
				return checkWhenEnabled = true;
			}
		};
		$window.on('scroll', handler);
		scope.$on('$destroy', function() {
			return $window.off('scroll', handler);
		});
		return $timeout((function() {
			if (attrs.infiniteScrollImmediateCheck) {
				if (scope.$eval(attrs.infiniteScrollImmediateCheck)) {
					return handler();
				}
			} else {
				return handler();
			}
		}), 0);
	}
};
} ]);

app.directive("scroll", function ($window) {
return function(scope, element, attrs) {
    angular.element($window).bind("scroll", function() {
         if (this.pageYOffset > 317) {
             scope.boolChangeClass = true;
         } else {
             scope.boolChangeClass = false;
         }
         return scope.$apply(attrs.scroll);
    });
};
});

app.directive('buttonsRadio', function() {
return {
 restrict: 'E',
 scope: { model: '=', options:'='},
 controller: function($scope){
     $scope.activate = function(option){
         $scope.model = option;
     };      
 },
 template: "<button type='button' class='btn' "+
             "ng-class='{active: option == model}'"+
             "ng-repeat='option in options' "+
             "ng-click='activate(option)'>{{option}} "+
           "</button>"
};
});

/** var ngColorPicker = angular.module('directive.colorPicker', []);**/

var colors = ["#fdb3ad", "#f79c88", "#fc8376", "#dd6c7d", "#c95b60", "#c1c9a3", "#96a16c", "#768347", "#799181", "#558581", 
              "#898fa7", "#a5c4db", "#7caacb", "#5f8daa", "#33627c", "#daaf94", "#b8a093", "#967b66", "#7c4e46", "#685245"];

/**
* Method taken from Brian Grinstead and modified the return 
* of rounded r,g and b.
* @https://github.com/bgrins/TinyColor/blob/master/tinycolor.js
**/
function hsvToRgb(h, s, v) {
h*=6;
var i = ~~h,
  f = h - i,
  p = v * (1 - s),
  q = v * (1 - f * s),
  t = v * (1 - (1 - f) * s),
  mod = i % 6,
  r = [v, q, p, p, t, v][mod] * 255,
  g = [t, v, v, q, p, p][mod] * 255,
  b = [p, p, t, v, v, q][mod] * 255;
  
  return [~~r, ~~g, ~~b, "rgb("+ ~~r + "," + ~~g + "," + ~~b + ")"];
}

function rgbToHex(r, g, b) {
return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
}

function setBackground(element, color) {
element.css({
  "background-color" : color
});
}

app.directive('ngColoorPicker', [function() {
return {
  restrict: 'E',
  replace: true,
  transclude : false,
  scope : '@=',
  template: '<div class="colorPicker"><table>'
  +           '<tr><td ng-repeat="color in colorList1">'
  +             '<div class="colorPickerItem" ng-style="{backgroundColor: color.color,border:color.select +\'px solid #000\'}" style="border: {{color.select}}px solid #000; padding: 5px; background-color: {{color.color}}" ng-click="selectColorBig(color)"></div>'
  +           '</td></tr>'
  +           '<tr><td ng-repeat="color in colorList2">'
  +             '<div class="colorPickerItem" ng-style="{backgroundColor: color.color,border:color.select +\'px solid #000\'}" style="border: {{color.select}}px solid #000; padding: 5px; background-color: {{color.color}}" ng-click="selectColorBig(color)"></div>'
  +           '</td></tr>'
  +           '<tr><td ng-repeat="color in colorList3">'
  +             '<div class="colorPickerItem" ng-style="{backgroundColor: color.color,border:color.select +\'px solid #000\'}" style="border: {{color.select}}px solid #000; padding: 5px; background-color: {{color.color}}" ng-click="selectColorBig(color)"></div>'
  +           '</td></tr>'
 
  +         '</table></div>',
  compile: function compile(tElement, tAttrs, transclude) {
    return {
      post: function postLink(scope, iElement, iAttrs, controller) { 
        scope.modelObject   = iAttrs.modelObject;
        scope.modelProperty = iAttrs.modelProperty;
        scope.modelArray = iAttrs.modelArray;
        scope.modelSubarray = iAttrs.modelSubarray;
        scope.colorList1 = [];
        scope.colorList2 = [];
        scope.colorList3 = [];
        var bigLimit = 10;
        var index = 0;
        angular.forEach(colors, function(color) {
          if (index < bigLimit) {
              scope.colorList1.push({
                color : color,
                select : 0
              });
          } else if (index < (2 * bigLimit)) {
              scope.colorList2.push({
                color : color,
                select : 0
              });
          } else {
              scope.colorList3.push({
                color : color,
                select : 0
              });
          }
          index++;
        });
      }
    };
  },
  controller: function($scope, $element, $timeout) {
    $scope.selectColorBig = function(color) {
      for (var i = 0; i < $scope.colorList1.length; i++) {
        $scope.colorList1[i].select = 0;
        if ($scope.colorList1[i].color === color.color) {
          $scope[$scope.modelObject][$scope.modelArray][$scope.modelSubarray][$scope.modelProperty] = color.color; 
          $scope.colorList1[i].select = 2;
        }
      }
      for (var i = 0; i < $scope.colorList2.length; i++) {
        $scope.colorList2[i].select = 0;
        if ($scope.colorList2[i].color === color.color) {
             $scope[$scope.modelObject][$scope.modelArray][$scope.modelSubarray][$scope.modelProperty] = color.color;              
          $scope.colorList2[i].select = 2;
        }
      }
      for (var i = 0; i < $scope.colorList3.length; i++) {
        $scope.colorList3[i].select = 0;
        if ($scope.colorList3[i].color === color.color) {
             $scope[$scope.modelObject][$scope.modelArray][$scope.modelSubarray][$scope.modelProperty] = color.color;       
          $scope.colorList3[i].select = 2;
        }
      }          
    };
  }
};
}]);
app.directive('ngColoorPickerSmall', [function() {
return {
  restrict: 'E',
  replace: true,
  transclude : false,
  scope : '@=',
  template: '<div class="colorPickerSmall"><table>'
  +           '<tr><td ng-repeat="color in colorListSmall1">'
  +             '<div class="colorPickerItemSmall" ng-style="{backgroundColor: color.color,border:color.select +\'px solid #000\'}" style="border: {{color.select}}px solid #000; padding: 5px; background-color: {{color.color}}" ng-click="selectColorSmall(color)"></div>'
  +           '</td></tr>'
  +           '<tr><td ng-repeat="color in colorListSmall2">'
  +             '<div class="colorPickerItemSmall" ng-style="{backgroundColor: color.color,border:color.select +\'px solid #000\'}" style="border: {{color.select}}px solid #000; padding: 5px; background-color: {{color.color}}" ng-click="selectColorSmall(color)"></div>'
  +           '</td></tr>'
  +           '<tr><td ng-repeat="color in colorListSmall3">'
  +             '<div class="colorPickerItemSmall" ng-style="{backgroundColor: color.color,border:color.select +\'px solid #000\'}" style="border: {{color.select}}px solid #000; padding: 5px; background-color: {{color.color}}" ng-click="selectColorSmall(color)"></div>'
  +           '</td></tr>'
  +           '<tr><td ng-repeat="color in colorListSmall4">'
  +             '<div class="colorPickerItemSmall" ng-style="{backgroundColor: color.color,border:color.select +\'px solid #000\'}" style="border: {{color.select}}px solid #000; padding: 5px; background-color: {{color.color}}" ng-click="selectColorSmall(color)"></div>'
  +           '</td></tr>'
 
  +         '</table></div>',
  compile: function compile(tElement, tAttrs, transclude) {
    return {
      post: function postLink(scope, iElement, iAttrs, controller) { 
        scope.modelObject   = iAttrs.modelObject;
        scope.modelProperty = iAttrs.modelProperty;
        scope.modelArray = iAttrs.modelArray;
        scope.modelSubarray = iAttrs.modelSubarray;
        scope.colorListSmall1 = [];
        scope.colorListSmall2 = [];
        scope.colorListSmall3 = [];
        scope.colorListSmall4 = [];
        
        var smallLimit = 5;
        var index = 0;
        angular.forEach(colors, function(color) {
          if (index < smallLimit) {
              scope.colorListSmall1.push({
                color : color,
                select : 0
              });
          } else if (index < (2 * smallLimit)) {
              scope.colorListSmall2.push({
                color : color,
                select : 0
              });
          } else if (index < (3 * smallLimit)) {
              scope.colorListSmall3.push({
                  color : color,
                  select : 0
                });
            } else {
              scope.colorListSmall4.push({
                color : color,
                select : 0
              });
          }
          index++;
        });
      }
    };
  },
  controller: function($scope, $element, $timeout) {
    $scope.selectColorSmall = function(color) {
      for (var i = 0; i < $scope.colorListSmall1.length; i++) {
        $scope.colorListSmall1[i].select = 0;
        if ($scope.colorListSmall1[i].color === color.color) {
          $scope[$scope.modelObject][$scope.modelArray][$scope.modelSubarray][$scope.modelProperty] = color.color; 
          $scope.colorListSmall1[i].select = 2;
        }
      }
      for (var i = 0; i < $scope.colorListSmall2.length; i++) {
        $scope.colorListSmall2[i].select = 0;
        if ($scope.colorListSmall2[i].color === color.color) {
             $scope[$scope.modelObject][$scope.modelArray][$scope.modelSubarray][$scope.modelProperty] = color.color;              
          $scope.colorListSmall2[i].select = 2;
        }
      }
      for (var i = 0; i < $scope.colorListSmall3.length; i++) {
        $scope.colorListSmall3[i].select = 0;
        if ($scope.colorListSmall3[i].color === color.color) {
             $scope[$scope.modelObject][$scope.modelArray][$scope.modelSubarray][$scope.modelProperty] = color.color;       
          $scope.colorListSmall3[i].select = 2;
        }
      }
      for (var i = 0; i < $scope.colorListSmall4.length; i++) {
          $scope.colorListSmall4[i].select = 0;
          if ($scope.colorListSmall4[i].color === color.color) {
               $scope[$scope.modelObject][$scope.modelArray][$scope.modelSubarray][$scope.modelProperty] = color.color;       
            $scope.colorListSmall4[i].select = 2;
          }
       }          
      
    };
  }
};
}]);

app.directive('ngAvatar', [function() {
return {
  restrict: 'E',
  replace: false,
  scope: { 
	  color:"@",
	  name:"@",
	  fullname:"@"
  },
  template: '<div title="{{fullname}}" class="userheaderavatar" ng-style="{backgroundColor: color}" style="background-color:{{color}}">{{name}}</b></div>'
};
}]);

app.directive('ngYouAvatar', [function() {
	return {
	  restrict: 'E',
	  replace: false,
	  scope: { 
	  },
	  template: '<div class="numberCircle">' +
		    '<div class="height_fix"></div>' +
		    '<div class="content">+</div>'
	};
	}]);

app.directive('ngAvatarSmall', [function() {
return {
  restrict: 'E',
  replace: false,
  scope: { 
	  color:"@",
	  name:"@",
	  fullname:"@"
  },
  template: '<div title="{{fullname}}" class="smallAvatar" ng-style="{backgroundColor: color}" style="background-color:{{color}}" >{{name}}</b></div>',
};
}]);


app.factory('Title', function ($window) {
return {
    set: function(val){
        $window.document.title = val;
    },
    defaultTitle: function() { 
    	return "Bewertungen von Behandlungen, Medikamenten, Therapien, alternativen Heilmethoden - KURAVIS"; 
    },
    urlDecode : function(url) {
        if (!url) {
            return "";
        }
        var result = url;
        result = result.replace(/-ae-/g, 'ä');
        result = result.replace(/-Ae-/g, 'Ä');
        result = result.replace(/-Oe-/g, 'Ö');
        result = result.replace(/-oe-/g, 'ö');
        result = result.replace(/-Ue-/g, 'Ü');
        result = result.replace(/-ue-/g, 'ü');
        result = result.replace(/-ss-/g, 'ß');
        result = result.replace(/__/g, ' ');
        
        return result;
    },
    urlEncode : function(url) {
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
        
        return result;
    },    
};
});

app.directive('ngEnter', function() {
return function(scope, element, attrs) {
    element.bind("keydown keypress", function(event) {
        if(event.which === 13) {
            scope.$apply(function(){
                scope.$eval(attrs.ngEnter);
            });

            event.preventDefault();
        }
    });
};
});

app.directive('ngTitle', [function() {
return {
	restrict: 'E',
    replace: false,
    scope: { 
    	text:"@"
    },
    template: '<div class="title">{{text}}</div>'
};
}]);

app.directive('ngLighttitle', [function() {
	return {
		restrict: 'E',
	    replace: false,
	    scope: { 
	    	text:"@"
	    },
	    template: '<div class="lighttitle">{{text}}</div>'
	};
	}]);

app.directive('ngSubtitle', [function() {
return {
	restrict: 'E',
    replace: false,
    scope: { 
    	text:"@"
    },
    template: '<div class="subtitle">{{text}}</div>'
};
}]);

app.directive('ngSubtitleSpan', [function() {
	return {
		restrict: 'E',
	    replace: false,
	    scope: { 
	    	text:"@"
	    },
	    template: '<span class="subtitle">{{text}}</span>'
	};
	}]);

app.directive('ngUserheader',  ['$location', function($location) {
	return {
	restrict: 'E',
    replace: false,
    scope: { 
    	letter:"@",
    	color:"@",
    	title:"@",
    	subtitle:"@",
    	id:"@",
    	ratingValue:"@"
    },
    template: '<div class="userheader"><div ng-click="selectUser(id)"><div class="userheaderavatar"  ng-style="{backgroundColor: color}" style="cursor: pointer;float: left;background-color:{{color}}">{{letter}}</div></div><div class="userheaderbody"><div class="userheadertitle" ng-class="{userheadertitlesimple:!subtitle.length}">{{title}}</div><div class="userheadersubtitle" ng-hide="!ratingValue.length">{{subtitle}}</div></div></div>',
    controller: function($scope, $element, $timeout) {
        $scope.selectUser = function(userId) {
        	if ((typeof(userId) !== 'undefined') && (userId != null)) { 
        		var url = "/Nutzer/" + userId;
        		$location.path(url);
        		
        	}
        };
    }
};
}]);

app.directive('ngReviewtext', [function() {
return {
	restrict: 'E',
    replace: false,
    scope: { 
    	report: "&",
    	text:"@",
    	subtitle:"@"
    },
    template: '<div class="reviewtext"><table><tr><td><div class="reviewtextsubtitle"><div class="reviewtextsubtitletext">{{subtitle}}</div></div></td><td><div class="reviewtextreport" data-ng-click="report()"><img title="Melden" src="/assets/images/reportIcon.png"></div></td></tr></table></div>'
};
}]);


app.directive('ngSeverity', [function() {
return {
  restrict: 'E',
  replace: false,
  scope: { 
	  sideEffects:"=",
	  editable:"@",
	  removeEnabled:"@",
	  size:"@"
  },
  template: '<table width="{{size}}%" align="center">' +
  '<tr ng-show="sideEffects.length > 0">'+
  '<td ></td>'+
  '<td align="center">Leicht</td>'+
  '<td align="center">Mittel</td>'+
  '<td align="center">Schwer</td>'+
  '<td align="center">Extrem</td>' +
  '<td ng-show="removeEnabled"></td>' +
  '</tr>' +
  '<tr style="height:20px" data-ng-repeat="side in sideEffects">' +
  '<td width="30%" align="left">'+
  '<div class="sectiontitle">{{side.sideEffect.name}}</div>' +
  '</td>'+
  '<td align="center"  class="lineInTheMiddle">'+  
  '<a ng-click="setSeverity(side,0)"><div class="blackDot" ng-style="{backgroundColor: (side.severity==0)?\'#000000\':\'#FFFFFF\'}"></div></a>'+
  '</td >'+
  '<td align="center"  class="lineInTheMiddle">'+   
  '<a ng-click="setSeverity(side,1)"><div class="blackDot" ng-style="{backgroundColor: (side.severity==(1/3))?\'#000000\':\'#FFFFFF\'}"></div></a>'+
  '</td>'+
  '<td align="center"  class="lineInTheMiddle">'+   
  '<a ng-click="setSeverity(side,2)"><div class="blackDot" ng-style="{backgroundColor: (side.severity==(2/3))?\'#000000\':\'#FFFFFF\'}"></div></a>'+ 
  '</td>'+
  '<td align="center"  class="lineInTheMiddle">'+    
  '<a ng-click="setSeverity(side,3)"><div class="blackDot" ng-style="{backgroundColor: (side.severity==1)?\'#000000\':\'#FFFFFF\'}"></div></a>'+
  '</td> '+

  '<td style="width: 10%;" align="center" ng-show="removeEnabled"> '+
  '<button class="btn btn-sm btn-primary pull-right" ng-click="removeSideEffect(side)">löschen</button>'+
  '</td>'+
  '</tr>'+
  '<tr ng-show="sideEffects.length > 0">'+
  '<td ></td>'+
  '<td align="center">Leicht</td>'+
  '<td align="center">Mittel</td>'+
  '<td align="center">Schwer</td>'+
  '<td align="center">Extrem</td>' +
  '<td ng-show="removeEnabled"></td>' +
  '</tr>' +
  '</table>',
  controller: function($scope, $element, $timeout) {
	  var severityHlp = {
				min : 0,
				max : 3,
				normalize : function(value) {
					return (value - this.min) / (this.max - this.min);
				},
				denormalize : function(value) {
					return value * (this.max - this.min) + this.min;
				},
				denormalizeToInt : function(value) {
					return Math.round(this.denormalize(value));
				},
				denormalizeToDouble : function(value, digitsAfterPoint) {
					return this.denormalize(value).toFixed(digitsAfterPoint);
				}
		};
	  
	  $scope.setSeverity = function(sideEffect, aSeverity) {
		  if ($scope.editable === "true") { 
			  sideEffect.severity = severityHlp.normalize(aSeverity);
		  }
	  };
	  
	  $scope.removeSideEffect = function(sideEffect) {
			for(var i=0; i<$scope.sideEffects.length; i++){
				var se = $scope.sideEffects[i];
				if(se.sideEffect.id == sideEffect.sideEffect.id){
					$scope.sideEffects.splice(i, 1);
				}
			}
			
		};
	}
};
}]);

var ratingCtrl = function(scope, imgWidth, imgActiveWidth, backImg, frontImg, cssSuffix, prefix){
	var numImages = 5;
	
	scope.height = imgWidth;
	scope.backImagesWidth = imgWidth * numImages;
	scope.backImage = backImg;
	scope.frontImage = frontImg;
	scope.prefix = prefix || "";
	scope.width = 0;
	scope.showValue = null;
	scope.cssSuffix = cssSuffix || "";
	
	var ni = numImages -1;
	
	if(scope.value){
		var tmp1 = Math.round((1 - scope.value) * ni * 100) / 100;
		var tmp2 = Math.floor(tmp1);
		var f1 = imgWidth * tmp2;
		var f2 = 0;
		if(tmp1 - tmp2 > 0){
			f2 = (imgWidth - imgActiveWidth) / 2 + imgActiveWidth * (tmp1 - tmp2);
		}
		scope.width = imgWidth + Math.round(f1 + f2);
	}
};
/* if you want to add something AFTER this component, you MUST enclose it in a <div>, e.g. <div><kur-rating/></div>whatever... */
app.directive('kurRating', [function() {
	return {
		restrict: 'E',
	    replace: false,
	    scope: { 
	    	value:"@",
	    	hideNumber:"@",
	    	hideText:"@",
	    	noRatingText:"@"
	    },
	    templateUrl: '/partials/templates/rating.html',
	    link: function(scope, element, attributes) {
	    	attributes.$observe('value', function(v){
	    		ratingCtrl(scope, 20, 14, "red_white.png", "red_full.png", "");
	    	});
	    }
	};
}]);

app.directive('kurRatingLight', [function() {
	return {
		restrict: 'E',
	    replace: false,
	    scope: { 
	    	value:"@",
	    	hideNumber:"@",
	    	hideText:"@",
	    	noRatingText:"@"
	    },
	    templateUrl: '/partials/templates/rating.html',
	    link: function(scope, element, attributes) {
	    	attributes.$observe('value', function(v){
	    		ratingCtrl(scope, 20, 14, "red_white.png", "red_full.png", "Light");
	    	});
	    }
	};
}]);

app.directive('kurRatingBig', [function() {
	return {
		restrict: 'E',
	    replace: false,
	    scope: { 
	    	value:"@",
	    	hideNumber:"@",
	    	hideText:"@",
	    	noRatingText:"@"
	    },
	    templateUrl: '/partials/templates/rating.html',
	    link: function(scope, element, attributes) {
	    	attributes.$observe('value', function(v){
	    		ratingCtrl(scope, 40, 28, "red_white_big.png", "red_full_big.png", "Big");
	    	});
	    }
	};
}]);

app.directive('kurRatingEditBig', [function() {
	return {
		restrict: 'E',
	    replace: false,
	    scope: { 
	    	value:"=",
	    	hideNumber:"@",
	    	hideText:"@",
	    	noRatingText:"@"
	    },
	    template: "<kur-rating-big ng-mousemove='mouseMove($event)' ng-mousedown='mouseDown($event)' ng-mouseleave='mouseLeave()' value='{{intValue}}' no-rating-text='{{noRatingText}}'/>",
	    controller: function($scope, $element, $timeout) {
	    	var imgSize = 40;
	    	var numImages = 5;
	    	$scope.intValue = $scope.value;
	    	
	    	function getIndex(ev){
	    		var d = getClickPosition(ev);
	    		if(d.x > numImages * imgSize){
	    			return -1;
	    		}
	    		return Math.floor(d.x / imgSize);
	    	}
	    	
	    	function getClickPosition(e) {
	    	    var parentPosition = getPosition(e.currentTarget);
	    	    var xPosition = e.clientX - parentPosition.x;
	    	    var yPosition = e.clientY - parentPosition.y;
	    	    return {x:xPosition, y:yPosition};
	    	}
	    	 
	    	function getPosition(element) {
	    	    var xPosition = 0;
	    	    var yPosition = 0;
	    	      
	    	    while (element) {
	    	        xPosition += (element.offsetLeft - element.scrollLeft + element.clientLeft);
	    	        yPosition += (element.offsetTop - element.scrollTop + element.clientTop);
	    	        element = element.offsetParent;
	    	    }
	    	    return { x: xPosition, y: yPosition };
	    	}
	    	
	    	function getValue(ev){
	    		var idx = getIndex(ev);
	    		if(idx <0){
	    			return -1;
	    		}
	    		var ni = numImages - 1;
	    		var value = (ni - idx) / ni;
	    		//5 -> 0
	    		//...
	    		//0 -> 1
	    		return value;
	    	}
	    	
	    	var prevSet = false;
	    	var prevValue = null;
	    	
	    	$scope.mouseMove = function(ev){
	    		//change to new if old is not selected
	    		//console.log("mouse move");

	    		if(!prevSet){
	    			prevValue = $scope.intValue;
	    			prevSet = true;
	    		}
	    		var value = getValue(ev);
	    		if(value >=0){
	    			$scope.intValue = value;
	    		}
	    	};
	    	
	    	$scope.mouseLeave = function(ev){
	    		if(prevSet){
	    			$scope.intValue = prevValue;
	    		}
	    			prevValue = null;
    			prevSet = false;
	    	};
	    	
	    	$scope.mouseDown = function(ev){
	    		var value = $scope.intValue;
    			if(value == $scope.value){
    				value = null;
    			}
    			$scope.intValue = value;
    			$scope.value = value;
    			prevValue = value;
	    	};
	    },
	    link: function(scope, element, attributes){
	    	scope.$watch('value', function(v){
	    		scope.intValue = scope.value;
	    	});
	    }
	};
}]);

app.directive('ngVotescount', [function() {
return {
	restrict: 'E',
    replace: false,
    scope: { 
    	value:"@",
    	text:"@"
    },
    template: '<span class="icon-avis-left votesCountText">{{value}}</span>'
};
}]);

app.directive('ngActiveVotesCount', [function() {
return {
	restrict: 'E',
    replace: false,
    scope: { 
    	value:"@",
    	reviewId:"@",
    	votescount:"@",
    	vote:"&",
    	urlPrefix:"@"
    },
    template: 
    	'<a href="/{{urlPrefix}}/{{reviewId}}/Avis"><span class="icon-avis-left"> </span></a>' +
    	'<a class="votesCountText" data-ng-click="vote()" style="cursor: pointer">{{value}}</a>'
};
}]);

app.directive('ngCommentscount', [function() {
return {
	restrict: 'E',
    replace: false,
    scope: { 
    	value:"@",
    	text:"@"
    },
    template: '<span class="icon-comment-left commentsCountText">{{value}}</span>'
};
}]);

app.directive('usersList', ['$location', '$window', function($location, $window) {
return {
	restrict: 'E',
    replace: true,
    scope: { 
  	  users:"=users",
    },
    templateUrl: '/partials/templates/userslist.html',
      
    controller: function($scope, $element, $timeout) {
        $scope.selectUser = function(value) {
        	if (typeof(value) !== 'undefined') { 
        		 $window.open("Nutzer/" + value._id);
        	}
        };
    }
};
}]);

app.directive('firstPageUsersList', [function() {
return {
	restrict: 'E',
    replace: true,
    scope: { 
  	  users:"=users",
  	  sign:"&"
    },
    template: '<div>' +
	'<div ng-repeat="user in users|limitTo:144">' +
		'<span ng-class="{firstUserListItem: $index%7==0}" class="userListItem" ng-click="selectUser(user)" ng-show="$index != 6">' +
			'<ng-avatar ng-style="{backgroundColor: user.settings.profile.avatarColor}" color="{{user.settings.profile.avatarColor}}" fullname="{{user.name}}" name="{{user.name.substring(0,1)}}"></ng-avatar>' +
		'</span>' +
		'<span ng-class="{firstUserListItem: $index%7==0}" class="userListItem " style="height:45px"  ng-click="sign()" ng-show="$index == 6">' +
		'<ng-you-avatar></ng-you-avatar>' +
		'<img class="thisCanBeYouArrow hidden-xs" src="/assets/images/arrow.png"><span class="thisCanBeYouText hidden-xs" style="color:#FFFFFF" ng-click="sign()"><span style="font-family:SourceSansProSemiBold; font-size:22px;">Registrieren</span><br>Jetzt mitmachen. Kostenlos &amp; anonym.</span></span>' +
		
	'</div>' +
	'<div style="clear: both"></div>' +
	'</div>' 
	,
    controller: function($scope, $element, $timeout, $location) {
        $scope.selectUser = function(value) {
        	if (typeof(value) !== 'undefined') { 
        		$location.path("/Nutzer/" + value._id);
        	}
        };
    }
};
}]);

app.directive('invitationCount', [function() {
	return {
		restrict: 'E',
	    replace: false,
	    scope: { 
	    	value:"@"
	    },
	    template: '<div class="rating"><div class="invitationBox">{{value}}</div></div>'
	};
}]);

app.directive('sharedDiseaseComponent', ["$location", "Disease", function($location, Disease) {
	return {
		restrict: 'E',
	    replace: false,
	    template: '<div class="noPaddingRight">' +
				  '<input id="disease_selector" class="searchbar" type="hidden"></input>' +		
				  '</div>',		
		compile: function compile(tElement, tAttrs, transclude) {
		    return {
		      post: function postLink($scope, iElement, iAttrs, controller) { 
		    	  	$scope.preload_disease_data = [];
					$scope.initDiseases = function() {
						Disease
								.queryNames(false)
								.then(
										function(diseases) {
											for ( var i = 0; i < diseases.length; i++) {
												var disease = diseases[i];
												$scope.preload_disease_data
														.push({
															id : i,
															text : disease.name,
															count : disease.treatmentReviewsCount
														});
											}									
										});
						
					};	
					$scope.initDiseases();	
					
					if($scope.diseaseName.length > 0) {
						$("#disease_selector").select2("data", {id : null, text : $scope.diseaseName.toUpperCase(), count : "0"});
					}
		      }
		    };
		  },				  
	    controller: function($scope, $element, $timeout) {
	    	$scope.newDisease = "";
	    	
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
	    									$scope.preload_disease_data,
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
	    		$scope.diseaseName = e.added.text;
	    		$scope.$apply( $location.path(encodeURIComponent($scope.diseaseName)));
	    	});	
	    }
	};
}]);


app.directive('customSelectOptions', ["$location","$timeout", function($location, $timeout) {
	return {
		restrict: 'E',
	    replace: false,
	    scope: { 
	    	  options:"=options",
	    	  selectedOption:"=selectedOption",
	    	  selectId:"@",
	    	  minimumSearchResult:"@",
	    	  sort:"@",
	    	  action:"&"
	      },	    
	    template: '<div class="noPaddingRight">' +
				  '<input id="customSel" class="customSelectDesign" type="hidden"></input>' +		
				  '</div>',		
		  link: function link($scope, $element, $attrs) {
			  $( $element.find('div')[0].childNodes[0])
	    	.select2(
	    			{
	    				minimumResultsForSearch:$scope.minimumSearchResult,
	    				placeholder : "-",
	    				matcher: function ( term , text, element ) {
	    		            var match = (text.toUpperCase()+element.attr("synonyms").toUpperCase()).indexOf(term.toUpperCase())>=0;
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
	    						newString = '<span class="label success">Neu</span> ';
	    					}
	    					return "<div class='select2-result-label'>"
	    							+ newString 
	    							+ preString
	    							+ "<span class='select2-match' style='font-weight:bold'>"
	    							+ matchString
	    							+ "</span>"
	    							+ postString
	    							+ "</div>";

	    				},
	    				query : function(query) {
	    					var data = {
	    						results : []
	    					};
	    					$
	    							.each(
	    									$scope.options,
	    									function() {
	    										if (query.term.length == 0
	    												|| this.name
	    														.toUpperCase()
	    														.indexOf(
	    																query.term
	    																		.toUpperCase()) >= 0) {

	    												data.results
	    														.push({
	    															id : this.id,
	    															text : this.name,
	    														});

	    										}
	    									});
	    					if($scope.sort){
	    					data.results.sort(function(
	    							a, b) {
	    						if (isNaN(a.text) || isNaN(b.text)) {
	    							return a.text.localeCompare(b.text);
	    						} else {
	    							return (a.text - b.text);
	    						}
	    					});
	    					}

	    					query.callback(data);
	    				}
	    			});
	    	
			  $scope.$watch('selectedOption', function(v){
				  if ($scope.selectedOption) {
					  $( $element.find('div')[0].childNodes[0]).select2("data", { id: null, text: $scope.selectedOption});
				  } else {
					  $( $element.find('div')[0].childNodes[0]).select2("data", { id: null, text: ""});
				  }
		    	}); 
			  
	    	$element.on("change", function(e) {
	    		$timeout(function() {
	    			$scope.selectedOption = e.added.text;
	    			$scope.action();
	    		}, 0);
	    	});	
	    }
	};
}]);

app.directive('kurFileDropAvailable', [ '$parse', '$http', '$timeout', function($parse, $http, $timeout) {
	return function(scope, elem, attr) {
		if ('draggable' in document.createElement('span')) {
			var fn = $parse(attr['kurFileDropAvailable']);
			//$timeout(function() {
				fn(scope);
			//});
		}
	};
} ]);

app.directive('angucomplete', function ($parse, $http, $sce, $timeout) {
    return {
        restrict: 'EA',
        scope: {
            "id": "@id",
            "placeholder": "@placeholder",
            "selectedObject": "=selectedobject",
            "url": "@url",
            "dataField": "@datafield",
            "titleField": "@titlefield",
            "descriptionField": "@descriptionfield",
            "imageField": "@imagefield",
            "inputClass": "@inputclass",
            "userPause": "@pause",
            "localData": "=localdata",
            "searchFields": "@searchfields",
            "minLengthUser": "@minlength",
            "matchClass": "@matchclass"
        },
        template: '<div class="angucomplete-holder"><input id="{{id}}_value" ng-model="searchStr" type="text" placeholder="{{placeholder}}" class="{{inputClass}}"/><div id="{{id}}_dropdown" class="angucomplete-dropdown" ng-if="showDropdown"><div class="angucomplete-searching" ng-show="searching">Searching...</div><div class="angucomplete-searching" ng-show="!searching && (!results || results.length == 0)">No results found</div><div class="angucomplete-row" ng-repeat="result in results" ng-click="selectResult(result)" ng-mouseover="hoverRow()" ng-class="{\'angucomplete-selected-row\': $index == currentIndex}"><div ng-if="imageField" class="angucomplete-image-holder"><img ng-if="result.image && result.image != \'\'" ng-src="{{result.image}}" class="angucomplete-image"/><div ng-if="!result.image && result.image != \'\'" class="angucomplete-image-default"></div></div><div class="angucomplete-title" ng-if="matchClass" ng-bind-html="result.title"></div><div class="angucomplete-title" ng-if="!matchClass">{{ result.title }}</div><div ng-if="result.description && result.description != \'\'" class="angucomplete-description">{{result.description}}</div></div></div></div>',

        link: function($scope, elem, attrs) {
            $scope.lastSearchTerm = null;
            $scope.currentIndex = null;
            $scope.justChanged = false;
            $scope.searchTimer = null;
            $scope.searching = false;
            $scope.pause = 500;
            $scope.minLength = 3;
            $scope.searchStr = null;

            if ($scope.minLengthUser && $scope.minLengthUser != "") {
                $scope.minLength = $scope.minLengthUser;
            }

            if ($scope.userPause) {
                $scope.pause = $scope.userPause;
            }

            var isNewSearchNeeded = function(newTerm, oldTerm) {
                return newTerm.length >= $scope.minLength && newTerm != oldTerm;
            };

            $scope.processResults = function(responseData, str) {
                if (responseData && responseData.length > 0) {
                    $scope.results = [];

                    var titleFields = [];
                    if ($scope.titleField && $scope.titleField != "") {
                        titleFields = $scope.titleField.split(",");
                    }

                    for (var i = 0; i < responseData.length; i++) {
                        // Get title variables
                        var titleCode = [];

                        for (var t = 0; t < titleFields.length; t++) {
                            titleCode.push(responseData[i][titleFields[t]]);
                        }

                        var description = "";
                        if ($scope.descriptionField) {
                            description = responseData[i][$scope.descriptionField];
                        }

                        var image = "";
                        if ($scope.imageField) {
                            image = responseData[i][$scope.imageField];
                        }

                        var text = titleCode.join(' ');
                        if ($scope.matchClass) {
                            var re = new RegExp(str, 'i');
                            var strPart = text.match(re)[0];
                            text = $sce.trustAsHtml(text.replace(re, '<span class="'+ $scope.matchClass +'">'+ strPart +'</span>'));
                        }

                        var resultRow = {
                            title: text,
                            description: description,
                            image: image,
                            originalObject: responseData[i]
                        };

                        $scope.results[$scope.results.length] = resultRow;
                    }


                } else {
                    $scope.results = [];
                }
            };

            $scope.searchTimerComplete = function(str) {
                // Begin the search

                if (str.length >= $scope.minLength) {
                    if ($scope.localData) {
                        var searchFields = $scope.searchFields.split(",");

                        var matches = [];

                        for (var i = 0; i < $scope.localData.length; i++) {
                            var match = false;

                            for (var s = 0; s < searchFields.length; s++) {
                                match = match || ($scope.localData[i][searchFields[s]].toLowerCase().indexOf(str.toLowerCase()) >= 0);
                            }

                            if (match) {
                                matches[matches.length] = $scope.localData[i];
                            }
                        }

                        $scope.searching = false;
                        $scope.processResults(matches, str);

                    } else {
                        $http.get($scope.url + str, {}).
                            success(function(responseData, status, headers, config) {
                                $scope.searching = false;
                                $scope.processResults(responseData[$scope.dataField], str);
                            }).
                            error(function(data, status, headers, config) {
                                console.log("error");
                            });
                    }
                }

            };

            $scope.hoverRow = function(index) {
                $scope.currentIndex = index;
            };

            $scope.keyPressed = function(event) {
                if (!(event.which == 38 || event.which == 40 || event.which == 13)) {
                    if (!$scope.searchStr || $scope.searchStr == "") {
                        $scope.showDropdown = false;
                        $scope.lastSearchTerm = null;
                    } else if (isNewSearchNeeded($scope.searchStr, $scope.lastSearchTerm)) {
                        $scope.lastSearchTerm = $scope.searchStr;
                        $scope.showDropdown = true;
                        $scope.currentIndex = -1;
                        $scope.results = [];

                        if ($scope.searchTimer) {
                            $timeout.cancel($scope.searchTimer);
                        }

                        $scope.searching = true;

                        $scope.searchTimer = $timeout(function() {
                            $scope.searchTimerComplete($scope.searchStr);
                        }, $scope.pause);
                    }
                } else {
                    event.preventDefault();
                }
            };

            $scope.selectResult = function(result) {
                if ($scope.matchClass) {
                    result.title = result.title.toString().replace(/(<([^>]+)>)/ig, '');
                }
                $scope.searchStr = $scope.lastSearchTerm = result.title;
                $scope.selectedObject = result;
                $scope.showDropdown = false;
                $scope.results = [];
                //$scope.$apply();
            };

            var inputField = elem.find('input');

            inputField.on('keyup', $scope.keyPressed);

            elem.on("keyup", function (event) {
                if(event.which === 40) {
                    if (($scope.currentIndex + 1) < $scope.results.length) {
                        $scope.currentIndex ++;
                        $scope.$apply();
                        event.preventDefault;
                        event.stopPropagation();
                    }

                    $scope.$apply();
                } else if(event.which == 38) {
                    if ($scope.currentIndex >= 1) {
                        $scope.currentIndex --;
                        $scope.$apply();
                        event.preventDefault;
                        event.stopPropagation();
                    }

                } else if (event.which == 13) {
                    if ($scope.currentIndex >= 0 && $scope.currentIndex < $scope.results.length) {
                        $scope.selectResult($scope.results[$scope.currentIndex]);
                        $scope.$apply();
                        event.preventDefault;
                        event.stopPropagation();
                    } else {
                        $scope.results = [];
                        $scope.$apply();
                        event.preventDefault;
                        event.stopPropagation();
                    }

                } else if (event.which == 27) {
                    $scope.results = [];
                    $scope.showDropdown = false;
                    $scope.$apply();
                } else if (event.which == 8) {
                    $scope.selectedObject = null;
                    $scope.$apply();
                }
            });
        }
    };
});

app.directive('scrollTo', function ($location, $anchorScroll) {
	  return function(scope, element, attrs) {
	    element.bind('click', function(event) {
	        event.stopPropagation();
	        scope.$on('$locationChangeStart', function(ev) {
	            ev.preventDefault();
	        });
	        var location = attrs.scrollTo;
	        $location.hash(location);
	        $anchorScroll();
	    });
	  };
});

app.directive('numInput', function (dateFilter) {
    return {
        require: 'ngModel',
        template: '<input type="text"></input>',
        replace: true,
        restrict: 'E',
        link: function (scope, elm, attrs, ngModelCtrl) {
            ngModelCtrl.$formatters.unshift(function (modelValue) {
                return modelValue;
            });

            ngModelCtrl.$parsers.unshift(function (viewValue) {
                var s = viewValue.replace(",",".");
                return parseFloat(s);
            });
        },
    };
});
