package com.capstone.safeGuard.apis.member.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MailUtil {
	private final JavaMailSender emailSender;

	public void sendEmail(String toEmail, String title, String text) {
		SimpleMailMessage emailForm = createEmailForm(toEmail, title, text);
		try {
			emailSender.send(emailForm);
		} catch (RuntimeException e) {
			log.info("MailService.sendEmail exception occur toEmail: {}, " +
				"title: {}, text: {}", toEmail, title, text);
			throw new RuntimeException("MailService.sendEmail exception", e);
		}
	}

	// 발신할 이메일 데이터 세팅
	private SimpleMailMessage createEmailForm(String toEmail, String title, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject(title);
		message.setText(text);

		return message;
	}
}
