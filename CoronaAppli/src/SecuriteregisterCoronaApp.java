import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/registerVra")
public class SecuriteregisterCoronaApp extends HttpServlet {
  private static final long serialVersionUID = 1L;

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Récupération des paramètres de la requête
    String username = request.getParameter("username");
    String firstName = request.getParameter("firstName");
    String lastName = request.getParameter("lastName");
    String password = request.getParameter("password");
    String email = request.getParameter("email");
    String city = request.getParameter("city");
    String postalCode = request.getParameter("postalCode");

    // Validation des entrees

    if (username == null || password == null || email == null) {
      response.sendRedirect("register.html");
      return;
    }

    // Chargement du Driver JDBC
    try {

      Class.forName("com.mysql.cj.jdbc.Driver");

    } catch (ClassNotFoundException e) {

      System.out.println("La classe Driver n'existe pas");
      e.printStackTrace();
    }

    // Création de la connexion à la base de données
    try (
        Connection conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3307/jordan", "root", "1708")) {

      // Verification si le mom d'utilisateur n'est pas encore pri
      String sql = "SELECT * FROM usersApp WHERE username=?";

      try (PreparedStatement statement = conn.prepareStatement(sql)) {
        statement.setString(1, username);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
          response.sendRedirect("register.html?error=usernameExists");
          return;
        }
      }
      // Verification si l'adresse mail n'est pas deja utilisee

      sql = "SELECT * FROM usersApp WHERE email=?";
      try (PreparedStatement statement = conn.prepareStatement(sql)) {

        statement.setString(1, email);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
          response.sendRedirect("register.html?error=emailExists");
          return;
        }
      }

      // Insertion dans la base de Donnees

      sql = "INSERT INTO usersApp (username, first_name, last_name, password, email, city, postal_code) VALUES (?,?,?,?,?,?,?)";
      try (PreparedStatement statement = conn.prepareStatement(sql)) {

        statement.setString(1, username);
        statement.setString(2, firstName);
        statement.setString(3, lastName);
        statement.setString(4, password);
        statement.setString(5, email);
        statement.setString(6, city);
        statement.setString(7, postalCode);

        int ex = statement.executeUpdate();

        // Verification si l'insertion a reussi
        if (ex > 0) {
          response.sendRedirect("central.html");

        } else {

          response.sendRedirect("register.html?error=InsertionError");
        }
      }
    } catch (SQLException e) {

      System.out.println("Echec");

      e.printStackTrace();
    }

  }
}
