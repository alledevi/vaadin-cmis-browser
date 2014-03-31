package com.github.atave.VaadinCmisBrowser.cmis.api;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import java.util.ArrayList;
import java.util.Collection;


/**
 * A simple read-only view of a {@link org.apache.chemistry.opencmis.client.api.Folder}.
 */
public class FolderView extends FileView {

    public FolderView(Folder delegate) {
        super(delegate);
    }

    private Folder getDelegate() {
        return (Folder) delegate;
    }

    /**
     * Returns if the folder is the root folder.
     */
    public boolean isRootFolder() {
        return getDelegate().isRootFolder();
    }

    /**
     * Returns the children of this folder.
     */
    public Collection<FileView> getChildren() {
        Collection<FileView> children = new ArrayList<>();

        for (CmisObject object : getDelegate().getChildren()) {
            if (object instanceof Document) {
                children.add(new DocumentView((Document) object));
            } else if (object instanceof Folder) {
                children.add(new FolderView((Folder) object));
            }
        }

        return children;
    }

    /**
     * Returns the documents whose first parent is this folder.
     */
    public Collection<DocumentView> getDocuments() {
        Collection<DocumentView> documents = new ArrayList<>();

        for (FileView file : getChildren()) {
            if (file.isDocument()) {
                documents.add(file.asDocument());
            }
        }

        return documents;
    }

    /**
     * Returns the direct subfolders.
     */
    public Collection<FolderView> getFolders() {
        Collection<FolderView> documents = new ArrayList<>();

        for (FileView file : getChildren()) {
            if (file.isFolder()) {
                documents.add(file.asFolder());
            }
        }

        return documents;
    }

    /**
     * Gets the parent folder object
     *
     * @return the parent folder object
     * @throws CmisObjectNotFoundException if this folder is already the root folder.
     */
    public FolderView getParent() throws CmisObjectNotFoundException {
        Folder parent = getDelegate().getFolderParent();
        if (parent != null) {
            return new FolderView(parent);
        } else {
            throw new CmisObjectNotFoundException("Folder #" + getId() + " is already the root folder");
        }
    }

    /**
     * Returns the path of the folder.
     */
    @Override
    public String getPath() {
        return getDelegate().getPath();
    }

}
