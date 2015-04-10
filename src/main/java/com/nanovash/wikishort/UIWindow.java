package com.nanovash.wikishort;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIWindow extends JFrame {

	JPanel contentPane;
	JTextField field;
	JScrollPane pane;
    JTextArea data;
	DefaultListModel<String> values;
	JList<String> list;

	public UIWindow() {
        setTitle("WikiShort");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.setBackground(Color.WHITE);
		setContentPane(contentPane);

		field = new JTextField();
		contentPane.add(field, BorderLayout.NORTH);
		field.setColumns(10);

		 final JButton search = new JButton("Search");
        search.addActionListener(e -> {
			CustomThread.t.search = WikiShort.window.field.getText().trim().replaceAll(" ", "_").toLowerCase();
			synchronized (CustomThread.t) {
				CustomThread.t.notify();
			}
		});
		contentPane.add(search, BorderLayout.SOUTH);

		pane = new JScrollPane();
		data = new JTextArea();
		data.setEditable(false);
		data.setLineWrap(true);
		data.setWrapStyleWord(true);
		values = new DefaultListModel<>();
		list = new JList<>(values);
		contentPane.add(pane, BorderLayout.CENTER);
		pane.setViewportView(data);

		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && list.getSelectedIndex() != 0) {
					field.setText((CustomThread.t.search = list.getSelectedValue().split(",")[0]));
					search.doClick();
				}
			}
		});

		field.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					search.doClick();
			}
		});
	}
}