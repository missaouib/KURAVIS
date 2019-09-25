<!--
  Copyright 2015 MobileMan GmbH
  www.mobileman.com
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
    http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!DOCTYPE html>
<html lang="en" ng-app="kuravis">
<head>
<meta name="fragment" content="!" >
<link rel="icon" href="/assets/images/favicon.ico" type="image/x-icon">
<link rel="shortcut icon" href="/assets/images/favicon.ico" type="image/x-icon"> 
<meta charset="utf-8">
<title>Bewertungen von Behandlungen, Medikamenten, Therapien, alternativen Heilmethoden - KURAVIS</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1, maximum-scale=1">
<meta name="Robots" content="index, follow" /> 
<meta name="description" content="" /> 
<meta name="keywords" content="" /> 
<meta name="language" CONTENT="de" /> 
<meta name="robots" content="noarchive" /> 
<meta name="author" content="MobileMan GmbH" />
<meta name="rendered" content="<%= new java.util.Date().toString() %>" /> 
<meta http-equiv="Content-Style-Type" content="text/css" /> 
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"> 
<meta http-equiv="content-language" content="de" /> 
<meta http-equiv="pragma" content="no-cache" /> 
<meta http-equiv="expires" content="0" /> 

<!-- styles -->
<link href="/jqueryui/css/ui-lightness/jquery-ui-1.10.3.custom.css" rel="stylesheet">

<link href="/bootstrap3/css/bootstrap.css" rel="stylesheet" media="screen">

<link href="/assets/select2-3.4.2/select2.css" rel="stylesheet">
<link href="/angular/nggrid/ng-grid.min.css " rel="stylesheet">
<link rel="stylesheet" href="/assets/iosOverlay/css/iosOverlay.css">
<link rel="stylesheet" type="text/css" href="/assets/datepicker/css/datepicker.css">
<link rel="stylesheet" type="text/css" href="/assets/timepicker/css/bootstrap-timepicker.min.css">

<link rel="stylesheet" href="/assets/bootstrap-calendar-master/css/calendar.css">

<!-- 
<link rel="stylesheet" href="/assets/iosOverlay/css/custom.css">
<link rel="stylesheet" href="/assets/iosOverlay/css/prettify.css">
 -->

<style type="text/css">
body {
	padding-top: 0px;
	padding-bottom: 0px;
	-webkit-padding-start:0px; /* reset chrome default */
}

/* Custom container */
.container {
	margin: 0 auto;
	max-width: 850px;
}

.container>hr {
	margin: 60px 0;
}

div {
	border: 0px solid red;
}
</style>

<link href="/css/font.css" rel="stylesheet">
<link href="/css/kuravis.css" rel="stylesheet">
<link href="/css/kuravis.less" rel="stylesheet/less" type="text/css"/>


<script src="/css/less-1.4.1.min.js" type="text/javascript"></script>

<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
      <script src="bootstrap/js/html5shiv.js"></script>
    <![endif]-->

</head>

<body lang="en" data-status="{{workStatus}}">
<div class="feedback">                
        <div ng-click="showFeebackModal()">Feedback</div>
</div>
<img id="loading-indicator" src="/assets/images/ajax-loader.gif" style="display:none"/>

