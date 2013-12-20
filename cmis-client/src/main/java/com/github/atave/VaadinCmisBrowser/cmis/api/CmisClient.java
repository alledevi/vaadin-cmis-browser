package com.github.atave.VaadinCmisBrowser.cmis.api;

import com.github.atave.junderscore._map;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.util.FileUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;


/**
 * A simple CMIS client.
 */
public abstract class CmisClient {

    // CmisClient state
    private Session currentSession;
    private Folder currentFolder;
    private String versionableCmisType;

    // Helpers

    /**
     * Returns the {@link org.apache.chemistry.opencmis.client.api.SessionFactory} to use.
     */
    protected abstract SessionFactory getSessionFactory();

    /**
     * Returns the {@link SessionParametersFactory} used to configure new {@code Session}s.
     */
    protected abstract SessionParametersFactory getSessionParametersFactory();

    /**
     * Returns the versionable cmis type for the current session, since cmis:document
     * is actually not versionable.
     */
    private String getVersionableCmisType() {
        return getVersionableCmisType(currentSession.getTypeDescendants(null, -1, true));
    }

    private String getVersionableCmisType(List<Tree<ObjectType>> trees) {
        for (Tree<ObjectType> tree : trees) {
            ObjectType objectType = tree.getItem();
            if (objectType instanceof DocumentType) {
                DocumentType documentType = (DocumentType) objectType;
                if (documentType.getContentStreamAllowed() == ContentStreamAllowed.ALLOWED
                        && documentType.isVersionable()) {
                    return documentType.getId();
                }
            }

            String versionableCmisType = getVersionableCmisType(tree.getChildren());
            if (versionableCmisType != null) {
                return versionableCmisType;
            }
        }

        return null;
    }

    /**
     * Gets an object by path or object id.
     *
     * @param pathOrIdOfObject the path or object id
     * @return the object
     */
    private CmisObject getObject(String pathOrIdOfObject) {
        return FileUtils.getObject(pathOrIdOfObject, currentSession);
    }

    /**
     * Determines an object existence.
     */
    private boolean exists(String pathOrIdOfObject) {
        try {
            getObject(pathOrIdOfObject);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Gets a folder by path or object id.
     *
     * @param pathOrIdOfObject the path or folder id
     * @return the folder object
     */
    private Folder getBareFolder(String pathOrIdOfObject) {
        return FileUtils.getFolder(pathOrIdOfObject, currentSession);
    }

    /**
     * Gets a document by path or object id.
     *
     * @param pathOrIdOfObject the path or document id
     * @return the document object
     */
    private Document getBareDocument(String pathOrIdOfObject) {
        Document document;

        try {
            document = (Document) getObject(pathOrIdOfObject);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Object is not a document!");
        }

        return document;
    }

    /**
     * Returns a PWC of the specified document.
     */
    private Document checkout(String pathOrIdOfObject) {
        return getBareDocument(getBareDocument(pathOrIdOfObject).checkOut().getId());
    }

    // API

    /**
     * Returns the available repositories.
     */
    public Collection<RepositoryView> getRepositories() {
        return new _map<RepositoryView, Repository>() {
            @Override
            protected RepositoryView process(Repository object) {
                return new RepositoryView(object);
            }
        }.on(getSessionFactory().getRepositories(getSessionParametersFactory().newInstance()));
    }

    /**
     * Connects the client to a repository.
     *
     * @param repositoryId see {@link RepositoryView#getId()}
     * @throws CmisBaseException if the connection could not be established
     */
    public void connect(String repositoryId) throws CmisBaseException {
        currentSession = getSessionFactory().createSession(getSessionParametersFactory().newInstance(repositoryId));
        currentFolder = currentSession.getRootFolder();
        versionableCmisType = getVersionableCmisType();
    }

    /**
     * Returns the path to the current directory.
     */
    public String getCurrentPath() {
        return currentFolder.getPath();
    }

    /**
     * Changes the current working directory.
     *
     * @param path the path to navigateTo to.
     */
    public void navigateTo(String path) {
        if (path.equals("..") && !currentFolder.isRootFolder()) {
            currentFolder = currentFolder.getFolderParent();
        } else if (!path.equals(".")) {
            currentFolder = getBareFolder(path);
        }
    }

    /**
     * Returns the files in the current directory.
     */
    public Collection<FileView> getChildren() {
        return new FolderView(currentFolder).getChildren();
    }

    /**
     * Returns the documents in the current directory.
     */
    public Collection<DocumentView> getDocuments() {
        return new FolderView(currentFolder).getDocuments();
    }

    /**
     * Returns the folders in the current directory.
     */
    public Collection<FolderView> getFolders() {
        return new FolderView(currentFolder).getFolders();
    }

    /**
     * Returns if {@code path} is a folder.
     */
    public boolean isFolder(String path) {
        return getObject(path) instanceof Folder;
    }

    /**
     * Returns if {@code path} is a document.
     */
    public boolean isDocument(String path) {
        return getObject(path) instanceof Document;
    }

    /**
     * Returns the file at the specified {@code path}.
     */
    public FileView getFile(String path) {
        return new FileView((FileableCmisObject) getObject(path));
    }

    /**
     * Returns the file at the specified {@code path}.
     */
    public DocumentView getDocument(String path) {
        return new DocumentView(getBareDocument(path));
    }

    /**
     * Returns the file at the specified {@code path}.
     */
    public FolderView getFolder(String path) {
        return new FolderView(getBareFolder(path));
    }

    /**
     * Creates a subfolder of the specified parent.
     *
     * @param parent the parent folder
     * @param name   the subfolder name
     * @return the created folder
     */
    public FolderView createFolder(String parent, String name) {
        Folder folder = FileUtils.createFolder(parent, name, null, currentSession);
        return new FolderView(folder);
    }

    /**
     * Creates a subfolder of the current folder.
     *
     * @param name the subfolder name
     * @return the created folder
     */
    public FolderView createFolder(String name) {
        return createFolder(currentFolder.getPath(), name);
    }

    /**
     * Uploads a document.
     *
     * @param parentIdOrPath  the id or path of the parent folder
     * @param fileName        the source file
     * @param mimeType        the MIME Type of the source file
     * @param inputStream     the input stream
     * @param length          the source file length
     * @param versioningState the versioning state
     * @param metadata        additional properties for the uploaded files
     * @return the uploaded document
     */
    public DocumentView upload(String parentIdOrPath, String fileName, String mimeType,
                               InputStream inputStream, BigInteger length, VersioningState versioningState,
                               String checkInComment, Map<? extends String, ?> metadata) {
        String cmisType = versionableCmisType;
        Folder parentFolder = getBareFolder(parentIdOrPath);

        if (mimeType == null) {
            mimeType = MimeTypes.getMIMEType(fileName);
        }

        // Setup basic properties
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, cmisType);
        properties.put(PropertyIds.NAME, fileName);

        // Setup cool properties
        if (metadata != null) {
            properties.putAll(metadata);
        }

        ContentStream contentStream = new ContentStreamImpl(fileName, length, mimeType, inputStream);

        Document document = null;

        String documentPath = parentFolder.getPath();
        if (!documentPath.endsWith("/")) {
            documentPath += "/";
        }
        documentPath += fileName;

        try {
            if (exists(documentPath)) {
                // Create new version of an existing document
                document = checkout(documentPath);
                document.checkIn(versioningState == VersioningState.MAJOR, null, contentStream, checkInComment);
                document = getBareDocument(documentPath);
            } else {
                // Create new document
                document = parentFolder.createDocument(properties, contentStream, versioningState);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new CmisRuntimeException("Cannot close source stream!", e);
                }
            }
        }

        return new DocumentView(document);
    }

