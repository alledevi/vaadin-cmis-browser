## Project Structure
This is a Maven project with two modules:
- **cmis-client**: implements a simple CMIS client that builds upon [Apache Chemistry OpenCMIS](http://chemistry.apache.org/java/opencmis.html) to provide a far less granular interface to a CMIS repository
- **vaadin-ui**: implements the web interface of the application with [vaadin](https://vaadin.com/)

## CmisClient API Tutorial

### Instantiation
```java
CmisClient client;
try {
    client = new OpenCmisInMemoryClient(user, password);
} catch(CmisBaseException e) {
    // Wrong username and password
}
```
This snippet instantiates a CmisClient implementation for the bundled OpenCMIS InMemory Server.  
Upon creation, the client automatically connects to the first available repository on the CMIS Server.  
Since both the bundled OpenCMIS InMemory Server and Alfresco Community 4.2 provide only a single repository, there's no need to manually call methods like `#getRepositories()` and `#connect(String)`.

### Navigation
```java
// Get the current folder
FolderView currentFolder = client.getCurrentFolder();

// Get the current path
String currentPath = currentFolder.getPath();

// List folders in the current directory
<...> = currentFolder.getFolders();

// Change the current directory
client.navigateTo("subdirectory");

// Back to the parent
client.navigateTo("..");

// A deep dive
client.navigateTo("/path/to/deep/folder");
```

### Folder creation and deletion
```java
FolderView createdFolder;

// Create folder in the current directory
createdFolder = client.createFolder("mysubdir");

// Create folder somewhere else
createdFolder = client.createFolder("/path/to/parent", "subfolderName");

// Time to delete stuff
client.deleteFolder(createdFolder);

// Also
client.deleteFolder("/path/to/some/other/folder");
```

### Document upload (vaadin example)
```java
DocumentUploader receiver = new DocumentUploader(client, "/path/to/parent") {
    @Override
    protected onCmisUploadReceived(DocumentView uploadedDocument) {
       // do something (if anything)
    }
};
    
Upload upload = new Upload("Upload Document", receiver);

// ...

// In an event listener of some sort, before the upload starts, set additional info
receiver.setFileName("readme.txt");
receiver.setCheckInComment("awesome stuff!");
receiver.setVersioningState(VersioningState.MAJOR);

receiver.setProperty(PropertyIds.DESCRIPTION, "a great document description");
receiver.setProperty("my:property1", "value1");
receiver.setProperty("my:property2", "value2");
```

### Document download (vaadin example)
```java
// Get the document
DocumentView document = client.getDocument("/path/to/document");

// Make sure it's the correct version
String requestedVersion = <...>;
document = document.getObjectOfVersion(requestedVersion);

// Create a stream resource
StreamResource.StreamSource source = new DocumentDownloader(document);
StreamResource resource = new StreamResource(source, document.getName());

// Let the user download the document
Link downloadLink = new Link("Download", resource);
```

### Document deletion
```java
// Delete all versions of a document
client.deleteDocument("/path/of/document/to/nuke");

// Also
client.deleteDocument(document);

// Delete a single version of a document
String versionToDelete = <...>;
client.deleteDocument("/path/to/document", versionToDelete);

// Also
client.deleteDocument(document, versionToDelete);
```

### Document search
```java
String name = "fileName.txt";
String text = "Lorem ipsum";

// Search all versions of all documents
ItemIterable<DocumentView> results = client.search(name, text);

for(DocumentView document : results) {
    // do something with the document
}

// Repeat the search, but ignore the name
results = client.search(null, text);

// This time, ignore the text
results = client.search(name, null);

// Let's specify additional properties the documents must match:
// 1. it must have been created by userA'
// 2. it must have been modified by 'userB'
// 3. it must have been modified before Christmas
Collection<PropertyMatcher> matchers = new ArrayList<>();
matchers.add(new PropertyMatcher("cmis:createdBy", QueryOperator.EQUALS, PropertyType.STRING, "userA"));
matchers.add(new PropertyMatcher("cmis:lastModifiedBy", QueryOperator.EQUALS, PropertyType.STRING, "userB"));
matchers.add(new PropertyMatcher("cmis:lastModificationDate", QueryOperator.LESS_THAN, PropertyType.DATETIME, new Date(2013, 12, 25)));

results = client.search(name, text, matchers);
```

### Document tagging (Alfresco example)
```java

AlfrescoClient client = new AlfrescoClient("admin", "admin");

// Create some tags
client.createTag("tag1");
client.createTag("tag2");
client.createTag("tag4");
client.createTag("ops");

// Delete a tag
client.deleteTag("ops");

// Rename a tag
client.editTag("tag4", "tag3");

// Get all tags in the repository
Map<String, String> allTags = client.getAllTags();

// Get all tags of a document
String documentPath = "/path/to/my/document";
String documentId = client.getDocument(documentPath).getId();
Collection<String> tags = client.getTags(documentId);

// Set tags of a document
String[] tags = {"tag2", "tag3"};
client.setTags(documentId, Arrays.asList(tags));

// Add tags to a document
client.addTags(documentId, Collections.singleton("tag1"));
client.addTags(documentId, Collections.singleton("tag0"));

// Remove tags from a document
client.removeTags(documentId, Collections.singleton("tag0");

// Search for documents with tags "tag2" and "tag3"
PropertyMatcher matcher = new AlfrescoClient.TagMatcher(tags);
ItemIterable<DocumentView> results = client.search(null, null, Collections.singleton(matcher));
```
