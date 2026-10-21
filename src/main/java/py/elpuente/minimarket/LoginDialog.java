package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

/**
 * Inicio de sesion. Usuario y contrasena son SOLO numericos y pueden ser iguales.
 * Si todavia no existe ningun usuario, la misma pantalla sirve para crear el primero.
 */
final class LoginDialog extends JDialog {
    private static final int MAX_INTENTOS = 5;
    private static final int SEGUNDOS_BLOQUEO = 30;

    private final UsuarioDao dao = new UsuarioDao();
    private final boolean primerUso;
    private final JTextField txtUsuario = Ui.campo(20, 15f);
    private final JPasswordField txtClave = Ui.campoClave(20, 15f);
    private final JPasswordField txtClave2 = Ui.campoClave(20, 15f);
    private final JCheckBox chkVer = new JCheckBox("Mostrar contraseña");
    private final JLabel lblMsg = Ui.etiqueta(" ", 12f, Font.PLAIN, Ui.PELIGRO);
    private final JButton btn;

    private boolean exitoso;
    private int fallos;
    private Timer bloqueo;

    LoginDialog(boolean primerUso) {
        super((Frame) null, Ui.TITULO, true);
        this.primerUso = primerUso;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        Ui.soloDigitos(txtUsuario, UsuarioDao.MAX_DIGITOS);
        Ui.soloDigitos(txtClave, UsuarioDao.MAX_DIGITOS);
        Ui.soloDigitos(txtClave2, UsuarioDao.MAX_DIGITOS);
        btn = Ui.boton(primerUso ? "Crear usuario y entrar" : "Ingresar", 14f);
        btn.setBorder(new EmptyBorder(14, 10, 14, 10));

        JPanel raiz = new JPanel(new GridLayout(1, 2));
        raiz.add(panelMarca());
        raiz.add(panelFormulario());
        setContentPane(raiz);
        setSize(880, primerUso ? 600 : 540);
        setLocationRelativeTo(null);

        txtUsuario.addActionListener(e -> txtClave.requestFocusInWindow());
        txtClave.addActionListener(e -> {
            if (primerUso) txtClave2.requestFocusInWindow(); else enviar();
        });
        txtClave2.addActionListener(e -> enviar());
        btn.addActionListener(e -> enviar());
        chkVer.addActionListener(e -> {
            char eco = chkVer.isSelected() ? (char) 0 : '•';
            txtClave.setEchoChar(eco);
            txtClave2.setEchoChar(eco);
        });
        SwingUtilities.invokeLater(txtUsuario::requestFocusInWindow);
    }

    boolean exitoso() { return exitoso; }