<div id="wrap">
	<span ng-controller="SessionCtrl"></span>
	<script>
		var mobileCloseMenu = function(){
			jQuery('#bs-example-navbar-collapse-1').removeClass('in').addClass('collapse');
			jQuery('#menuButton').addClass('collapsed');
		};
	</script>
	<nav class="headerBar navbar navbar-fixed-top" role="navigation">
		<div class="container">
			<div class="navbar-header">
				<button id="menuButton" type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" onClick="window.setTimeout(function(){jQuery('#submenu1').toggleClass('open');}, 100)">
				    <span class="icon-bar"></span>
				    <span class="icon-bar"></span>
				    <span class="icon-bar"></span>
			   	</button>
			   	<div class="navbar-brand">
			 		<div class="logo">
						<div class="logoBrand">
							<a href="{{userLogged?'/Behandlungen':'/'}}">
								<img src="/assets/images/kuravis_logo.png"/>
							</a>
						</div>
						<span class="kuravisVersionHeader">beta</span>
					</div>  
			   	</div>
			</div>
			<div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
    			<ul class="nav navbar-nav navbar-right">
					<li class="ng-cloak menu-item-like-a hidden-xs" ng-show="userLogged" >{{userLogged.name}}</li>
					<li id="submenu1" class="dropdown">
						<a ng-cloak class="ng-cloak, dropdown-toggle hidden-xs" data-toggle="dropdown" href="#" style="margin-top:15px;">
							<span class="icon-bar2"></span>
				    		<span class="icon-bar2"></span>
				    		<span class="icon-bar2"></span>
						</a>
						<ul class="dropdown-menu" style="z-index: 10001">
							<li><a style="cursor: pointer" onClick="mobileCloseMenu()" href="/Behandlungen">Suchen</a></li>
							<li><a style="cursor: pointer" ng-show="userLogged" onClick="mobileCloseMenu()" href="/Nutzer/{{userLogged._id}}">Profil</a></li>
							<li><a style="cursor: pointer" onClick="mobileCloseMenu()" href="/Nutzenbewertung">Bewertung</a></li>
							<li><a style="cursor: pointer" ng-show="userLogged" onClick="mobileCloseMenu()" href="/Tagebuch/{{userLogged._id}}">Tagebuch</a></li>
							<li><a style="cursor: pointer" ng-show="userLogged" onClick="mobileCloseMenu()" href="/Einladung">Einladungen</a></li>
							<!--
							<li><a style="cursor: pointer" ng-show="userLogged && (userLogged.roles[0] === 'admin')" onClick="mobileCloseMenu()" href="/subscriptions">Subscriptions</a></li>
							<li><a style="cursor: pointer" ng-show="userLogged" onClick="mobileCloseMenu()" href="/notifications">Beobachtungliste</a></li>
							-->
							<li><a style="cursor: pointer" ng-show="userLogged" onClick="mobileCloseMenu()" href="/Einstellungen">Einstellungen</a></li>
							<li><a style="cursor: pointer" ng-show="userLogged" onClick="mobileCloseMenu()" ng-click="logout()">Abmelden</a></li>
							<li><a style="cursor: pointer" ng-hide="userLogged" onClick="mobileCloseMenu()" ng-click="startSignIn()">Anmelden</a></li>
						</ul>
					</li>
    			</ul>
    		</div>
		</div>
	</nav>
	<div id="main_content" class="container" ng-if="!isFullWidthView()">
		<alert  ng-cloak class="ng-cloak, alerts" ng-repeat="alert in alerts" type="alert.type" close="closeAlert($index)">{{alert.msg}}</alert>
		<div ng-view autoscroll="true"></div>
	</div>

	<div ng-if="isFullWidthView()" class="dummyContainer">
		<div ng-view autoscroll="true"></div>
	</div>
</div>
<div id="footer">
	<div class="footerNav">
		<div class="container">
			<ul class="nav footerItems">
				<li class="col-md-2 col-sm-2 footerItem footerItemWidthStandard"><a href="/About">About</a></li>
				<li class="col-md-2 col-sm-2 footerItem footerItemWidthWide"><a href="/Gesundheitsverzeichnis">Gesundheitsverzeichnis</a></li>
				
			</ul>
		</div>
	</div>
	<div class="footerInfo">
		<div class="container">
			<p>
			
			</p>
		</div>
	</div>
</div>
<div class="clearfix"></div>

