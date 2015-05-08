package com.nanovash.wikishort;

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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;

public class UIWindow extends JFrame {

	JPanel contentPane = new JPanel(),
			searchBar = new JPanel(),
			buttons = new JPanel();
	JTextField field = new JTextField();
	JScrollPane pane = new JScrollPane();
	JTextArea data = new JTextArea();
	JList<String> list = new JList<>();
	LinkedHashMap<String, String> links = new LinkedHashMap<>();

	public UIWindow() {
		setTitle("WikiShort");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.setBackground(Color.WHITE);
		setContentPane(contentPane);

		contentPane.add(searchBar, BorderLayout.NORTH);
		field.setColumns(10);
		searchBar.setLayout(new BorderLayout(0, 0));
		searchBar.add(field, BorderLayout.CENTER);
		searchBar.add(buttons, BorderLayout.WEST);
		buttons.setLayout(new GridLayout(1, 2));

		final JButton search = new JButton("Search");
		search.setFocusPainted(false);
		search.addActionListener(e -> {
			CustomThread.t.search = WikiShort.window.field.getText().trim().toLowerCase();
			wakeThread();
		});
		contentPane.add(search, BorderLayout.SOUTH);

		data.setEditable(false);
		data.setLineWrap(true);
		data.setWrapStyleWord(true);
		contentPane.add(pane, BorderLayout.CENTER);
		pane.setViewportView(data);

		JButton left = createButton("\u21E6");
		JButton right = createButton("\u21E8");
		left.addActionListener(e -> {
			CustomThread.t.hm.goBack();
			wakeThread();
		});
		right.addActionListener(e -> {
			CustomThread.t.hm.goForward();
			wakeThread();
		});

		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && list.getSelectedIndex() != 0) {
					field.setText(CustomThread.t.search = links.get(list.getSelectedValue()));
					wakeThread();
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

	public JButton createButton(String s) {
		JButton button = new JButton(s);
		button.setPreferredSize(new Dimension(50, 0));
		button.setFont(button.getFont().deriveFont(15f));
		button.setFocusPainted(false);
		buttons.add(button);
		return button;
	}

	public void wakeThread() {
		synchronized (CustomThread.t) {
			CustomThread.t.notify();
		}
	}
}