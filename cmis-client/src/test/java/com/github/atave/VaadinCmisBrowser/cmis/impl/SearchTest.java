package com.github.atave.VaadinCmisBrowser.cmis.impl;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.PropertyMatcher;
import com.github.atave.VaadinCmisBrowser.cmis.api.PropertyType;
import com.github.atave.VaadinCmisBrowser.cmis.api.QueryOperator;
import com.github.atave.VaadinCmisBrowser.utils.Config;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class SearchTest extends CmisClientTestBase {


    @Test
    public void searchGeneratedDocuments() {
        int docsPerFolder = Integer.parseInt(Config.get(DOCS_PER_FOLDER));
        int depth = Integer.parseInt(Config.get(DEPTH));
        int folderPerFolder = Integer.parseInt(Config.get(FOLDER_PER_FOLDER));
        int numFolders = (int) (Math.pow(folderPerFolder, depth) - 1);
        int generatedDocuments = docsPerFolder * numFolders;

        ItemIterable<DocumentView> results = client.search(null, "Lorem");

        assertEquals(results.getTotalNumItems(), generatedDocuments);
    }

    @Test
    public void searchVersionedDocuments() {
        String fileName = "FIND_ME.txt";
        String[] contents = {"ax", "by", "x'y"};
        createVersionedDocument(fileName, contents);

        ItemIterable<DocumentView> results = client.search(fileName, "a");
        assertEquals(results.getTotalNumItems(), 1);
        assertEquals(contents[0], asString(results.iterator().next().download()));

        results = client.search(fileName, "b");
        assertEquals(results.getTotalNumItems(), 1);
        assertEquals(contents[1], asString(results.iterator().next().download()));

        results = client.search(fileName, "x");
        assertEquals(results.getTotalNumItems(), 2);

        results = client.search(fileName, "y");
        assertEquals(results.getTotalNumItems(), 2);

        results = client.search(fileName, "'");
        assertEquals(results.getTotalNumItems(), 1);
        assertEquals(contents[2], asString(results.iterator().next().download()));
    }

    @Test
    public void searchWithMatchers() {
        String fileName = "FIND_ME_2.txt";
        String[] contents = {"opa", "ola", "opaopa"};
        createVersionedDocument(fileName, contents);

        Collection<PropertyMatcher> matchers = new ArrayList<>();
        String user = Config.get(USER);
        Date today = new Date();

        matchers.add(new PropertyMatcher(PropertyIds.CREATED_BY, QueryOperator.EQUALS, PropertyType.STRING, user));
        matchers.add(new PropertyMatcher(PropertyIds.CREATION_DATE, QueryOperator.LESS_THAN, PropertyType.DATETIME, today));
        matchers.add(new PropertyMatcher(PropertyIds.LAST_MODIFIED_BY, QueryOperator.EQUALS, PropertyType.STRING, user));
        matchers.add(new PropertyMatcher(PropertyIds.LAST_MODIFICATION_DATE, QueryOperator.LESS_THAN, PropertyType.DATETIME, today));

        // Only matchers
        ItemIterable<DocumentView> results = client.search(null, null, matchers);
        assertEquals(results.getTotalNumItems(), contents.length);

        // Matchers and fileName
        results = client.search(fileName, null, matchers);
        assertEquals(results.getTotalNumItems(), contents.length);

        // Matchers and contents
        results = client.search(null, contents[2], matchers);
        assertEquals(results.getTotalNumItems(), 1);
    }

}