<div modal="doLogin" class="modal fade">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-body">
				<div class="title row">
					<div class="col-md-9">Anmeldung f&uuml;r bestehende Nutzer</div>
					<div class="col-md-3"><a class="btn btn-primary pull-right" ng-click="golbalIn2Up()">Kostenlose Registrierung</a></div>
				</div>
				<div class="col-md-12">&nbsp;</div>
				<div ng-show="signInMessage" class="text-danger">
					Anmeldung ist fehlgeschlagen
				</div>
				<form ng-submit="globalSignIn()" name="signInForm" role="form">
					<input class="signInput" type="email" ng-model="signInLogin" ng-change="updateCaptcha()" placeholder="E-mail" ng-enter="globalSignIn()"/> 
					<input class="signInput" type="password" ng-model="signInPassword" placeholder="Passwort" ng-enter="globalSignIn(signInLogin, signInPassword, signInCaptcha)"/>
					<div class="row">
					<div class="col-md-6">
						<label>
						<input type="checkbox" id="signInRememberMe" ng-model="signInRememberMe" style="margin-bottom:5px" ng-enter="globalSignIn()"/> Passwort merken
						</label>
					</div>
					<div class="col-md-6">
						<a class="pull-right" style="cursor: pointer" ng-click="golbalIn2Reset()">Passwort vergessen?</a>
					</div>
					</div>
					<div class="row" ng-show="showCaptcha">
						<div class="col-md-6">
							<label>Bitte Zeichen wiedergeben:</label>
							<input type="password" ng-model="signInCaptcha" /> 
						</div>
						
						<div class="col-md-6">
							<img ng-src="/{{captchaImage}}" /><br /> 
							<a style="cursor: pointer" ng-click="updateCaptcha()">Bild &auml;ndern</a>
						</div>
					</div>
				</form>
				<!-- 
				<div class="text-center" ng-hide="$rootScope.invitationEmail || $rootScope.newReviewPending">
 					Interesse geweckt? <a style="cursor: pointer" ng-click="globalSubscribe()">In KURAVIS Interessentenliste eintragen</a>
				</div>
				 --> 
			</div>
			<div class="modal-footer">
				<button class="btn btn-default" ng-click="doLogin = false;">Abbrechen</button> 
				<button class="btn btn-primary" ng-click="globalSignIn(signInLogin, signInPassword, signInCaptcha, true)">Anmelden</button>
			</div>
		</div>
	</div>
</div>

<div modal="doResetPassword" class="modal fade">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-body">
				<div class="title">
					Passwort zur&uuml;cksetzen
				</div>
				
				<div class="text-success" ng-show="resetPasswordSend">
					Eine E-Mail zum Zur&uuml;cksetzen der E-Mail wurde versandt<br>
				</div>
				<div class="text-danger" ng-show="resetPasswordError">
					Diese E-Mail wurde nicht erkannt.
				</div>
				
				<div>
					<span class="text-info" ng-hide="signInLogin">Bitte g&uuml;ltige E-Mail angeben um Passwort zur&uuml;ckzusetzen.</span>
				</div>
				<form ng-submit="golbalResetPassword()" name="resetPasswordForm" class="form-inline">
					<input ng-hide="resetPasswordSend" class="signInput" type="email" ng-model="signInLogin" placeholder="E-Mail" ng-enter="golbalResetPassword()">
						
					</input> 
				</form>				 
			</div>
			<div class="modal-footer">
				<button class="btn btn-default" ng-click="doResetPassword = false;">Schliessen</button> 
				<button ng-hide="resetPasswordSend" class="btn btn-primary" ng-click="golbalResetPassword()" ng-disabled="!signInLogin">Senden</button>
			</div>
		</div>
	</div>
</div>


<div modal="doSendFeedback" class="modal fade">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-body">
				<div class="title">Feedback</div>
				<div class="text-info" ng-hide="feedbackSent">
						Helfen Sie uns, KURAVIS zu verbessern. F�r Ihr Feedback danken wir im Voraus bestens.
				</div>
				<div class="text-success" ng-show="feedbackSent">
					Herzlichen Dank f&uuml;r Ihr Feedback!
				</div>
				<form ng-submit="globalSendFeedback()" name="sendFeedbackForm" class="form-inline">
					<textarea ng-hide="feedbackSent" class="input-xxlarge background-color resizeForbidden" style="width:100%;border:0px;" ng-model="feedbackText" maxlength="5000" rows="8">		
					</textarea> 
				</form>				 
			</div>
			<div class="modal-footer">
				<button class="btn btn-default" ng-click="globalSendFeedbackModalHide()">Abbrechen</button> 
				<button ng-hide="feedbackSent" class="btn btn-primary" ng-click="globalSendFeedback()" ng-disabled="!feedbackText">Senden</button>
			</div>
		</div>
	</div>
