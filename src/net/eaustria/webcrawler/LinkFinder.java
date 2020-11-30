/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.eaustria.webcrawler;

/**
 * @author bmayr
 */

import java.net.URL;

import org.htmlparser.Node;
import org.htmlparser.NodeFactory;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.util.ArrayList;
import java.util.List;

public class LinkFinder implements Runnable {

    private String url;
    private ILinkHandler linkHandler;
    /**
     * Used fot statistics
     */
    private static final long t0 = System.currentTimeMillis();

    public LinkFinder(String url, ILinkHandler handler) {
        this.url = url;
        this.linkHandler = handler;
    }

    @Override
    public void run() {
        getSimpleLinks(url);
    }

    private void getSimpleLinks(String url) {
        if (linkHandler.size() != 100 && !linkHandler.visited(url)) {
            Parser parser;
            NodeFilter nodeFilter;
            NodeList nodeList;
            nodeFilter = new NodeClassFilter(LinkTag.class);
            if (0 <= linkHandler.size()) {
                nodeFilter = new AndFilter(
                        nodeFilter,
                        new NodeFilter() {
                            public boolean accept(Node node) {
                                return (((LinkTag) node).isHTTPSLink());
                            }
                        });
            }

            try {
                parser = new Parser(url);
                nodeList = parser.extractAllNodesThatMatch(nodeFilter);
                for (int i = 0; i < nodeList.size(); i++) {
                    Node currentNode = nodeList.elementAt(i);
                    if (currentNode instanceof LinkTag) {
                        LinkTag linkTag = (LinkTag) currentNode;
                        if (!linkHandler.visited(linkTag.getLink()) && (!linkTag.getLink().equals(""))) {
                            //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Maybe Wrong
                        String link = linkTag.getLink();
                        linkHandler.queueLink(link);
                        }
                    }
                }
            } catch (ParserException e) {
                e.getCause();
            } catch (Exception e) {
                e.getCause();
            }
        }
        System.out.println("Benchmark: " + (System.currentTimeMillis() - t0) + "ms");
    }
}

