package com.github.atave.VaadinCmisBrowser.vaadin.utils;

import com.github.atave.VaadinCmisBrowser.cmis.api.Thumbnailer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Image;
import org.apache.http.client.HttpResponseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Thumbnail extends Image implements StreamResource.StreamSource {

    private Thumbnailer thumbnailer;
    private String objectId;
    private StreamResource resource;

    /**
     * Creates a new {@code Thumbnail} with a caption.
     *
     * @param caption     a caption
     * @param thumbnailer an object providing thumbnailing services
     * @param objectId    a {@code cmis:objectId}
     */
    public Thumbnail(String caption, Thumbnailer thumbnailer, String objectId) {
        super(caption);
        setUp(thumbnailer, objectId);
    }

    /**
     * Creates a new {@code Thumbnail}.
     */
    public Thumbnail(Thumbnailer thumbnailer, String objectId) {
        setUp(thumbnailer, objectId);
    }

    private String getTimestampedFilename() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return objectId + df.format(new Date()) + ".png";
    }

    private void setUp(Thumbnailer thumbnailer, String objectId) {
        this.thumbnailer = thumbnailer;
        this.objectId = objectId;

        resource = new StreamResource(this, getTimestampedFilename());
        resource.setCacheTime(0);
        this.setSource(resource);
    }

    /**
     * Returns new input stream that is used for reading the resource.
     */
    @Override
    public InputStream getStream() {
        try {
            return thumbnailer.getThumbnail(objectId);
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                resource.setFilename(getTimestampedFilename());
                this.markAsDirty();
                return new ByteArrayInputStream(new byte[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
