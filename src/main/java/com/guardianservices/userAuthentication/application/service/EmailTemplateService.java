package com.guardianservices.userAuthentication.application.service;

import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.EmailTemplateJpaEntity;
import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;

    public Optional<EmailTemplateJpaEntity> findActiveTemplate(String templateName, String product) {
        // First, try to find a template specific to the product
        Optional<EmailTemplateJpaEntity> template = emailTemplateRepository.findByTemplateNameAndProductAndIsActiveTrue(templateName, product);
        
        // If not found, try to find a generic template (where product is null)
        if (template.isEmpty()) {
            return emailTemplateRepository.findByTemplateNameAndProductIsNullAndIsActiveTrue(templateName);
        }
        
        return template;
    }

    public String renderTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
