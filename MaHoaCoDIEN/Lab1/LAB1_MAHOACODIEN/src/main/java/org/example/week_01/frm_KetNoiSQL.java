package org.example.week_01;
import java.awt.*;
import javax.swing.*;

public class frm_KetNoiSQL extends JFrame {

    private final JTextField txt_server = new JTextField("127.0.0.1", 24);
    private final JTextField txt_port = new JTextField("14333", 8);
    private final JTextField txt_database = new JTextField("LAB1_MaHoaCoDien", 24);
    private final JTextField txt_user = new JTextField("lab1_user", 24);
    private final JPasswordField txt_password = new JPasswordField(24);
    private final JButton btn_test = new JButton("Test Connection");
    private final JButton btn_history = new JButton("Xem lịch sử");
    private final JLabel lbl_status = new JLabel("Chưa kết nối.");

    public frm_KetNoiSQL() {
        super("Kết nối Microsoft SQL Server");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel head = new JLabel("KẾT NỐI MICROSOFT SQL SERVER");
        head.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel form = new JPanel(new GridBagLayout());
        String[] labels = {"Server:", "Port:", "Database:", "User:", "Password:"};
        JComponent[] fields = {txt_server, txt_port, txt_database, txt_user, txt_password};
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0; c.gridy = i;
            form.add(new JLabel(labels[i]), c);
            c.gridx = 1;
            form.add(fields[i], c);
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btns.add(btn_test);
        btns.add(btn_history);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        for (JComponent comp : new JComponent[] {head, form, btns, lbl_status}) {
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            root.add(comp);
        }
        setContentPane(root);

        btn_test.addActionListener(e -> {
            try {
                int port = Integer.parseInt(txt_port.getText().trim());
                DbConfig.set(txt_server.getText().trim(), port, txt_database.getText().trim(),
                        txt_user.getText().trim(), txt_password.getPassword());
                lbl_status.setText("<html>" + DbConfig.test() + "</html>");
            } catch (Exception ex) {
                DbConfig.clear();
                lbl_status.setText("<html>Kết nối thất bại: " + ex.getMessage() + "</html>");
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Không kết nối được SQL Server", JOptionPane.ERROR_MESSAGE);
            }
        });
        btn_history.addActionListener(e -> new frm_LichSu().setVisible(true));

        setSize(520, 330);
        setLocationRelativeTo(null);
    }
}