</div>

<div modal="doSignup" class="modal fade">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-body">
				<div class="pull-right">
					<img id="loading-indicator-modal" class="pull-right loading-indicator-modal" src="/assets/images/ajax-loader.gif" ng-show="workStatus=='running'"/>
				</div>
				<div class="title">
					Kostenlose Registrierung
				</div>
				<div>
					<form class="form-inline" role="form" name="myForm">
					<div class="text-success" ng-show="signUpSubmited">
						Ihre Anfrage wurde angenommen. Bitte &uuml;berpr&uuml;fen Sie Ihr E-Mail Postfach und klicken Sie auf den Aktivierungslink in der E-Mail.<br />
					</div>
					<div class="text-warning" ng-show="signUpSubmitedError">
						Fehler. Bitte &uuml;berpr&uuml;fen Sie Ihre Angaben.<br />
					</div>
					<div ng-hide="signUpSubmited" class="controls">
						<div class="text-danger" ng-show="submitted && myForm.signUpName.$error.required">Pflichtfeld</div>
						<input class="signInput" type="text" required class="input-text background-color" ng-model="signUpName" name="signUpName" placeholder="Nutzername"> 
						<span class="text-danger" ng-show="submitted && myForm.signUpEmail.$error.email">Email ist nicht g�ltig!</span>
						<span class="text-danger" ng-show="submitted && myForm.signUpEmail.$error.required">Pflichtfeld</span>
						<input class="signInput" type="email" required class="input-text background-color" ng-model="signUpEmail" name="signUpEmail" placeholder="E-Mail">
						<div ng-show="pwdsubmitted && signUpPassword2!=null && signUpPassword!=signUpPassword2" class="text-danger"">
							Passw&ouml;rter stimmen nicht &uuml;berein.
						</div>
						<div class="text-danger" ng-show="submitted && myForm.signUpPassword.$error.required">Pflichtfeld</div>
						<input class="signInput" type="password" required class="input-text background-color" ng-model="signUpPassword" name="signUpPassword" placeholder="Passwort">
						<div class="text-danger" ng-show="submitted && myForm.signUpPassword2.$error.required">Pflichtfeld</div>
						<input class="signInput" type="password" required class="input-text background-color" ng-blur="pwdsubmitted=true" ng-model="signUpPassword2" name="signUpPassword2" placeholder="Passwort wiederholen">
						<div class="text-danger" ng-show="submitted && myForm.signUpChecked.$error.required">Pflichtfeld</div>
						<div class="checkbox">
							<input type="checkbox" id="signUpCheckedId" name="signUpChecked" ng-model="signUpChecked" required class="background-color" style="margin-bottom:5px"/> 
							Ja, ich akzeptiere die <a href="/Richtlinien" target="_blank">Richtlinien</a> und die <a href="/Nutzungsbedingungen" target="_blank">Nutzungsbedingungen</a>
						</div>
					</div>
					</form>
				</div>
			</div>
			<div class="modal-footer">
				<button ng-hide="signUpSubmited" class="btn btn-default" ng-click="submitted=false;cancelSignUp();">Abbrechen</button>
				<button ng-hide="signUpSubmited" class="btn btn-primary" ng-click="submitted=true;globalSignUp(signUpName, signUpEmail, signUpPassword, signUpPassword2)" ng-disabled="!myForm.$valid && !signUpChecked">Registrieren</button>
				<button ng-show="signUpSubmited" class="btn btn-primary" ng-click="doSignup=false">OK</button>
			</div>
		</div>
	</div>
