package com.nanovash.wikishort;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

	List<String> searches = new ArrayList<>();
	int index = -1;

	public void add(String s) {
		searches = searches.subList(0, ++index);
		searches.add(s);
	}

	public void goBack() {
		if (index > 0) {
			CustomThread.t.search = searches.get(--index);
			WikiShort.window.field.setText(searches.get(index));
			CustomThread.t.isHistory = true;
		}
	}

	public void goForward() {
		if (index < searches.size() - 1) {
			CustomThread.t.search = searches.get(++index);
			WikiShort.window.field.setText(searches.get(index));
			CustomThread.t.isHistory = true;
		}
	}
}
