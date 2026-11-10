package org.example.week_01;

import java.awt.*;
import javax.swing.*;

public class frm_LichSu extends JFrame {

    private final JTable tbl_history = new JTable();
    private final JButton btn_refresh = new JButton("Tải lại");
    private final JLabel lbl_status = new JLabel("Đang tải...");

    public frm_LichSu() {
        super("Lịch sử thao tác (SQL Server)");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel head = new JLabel("LỊCH SỬ THAO TÁC - BẢNG DBO.HISTORY");
        head.setFont(new Font("Segoe UI", Font.BOLD, 18));
        head.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(btn_refresh);
        south.add(lbl_status);

        setLayout(new BorderLayout());
        add(head, BorderLayout.NORTH);
        add(new JScrollPane(tbl_history), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        tbl_history.setModel(HistoryDao.emptyModel());
        btn_refresh.addActionListener(e -> loadData());

        setSize(800, 420);
        setLocationRelativeTo(null);
        loadData();
    }

    private void loadData() {
        try {
            tbl_history.setModel(HistoryDao.loadLatest(50));
            lbl_status.setText("Đã tải " + tbl_history.getRowCount() + " dòng mới nhất từ dbo.history.");
        } catch (java.sql.SQLException ex) {
            lbl_status.setText("Không đọc được lịch sử: " + ex.getMessage());
        }
    }
}