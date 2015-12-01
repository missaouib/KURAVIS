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
 * MailManagerImpl.java
 * 
 * Projekt: KURAVA
 * 
 * @author MobileMan GmbH
 * @date 12.7.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.services.messaging.mail.impl;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.mobileman.kuravis.core.domain.event.TreatmentEvent;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReview;
import com.mobileman.kuravis.core.domain.treatment_review.TreatmentReviewSummary;
import com.mobileman.kuravis.core.domain.user.FollowedEntity;
import com.mobileman.kuravis.core.domain.user.User;
import com.mobileman.kuravis.core.services.messaging.mail.MailService;
import com.mongodb.DBObject;

/**
 * @author MobileMan GmbH
 *
 */
@Service()
public class MailServiceImpl implements MailService {
	
	private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
	
	private static final String EMAIL_ENCODING = "UTF-8";
	
	private static final Locale DEFAULT_LOCALE = Locale.GERMANY;
	
	private JavaMailSender mailSender;
	
	private String systemAdminEmail;
	
	private String memberEmail;
	
	private String supportEmail;
	
	private String spamEmail;
	
	private String kontaktEmail;
	
	private String serverDNSName;
	
	@Autowired
	private ReloadableResourceBundleMessageSource messageSource;
	
	@Autowired
	private VelocityEngine velocityEngine;
	
	/**
	 *
	 * @return velocityEngine
	 */
	public VelocityEngine getVelocityEngine() {
		return this.velocityEngine;
	}
	
	/**
	 *
	 * @return serverDNSName
	 */
	public String getServerDNSName() {
		return this.serverDNSName;
	}

	/**
	 *
	 * @param serverDNSName serverDNSName
	 */
	public void setServerDNSName(String serverDNSName) {
		this.serverDNSName = serverDNSName;
	}

	/**
	 *
	 * @return systemAdminEmail
	 */
	public String getSystemAdminEmail() {
		return this.systemAdminEmail;
	}

	/**
	 *
	 * @param systemAdminEmail systemAdminEmail
	 */
	public void setSystemAdminEmail(String systemAdminEmail) {
		this.systemAdminEmail = systemAdminEmail;
	}

	/**
	 *
	 * @return memberEmail
	 */
	public String getMemberEmail() {
		return this.memberEmail;
	}

	/**
	 *
	 * @param memberEmail memberEmail
	 */
	public void setMemberEmail(String memberEmail) {
		this.memberEmail = memberEmail;
	}

	/**
	 *
	 * @return supportEmail
	 */
	public String getSupportEmail() {
		return this.supportEmail;
	}

	/**
	 *
	 * @param supportEmail supportEmail
	 */
	public void setSupportEmail(String supportEmail) {
		this.supportEmail = supportEmail;
	}

	/**
	 *
	 * @return spamEmail
	 */
	public String getSpamEmail() {
		return this.spamEmail;
	}

	/**
	 *
	 * @param spamEmail spamEmail
	 */
	public void setSpamEmail(String spamEmail) {
		this.spamEmail = spamEmail;
	}

	/**
	 *
	 * @return kontaktEmail
	 */
	public String getKontaktEmail() {
		return this.kontaktEmail;
	}

	/**
	 *
	 * @param kontaktEmail kontaktEmail
	 */
	public void setKontaktEmail(String kontaktEmail) {
		this.kontaktEmail = kontaktEmail;
	}

	/**
	 *
	 * @param mailSender mailSender
	 */
	@Autowired
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Async
	@Override
	public void sendResetCredientialsEmail(final DBObject user, final String resetPasswordUuid) {
		if (log.isDebugEnabled()) {
			log.debug("sendResetCredientialsEmail(" + user + ") - start");
		}
		
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			
			@Override
            public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws Exception {
				if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - start"); //$NON-NLS-1$
				}

				MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, EMAIL_ENCODING);
				messageHelper.setSentDate(new Date());
				messageHelper.setTo((String) user.get("email"));
				messageHelper.setFrom(getMemberEmail());
				
				String subject = messageSource.getMessage("reset.password.email.subject", null, DEFAULT_LOCALE);
				messageHelper.setSubject(subject);
				
