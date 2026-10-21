package py.elpuente.minimarket;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Crea usuarios de acceso adicionales (usuario y contrasena numericos; pueden coincidir). */
final class UsuarioDialog extends BaseDialog {
    private final UsuarioDao dao = new UsuarioDao();
    private final JTextField txtUsuario = Ui.campo(22, 14f);
    private final JPasswordField txtClave = Ui.campoClave(22, 14f);
    private final JPasswordField txtClave2 = Ui.campoClave(22, 14f);

    UsuarioDialog(Window padre) {
        super(padre, "Usuarios", "Volver al menú");
        Ui.soloDigitos(txtUsuario, UsuarioDao.MAX_DIGITOS);
        Ui.soloDigitos(txtClave, UsuarioDao.MAX_DIGITOS);
        Ui.soloDigitos(txtClave2, UsuarioDao.MAX_DIGITOS);

        JButton crear = Ui.boton("Crear usuario", 13f);
        JPanel form = Ui.tarjeta(new GridBagLayout());
        GridBagConstraints t = new GridBagConstraints();
        t.gridx = 0; t.gridy = 0; t.gridwidth = 2; t.anchor = GridBagConstraints.WEST; t.insets = new Insets(0, 4, 4, 0);
        form.add(Ui.etiqueta("Nuevo usuario de acceso", 16f, Font.BOLD, Ui.TEXTO), t);
        t.gridy = 1; t.insets = new Insets(0, 4, 14, 0);
        form.add(Ui.etiqueta("Solo números, entre " + UsuarioDao.MIN_DIGITOS + " y " + UsuarioDao.MAX_DIGITOS
                + " dígitos. El usuario y la contraseña pueden ser iguales.", 12f, Font.PLAIN, Ui.GRIS), t);
        fila(form, 2, "Usuario", txtUsuario);
        fila(form, 3, "Contraseña", txtClave);
        fila(form, 4, "Repetir contraseña", txtClave2);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 5; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(12, 0, 0, 4);
        form.add(crear, c);

        JPanel arriba = Ui.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        arriba.add(form);
        contenido.add(arriba, BorderLayout.NORTH);

        txtUsuario.addActionListener(e -> txtClave.requestFocusInWindow());
        txtClave.addActionListener(e -> txtClave2.requestFocusInWindow());
        txtClave2.addActionListener(e -> crear.doClick());
        crear.addActionListener(e -> crear());
        SwingUtilities.invokeLater(txtUsuario::requestFocusInWindow);
    }

    private void crear() {
        String u = txtUsuario.getText().trim();
        String c = new String(txtClave.getPassword());
        if (!UsuarioDao.valido(u)) {
            Ui.aviso(this, "El usuario debe tener solo números (mínimo " + UsuarioDao.MIN_DIGITOS + " dígitos).");
            return;
        }
        if (!UsuarioDao.valido(c)) {
            Ui.aviso(this, "La contraseña debe tener solo números (mínimo " + UsuarioDao.MIN_DIGITOS + " dígitos).");
            return;
        }
        if (!c.equals(new String(txtClave2.getPassword()))) {
            Ui.aviso(this, "Las contraseñas no coinciden.");
            return;
        }
        try {
            if (dao.crear(u, c)) {
                Ui.info(this, "Usuario " + u + " creado.");
                txtUsuario.setText("");
                txtClave.setText("");
                txtClave2.setText("");
                txtUsuario.requestFocusInWindow();
            } else {
                Ui.aviso(this, "Ese usuario ya existe.");
            }
        } catch (SQLException ex) {
            Ui.error(this, "No se pudo crear el usuario:\n" + ex.getMessage());
        }
    }
}
