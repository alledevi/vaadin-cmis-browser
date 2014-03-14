package com.github.atave.VaadinCmisBrowser.cmis.api;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


public interface TaggingService {

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
     */
    void deleteTag(String tag) throws IOException;

    /**
     * Renames a tag.
     *
     * @param tag    the old name
     * @param newTag the new name
     */
    void editTag(String tag, String newTag) throws IOException;

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
    Set<String> getTags(String objectId) throws IOException;

    /**
     * Replaces the set of tags of an object.
     *
     * @param objectId the {@code cmis:objectId}
     * @param tags     the set of tags to set
     */
    void setTags(String objectId, Set<String> tags);

    /**
     * Adds tags to an object.
     *
     * @param objectId the {@code cmis:objectId}
     * @param tags     the tags to add
     * @return the tags of the specified object
     */
    Set<String> addTags(String objectId, Set<String> tags) throws IOException;

    /**
     * Removes a tag from an object.
     *
     * @param objectId the {@code cmis:objectId}
     * @param tags     the tags to remove
     */
    void removeTags(String objectId, Set<String> tags) throws IOException;

    /**
     * Searches for objects with the specified tag.
     *
     * @param tag the tag to match
     * @return a {@code Set} of {@code cmis:objectId}s
     */
    Set<String> getObjectIds(String tag) throws IOException;
}