				Map<String, Object> model = new HashMap<String, Object>();
                model.put("user", user);
                model.put("resetPasswordUuid", resetPasswordUuid);
                model.put("dns_server_name", getServerDNSName());
                
                String htmlMessage = VelocityEngineUtils.mergeTemplateIntoString(
                		getVelocityEngine(), "reset-credentials-email-body.vm", EMAIL_ENCODING, model);
                String textMessage = VelocityEngineUtils.mergeTemplateIntoString(
                		getVelocityEngine(), "reset-credentials-email-body-text.vm", EMAIL_ENCODING, model);
                
//                String textMessage = HTMLTextParser.htmlToText(htmlMessage);
                messageHelper.setText(textMessage, htmlMessage);

				if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - returns"); //$NON-NLS-1$
				}
            }
        };
        
        this.mailSender.send(preparator);
        
        if (log.isDebugEnabled()) {
			log.debug("sendResetCredientialsEmail(...) - end");
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Async
	@Override
	public void sendActivationEmail(final DBObject user, final String activationUuid) {
		if (log.isDebugEnabled()) {
			log.debug("sendActivationEmail(" + user + ", " + activationUuid + ") - start");
		}
		
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
	        
			
			@Override
			public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws Exception {
				if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - start"); //$NON-NLS-1$
				}

				try {
					MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, EMAIL_ENCODING);
					messageHelper.setSentDate(new Date());
					messageHelper.setTo((String) user.get("email"));
					messageHelper.setFrom(getMemberEmail());
					
					String subject = messageSource.getMessage("activation.email.subject", null, DEFAULT_LOCALE);
					messageHelper.setSubject(subject);
					                
					Map<String, Object> model = new HashMap<String, Object>();
					model.put("userName", user.get("name"));
					model.put("dns_server_name", getServerDNSName());
					model.put("activationUuid", activationUuid);
					
					String htmlMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "sign-up-activation-email-body.vm", EMAIL_ENCODING, model);
					String textMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "sign-up-activation-email-body-text.vm", EMAIL_ENCODING, model);
//					String textMessage = HTMLTextParser.htmlToText(htmlMessage);
					messageHelper.setText(textMessage, htmlMessage);
				} catch (Exception e) {
					log.error("sendActivationEmail - Message preparation failed", e);
				}
                 
                if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - returns"); //$NON-NLS-1$
				}
            }
        };
        
        this.mailSender.send(preparator); 
        
		if (log.isDebugEnabled()) {
			log.debug("sendActivationEmail(" + user + ", " + activationUuid + ") - end"); //$NON-NLS-1$
		}
	}
	
	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.messaging.mail.MailService#sendWelcomeEmail(com.mongodb.DBObject)
	 */
	@Async
	@Override
	public void sendWelcomeEmail(final DBObject user) {
		if (log.isDebugEnabled()) {
			log.debug("sendWelcomeEmail(" + user + ") - start");
		}
		
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
	        
			
			@Override
			public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws Exception {
				if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - start"); //$NON-NLS-1$
				}

				try {
					MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, EMAIL_ENCODING);
					messageHelper.setSentDate(new Date());				
					messageHelper.setTo((String) user.get("email"));
					messageHelper.setFrom(getMemberEmail());
					
					String subject = messageSource.getMessage("welcome.email.subject", null, DEFAULT_LOCALE);
					messageHelper.setSubject(subject);
					                
					Map<String, Object> model = new HashMap<String, Object>();
					model.put("userName", user.get("name"));
					model.put("dns_server_name", getServerDNSName());
					
					String htmlMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "sign-up-welcome-email-body.vm", EMAIL_ENCODING, model);
					String textMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "sign-up-welcome-email-body-text.vm", EMAIL_ENCODING, model);					
