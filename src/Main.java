import java.sql.SQLException;
import utilidades.GestorErrores;
import vistas.MenuPrincipal;

public class Main {
    public static void main(String[] args) {
        MenuPrincipal menu = new MenuPrincipal();
        try {
            menu.iniciar();
        } catch (SQLException e) {
            System.err.println(GestorErrores.mostrarError(e));
        }
    }
}