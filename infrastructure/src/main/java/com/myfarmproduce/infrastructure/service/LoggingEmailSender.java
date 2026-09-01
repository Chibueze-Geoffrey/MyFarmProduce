package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Dev email sender - logs the message instead of sending. Swap for SMTP/SendGrid. */
@Component
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(String to, String subject, String htmlBody) {
        log.info("EMAIL -> {} | {}\n{}", to, subject, htmlBody);
    }
}