    /**
     * Deletes all versions of a document.
     *
     * @param documentPath the absolute path of the document
     */
    public void deleteDocument(String documentPath) {
        getBareDocument(documentPath).deleteAllVersions();
    }

    /**
     * Deletes a single version of a document.
     *
     * @param documentPath the absolute path of the document
     * @param versionLabel the version of the document to delete
     * @see DocumentView#getVersionLabel()
     */
    public void deleteDocument(String documentPath, String versionLabel) {
        DocumentView documentView = new DocumentView(getBareDocument(documentPath)).getObjectOfVersion(versionLabel);
        getBareDocument(documentView.getId()).delete(false);
    }

    /**
     * Deletes recursively the specified folder.
     *
     * @param folderPath path of the folder to delete
     * @return the paths of folder and documents that could not be deleted
     */
    public Collection<String> deleteFolder(String folderPath) {
        List<String> failed = getBareFolder(folderPath).deleteTree(true, UnfileObject.DELETE, true);
        return new _map<String, String>() {
            @Override
            protected String process(String objectId) {
                if (isFolder(objectId)) {
                    return getBareFolder(objectId).getPath();
                } else {
                    // Ignore multiple paths
                    return getBareDocument(objectId).getPaths().get(0);
                }
            }
        }.on(failed);
    }

    /**
     * Queries the current CMIS repository.
     *
     * @param name     a string that has to be contained in the name of the document
     * @param text     a string that has to be contained in the text of the document
     * @param metadata a set of additional properties the document must match
     * @return the result of the query as an {@link org.apache.chemistry.opencmis.client.api.ItemIterable}
     */
    public CmisQueryResult search(String name, String text, Map<String, String> metadata) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ")
                .append(PropertyIds.OBJECT_ID)
                .append(" FROM ")
                .append(BaseTypeId.CMIS_DOCUMENT.value());

        List<String> whereClauses = new ArrayList<String>();

        if (name != null && !name.equals("")) {
            whereClauses.add(PropertyIds.NAME + " LIKE " + escape(name));
        }

        if (text != null && !text.equals("")) {
            whereClauses.add("CONTAINS(" + escape(text) + ")");
        }

        for (String key : metadata.keySet()) {
            String val = metadata.get(key);
            whereClauses.add(key + " " + val);
        }

        if (!whereClauses.isEmpty()) {
            queryBuilder.append(" WHERE");

            for (int i = 0; i < whereClauses.size(); ++i) {
                if (i != 0) {
                    queryBuilder.append(" AND ");
                }
                queryBuilder.append(whereClauses.get(i));
            }
        }

        String query = queryBuilder.toString();
        return new CmisQueryResult(this, currentSession.query(query, true));
    }

    static String escape(String input) {
        input = input.replaceAll("'", "\\'");
        return "'" + input + "'";
    }

}
