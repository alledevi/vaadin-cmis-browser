package com.github.atave.VaadinCmisBrowser.cmis.impl;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.PropertyMatcher;
import com.github.atave.VaadinCmisBrowser.cmis.api.PropertyType;
import com.github.atave.VaadinCmisBrowser.cmis.api.QueryOperator;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AlfrescoTests {

    private static AlfrescoClient client;

    private static InputStream asInputStream(String string) {
        try {
            return new ByteArrayInputStream(string.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DocumentView createVersionedDocument(String fileName, String[] contents) {
        DocumentView document = null;

        for (String content : contents) {
            document = client.upload("/", fileName, null, asInputStream(content),
                    BigInteger.valueOf(content.length()), VersioningState.MAJOR,
                    null, null);
        }

        return document;
    }

    private static final String fileName = "___ALFRESCO_DOC.txt";
    private static String filePath = "/" + fileName;
    private static final String[] contents = {"1", "2", "3"};
    private static final String[] tags = {"tag1", "tag2"};
    private static final String createdTag = "derp";

    @BeforeClass
    public static void setUp() {
        client = new AlfrescoClient("admin", "admin");

        if (!client.exists(filePath)) {
            createVersionedDocument(fileName, contents);
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        if (client.exists(filePath)) {
            client.deleteDocument(filePath);
        }

        for (String tag : tags) {
            Assert.assertTrue(client.deleteTag(tag));
        }
    }

    protected static void checkTags(Collection<String> actual) {
        checkTags(tags, actual);
    }

    protected static void checkTags(String[] tags, Collection<String> actual) {
        for (String tag : tags) {
            Assert.assertTrue(actual.contains(tag));
        }
    }

    @Test
    public void addTag() throws IOException {
        String objectId = client.getDocument(filePath).getId();

        Collection<String> returnedTags = client.addTags(objectId, Arrays.asList(tags));
        checkTags(returnedTags);
    }

    @Test
    public void createTag() throws IOException, InterruptedException {
        String objectId = client.createTag(createdTag);
        Assert.assertNotNull(objectId);
        Assert.assertFalse(objectId.isEmpty());

        objectId = client.createTag(tags[0]);
        Assert.assertNull(objectId);
    }

    @Test
    public void deleteTag() throws IOException, InterruptedException {
        boolean result = client.deleteTag(createdTag);
        Assert.assertTrue(result);

        result = client.deleteTag("fail_because_this_tag_is_missing");
        Assert.assertFalse(result);
    }

    @Test
    public void editTag() throws IOException, InterruptedException {
        String newTag = "opaopa";
        String oldTag = tags[0];

        boolean result = client.editTag(oldTag, newTag);
        Assert.assertTrue(result);

        result = client.editTag(newTag, oldTag);
        Assert.assertTrue(result);

        result = client.editTag(newTag, oldTag);
        Assert.assertFalse(result);
    }

    @Test
    public void excludeUnfiledObjectsFromSearchResults() {
        PropertyMatcher matcher = new PropertyMatcher(PropertyIds.CREATION_DATE, QueryOperator.LESS_THAN_OR_EQUALS,
                PropertyType.DATETIME, new Date());
        ItemIterable<DocumentView> results = client.search(null, null, Collections.singleton(matcher));

        int unfiled = 0;
        for (DocumentView document : results) {
            if (document.getPath() == null) {
                ++unfiled;
            } else {
                Assert.assertNotEquals(document.getName(), "woman3.png");
            }
        }

        Assert.assertTrue(unfiled > 0);
    }

    @Test
    public void getAllTags() throws IOException {
        Map<String, String> allTags = client.getAllTags();
        checkTags(allTags.keySet());
    }

    @Test
    public void getObjectIds() throws IOException {
        String objectId = client.getDocument(filePath).getId();
        objectId = AlfrescoClient.normalizeNodeRef(objectId);

        for (String tag : tags) {
            Collection<String> objectIds = client.getObjectIds(tag);
            Assert.assertArrayEquals(Collections.singleton(objectId).toArray(), objectIds.toArray());
        }
    }

    @Test
    public void getTags() throws IOException {
        String objectId = client.getDocument(filePath).getId();
        checkTags(client.getTags(objectId));
    }

    @Test
    public void setTags() throws IOException {
        String[] newTags = {"tag5", "tag6"};

        for (String tag : newTags) {
            client.createTag(tag);
        }

        String objectId = client.getDocument(filePath).getId();

        client.setTags(objectId, Arrays.asList(newTags));
        checkTags(newTags, client.getTags(objectId));

        client.setTags(objectId, Arrays.asList(tags));
        checkTags(client.getTags(objectId));

        for (String tag : newTags) {
            client.deleteTag(tag);
        }
    }

    private Collection<PropertyMatcher> getMatchers(String... tags) {
        Collection<PropertyMatcher> matchers = new ArrayList<>();

        Date now = new Date();

        matchers.add(new PropertyMatcher(
                PropertyIds.CREATION_DATE,
                QueryOperator.GREATER_THAN,
                PropertyType.DATETIME,
                new Date(System.currentTimeMillis() - 1000 * 3600 * 24)
        ));

        matchers.add(new PropertyMatcher(
                PropertyIds.CREATION_DATE,
                QueryOperator.LESS_THAN_OR_EQUALS,
                PropertyType.DATETIME,
                now
        ));

        matchers.add(new PropertyMatcher(
                PropertyIds.LAST_MODIFICATION_DATE,
                QueryOperator.LESS_THAN_OR_EQUALS,
                PropertyType.DATETIME,
                now
        ));

        matchers.add(new PropertyMatcher(
                PropertyIds.CREATED_BY,
                QueryOperator.EQUALS,
                PropertyType.STRING,
                client.getUser()
        ));

        matchers.add(new PropertyMatcher(
                PropertyIds.LAST_MODIFIED_BY,
                QueryOperator.EQUALS,
                PropertyType.STRING,
                client.getUser()
        ));

        matchers.add(new AlfrescoClient.TagMatcher(tags));

        return matchers;
    }

    @Test
    public void tagSearch() throws IOException {
        // Make some new documents
        String[] files = {"__File1", "__File2", "__File3"};

        String[][] contents = {
                {"a", "b", "c"},
                {"d", "e", "f"},
                {"g", "h", "i"}
        };

        String[][] newTags = {
                {"tagA", "tagB", "tagC"},
                {"tagB", "tagD", "tagE"},
                {"tagE", "tagF", "tagB"}
        };

        for (int i = 0; i < files.length; ++i) {
            createVersionedDocument(files[i], contents[i]);
            String objectId = client.getDocument("/" + files[i]).getId();
            client.addTags(objectId, Arrays.asList(newTags[i]));
        }

        // Search these documents
        ItemIterable<DocumentView> results = client.search(null, null, getMatchers("tagB"));
        Assert.assertEquals(3, results.getTotalNumItems());

        results = client.search(null, null, getMatchers("tagE"));
        Assert.assertEquals(2, results.getTotalNumItems());

        results = client.search(null, null, getMatchers("tagA", "tagB"));
        Assert.assertEquals(3, results.getTotalNumItems());

        results = client.search(files[1], null, null);
        Assert.assertEquals(1, results.getTotalNumItems());
        Assert.assertEquals(files[1], results.iterator().next().getName());

        results = client.search(null, null, getMatchers("nonexistenttag"));
        Assert.assertEquals(0, results.getTotalNumItems());

        // Cleanup
        for (int i = 0; i < files.length; ++i) {
            for (String tag : newTags[i]) {
                client.deleteTag(tag);
            }

            client.deleteDocument("/" + files[i]);
        }
    }
}