    private JPanel panelMarca() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setPaint(new GradientPaint(0, 0, Ui.PRIMARIO_OSC, getWidth(), getHeight(), Ui.PRIMARIO));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(255, 255, 255, 18));
                g.fillOval(-120, getHeight() - 220, 380, 380);
                g.fillOval(getWidth() - 140, -100, 280, 280);
                g.dispose();
            }
        };
        p.setOpaque(false);
        Ui.Tarjeta insignia = Ui.tarjeta(new BorderLayout());
        insignia.setBorder(new EmptyBorder(16, 26, 16, 26));
        insignia.add(Ui.logo(2.0), BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.insets = new Insets(0, 0, 28, 0);
        p.add(insignia, c);
        c.gridy = 1; c.insets = new Insets(0, 0, 6, 0);
        p.add(Ui.etiqueta("Bienvenido", 24f, Font.BOLD, Color.WHITE), c);
        c.gridy = 2; c.insets = new Insets(0, 0, 0, 0);
        p.add(Ui.etiqueta("Sistema de ventas y control de precios", 12f, Font.PLAIN, new Color(204, 240, 235)), c);
        return p;
    }

    private JPanel panelFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(30, 48, 30, 48));
        chkVer.setOpaque(false);
        chkVer.setFont(Ui.fuente(11f, Font.PLAIN));
        chkVer.setForeground(Ui.GRIS);
        chkVer.setFocusPainted(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST;
        int y = 0;
        c.gridy = y++; p.add(Ui.etiqueta(primerUso ? "Crear usuario" : "Iniciar sesión", 22f, Font.BOLD, Ui.TEXTO), c);
        c.gridy = y++; c.insets = new Insets(4, 0, 22, 0);
        p.add(Ui.etiqueta(primerUso
                ? "<html>Aún no hay usuarios. Cree el primero: usuario y contraseña solo con números.</html>"
                : "Ingrese su usuario y contraseña numéricos.", 12f, Font.PLAIN, Ui.GRIS), c);

        c.insets = new Insets(0, 0, 6, 0);
        c.gridy = y++; p.add(Ui.etiqueta("Usuario", 12f, Font.BOLD, Ui.TEXTO), c);
        c.insets = new Insets(0, 0, 14, 0);
        c.gridy = y++; p.add(txtUsuario, c);

        c.insets = new Insets(0, 0, 6, 0);
        c.gridy = y++; p.add(Ui.etiqueta("Contraseña", 12f, Font.BOLD, Ui.TEXTO), c);
        c.insets = new Insets(0, 0, primerUso ? 14 : 8, 0);
        c.gridy = y++; p.add(txtClave, c);

        if (primerUso) {
            c.insets = new Insets(0, 0, 6, 0);
            c.gridy = y++; p.add(Ui.etiqueta("Repetir contraseña", 12f, Font.BOLD, Ui.TEXTO), c);
            c.insets = new Insets(0, 0, 8, 0);
            c.gridy = y++; p.add(txtClave2, c);
        }
        c.insets = new Insets(0, 0, 12, 0);
        c.gridy = y++; p.add(chkVer, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 14, 0); p.add(lblMsg, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 0, 0); p.add(btn, c);
        c.gridy = y; c.weighty = 1; p.add(Box.createVerticalGlue(), c);
        return p;
    }

    private void mensaje(String texto) { lblMsg.setText(texto); }

    private void enviar() {
        if (!btn.isEnabled()) return;
        String usuario = txtUsuario.getText().trim();
        String clave = new String(txtClave.getPassword());

        if (!UsuarioDao.valido(usuario)) {
            mensaje("El usuario debe tener solo números (mínimo " + UsuarioDao.MIN_DIGITOS + " dígitos).");
            return;
        }
        if (!UsuarioDao.valido(clave)) {
            mensaje("La contraseña debe tener solo números (mínimo " + UsuarioDao.MIN_DIGITOS + " dígitos).");
            return;
        }
        try {
            if (primerUso) {
                if (!clave.equals(new String(txtClave2.getPassword()))) {
                    mensaje("Las contraseñas no coinciden.");
                    return;
                }
                if (!dao.crear(usuario, clave)) {
                    mensaje("Ese usuario ya existe.");
                    return;
                }
                entrar(usuario);
            } else if (dao.verificar(usuario, clave)) {
                entrar(usuario);
            } else {
                fallos++;
                txtClave.setText("");
                txtClave.requestFocusInWindow();
                if (fallos >= MAX_INTENTOS) bloquear();
                else mensaje("Usuario o contraseña incorrectos. Intentos restantes: " + (MAX_INTENTOS - fallos));
            }
        } catch (SQLException ex) {
            mensaje("No se pudo conectar a la base de datos.");
        }
    }

    private void entrar(String usuario) {
        Sesion.iniciar(usuario);
        exitoso = true;
        dispose();
    }

    /** Tras demasiados intentos fallidos, espera unos segundos antes de permitir otro. */
    private void bloquear() {
        btn.setEnabled(false);
        int[] restante = {SEGUNDOS_BLOQUEO};
        mensaje("Demasiados intentos. Espere " + restante[0] + " s.");
        bloqueo = new Timer(1000, e -> {
            restante[0]--;
            if (restante[0] <= 0) {
                bloqueo.stop();
                fallos = 0;
                btn.setEnabled(true);
                mensaje(" ");
            } else {
                mensaje("Demasiados intentos. Espere " + restante[0] + " s.");
            }
        });
        bloqueo.start();
    }
}
