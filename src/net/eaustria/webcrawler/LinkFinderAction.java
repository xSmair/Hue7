/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.eaustria.webcrawler;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @author bmayr
 */

// Recursive Action for forkJoinFramework from Java7

public class LinkFinderAction extends RecursiveAction {

    private String url;
    private ILinkHandler cr;
    /**
     * Used for statistics
     */
    private static final long t0 = System.currentTimeMillis();

    public LinkFinderAction(String url, ILinkHandler cr) {
        this.url = url;
        this.cr = cr;
    }

    @Override
    public void compute() {
        if (cr.size() <= 100) {
            if (!cr.visited(url)) {
                List<LinkFinderAction> linkFinderActionList = new ArrayList<>();
                Parser parser;
                NodeFilter filter = new NodeClassFilter(LinkTag.class);
                NodeList list;
                if (0 <= cr.size()) {
                    filter = new AndFilter(filter, new NodeFilter() {
                        @Override
                        public boolean accept(Node node) {
                            boolean isHTTPLink = ((LinkTag) node).isHTTPLink();
                            boolean isHTTPSLink = ((LinkTag) node).isHTTPSLink();
                            if (isHTTPLink || isHTTPSLink) {
                                return true;
                            }
                            return false;
                        }
                    });
                }
                try {
                    parser = new Parser(url);
                    list = parser.extractAllNodesThatMatch(filter);
                    for (int i = 0; i < list.size(); i++) {
                        Node currentNode = list.elementAt(i);
                        if (currentNode instanceof LinkTag) {
                            LinkTag linkTag = (LinkTag) currentNode;
                            linkFinderActionList.add(new LinkFinderAction(linkTag.getLink(), cr));
                        }
                    }
                    cr.addVisited(url);
                    invokeAll(linkFinderActionList);
                } catch (ParserException e) {
                    e.getCause();
                }
            }
        }
        System.out.println("Benchmark: " + (System.currentTimeMillis() - t0) + "ms");
    }
}

