package com.github.atave.VaadinCmisBrowser.cmis.impl;

import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CreationTest extends CmisClientTestBase {

    @Test
    public void createFolder() {
        final String name = "CreatedFolder";
        for (FolderView folder : client.getCurrentFolder().getFolders()) {
            FolderView createdFolder = client.createFolder(folder.getPath(), name);

            testCreationProperties(createdFolder);

            boolean exists = false;

            for (FolderView folderView : folder.getFolders()) {
                if (folderView.getName().equals(name)) {
                    exists = true;
                    break;
                }
            }

            assertTrue(exists);
        }
    }

}
