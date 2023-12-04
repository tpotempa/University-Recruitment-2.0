package pl.edu.anstar.recruitment.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.apache.commons.lang3.StringUtils;

@Service
public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMail(String from, String fromName, String subject, String toAddresses, String ccAddresses, String bccAddresses, String body) {
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
            message.setTo(toAddresses.split("[,;]"));
            message.setFrom(from, fromName);
            message.setSubject(subject);
            if (StringUtils.isNotBlank(ccAddresses))
                message.setCc(ccAddresses.split("[;,]"));
            if (StringUtils.isNotBlank(bccAddresses))
                message.setBcc(bccAddresses.split("[;,]"));
            message.setText(body, false);
        };
        mailSender.send(preparator);
        LOGGER.info("Email sent successfully to {},{} with subject {}", toAddresses, ccAddresses, subject);
    }
}