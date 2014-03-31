package com.github.atave.VaadinCmisBrowser.cmis.impl;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeletionTest extends CmisClientTestBase {

    private final String[] contents = {"useless content", "more useless content"};

    @Test
    public void deleteDocumentAllVersions() {
        String fileName = "DELETE_ME.txt";
        DocumentView versionedDocument = createVersionedDocument(fileName, contents);
        final String documentPath = versionedDocument.getPath();
        client.deleteDocument(documentPath);

        boolean found = false;

        for (DocumentView document : client.getCurrentFolder().getDocuments()) {
            if (document.getPath().equals(documentPath)) {
                found = true;
                break;
            }
        }

        assertFalse(found);
    }

    @Test
    public void deleteDocumentSingleVersion() {
        String fileName = "DELETE_ME_2.txt";
        DocumentView versionedDocument = createVersionedDocument(fileName, contents);
        final String documentPath = versionedDocument.getPath();

        if (!versionedDocument.isLatestVersion()) {
            versionedDocument = versionedDocument.getObjectOfLatestVersion(false);
        }

        String latestVersion = versionedDocument.getVersionLabel();
        client.deleteDocument(documentPath, latestVersion);

        // Check that a version of that document is missing
        versionedDocument = client.getDocument(documentPath);
        assertEquals(versionedDocument.getAllVersions().size(), contents.length - 1);
        assertNotEquals(versionedDocument.getVersionLabel(), latestVersion);

        boolean found = false;

        for (DocumentView document : client.getCurrentFolder().getDocuments()) {
            if (document.getPath().equals(documentPath)) {
                found = true;
                break;
            }
        }

        assertTrue(found);

        // Check that the current latest version is different from the previous one
        versionedDocument = client.getDocument(documentPath);
        if (!versionedDocument.isLatestVersion()) {
            versionedDocument = versionedDocument.getObjectOfLatestVersion(false);
        }

        assertNotEquals(latestVersion, versionedDocument.getVersionLabel());
    }

    @Test
    public void deleteFolder() {
        FolderView folder = client.getCurrentFolder().getFolders().iterator().next();
        final String folderPath = folder.getPath();
        client.deleteFolder(folderPath);

        boolean found = false;

        for (FolderView folderView : client.getCurrentFolder().getFolders()) {
            if (folderView.getPath().equals(folderPath)) {
                found = true;
                break;
            }
        }

        assertFalse(found);
    }

}