</div>

<div modal="signUpSubmited" class="modal fade">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-body">
				<div class="title">
					Registration erfolgreich
				</div>
				
				<div>
					Ihre Anfrage wurde angenommen. <br>
					Bitte &uuml;berpr&uuml;fen Sie Ihr E-Mail Postfach und klicken Sie auf den Aktivierungslink in der E-Mail.
				</div>				
				<br>
			</div>
			<div class="modal-footer">
				<button class="btn btn-primary" ng-click="signUpSubmited = false">Weiter</button> 
			</div>
		</div>
	</div>
</div>

	<script>
		 if(!window.console){ window.console = {log: function(){} }; } 
	</script>
	<!-- <script src="//code.jquery.com/jquery.js"></script>  -->
	<!-- <script src="/jquery/jquery-2.0.3.min.js"></script> -->
	<script src="/jquery/jquery-2.0.3.js"></script>
	<!-- jQuery before angular -->
	<!-- <script src="jqueryui/js/jquery-ui-1.10.3.custom.min.js"></script>  -->
	<script src="/angular/upload/angular-file-upload-shim.min.js"></script> <!--  note - must be before angular.js -->
	<script src="/angular/1.2.9/angular.js"></script>
	<script src="/angular/1.2.9/angular-resource.js"></script>
	<script src="/angular/1.2.9/angular-route.js"></script>
	<script src="/bootstrap/ui-bootstrap-tpls-0.5.0.js"></script>
	<script src="/bootstrap3/js/bootstrap.min.js"></script>
	<script src="/angular/nggrid/ng-grid-2.0.7.min.js"></script>
	<script src="/angular/upload/angular-file-upload.js"></script> 
	 
	<!-- Select2 -->
	<script src="/assets/select2-3.4.2/select2.js"></script>
	<!-- classie -->
	<script src="/assets/classie/classie.js"></script>
	<!-- highcharts -->
	<script src="https://code.highcharts.com/highcharts.js"></script>
	
	<script src="/assets/iosOverlay/js/iosOverlay.js"></script>

	<script src="/assets/retina/js/retina-1.1.0.min.js"></script>
	<!-- datepicker -->
	<script type="text/javascript" src="/assets/datepicker/js/bootstrap-datepicker.js"></script>
	<script type="text/javascript" src="/assets/datepicker/js/locales/bootstrap-datepicker.de.js"></script>
	<script type="text/javascript" src="/assets/timepicker/js/bootstrap-timepicker.min.js"></script>
	
	<script src="/assets/bootstrap-calendar-master/components/underscore/underscore-min.js"></script>
	<script src="/assets/bootstrap-calendar-master/js/language/de-DE.js"></script>
	<script src="/assets/bootstrap-calendar-master/js/calendar.js"></script>

	<script src="/js/app.js"></script>
	<script src="/js/directives.js"></script>
	<script src="/js/services.js"></script>
	<script src="/js/controllers/session.js"></script>
	<script src="/js/controllers/start.js"></script>
	<script src="/js/controllers/disease.js"></script>
	<script src="/js/controllers/edit.js"></script>
	<script src="/js/controllers/treatment.js"></script>
	<script src="/js/controllers/review.js"></script>
	<script src="/js/controllers/user.js"></script>
	<script src="/js/controllers/diary.js"></script>
	<script src="/js/controllers/settings.js"></script>
	<script src="/js/controllers/notifications.js"></script>
	<script src="/js/controllers/invitation.js"></script>
	<script src="/js/controllers/votes.js"></script>
	<script src="/js/controllers/activate.js"></script>
	<script src="/js/controllers/admin.js"></script>
	<script src="/js/controllers/resetpassword.js"></script>
	<script src="/js/controllers/subscription.js"></script>
	<script src="/js/controllers/dictionary.js"></script>
	<script src="/js/controllers/summary.js"></script>
</body>
</html>
