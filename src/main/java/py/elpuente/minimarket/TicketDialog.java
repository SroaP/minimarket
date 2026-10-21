package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.print.PrinterException;

/** Muestra el ticket en pantalla. Si hay impresora, tambien permite imprimirlo. */
final class TicketDialog extends JDialog {

    TicketDialog(Window padre, Ticket ticket) {
        super(padre, "Ticket N° " + ticket.numero(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Ui.FONDO);
        setLayout(new BorderLayout(0, 12));

        JTextArea papel = new JTextArea(ticket.texto());
        papel.setEditable(false);
        papel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        papel.setForeground(Ui.TEXTO);
        papel.setBackground(Color.WHITE);
        papel.setBorder(new EmptyBorder(20, 22, 20, 22));

        JScrollPane sp = new JScrollPane(papel);
        sp.setBorder(new LineBorder(Ui.BORDE));
        JPanel marco = Ui.transparente(new BorderLayout());
        marco.setBorder(new EmptyBorder(20, 24, 0, 24));
        marco.add(sp, BorderLayout.CENTER);

        JButton cerrar = Ui.boton("Cerrar", 12f, Ui.Estilo.NEUTRO);
        JButton imprimir = Ui.boton("Imprimir", 12f);
        cerrar.addActionListener(e -> dispose());
        imprimir.addActionListener(e -> {
            try {
                if (!papel.print()) return;   // el usuario cancelo
            } catch (PrinterException ex) {
                Ui.aviso(this, "No se pudo imprimir. Verifique que haya una impresora configurada.\n" + ex.getMessage());
            }
        });
        JPanel botones = Ui.transparente(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botones.setBorder(new EmptyBorder(0, 0, 18, 0));
        botones.add(cerrar);
        botones.add(imprimir);

        add(marco, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        setSize(500, 640);
        setLocationRelativeTo(padre);
    }
}
