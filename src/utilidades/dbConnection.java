
package utilidades;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbConnection {

    private static dbConnection instancia;
    private Connection conexion;

    private final String URL = "jdbc:mysql://localhost:3306/crediya_db";
    private final String USER = "root";
    private final String PASS = "28260920";

    private dbConnection() {
        try {
            conexion = DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.err.println("Error en la conexion: " + e.getMessage());
        }
    }

    public static dbConnection getInstancia() {
        if (instancia == null) {
            instancia = new dbConnection();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }

    public void desconectar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexion: " + e.getMessage());
        }
    }
}