//					String textMessage = HTMLTextParser.htmlToText(htmlMessage);
					messageHelper.setText(textMessage, htmlMessage);
				} catch (Exception e) {
					log.error("sendWelcomeEmail - Message preparation failed", e);
				}
                 
                if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - returns"); //$NON-NLS-1$
				}
            }
        };
        
        this.mailSender.send(preparator); 
        
		if (log.isDebugEnabled()) {
			log.debug("sendWelcomeEmail(" + user + ") - end"); //$NON-NLS-1
		}
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Async
	@Override
	public void sendNewsAndAnnouncements(final User user, final Map<String, List<TreatmentReview>> reviewsBySummaryId, final Map<String, List<TreatmentReviewSummary>> summariesByDisease) {
		if (log.isDebugEnabled()) {
			log.debug("sendNewsAndAnnouncements(" + user + ", " + reviewsBySummaryId + ", " + summariesByDisease + ") - start");
		}
		
		if (summariesByDisease == null) {
			return;
		}
		
		final Date sentDate = new Date();
		final String subject = messageSource.getMessage("notification.news_announcements.email.subject", null, DEFAULT_LOCALE);
				
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
	        
			
			@Override
			public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws Exception {
				if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - start"); //$NON-NLS-1$
				}

				try {
					MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, EMAIL_ENCODING);
					messageHelper.setSentDate(sentDate);
					messageHelper.setTo(user.getEmail());
					messageHelper.setFrom(getMemberEmail());
					messageHelper.setSubject(subject);
					
					Map<String, Object> model = new HashMap<String, Object>();
					model.put("user", user);
					model.put("dns_server_name", getServerDNSName());
					model.put("summariesByDisease", summariesByDisease);
					model.put("reviewsBySummary", reviewsBySummaryId);
					
					String htmlMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "news-announcements-email-body.vm", EMAIL_ENCODING, model);
					String textMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "news-announcements-email-body-text.vm", EMAIL_ENCODING, model);

					messageHelper.setText(textMessage, htmlMessage);
					
				} catch (Exception e) {
					log.error("sendNewsAndAnnouncements - Message preparation failed", e);
				}
                 
                if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - returns"); //$NON-NLS-1$
				}
            }
        };
        
        this.mailSender.send(preparator);
		
		if (log.isDebugEnabled()) {
			log.debug("sendNewsAndAnnouncements(...) - end"); //$NON-NLS-1$
		}
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.messaging.mail.MailService#sendWeeklyUpdates(User, DBObject)
	 */
	@Async
	@Override
	public void sendWeeklyUpdates(final User user, final DBObject data) {
		if (log.isDebugEnabled()) {
			log.debug("sendWeeklyUpdates(" + user + ", " + data + ") - start");
		}
		
		final String subject = messageSource.getMessage("notification.weeklyupdates.email.subject", null, DEFAULT_LOCALE);
		
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
	        
			
			@Override
			public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws Exception {
				if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - start"); //$NON-NLS-1$
				}

				try {
					MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, EMAIL_ENCODING);
					messageHelper.setSentDate(new Date());
					messageHelper.setTo(user.getEmail());
					messageHelper.setFrom(getMemberEmail());
					messageHelper.setSubject(subject);
					
					Map<String, Object> model = new HashMap<String, Object>();
					model.put("user", user);
					model.put("data", data);
					model.put("dns_server_name", getServerDNSName());
					
					String htmlMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "weekly-update-email-body.vm", EMAIL_ENCODING, model);
					String textMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "weekly-update-email-body-text.vm", EMAIL_ENCODING, model);						

					messageHelper.setText(textMessage, htmlMessage);
				} catch (Exception e) {
					log.error("sendWeeklyUpdates - Message preparation failed", e);
				}
                 
                if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - returns"); //$NON-NLS-1$
				}
            }
        };
        
        this.mailSender.send(preparator);
        
		if (log.isDebugEnabled()) {
			log.debug("sendWeeklyUpdates(...) - end"); //$NON-NLS-1$
		}
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.messaging.mail.MailService#sendInvitationEmail(com.mongodb.DBObject, java.lang.String)
	 */
	@Async
	@Override
	public void sendInvitationEmail(final DBObject sender, final String email) {
		if (log.isDebugEnabled()) {
			log.debug("sendInvitationEmail(" + sender + ", " + email + ") - start");
		}
		
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
	        
			
			@Override
			public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws Exception {
				if (log.isDebugEnabled()) {
					log.debug("sendInvitationEmail.MimeMessagePreparator.prepare(MimeMessage) - start"); //$NON-NLS-1$
				}

				try {
					MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, EMAIL_ENCODING);
					messageHelper.setSentDate(new Date());				
					messageHelper.setTo(email);
					messageHelper.setFrom(getMemberEmail());
					
					String subject = messageSource.getMessage("invitation.email.subject", null, DEFAULT_LOCALE);
					messageHelper.setSubject(subject);
					                
					Map<String, Object> model = new HashMap<String, Object>();
					model.put("userName", sender.get("name"));
					model.put("email", URLEncoder.encode(email, "UTF-8"));
					model.put("dns_server_name", getServerDNSName());
					
					String htmlMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "invitation-email-body.vm", EMAIL_ENCODING, model);
					String textMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "invitation-email-body-text.vm", EMAIL_ENCODING, model); 
