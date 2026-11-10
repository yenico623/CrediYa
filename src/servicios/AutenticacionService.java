package servicios;

import dao.ClienteDao;
import dao.EmpleadoDao;
import dao.UsuarioDao;
import java.sql.SQLException;
import modelos.Cliente;
import modelos.Empleado;
import modelos.Usuario;

public class AutenticacionService {

    private final UsuarioDao usuarioDao;
    private final EmpleadoDao empleadoDao;
    private final ClienteDao clienteDao;

    public AutenticacionService() {
        this.usuarioDao = new UsuarioDao();
        this.empleadoDao = new EmpleadoDao();
        this.clienteDao = new ClienteDao();
    }

    public Usuario autenticarUsuario(String username, String password) throws SQLException {
        return usuarioDao.autenticarUsuario(username, password);
    }

    public Empleado obtenerEmpleadoPorUsuario(int usuarioId) throws SQLException {
        return empleadoDao.obtenerPorUsuarioId(usuarioId);
    }

    public Cliente buscarClientePorDocumento(String documento) throws SQLException {
        return clienteDao.buscarCliente(documento);
    }
}

