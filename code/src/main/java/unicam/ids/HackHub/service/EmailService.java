package unicam.ids.HackHub.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    /**
     * Invia un'email semplice
     *
     * @param recipientEmail destinatario
     * @param senderEmail    mittente
     * @param text           corpo dell'email
     */
    public void sendEmail(String recipientEmail, String senderEmail, String subject, String text) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setFrom(senderEmail);
            helper.setSubject(subject);
            helper.setText(text);

            if (false) // Momentaneamente così per non fare inviare
                javaMailSender.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Errore durante l'invio dell'email a " + recipientEmail, ex);
        }
    }
}
