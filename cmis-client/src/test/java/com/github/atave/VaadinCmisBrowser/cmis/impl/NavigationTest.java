package com.github.atave.VaadinCmisBrowser.cmis.impl;

import com.github.atave.VaadinCmisBrowser.cmis.api.Config;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.VaadinCmisBrowser.cmis.api.RepositoryView;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class NavigationTest extends CmisClientTestBase {

    @Test
    public void getRepositories() {
        Collection<RepositoryView> repositories = client.getRepositories();

        // There is exactly one repository
        assertEquals(repositories.size(), 1);
    }

    @Test
    public void listCurrentFolder() {
        FolderView folder = client.getCurrentFolder();
        assertEquals(folder.getDocuments().size(), Integer.parseInt(Config.get(DOCS_PER_FOLDER)));
        assertEquals(folder.getFolders().size(), Integer.parseInt(Config.get(FOLDER_PER_FOLDER)));
    }

    @Test
    public void navigateTo() {
        FolderView currentFolder = client.getCurrentFolder();
        for (FolderView folder : currentFolder.getFolders()) {
            client.navigateTo(folder.getPath());
            assertEquals(folder.getPath(), client.getCurrentFolder().getPath());
            client.navigateTo("..");
            assertEquals(folder.getParent().getPath(), currentFolder.getPath());
        }
    }

}
