package com.nanovash.wikishort;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;
import org.apache.commons.collections.IteratorUtils;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class CustomThread extends Thread {

    final static CustomThread t = new CustomThread();
    WebClient client = new WebClient(BrowserVersion.FIREFOX_24);
    String search;

    public CustomThread() {
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setPrintContentOnFailingStatusCode(false);
        client.getOptions().setCssEnabled(false);
    }

    public void run() {
        main:
        while(true) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException ignored) {}
            UIWindow window = WikiShort.window;
            if(window.field.getText().equals("")) {
                setError("No value to search, please enter a value");
                continue;
            }
            window.data.setText("");
            HtmlPage page = null;
            try {
                page = client.getPage("https://en.wikipedia.org/wiki/" + window.field.getText().replaceAll(" ", "_"));
            }
            catch (IOException ignored) {}
            catch(FailingHttpStatusCodeException e2) {
                setError("Such article doesn't exist");
                continue;
            }
            List<DomElement> elements = IteratorUtils.toList(page.getHtmlElementById("mw-content-text").getChildElements().iterator());
            window.values.clear();
            window.data.setForeground(Color.BLACK);
            if (Pattern.compile(search.toLowerCase().replaceAll("_", " ") + ".* may refer to:").matcher(page.asText().toLowerCase()).find()) {
                window.values.addElement("Please choose one of the following options:");
                for(DomElement element : elements) {
                    if(element instanceof HtmlUnorderedList)
                        for(String s : element.asText().split("\n"))
                            window.values.addElement(s);
                    else if(element instanceof HtmlHeading2 && element.getChildElements().iterator().next().getTextContent().equals("See also")) {
                        window.pane.setViewportView(window.list);
                        continue main;
                    }
                }
            }
            DomElement p = null;
            for(DomElement element : elements) {
                if(element instanceof HtmlParagraph) {
                    p = element;
                    break;
                }
            }
            window.pane.setViewportView(window.data);
            window.data.setText(p.getTextContent().replaceAll("\\[[0-9]+\\]", ""));
        }
    }

    private void setError(String error) {
        UIWindow window = WikiShort.window;
        window.data.setForeground(Color.RED);
        window.data.setText(String.format("Error: %s", error));
    }
}