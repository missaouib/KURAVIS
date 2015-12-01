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
package com.mobileman.kuravis.core.util;

import java.util.Date;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;

import com.ibm.icu.util.Calendar;

public abstract class DateTimeUtils {

	/**
	 * @param date
	 * @return formatted elapsed time from now
	 */
	public static String fmtElapsedTime(Date date) {
		if (date == null) {
			return "";
		}
		Period period = new Period(date.getTime(), Calendar.getInstance().getTimeInMillis());
		PeriodFormatterBuilder pf = new PeriodFormatterBuilder();
		pf.appendPrefix(" vor ");
		if (period.getYears() > 0) {
			pf.appendYears().appendSuffix(" Jahr", " Jahren");
		} else if (period.getMonths() > 0) {
			pf.appendMonths().appendSuffix(" Monat", " Monaten");
		} else if (period.getWeeks() > 0) {
			pf.appendWeeks().appendSuffix(" Woche ", " Wochen");
		} else if (period.getDays() > 0) {
			pf.appendDays().appendSuffix(" Tag ", " Tagen");
		} else if (period.getHours() > 0) {
			pf.appendHours().appendSuffix(" Stunde ", " Stunden");
		} else if (period.getMinutes() > 0) {
			pf.appendMinutes().appendSuffix(" Minute ", " Minuten");
		} else if (period.getSeconds() > 0) {
			pf.appendSeconds().appendSuffix(" Sekunde ", " Sekunden");
		}
		return pf.toFormatter().print(period);
	}

}
