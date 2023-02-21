import java.io.ByteArrayOutputStream;
import java.io.IOException;
//import java.io.OutputStream;
//import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
//import java.util.Collections;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

@WebServlet("/mailSender")
public class confirmationServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Verification de l'existence de la session

        HttpSession session = request.getSession(false);
        if (session == null) {
            // Si la session n'existe pas, redirection vers la page de connexion
            response.sendRedirect("login.html");
            return;
        }

        // Recuperation des informations de session
        String username = (String) request.getSession().getAttribute("username");
        String firstName = (String) request.getSession().getAttribute("firstName");
        String lastName = (String) request.getSession().getAttribute("lastName");
        String email = (String) request.getSession().getAttribute("email");
        String city = (String) request.getSession().getAttribute("city");
        String postalCode = (String) request.getSession().getAttribute("postalCode");
        LocalDate date1 = LocalDate.parse((String) request.getSession().getAttribute("date1"));
        LocalTime heure1 = LocalTime.parse((String) request.getSession().getAttribute("heure1"));
        LocalDate date2 = LocalDate.parse((String) request.getSession().getAttribute("date2"));
        LocalTime heure2 = LocalTime.parse((String) request.getSession().getAttribute("heure2"));
        String vaccine = (String) request.getSession().getAttribute("vaccine");

        // Creation du document PDF
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

         // Création d'une police pour les titres
            Font titleFont = new Font(FontFamily.HELVETICA, 16, Font.BOLD);

            // Ajout du titre principal
            Paragraph title = new Paragraph("Terminbestätigung für " + firstName + " " +lastName, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Ajout des informations utilisateur
            document.add(new Paragraph("Name: " + lastName));
            document.add(new Paragraph("Vorname: " + firstName));
            document.add(new Paragraph("Email: " + email));
            document.add(new Paragraph("Stadt: " + city));
            document.add(new Paragraph("Postleitzahl: " + postalCode));

            // Ajout des informations de rendez-vous
            Paragraph appointmentHeader = new Paragraph("Termine:", titleFont);
            appointmentHeader.setSpacingAfter(20);
            document.add(appointmentHeader);

            document.add(new Paragraph("Erster Termin: " + date1 + " um " + heure1));
            document.add(new Paragraph("Zweiter Termin: " + date2 + " um " + heure2));
            document.add(new Paragraph("Impfstoff: " + vaccine));

            // Ajout d'un message de confirmation
            Paragraph confirmation = new Paragraph("Vielen Dank für Ihre Terminbuchung. Bitte bringen Sie Ihren Personalausweis  mit.");
            confirmation.setAlignment(Element.ALIGN_CENTER);
            confirmation.setSpacingBefore(20);
            document.add(confirmation);


        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        // Envoi du document PDF au client par email
        String to = email; // client's email address
        String from = "coronaappljma@gmail.com"; // l'adresse email pour l'envoie
        String password = "fenjfqbjsxgthyha"; // Mot de passe de l'adresse mail
        String host = "smtp.gmail.com"; // your email provider's SMTP server
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");

        Session session2 = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        MimeMessage message = new MimeMessage(session2);
        try {
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Terminbestätigung für " + lastName);

         // Créer la partie corps du message
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent("Sehr geehrte/r " +"<b>"+ firstName + " " + lastName +"</b>"
                    + ",<br/><br/>anbei erhalten Sie die Terminbestätigung für Ihren Impftermin. "
                    + "Bitte bringen Sie das Dokument zum Impfzentrum mit.<br/><br/>Mit freundlichen Grüßen<br/>"
                    + "Ihr Impfzentrum", "text/html");

            // Créer le corps de la pièce jointe
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            attachmentBodyPart.setFileName(username + "Reservation.pdf");
            attachmentBodyPart.setDataHandler(new javax.activation.DataHandler(new javax.mail.util.ByteArrayDataSource(baos.toByteArray(), "application/pdf")));

            // Créez le message multipart et ajoutez-y les parties du corps.
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentBodyPart);

            // Définir le message multipartite comme le message e-mail
            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Le mail a ete envoye avec succes.");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

     // Définir le type de contenu de la réponse à "text/html".
        response.setContentType("text/html");

        // Write a success message to the response output stream
        response.getWriter().write("<h1>Die Terminbestätigung wurde erfolgreich an Ihre E-Mail-Adresse gesendet!</h1>");
    }
}
