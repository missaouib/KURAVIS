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
//debugger;

var system = require('system');

var url = system.args[1] || '';
if (url.length > 0) {
	var page = require('webpage').create();
	page.open(url, function(status) {
		if (status == 'success') {
			var delay = 0;
			var checker = (function() {
				var html = page.evaluate(function() {
					var body = document.getElementsByTagName('body')[0];
					var dataStatus = body.getAttribute('data-status');

					if (dataStatus == 'ready' || dataStatus == 'ready-timeout') {
						return document.getElementsByTagName('html')[0].outerHTML;
					}
				});
				if (html) {
					clearTimeout(delay);
					console.log(html); 
					phantom.exit();
				}
			});

			delay = setInterval(checker, 100);
		}
	});
	// if something goes wrong stop
	setInterval(function() {
		phantom.exit();
	}, 20000);
}
