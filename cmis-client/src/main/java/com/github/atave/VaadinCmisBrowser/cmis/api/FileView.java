package com.github.atave.VaadinCmisBrowser.cmis.api;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.enums.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * A simple read-only view of a {@link org.apache.chemistry.opencmis.client.api.FileableCmisObject}.
 */
public class FileView {

    protected final FileableCmisObject delegate;

    public FileView(FileableCmisObject delegate) {
        this.delegate = delegate;
    }

    /**
     * Casts itself as a {@link DocumentView}.
     */
    public DocumentView asDocument() {
        return new DocumentView((Document) delegate);
    }

    /**
     * Casts itself as a {@link FolderView}.
     */
    public FolderView asFolder() {
        return new FolderView((Folder) delegate);
    }

    /**
     * @return whether or not this object supports the specified {@code action}.
     */
    public boolean can(Action action) {
        return delegate.getAllowableActions().getAllowableActions().contains(action);
    }

    /**
     * @return the object id.
     */
    public String getId() {
        return delegate.getId();
    }

    /**
     * @return the name of this CMIS object (CMIS property
     * <code>cmis:name</code>).
     */
    public String getName() {
        return delegate.getName();
    }

    /**
     * @return the description of this CMIS object (CMIS property
     * <code>cmis:description</code>).
     */
    public String getDescription() {
        return delegate.getDescription();
    }

    /**
     * @return the user who created this CMIS object (CMIS property
     * <code>cmis:createdBy</code>).
     */
    public String getCreatedBy() {
        return delegate.getCreatedBy();
    }

    /**
     * @return the timestamp when this CMIS object has been created (CMIS
     * property <code>cmis:creationDate</code>).
     */
    public GregorianCalendar getCreationDate() {
        return delegate.getCreationDate();
    }

    /**
     * @return the user who modified this CMIS object (CMIS property
     * <code>cmis:lastModifiedBy</code>).
     */
    public String getLastModifiedBy() {
        return delegate.getLastModifiedBy();
    }

    /**
     * @return the timestamp when this CMIS object has been modified (CMIS
     * property <code>cmis:lastModificationDate</code>).
     */
    public GregorianCalendar getLastModificationDate() {
        return delegate.getLastModificationDate();
    }

    /**
     * @return the list of parent folders of this object or an empty list if
     * this object is unfiled or if this object is the root folder
     */
    public Collection<FolderView> getParents() {
        Collection<FolderView> parents = new ArrayList<>();

        for (Folder folder : delegate.getParents()) {
            parents.add(new FolderView(folder));
        }

        return parents;
    }

    /**
     * @return the path of this object or {@code null} if this object is unfiled or if this object is the root folder.
     */
    public String getPath() {
        List<String> paths = delegate.getPaths();
        if (paths.isEmpty()) {
            return null;
        } else {
            return paths.get(0);
        }
    }

    /**
     * @return if the file is a document.
     */
    public boolean isDocument() {
        return delegate instanceof Document;
    }

    /**
     * @return if the file is a folder.
     */
    public boolean isFolder() {
        return delegate instanceof Folder;
    }

    /**
     * Returns the value of the requested property. If the property is not
     * available, <code>null</code> is returned.
     *
     * @param propertyId the id of the property
     */
    public <T> T getProperty(String propertyId) {
        return delegate.getPropertyValue(propertyId);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileView)) return false;

        FileView fileView = (FileView) o;

        return getId().equals(fileView.getId());
    }
}