//					String textMessage = HTMLTextParser.htmlToText(htmlMessage);
					messageHelper.setText(textMessage, htmlMessage);
				} catch (Exception e) {
					log.error("sendInvitationEmail - Message preparation failed", e);
				}
                 
                if (log.isDebugEnabled()) {
					log.debug("sendInvitationEmail.MimeMessagePreparator.prepare(MimeMessage) - returns"); //$NON-NLS-1$
				}
            }
        };
        
        this.mailSender.send(preparator); 
        
		if (log.isDebugEnabled()) {
			log.debug("sendInvitationEmail(" + sender + ", " + email + ") - end"); //$NON-NLS-1
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Async
	@Override
	public void sendUserSubscribedEmail(final DBObject subscription) {
		if (log.isDebugEnabled()) {
			log.debug("sendUserSubscribedEmail(" + subscription + ") - start");
		}
		
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
	        
			
			@Override
			public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws Exception {
				if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - start"); //$NON-NLS-1$
				}

				try {
					MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, EMAIL_ENCODING);
					messageHelper.setSentDate(new Date());
					messageHelper.setTo((String) subscription.get("email"));
					messageHelper.setFrom(getMemberEmail());
					
					String subject = messageSource.getMessage("subscription.email.subject", null, DEFAULT_LOCALE);
					messageHelper.setSubject(subject);
					                
					Map<String, Object> model = new HashMap<String, Object>();
					model.put("email", subscription.get("email"));
					model.put("dns_server_name", getServerDNSName());
					
					String htmlMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "subscribe-email-body.vm", EMAIL_ENCODING, model);
					String textMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "subscribe-email-body-text.vm", EMAIL_ENCODING, model);
