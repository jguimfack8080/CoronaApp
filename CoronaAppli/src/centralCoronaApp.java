import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/centralVrai")

public class centralCoronaApp extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		
		HttpSession session = request.getSession(false);
		
		if (session == null) {
			// Si la session n'existe pas, redirection vers la page de connexion
			response.sendRedirect("login.html");
			return;
		}
		// Recuperation des variables de sessions depuis la page login.html
		String username = (String) session.getAttribute("username");
		String firstName = (String) session.getAttribute("firstName");
		String lastName = (String) session.getAttribute("lastName");
		String email = (String) session.getAttribute("email");
		String city = (String) session.getAttribute("city");
		String postalCode = (String) session.getAttribute("postalCode");

		// Recuperation des informations de rendez-vous depuis le formulaire de la page
		// central.html
		String date1 = request.getParameter("date1");
		String heure1 = request.getParameter("heure1");
		String date2 = request.getParameter("date2");
		String heure2 = request.getParameter("heure2");
		String vaccine = request.getParameter("vaccine");

		// Verification si toutes les donnes du formulaire ont ete remplies.
		if (date1 != null && !date1.isEmpty() && heure1 != null && !heure1.isEmpty() && date2 != null
				&& !date2.isEmpty() && heure2 != null && !heure2.isEmpty() && vaccine != null && !vaccine.isEmpty())

		{

			// Declaration de mes variables de ssessions qui recuperent les informations des
			// rendez-vous
			session.setAttribute("date1", date1);
			session.setAttribute("heure1", heure2);
			session.setAttribute("date2", date2);
			session.setAttribute("heure2", heure2);
			session.setAttribute("vaccine", vaccine);

			// Verification de la validite des dates et heures de rendez-vous
			// Verification de la validite des dates et heures de rendez-vous

			if (!date1.equals(date2)) 
			{
				// Verification que la date2 est minimum 14 apres la date 1
				LocalDate d1 = LocalDate.parse(date1);
				LocalDate d2 = LocalDate.parse(date2);
				long diff = ChronoUnit.DAYS.between(d1, d2);

				if (diff < 14) 
				{

					response.getWriter().println(
							"La date du deuxieme rendez-vous doit etre minimum 14 jous apres la date du premier rendez-vous.");

					return;
				}
			}

			// Verification de la disponibilite des heures de rendez-vous choisies
			try 
			{

				// Connexion a la base de donnees
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3307/jordan", "root", "1708");

				// Verification de la disponibilite de l'heure1
				// Compter le nombre de rendez-vous pour l'heure 1 a la date 1
				PreparedStatement stmt1 = con
						.prepareStatement("SELECT COUNT(*) FROM appointmentsApp WHERE date1=? AND heure1=?");
				stmt1.setString(1, date1);
				stmt1.setString(2, heure1);
				ResultSet rs1 = stmt1.executeQuery();
				rs1.next();
				int count1 = rs1.getInt(1);

				// Verifier si le creneau horaire 1 a la date 1 est disponible
				if (count1 >= 2) {
					response.getWriter().println(
							"<br><h2>L'heure du premier rendez-vous choisie n'est plus disponible. Veuillez en choisir une autre.<h2>");
					return;
				}

				// Compter le nombre de rendez-vous pour l'heure 2 a la date 2
				PreparedStatement stmt2 = con
						.prepareStatement("SELECT COUNT(*) FROM appointmentsApp WHERE date2=? AND heure2=?");
				stmt2.setString(1, date2);
				stmt2.setString(2, heure2);
				ResultSet rs2 = stmt2.executeQuery();
				rs2.next();
				int count2 = rs2.getInt(1);

				// Verifier si le creneau horaire 2 a la date 2 est disponible
				if (count2 >= 2) {

					response.getWriter().println(
							"<br><h2>L'heure du deuxieme rendez-vous choisie n'est plus disponible. Veuillez en choisir une autre.</h2>");
					return;
				}

				// Verification pour que l'utilisateur ne puisse pas reserve un meme rendez vous
				// plusieurs fois

				// Compter le nombre de rendez-vous pour l'heure 1 a la date 1
				PreparedStatement stmt3 = con.prepareStatement(
						"SELECT COUNT(*) FROM appointmentsApp WHERE date1=? AND heure1=? AND username=?");
				stmt3.setString(1, date1);
				stmt3.setString(2, heure1);
				stmt3.setString(3, username);
				ResultSet rs3 = stmt3.executeQuery();
				rs3.next();
				count1 = rs3.getInt(1);

				// Verifier si l'utilisateur connecte a deja reserve ce creneau horaire
				if (count1 > 0) {

					response.getWriter().println(
							"Erreur : vous avez deja reserve un rendez-vous pour l'heure du premier creneau horaire choisi.");
					return;
				}

				// Compter le nombre de rendez-vous pour l'heure 2 a la date 2
				PreparedStatement stmt4 = con.prepareStatement(
						"SELECT COUNT(*) FROM appointmentsApp WHERE date2=? AND heure2=? AND username=?");
				stmt4.setString(1, date2);
				stmt4.setString(2, heure2);
				stmt4.setString(3, username);
				ResultSet rs4 = stmt4.executeQuery();
				rs4.next();
				count2 = rs4.getInt(1);

				// Verifier si l'utilisateur connecte a deja reserve ce creneau horaire
				if (count2 > 0) 
				{
					response.getWriter().println(
							"Erreur : Vous avez deja reserve un rendez-vous pour l'heure du deuxieme creneau horaire choisi.");
					return;
				}

				// Enregistrement du rendez-vous dans la base de donnees
				PreparedStatement stmt5 = con.prepareStatement(
						"INSERT INTO appointmentsApp (username, first_name, last_name, email, city, postal_code, date1, heure1, date2, heure2, vaccine) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				stmt5.setString(1, username);
				stmt5.setString(2, firstName);
				stmt5.setString(3, lastName);
				stmt5.setString(4, email);
				stmt5.setString(5, city);
				stmt5.setString(6, postalCode);
				stmt5.setString(7, date1);
				stmt5.setString(8, heure1);
				stmt5.setString(9, date2);
				stmt5.setString(10, heure2);
				stmt5.setString(11, vaccine);
				int rowsAffected = stmt5.executeUpdate();
				if (rowsAffected > 0) {
					response.getWriter().println("Votre rendez-vous est confirme.");
					response.sendRedirect("confirmation.html");
				} else {
					response.getWriter().println(
							"Erreur : votre rendez-vous n'a pas pu etre enregistre. Veuillez reessayer plus tard.");
				}
				con.close();
			} catch (Exception e) {
				response.getWriter().println("Erreur : " + e.getMessage());
			}

			// Toutes les données ont été remplies

		} else {

			// Verification pour chaque Donnee
			if (date1 != null && date1.isEmpty()) {

				response.getWriter().println("<br><h2>Veuillez choisir une date pour le premier rendez vous!</h2>");

			}

			if (heure1 != null && heure1.isEmpty()) {

				response.getWriter().println("<br><h2>Veulez choisir une date pour le deuxieme rendez-vous</h2>");
			}

			if (date2 != null && date2.isEmpty()) {

				response.getWriter().println("<br><h2>Veulez choisir une date pour le deuxieme rendez-vous</h2>");
			}

			if (heure2 != null && heure2.isEmpty()) {

				response.getWriter().println("<br><h2>Veulez choisir une heure pour le deuxieme rendez-vous.</h2>");
			}

			if (vaccine != null && vaccine.isEmpty()) {

				response.getWriter().println("<br><h2>Veulez choisir la marque de vaccin souhaitee.</h2>");
			}

			// response.getWriter().println("Veuillez remplir tous les champs");
		}

	}
}