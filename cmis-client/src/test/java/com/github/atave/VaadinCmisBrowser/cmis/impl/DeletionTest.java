package com.github.atave.VaadinCmisBrowser.cmis.impl;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.junderscore.Lambda1;
import org.junit.Test;

import static com.github.atave.junderscore.JUnderscore._;
import static org.junit.Assert.*;

public class DeletionTest extends CmisClientTestBase {

    private final String[] contents = {"useless content", "more useless content"};

    @Test
    public void deleteDocumentAllVersions() {
        String fileName = "DELETE_ME.txt";
        DocumentView versionedDocument = createVersionedDocument(fileName, contents);
        final String documentPath = versionedDocument.getPath();
        client.deleteDocument(documentPath);

        boolean found = _(client.getCurrentFolder().getDocuments()).some(new Lambda1<Boolean, DocumentView>() {
            @Override
            public Boolean call(DocumentView o) {
                return o.getPath().equals(documentPath);
            }
        });

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

        boolean found = _(client.getCurrentFolder().getDocuments()).some(new Lambda1<Boolean, DocumentView>() {
            @Override
            public Boolean call(DocumentView o) {
                return o.getPath().equals(documentPath);
            }
        });

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

        boolean found = _(client.getCurrentFolder().getFolders()).some(new Lambda1<Boolean, FolderView>() {
            @Override
            public Boolean call(FolderView o) {
                return o.getPath().equals(folderPath);
            }
        });

        assertFalse(found);
    }

}
