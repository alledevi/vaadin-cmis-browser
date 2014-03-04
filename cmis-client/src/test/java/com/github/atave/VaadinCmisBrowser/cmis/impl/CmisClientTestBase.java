package com.github.atave.VaadinCmisBrowser.cmis.impl;

import com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient;
import com.github.atave.VaadinCmisBrowser.cmis.api.Config;
import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public abstract class CmisClientTestBase {

    // Constants
    private static final String WAR = "../lib/chemistry-opencmis-server-inmemory-0.10.0.war";

    private static final String SERVER_PREFIX = "InMemoryServer";
    protected static final String FILLER_PREFIX = "RepositoryFiller";

    private static String join(String prefix, String suffix) {
        return prefix + "." + suffix;
    }

    // Server properties
    protected static final String REPO_ID = join(SERVER_PREFIX, "RepositoryId");
    protected static final String USER = join(SERVER_PREFIX, "User");
    protected static final String PASSWORD = join(SERVER_PREFIX, "Password");

    // Repository filler properties
    protected static final String DOCS_PER_FOLDER = join(FILLER_PREFIX, "DocsPerFolder");
    protected static final String FOLDER_PER_FOLDER = join(FILLER_PREFIX, "FolderPerFolder");
    protected static final String DEPTH = join(FILLER_PREFIX, "Depth");
    protected static final String CONTENT_SIZE_IN_KB = join(FILLER_PREFIX, "ContentSizeInKB");

    // Variables
    private static Server server;
    protected static CmisClient client;

    @BeforeClass
    public static void setUp() throws Exception {
        // Load the OpenCMIS InMemory Server webapp
        WebAppContext webApp = new WebAppContext();
        webApp.setWar(WAR);

        // Set the context path
        String[] splitAtomPubUrl = Config.get(OpenCmisInMemoryClient.ATOMPUB_URL).split("/");
        String contextPath = "/" + splitAtomPubUrl[splitAtomPubUrl.length - 2];
        webApp.setContextPath(contextPath);

        // Start the server
        server = new Server(0);
        server.setHandler(webApp);
        server.start();

        // Create the client
        int port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        String atomPubUrl = "http://localhost:" + port + contextPath + "/atom11";
        client = new OpenCmisInMemoryClient(Config.get(USER), Config.get(PASSWORD), atomPubUrl);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }

    // Helpers
    protected void testCreationProperties(FileView fileView) {
        assertEquals(fileView.getCreatedBy(), Config.get(USER));
        assertNotNull(fileView.getCreationDate());
    }

    protected void testModificationProperties(FileView fileView) {
        assertEquals(fileView.getLastModifiedBy(), Config.get(USER));
        assertNotNull(fileView.getLastModificationDate());
    }

    protected InputStream asInputStream(String string) {
        try {
            return new ByteArrayInputStream(string.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String asString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    protected void checkContents(DocumentView document, String content) {
        try (InputStream inputStream = document.download()) {
            String downloadedContent = asString(inputStream);
            assertEquals(downloadedContent, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected DocumentView createVersionedDocument(String fileName, String[] contents) {
        DocumentView document = null;

        for (String content : contents) {
            document = client.upload("/", fileName, null, asInputStream(content),
                    BigInteger.valueOf(content.length()), VersioningState.MAJOR,
                    null, null);
        }

        return document;
    }


}
