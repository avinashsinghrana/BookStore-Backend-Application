package com.bridgelabz.bookstore.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
public class AsyncTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTask.class);

    private static final int RETRY_COUNT = 1;
    private static final String USER_NAME = "testserver.travelx@gmail.com";
//    private static final String USER_NAME = "noreplytokenid@gmail.com";
//    private static final String USER_NAME = "pooponchipoo@gmail.com";
//    private static final String PASSWORD = "Noreply@123";
    private static final String PASSWORD = "jspqsqppgpprklyj";
//    private static final String PASSWORD = "Tom$Jerry";

    @Async
    public void sendEmail(String emailrecipients, String emailSubject, String emailBody) {
        trySendingEmail(emailrecipients, emailSubject, emailBody);
    }

    private void trySendingEmail(String emailrecipients, String emailSubject, String emailBody) {
        LOGGER.info("Sending Mail.......");
        Future<Boolean> emailTaskFuture;
        if (org.springframework.util.StringUtils.isEmpty(emailrecipients)) {
            LOGGER.info("EMAIL ADDRESS NULL OR INVALID, Exit");
            return;
        }
        emailrecipients = eliminateInvalidEmails(emailrecipients);

        if (emailrecipients == null) {
            LOGGER.info("No Valid Email Recipients, Exit");
            return;
        }

        emailrecipients = Arrays.stream(emailrecipients.split(",")).distinct().collect(Collectors.joining(","));


        Integer retryCount = RETRY_COUNT;
        int count = 1;
        while (count <= retryCount) {
            emailTaskFuture = sendMailFromGmail(emailrecipients, emailSubject, emailBody);
            boolean isEmailSent;
            try {
                isEmailSent = emailTaskFuture.get();
                if (isEmailSent)
                    break;
                else
                    LOGGER.info("Failed to send email with count {}, Retrying Next count", count);
                count++;
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Exception while Try sending Email", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Async
    public void sendEmail(String emailrecipients, String emailSubject, String emailBody, byte[] bs, String fileName) {
        trySendingEmail(emailrecipients, emailSubject, emailBody, Arrays.asList(bs), Arrays.asList(fileName));
    }

    public void trySendingEmail(String emailrecipients, String emailSubject, String emailBody, List<byte[]> bs, List<String> fileName) {

        if (org.springframework.util.StringUtils.isEmpty(emailrecipients)) {
            LOGGER.info("EMAIL ADDRESS NULL OR INVALID, Exit");
            return;
        }
        emailrecipients = eliminateInvalidEmails(emailrecipients);

        if (emailrecipients == null) {
            LOGGER.info("No Valid Email Recipients, Exit");
            return;
        }

        emailrecipients = Arrays.stream(emailrecipients.split(",")).distinct().collect(Collectors.joining(","));

        Future<Boolean> emailTaskFuture;
        Integer retryCount = RETRY_COUNT;
        int count = 1;
        while (count <= retryCount) {


            emailTaskFuture = sendMailFromGmail(emailrecipients, emailSubject, emailBody, fileName, bs);

            boolean isEmailSent;
            try {
                isEmailSent = emailTaskFuture.get();
                if (isEmailSent)
                    break;
                else
                    LOGGER.info("Failed to send email with count {}, Retrying Next count", count);
                count++;
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Exception while Try sending Email", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Async
    public Future<Boolean> sendMailFromGmail(String emailrecipients, String emailSubject, String emailBody, List<String> fileName, List<byte[]> ba) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.starttls.enable", "true");
        javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER_NAME, PASSWORD);
            }
        });
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.setFrom(new InternetAddress(USER_NAME));
            msg.setReplyTo(InternetAddress.parse(emailrecipients, false));
            msg.setSubject(emailSubject, "UTF-8");
            Multipart multiPart = new MimeMultipart();
            msg.setSentDate(new Date());
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(emailBody, "utf-8", "html");
            multiPart.addBodyPart(messageBodyPart);
            for (int i = 0; i < ba.size(); i++) {
                byte[] bs = ba.get(i);
                if (bs != null && bs.length > 0) {
                    DataSource fds = new ByteArrayDataSource(bs, "application/octet-stream");
                    MimeBodyPart attachment = new MimeBodyPart();
                    attachment.setDataHandler(new DataHandler(fds));
                    attachment.setDisposition(Part.ATTACHMENT);
                    attachment.setFileName(fileName.get(i));
                    multiPart.addBodyPart(attachment);
                }
            }
            msg.setContent(multiPart);
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailrecipients, false));
            Transport.send(msg);
            LOGGER.error("Email Sent Successfully");
            return new AsyncResult<>(true);
        } catch (Exception e) {
            LOGGER.error("Exception while Sending Email ", e);
            return new AsyncResult<>(false);
        }
    }

    @Async
    public void sendEmail(String emailrecipients, String emailSubject, String emailBody, List<byte[]> bs, List<String> fileName) {
        trySendingEmail(emailrecipients, emailSubject, emailBody, bs, fileName);
    }

    @Async
    public void sendEmail(String emailrecipients, String emailSubject, String emailBody, String ccList, byte[] bs, String fileName) {
        trySendingEmail(emailrecipients, emailSubject, emailBody, ccList, Arrays.asList(bs), Arrays.asList(fileName));
    }

    public void trySendingEmail(String emailrecipients, String emailSubject, String emailBody, String ccList, List<byte[]> bs, List<String> fileName) {

        if (!org.springframework.util.StringUtils.hasText(emailrecipients)) {
            LOGGER.info("EMAIL ADDRESS NULL OR INVALID, Exit");
            return;
        }

        emailrecipients = eliminateInvalidEmails(emailrecipients);

        if (!org.springframework.util.StringUtils.hasText(emailrecipients)) {
            LOGGER.info("No Valid Email Recipients, Exit");
            return;
        }

        emailrecipients = Arrays.stream(emailrecipients.split(",")).distinct().collect(Collectors.joining(","));

        if (org.springframework.util.StringUtils.hasText(ccList)) {
            ccList = eliminateInvalidEmails(ccList);
            if (org.springframework.util.StringUtils.hasLength(ccList))
                ccList = removeDuplicateEmailRecipientsFromCC(emailrecipients, ccList);
        }

        Future<Boolean> emailTaskFuture;
        Integer retryCount = RETRY_COUNT;
        int count = 1;
        while (count <= retryCount) {

            emailTaskFuture = sendMailFromGmailWithAttachmentAndCCRecipients(emailrecipients, emailSubject, emailBody, ccList, fileName, bs);

            boolean isEmailSent;
            try {
                isEmailSent = emailTaskFuture.get();
                if (isEmailSent)
                    break;
                else
                    LOGGER.info("Failed to send email with count {}, Retrying Next count", count);
                count++;
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Exception while Try sending Email", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Async
    public Future<Boolean> sendMailFromGmailWithAttachmentAndCCRecipients(String emailrecipients, String emailSubject, String emailBody, String ccRecipients, List<String> fileName,
                                                                          List<byte[]> ba) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.starttls.enable", "true");
        javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER_NAME, PASSWORD);
            }
        });
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.setFrom(new InternetAddress(USER_NAME));
            msg.setReplyTo(InternetAddress.parse(emailrecipients, false));
            if (!StringUtils.isEmpty(ccRecipients))
                msg.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccRecipients));
            msg.setSubject(emailSubject, "UTF-8");
            Multipart multiPart = new MimeMultipart();
            msg.setSentDate(new Date());
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(emailBody, "utf-8", "html");
            multiPart.addBodyPart(messageBodyPart);
            for (int i = 0; i < ba.size(); i++) {
                byte[] bs = ba.get(i);
                if (bs != null && bs.length > 0) {
                    DataSource fds = new ByteArrayDataSource(bs, "application/octet-stream");
                    MimeBodyPart attachment = new MimeBodyPart();
                    attachment.setDataHandler(new DataHandler(fds));
                    attachment.setDisposition(Part.ATTACHMENT);
                    attachment.setFileName(fileName.get(i));
                    multiPart.addBodyPart(attachment);
                }
            }
            msg.setContent(multiPart);
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailrecipients, false));
            Transport.send(msg);
            LOGGER.error("Email Sent Successfully");
            return new AsyncResult<>(true);
        } catch (Exception e) {
            LOGGER.error("Exception while Sending Email ", e);
            return new AsyncResult<>(false);
        }
    }

    private String removeDuplicateEmailRecipientsFromCC(String emailRecipients, String ccList) {
        return Arrays.stream(ccList.split(",")).map(String::trim).filter(s -> !emailRecipients.contains(s)).distinct().collect(Collectors.joining(","));
    }

    @Async
    public Future<Boolean> sendMailFromGmail(String emailrecipients, String emailSubject, String emailBody) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.starttls.enable", "true");
        javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER_NAME, PASSWORD);
            }
        });
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.setFrom(new InternetAddress(USER_NAME));
            msg.setReplyTo(InternetAddress.parse(emailrecipients, false));
            msg.setSubject(emailSubject, "UTF-8");
            msg.setSentDate(new Date());
            msg.setText(emailBody, "UTF-8", "html");
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailrecipients, false));
            Transport.send(msg);
            LOGGER.error("Email Sent Successfully");
            return new AsyncResult<>(true);
        } catch (Exception e) {
            LOGGER.error("Exception while Sending Email ", e);
            return new AsyncResult<>(false);
        }
    }

    private String eliminateInvalidEmails(String emailrecipients) {
        String[] emailIds = emailrecipients.split(",");
        StringBuilder sb = null;
        for (String email : emailIds) {
            if (Utils.isValidEmail(email)) {
                if (sb == null) {
                    sb = new StringBuilder();
                    sb.append(email);
                } else
                    sb.append(",").append(email);
            } else {
                LOGGER.info("Eliminating Invalid Email {} from email recipients {} ", email, emailrecipients);
            }
        }
        if (sb != null)
            return sb.toString();
        else
            return null;
    }

}
