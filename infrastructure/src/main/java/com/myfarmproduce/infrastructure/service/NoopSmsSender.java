package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** No-op SMS sender (SMS is optional for MVP - add Termii later). */
@Component
public class NoopSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(NoopSmsSender.class);

    @Override
    public void send(String phone, String message) {
        log.info("SMS -> {} | {}", phone, message);
    }
}
