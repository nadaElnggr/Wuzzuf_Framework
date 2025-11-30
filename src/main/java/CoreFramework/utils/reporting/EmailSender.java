package CoreFramework.utils.reporting;

import CoreFramework.config.ConfigManager;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import java.util.Properties;


public class EmailSender {

    /**
     * Send HTML email WITHOUT attachments.
     * (You can keep this for simple cases if you want.)
     */
    public static void sendFailureEmailHtml(String[] recipients, String subject, String htmlBody) {
        sendFailureEmailHtmlWithScreenshot(recipients, subject, htmlBody, null);
    }

    /**
     * Send HTML email with optional inline screenshot.
     * If screenshotBytes != null, it will be attached as inline image and referenced by cid:screenshot.
     */
    public static void sendFailureEmailHtmlWithScreenshot(
            String[] recipients,
            String subject,
            String htmlBody,
            byte[] screenshotBytes
    ) {
        // Only send real emails in prod
        if (!ConfigManager.isProd()) {
            System.out.println("[EmailSender] ENV is not PROD. Skipping email send.");
            System.out.println("[EmailSender] Would send to: " + (recipients == null ? "[]" : String.join(", ", recipients)));
            System.out.println("[EmailSender] Subject: " + subject);
            return;
        }

        if (recipients == null || recipients.length == 0) {
            System.out.println("[EmailSender] No recipients configured. Skipping email.");
            return;
        }

        final String smtpUser = ConfigManager.getSmtpUser();
        final String smtpPassword = ConfigManager.getSmtpPassword();

        if (smtpUser == null || smtpUser.isBlank() ||
                smtpPassword == null || smtpPassword.isBlank()) {
            System.out.println("[EmailSender] SMTP user or password not configured. Skipping email.");
            return;
        }

        final String host = "smtp.gmail.com";
        final int port = 587;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUser));

            InternetAddress[] toAddresses = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                toAddresses[i] = new InternetAddress(recipients[i]);
            }
            message.setRecipients(Message.RecipientType.TO, toAddresses);

            message.setSubject(subject);

            // multipart/related: HTML + inline images
            MimeMultipart related = new MimeMultipart("related");

            // 1) HTML part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
            related.addBodyPart(htmlPart);

            // 2) Optional screenshot as inline image
            if (screenshotBytes != null && screenshotBytes.length > 0) {
                MimeBodyPart imagePart = new MimeBodyPart();
                DataSource ds = new ByteArrayDataSource(screenshotBytes, "image/png");
                imagePart.setDataHandler(new DataHandler(ds));
                imagePart.setHeader("Content-ID", "<screenshot>");
                imagePart.setDisposition(MimeBodyPart.INLINE);
                related.addBodyPart(imagePart);
            }

            message.setContent(related);

            Transport.send(message);

            System.out.println("[EmailSender] HTML failure email sent to: " + String.join(", ", recipients));
        } catch (MessagingException e) {
            System.err.println("[EmailSender] Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
