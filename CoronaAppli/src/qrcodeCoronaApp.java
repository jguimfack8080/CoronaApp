//import java.io.File;
//import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

@WebServlet("/qrcode")
public class qrcodeCoronaApp extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	
    	// Obtention des attributs de session
    	HttpSession session = request.getSession();
    	String username = (String) session.getAttribute("username");
    	String firstName = (String) session.getAttribute("firstName");
    	String lastName = (String) session.getAttribute("lastName");
    	String email = (String) session.getAttribute("email");
    	String city = (String) session.getAttribute("city");
    	String postalCode = (String) session.getAttribute("postalCode");
    	String date1 = (String) session.getAttribute("date1");
    	String heure1 = (String) session.getAttribute("heure1");
    	String date2 = (String) session.getAttribute("date2");
    	String heure2 = (String) session.getAttribute("heure2");
    	

    	// Définition du contenu du QR Code
        String data = "Username: " + username + "\n" +
                      "First name: " + firstName + "\n" +
                      "Last name: " + lastName + "\n" +
                      "Email: " + email + "\n" +
                      "City: " + city + "\n" +
                      "Postal code: " + postalCode + "\n" +
        			  "Datum Erster Termin: " + date1 + "\n" +
                      "Uhrzeit erster Termin: " + heure1 + "\n" +
                      "Datum zweiter Termin: " + date2 + "\n" +
                      "Uhrzeit zweiter Termin: " + heure2 + "\n";

    	
    	//Déclaration du contenu du QR Code, de l'emplacement et du nom du fichier, ainsi que de la largeur et de la hauteur de l'image
        //String data = "Hallo je m'appele jordan";
        //File path = new File("./img.png");
        int width = 400;
        int height = 400;
        

        try {
            // Génération de la matrice du QR Code à partir des paramètres définis
            BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height);
            
            // Écriture de la matrice dans le flux de sortie de la réponse HTTP
            response.setContentType("image/png");
            MatrixToImageWriter.writeToStream(matrix, "png", response.getOutputStream());
            
        } catch (WriterException | IOException e) {
            // Gestion des erreurs de génération du QR Code
            System.err.println("Erreur lors de la création de l'image : " + e.getMessage());
        }
    }
}
