package com.nanovash.wikishort;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;
import org.apache.commons.collections.IteratorUtils;

import java.awt.Color;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

public class CustomThread extends Thread {

	final static CustomThread t = new CustomThread();
	WebClient client = new WebClient(BrowserVersion.FIREFOX_24);
	String lastSearch = "";
	String search;
	HistoryManager hm = new HistoryManager();
	boolean isHistory = false;

	public CustomThread() {
		client.getOptions().setJavaScriptEnabled(false);
		client.getOptions().setPrintContentOnFailingStatusCode(false);
		client.getOptions().setCssEnabled(false);
		client.getOptions().setRedirectEnabled(true);
	}

	public void run() {
		while (true) {
			try {
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException ignored) {
			}
			UIWindow window = WikiShort.window;
			if (window.field.getText().equals("")) {
				setError("No value to search, please enter a value");
				continue;
			}
			if (lastSearch != null && lastSearch.equalsIgnoreCase(search))
				continue;
			setText("");
			HtmlPage page = null;
			try {
				page = client.getPage("https://en.wikipedia.org/wiki/" + URLEncoder.encode(search.replaceAll(" ", "_").replaceAll("\"", ""), StandardCharsets.UTF_8.toString()));
			} catch (IOException e1) {
				finish();
				continue;
			} catch (FailingHttpStatusCodeException e2) {
				setError("Such article doesn't exist");
				continue;
			}
			List<DomElement> elements = IteratorUtils.toList(page.getHtmlElementById("mw-content-text").getChildElements().iterator());
			window.links.clear();
			window.data.setForeground(Color.BLACK);
			if (isListingValues(page)) {
				window.links.put("Please choose one of the following options:", "");
				for (DomElement element : elements) {
					if (element instanceof HtmlUnorderedList) {
						List<?> lis = page.getByXPath(element.getCanonicalXPath() + "/li");
						HtmlAnchor anchor;
						for (int i = 0; i < lis.size(); i++)
							if ((anchor = getAnchor((DomElement) lis.get(i))) != null)
								window.links.put(element.asText().split("\n")[i], anchor.getTextContent());
					} else if (element.getChildElements().iterator().next().getTextContent().matches("(See also|Other)"))
						break;
				}
				finish();
				window.list.setListData(window.links.keySet().toArray(new String[window.links.keySet().size()]));
				window.pane.setViewportView(window.list);
				continue;
			}
			String print = "";
			for (DomElement element : elements) {
				if (print.endsWith(":")) {
					print += element.getTextContent().replaceAll("\\[[0-9]+\\]", "");
					break;
				} else if (!print.equals(""))
					break;
				if (element instanceof HtmlParagraph)
					print += element.getTextContent().replaceAll("\\[[0-9]+\\]", "");
			}
			setText(print);
		}
	}

	private HtmlAnchor getAnchor(DomElement li) {
		for (DomElement element : li.getChildElements()) {
			HtmlAnchor anchor;
			if (element instanceof HtmlAnchor)
				return (HtmlAnchor) element;
			else if ((anchor = getAnchor(element)) != null)
				return anchor;
		}
		return null;
	}

	private boolean isListingValues(HtmlPage page) {
		return Pattern.compile(Pattern.quote(search.toLowerCase()) + ".* may (also )?refer to:").matcher(page.asText().toLowerCase()).find();
	}

	private void finish() {
		lastSearch = search;
		if (!isHistory)
			hm.add(search);
		isHistory = false;
	}

	private void setText(String text, boolean error) {
		WikiShort.window.pane.setViewportView(WikiShort.window.data);
		WikiShort.window.data.setForeground(error ? Color.RED : Color.BLACK);
		WikiShort.window.data.setText(text);
		if (!text.equals(""))
			finish();
	}

	private void setText(String text) {
		setText(text, false);
	}

	private void setError(String error) {
		setText(String.format("Error: %s", error), true);
	}
}