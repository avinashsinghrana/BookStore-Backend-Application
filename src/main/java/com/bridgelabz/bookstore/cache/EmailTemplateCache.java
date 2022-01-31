package com.bridgelabz.bookstore.cache;

import com.bridgelabz.bookstore.enums.MailStatus;
import com.bridgelabz.bookstore.model.EmailTemplate;
import com.bridgelabz.bookstore.repository.EmailTemplateRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class EmailTemplateCache extends Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailTemplateCache.class);

    volatile ConcurrentMap<String, EmailTemplate> emailCache = new ConcurrentHashMap<>();

    @Autowired
    private EmailTemplateRepo emailTemplateRepo;

    @Override
    @PostConstruct
    protected void constructCache() {
        load();
    }

    @Override
    public void load() {
        List<EmailTemplate> all = emailTemplateRepo.findAll();
        if (!CollectionUtils.isEmpty(all)) {
            LOGGER.info("========= Constructing Cache ============");
            emailCache = all.stream().filter(et -> MailStatus.ACTIVE.equals(et.getStatus())).collect(Collectors.toConcurrentMap(EmailTemplate::getName, Function.identity(), (serName1, serName2) -> serName1));
        }

    }

    public void reload() {
        emailCache.clear();
        load();
    }

    public Optional<EmailTemplate> getEmailTemplate(String emailTemplateName) {
        return emailCache.entrySet().stream().filter(service -> emailTemplateName.equals(service.getKey())).map(Map.Entry::getValue).findFirst();
    }
}
