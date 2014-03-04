package com.github.atave.VaadinCmisBrowser.cmis.impl;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UploadTest extends CmisClientTestBase {

    private final String fileName = "UploadedFile.txt";
    private final String content = "ehi";

    @Test
    public void uploadNewFile() {
        DocumentView uploadedDocument = client.upload("/", fileName, null,
                asInputStream(content), BigInteger.valueOf(content.length()),
                VersioningState.MAJOR, null, null);

        assertEquals(fileName, uploadedDocument.getName());
        testCreationProperties(uploadedDocument);
        checkContents(uploadedDocument, content);
    }

    @Test
    public void uploadNewVersionOfAFile() {
        String firstVersion = client.getDocument("/" + fileName).getVersionLabel();

        String checkInComment = "new minor version";
        VersioningState versioningState = VersioningState.MINOR;
        String content2 = "ehi";
        DocumentView uploadedDocument = client.upload("/", fileName, null, asInputStream(content2),
                BigInteger.valueOf(content2.length()), versioningState,
                checkInComment, null);

        testCreationProperties(uploadedDocument);
        testModificationProperties(uploadedDocument);
        checkContents(uploadedDocument, content2);

        assertEquals(versioningState == VersioningState.MAJOR,
                uploadedDocument.isMajorVersion());

        assertTrue(uploadedDocument.isLatestVersion());

        assertEquals(uploadedDocument.getCheckinComment(), checkInComment);

        // Check that all versions are available
        assertEquals(uploadedDocument.getAllVersions().size(), 2);

        // Check again the first version
        uploadedDocument = uploadedDocument.getObjectOfVersion(firstVersion);
        testCreationProperties(uploadedDocument);
        checkContents(uploadedDocument, content);
    }

}
