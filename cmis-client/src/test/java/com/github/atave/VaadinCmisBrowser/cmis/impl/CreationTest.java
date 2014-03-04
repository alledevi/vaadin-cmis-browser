package com.github.atave.VaadinCmisBrowser.cmis.impl;

import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.junderscore.Lambda1;
import org.junit.Test;

import static com.github.atave.junderscore.JUnderscore._;
import static org.junit.Assert.assertTrue;

public class CreationTest extends CmisClientTestBase {

    @Test
    public void createFolder() {
        final String name = "CreatedFolder";
        for (FolderView folder : client.getCurrentFolder().getFolders()) {
            FolderView createdFolder = client.createFolder(folder.getPath(), name);

            testCreationProperties(createdFolder);

            boolean exists = _(folder.getFolders()).some(new Lambda1<Boolean, FolderView>() {
                @Override
                public Boolean call(FolderView o) {
                    return o.getName().equals(name);
                }
            });
            assertTrue(exists);
        }
    }

}
