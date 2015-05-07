package com.nanovash.wikishort;

import java.awt.EventQueue;

public class WikiShort {

	static UIWindow window;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				window = new UIWindow();
				window.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		CustomThread.t.start();
	}
}