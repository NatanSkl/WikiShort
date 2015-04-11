package com.nanovash.wikishort;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
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
    String search;
    String lastSearch = "";

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
            if(lastSearch.equals(search))
                continue;
            window.data.setText("");
            HtmlPage page = null;
            try {
                page = client.getPage("https://en.wikipedia.org/wiki/" + URLEncoder.encode(window.field.getText().replaceAll(" ", "_").replaceAll("\"", ""), StandardCharsets.UTF_8.toString()));
            }
            catch (IOException e1) {
                continue;
            }
            catch(FailingHttpStatusCodeException e2) {
                setError("Such article doesn't exist");
                continue;
            }
            List<DomElement> elements = IteratorUtils.toList(page.getHtmlElementById("mw-content-text").getChildElements().iterator());
            window.links.clear();
            window.data.setForeground(Color.BLACK);
            if (Pattern.compile(Pattern.quote(search.toLowerCase()) + ".* may refer to:").matcher(page.asText().toLowerCase()).find()) {
                window.links.put("Please choose one of the following options:", "");
                for(DomElement element : elements) {
                    if(element instanceof HtmlUnorderedList) {
                        for (int i = 0; i < page.getByXPath(element.getCanonicalXPath() + "/li").size(); i++)
                            window.links.put(element.asText().split("\n")[i], getAnchor((DomElement) page.getByXPath(element.getCanonicalXPath() + "/li").get(i)).getTextContent());
                    }
                    else if(element instanceof HtmlHeading2 && (element.getChildElements().iterator().next().getTextContent().matches("(See also|Other)"))) {
                        window.list.setListData(window.links.keySet().toArray(new String[window.links.keySet().size()]));
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

    private HtmlAnchor getAnchor(DomElement li) {
        for(DomElement element : li.getChildElements()) {
            if(element instanceof HtmlAnchor)
                return (HtmlAnchor) element;
            else if(getAnchor(element) != null)
                return getAnchor(element);
        }
        return null;
    }

    private void setError(String error) {
        WikiShort.window.data.setForeground(Color.RED);
        WikiShort.window.data.setText(String.format("Error: %s", error));
    }
}