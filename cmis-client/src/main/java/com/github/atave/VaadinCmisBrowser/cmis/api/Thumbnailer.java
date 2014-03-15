package com.github.atave.VaadinCmisBrowser.cmis.api;

import java.io.IOException;
import java.io.InputStream;

public interface Thumbnailer {

    /**
     * Retrieves a thumbnail for an object.
     *
     * @param objectId the {@code cmis:objectId} of the object
     * @return an {@code InputStream} to read the thumbnail from
     */
    InputStream getThumbnail(String objectId) throws IOException;

}
