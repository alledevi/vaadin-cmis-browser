package com.github.atave.VaadinCmisBrowser.cmis.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;


public interface Tagger {

    /**
     * Creates a new tag.
     *
     * @param tag the tag to create
     * @return the {@code cmis:objectId} of the new tag
     */
    String createTag(String tag) throws IOException;

    /**
     * Deletes a tag.
     *
     * @param tag the tag to delete
     * @return {@code true} if the operation had success, {@code false} otherwise
     */
    boolean deleteTag(String tag) throws IOException;

    /**
     * Renames a tag.
     *
     * @param tag    the old name
     * @param newTag the new name
     * @return {@code true} if the operation had success, {@code false} otherwise
     */
    boolean editTag(String tag, String newTag) throws IOException;

    /**
     * Retrieves all tags.
     *
     * @return a table of every tag and their {@code cmis:objectId}.
     */
    Map<String, String> getAllTags() throws IOException;

    /**
     * Retrieves all tags for an object.
     *
     * @param objectId the {@code cmis:objectId}
     * @return the tags of the specified object
     */
    Collection<String> getTags(String objectId) throws IOException;

    /**
     * Replaces the tags of an object.
     *
     * @param objectId the {@code cmis:objectId}
     * @param tags     the tags to set
     */
    void setTags(String objectId, Collection<String> tags) throws IOException;

    /**
     * Add tags to an object.
     *
     * @param objectId the {@code cmis:objectId}
     * @param tags     the tags to add
     * @return the tags of the specified object
     */
    Collection<String> addTags(String objectId, Collection<String> tags) throws IOException;

    /**
     * Removes a tag from an object.
     *
     * @param objectId the {@code cmis:objectId}
     * @param tags     the tags to remove
     */
    void removeTags(String objectId, Collection<String> tags) throws IOException;

    /**
     * Searches for objects with the specified tag.
     *
     * @param tag the tag to match
     * @return {@code cmis:objectId}s
     */
    Collection<String> getObjectIds(String tag) throws IOException;
}
