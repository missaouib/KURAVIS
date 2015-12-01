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
/**
 * CaptchaUtil.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 14.10.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.util.security;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import nl.captcha.Captcha;
import nl.captcha.backgrounds.GradiatedBackgroundProducer;

/**
 * @author MobileMan GmbH
 *
 */
public class CaptchaUtil {
	
	private static final Color colorFrom = new Color(250, 245, 242);
	private static final Color colorTo = new Color(255, 255, 255);
	
	private static final int width = 200;
	private static final int height = 60;
	
	/**
	 * @return Captcha BufferedImage
	 */
	public static Captcha generateCaptcha() {
		
		GradiatedBackgroundProducer backgroundProducer = new GradiatedBackgroundProducer();
		backgroundProducer.setFromColor(colorFrom);
		backgroundProducer.setToColor(colorTo);
		
		Captcha captcha = new Captcha.Builder(width, height).addText()
				.addBackground(backgroundProducer).gimp().addNoise().addBorder().build();
		return captcha;
	}
	
	/**
	 * @param captcha 
	 * @return Captcha BufferedImage
	 */
	public static byte[] getCaptchaImageData(Captcha captcha) {
		if (captcha == null) {
			return null;
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baos);
		
		
		byte[] data = null;
		try {
			ImageIO.write(captcha.getImage(), "png", bos);
			baos.flush();
			data = baos.toByteArray();
			baos.close();
			bos.close();
		} catch (IOException e) { }
		
		return data;
	}
}