//					String textMessage = HTMLTextParser.htmlToText(htmlMessage);
					messageHelper.setText(textMessage, htmlMessage);
				} catch (Exception e) {
					log.error("sendUserSubscribedEmail - Message preparation failed", e);
				}
                 
                if (log.isDebugEnabled()) {
					log.debug("sendUserSubscribedEmail: $MimeMessagePreparator.prepare(MimeMessage) - returns"); //$NON-NLS-1$
				}
            }
        };
        
        this.mailSender.send(preparator); 
        
		if (log.isDebugEnabled()) {
			log.debug("sendUserSubscribedEmail(" + subscription + ") - end"); //$NON-NLS-1$
		}
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Async
	@Override
	public void sendUserFeedbackMail(String userFeedback, final String finalEmail) {
		if (log.isDebugEnabled()) {
			log.debug("sendUserFeedbackMail - start");
		}
		String email = finalEmail;
		if((finalEmail == null) || (finalEmail.length() == 0)) {
			email = "-";
		}		
		final String finalTextBody = "User's email: " + email + "\n\n" + userFeedback;
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
	        
			
			@Override
			public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws Exception {
				if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - start"); //$NON-NLS-1$
				}

				try {
					MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, EMAIL_ENCODING);
					messageHelper.setSentDate(new Date());
					messageHelper.setTo("kontakt@kuravis.de");
					messageHelper.setFrom(getMemberEmail());
					messageHelper.setSubject("Kuravis feedback");                
					messageHelper.setText(finalTextBody);
				} catch (Exception e) {
					log.error("sendUserFeedbackMail failed", e);
				}                 
            }
        };
        
        this.mailSender.send(preparator); 
        
		if (log.isDebugEnabled()) {
			log.debug("sendUserFeedbackMail end"); 
		}	
	
	}

	/** 
	 * {@inheritDoc}
	 * @see com.mobileman.kuravis.core.services.messaging.mail.MailService#sendTreatmentEventRemminder(com.mobileman.kuravis.core.domain.user.User, com.mobileman.kuravis.core.domain.event.TreatmentEvent)
	 */
	@Async
	@Override
	public void sendTreatmentEventRemminder(final User user, final TreatmentEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("sendWelcomeEmail(" + user + ") - start");
		}
		
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
	        
			
			@Override
			public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws Exception {
				if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - start"); //$NON-NLS-1$
				}

				try {
					MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, EMAIL_ENCODING);
					messageHelper.setSentDate(new Date());				
					messageHelper.setTo(user.getEmail());
					messageHelper.setFrom(getMemberEmail());
					
					String subject = messageSource.getMessage("welcome.email.subject", null, DEFAULT_LOCALE);
					messageHelper.setSubject(subject);
					
					Map<String, Object> model = new HashMap<String, Object>();
					model.put("userName", user.getName());
					model.put("dns_server_name", getServerDNSName());
					
					NumberFormat numberFormat = NumberFormat.getNumberInstance(DEFAULT_LOCALE);
					
					if (event.getDose() != null) {
						model.put("dose", numberFormat.format(event.getDose()));
					}
					
					if (event.getUnit() != null) {
						model.put("unit", event.getUnit().getName());
					}
					
					if (event.getQuantity() != null) {
						model.put("quantity", numberFormat.format(event.getQuantity()));
					}
					
					if (event.getType() != null) {
						model.put("treatmentType", event.getType().getName());
					}
					
					if (event.getTreatment() != null) {
						model.put("treatmentName", event.getTreatment().getName());
					}
					
					if (event.getStart() != null) {
						DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, DEFAULT_LOCALE);
						String startDate = df.format(event.getStart());
						model.put("startDate", startDate);
					}
					
					String htmlMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "treatment-event-reminder-email-body.vm", EMAIL_ENCODING, model);
					String textMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "treatment-event-reminder-email-body-text.vm", EMAIL_ENCODING, model);					

					messageHelper.setText(textMessage, htmlMessage);
					
				} catch (Exception e) {
					log.error("sendWelcomeEmail - Message preparation failed", e);
				}
                 
                if (log.isDebugEnabled()) {
					log.debug("$MimeMessagePreparator.prepare(MimeMessage) - returns"); //$NON-NLS-1$
				}
            }
        };
        
        this.mailSender.send(preparator); 
        
		if (log.isDebugEnabled()) {
			log.debug("sendWelcomeEmail(" + user + ") - end"); //$NON-NLS-1
		}
	}
	
	@Async
	@Override
	public void sendFollowNotification(final FollowedEntity entity) {
		if (log.isDebugEnabled()) {
			log.debug("sendFollowNotification(" + entity.getEntityType() + "," + entity.getText() + ")");
		}
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			
			@Override
			public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws Exception {
				try {
					MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, EMAIL_ENCODING);
					messageHelper.setSentDate(new Date());				
					messageHelper.setTo(entity.getUser().getEmail());
					messageHelper.setFrom(getMemberEmail());
					messageHelper.setSubject(messageSource.getMessage("follow.email.subject", new String[] { entity.getText() }, DEFAULT_LOCALE));
					Map<String, Object> model = new HashMap<String, Object>();
					model.put("followedEntity", entity);
					model.put("dns_server_name", getServerDNSName());
					
					String htmlMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "followNotification-email-body.vm", EMAIL_ENCODING, model);
					String textMessage = VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(), "followNotification-email-body-text.vm", EMAIL_ENCODING, model);					
					messageHelper.setText(textMessage, htmlMessage);
				} catch (Exception e) {
					log.error("sendFollowNotification - Message preparation failed", e);
				}
            }
        };
        
        this.mailSender.send(preparator); 
	}

}
