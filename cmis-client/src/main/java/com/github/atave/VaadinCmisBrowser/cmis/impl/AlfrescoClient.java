package com.github.atave.VaadinCmisBrowser.cmis.impl;

import com.github.atave.VaadinCmisBrowser.cmis.api.*;
import com.github.atave.VaadinCmisBrowser.utils.Config;
import com.github.atave.VaadinCmisBrowser.utils.RestClient;
import com.github.atave.junderscore.Lambda1;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static com.github.atave.junderscore.JUnderscore._;


/**
 * A {@link com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient} implementation for Alfresco.
 */
public class AlfrescoClient extends HttpAuthCmisClient implements Tagger {

    private static final String SCHEME_PROPERTY = "alfresco.scheme";
    private static final String HOSTNAME_PROPERTY = "alfresco.hostname";
    private static final String PORT_PROPERTY = "alfresco.port";
    private static final String BINDING_PROPERTY = "alfresco.binding";

    private static final String HOSTNAME = Config.get(HOSTNAME_PROPERTY);
    private static final int PORT = Integer.parseInt(Config.get(PORT_PROPERTY));
    private static final String SCHEME = Config.get(SCHEME_PROPERTY);
    private static final String BINDING = Config.get(BINDING_PROPERTY);

    private static final String BINDING_URL = SCHEME + "://" + HOSTNAME + ":" + PORT + BINDING;

    private static final String TAG_LIST_URL = "/alfresco/service/api/tags/workspace/SpacesStore";
    private static final String TAG_DETAIL_URL = "/alfresco/service/api/tags/%s";
    private static final String TAGS_FOR_NODE_URL = "/alfresco/service/api/node/workspace/SpacesStore/%s/tags";
    private static final String NODES_FOR_TAG_URL = "/alfresco/service/api/tags/workspace/SpacesStore/%s/nodes";

    private static final String NODE_REF_KEY = "nodeRef";

    public static final String TAGGABLE_ASPECT_ID = "cm:taggable";

    protected static String normalizeNodeRef(String nodeRef) {
        return nodeRef.replace("workspace://SpacesStore/", "").replaceFirst(";[^;]+$", "");
    }

    private final RestClient restClient;

    /**
     * Creates a new {@code AlfrescoClient}.
     *
     * @param username   the client's username
     * @param password   the client's password
     * @param bindingUrl the client's URL binding
     */
    public AlfrescoClient(String username, String password, String bindingUrl) {
        super(username, password, bindingUrl);
        restClient = new RestClient(HOSTNAME, PORT, SCHEME, username, password);
    }

    /**
     * Creates a new {@code AlfrescoClient} with the default URL binding.
     *
     * @param username the client's username
     * @param password the client's password
     */
    public AlfrescoClient(String username, String password) {
        this(username, password, BINDING_URL);
    }

    @Override
    protected SessionFactory getSessionFactory() {
        return SessionFactoryImpl.newInstance();
    }

    @Override
    public String createTag(String tag) throws IOException {
        JSONObject data = new JSONObject();
        data.put("name", tag.toLowerCase());

        return restClient.post(
                TAG_LIST_URL,
                data,
                new RestClient.EntityHandler<String, JSONObject>() {
                    @Override
                    public String handle(JSONObject parsedJSON) {
                        Boolean itemExists = (Boolean) parsedJSON.get("itemExists");
                        if (itemExists) {
                            return null;
                        } else {
                            return normalizeNodeRef((String) parsedJSON.get(NODE_REF_KEY));
                        }
                    }
                }
        );
    }

    @Override
    public boolean deleteTag(String tag) throws IOException {
        tag = tag.toLowerCase();

        return restClient.delete(
                String.format(TAG_DETAIL_URL, tag),
                new RestClient.EntityHandler<Boolean, JSONObject>() {
                    @Override
                    public Boolean handle(JSONObject parsedJSON) {
                        return (Boolean) parsedJSON.get("result");
                    }
                }
        );
    }

    @Override
    public boolean editTag(String tag, String newTag) throws IOException {
        tag = tag.toLowerCase();

        JSONObject data = new JSONObject();
        data.put("name", newTag.toLowerCase());

        return restClient.put(
                String.format(TAG_DETAIL_URL, tag),
                data,
                new RestClient.EntityHandler<Boolean, JSONObject>() {
                    @Override
                    public Boolean handle(JSONObject parsedJSON) {
                        return (Boolean) parsedJSON.get("result");
                    }
                }
        );
    }

    @Override
    public Map<String, String> getAllTags() throws IOException {
        return restClient.get(
                TAG_LIST_URL + "?details=true",
                new RestClient.EntityHandler<Map<String, String>, JSONObject>() {
                    @Override
                    public Map<String, String> handle(JSONObject parsedJSON) {
                        Map<String, String> map = new HashMap<>();

                        JSONObject data = (JSONObject) parsedJSON.get("data");
                        JSONArray items = (JSONArray) data.get("items");

                        for (Object object : items) {
                            JSONObject item = (JSONObject) object;
                            map.put(
                                    (String) item.get("name"),
                                    (String) item.get(NODE_REF_KEY)
                            );
                        }

                        return map;
                    }
                }
        );
    }

    @Override
    public Collection<String> getTags(String objectId) throws IOException {
        objectId = normalizeNodeRef(objectId);

        return restClient.get(
                String.format(TAGS_FOR_NODE_URL, objectId),
                new RestClient.EntityHandler<Collection<String>, JSONArray>() {
                    @Override
                    public Collection<String> handle(JSONArray parsedJSON) {
                        Collection<String> retval = new ArrayList<>();

                        for (Object object : parsedJSON) {
                            retval.add((String) object);
                        }

                        return retval;
                    }
                }
        );
    }

    @Override
    public void setTags(String objectId, Collection<String> tags) throws IOException {
        objectId = normalizeNodeRef(objectId);

        Map<String, Object> properties = new HashMap<>();
        List<String> tagIds = new ArrayList<>();

        Map<String, String> tagMap = getAllTags();
        for (String tag : tags) {
            tagIds.add(tagMap.get(tag.toLowerCase()));
        }

        properties.put(TAGGABLE_ASPECT_ID, tagIds);
        getObject(objectId).updateProperties(properties);
    }

    @Override
    public Collection<String> addTags(String objectId, Collection<String> tags) throws IOException {
        objectId = normalizeNodeRef(objectId);

        JSONArray data = new JSONArray();
        data.addAll(_(tags).map(new Lambda1<Object, String>() {
            @Override
            public Object call(String o) {
                return o.toLowerCase();
            }
        }));

        return restClient.post(
                String.format(TAGS_FOR_NODE_URL, objectId),
                data,
                new RestClient.EntityHandler<Collection<String>, JSONArray>() {
                    @Override
                    public Collection<String> handle(JSONArray parsedJSON) {
                        Collection<String> retval = new ArrayList<>();

                        for (Object object : parsedJSON) {
                            retval.add((String) object);
                        }

                        return retval;
                    }
                }
        );
    }

    @Override
    public void removeTags(String objectId, Collection<String> tags) throws IOException {
        objectId = normalizeNodeRef(objectId);

        Collection<String> currentTags = getTags(objectId);
        currentTags.removeAll(_(tags).map(new Lambda1<Object, String>() {
            @Override
            public Object call(String o) {
                return o.toLowerCase();
            }
        }));

        setTags(objectId, currentTags);
    }

    @Override
    public Collection<String> getObjectIds(String tag) throws IOException {
        tag = tag.toLowerCase();

        return restClient.get(
                String.format(NODES_FOR_TAG_URL, tag),
                new RestClient.EntityHandler<Collection<String>, JSONArray>() {
                    @Override
                    public Collection<String> handle(JSONArray parsedJSON) {
                        Collection<String> retval = new ArrayList<>();

                        for (Object object : parsedJSON) {
                            JSONObject jsonObject = (JSONObject) object;
                            String nodeRef = normalizeNodeRef((String) jsonObject.get(NODE_REF_KEY));
                            retval.add(nodeRef);
                        }

                        return retval;
                    }
                }
        );
    }

    @Override
    public ItemIterable<DocumentView> search(String name, String text, Collection<PropertyMatcher> matchers) {
        Set<String> ids = null;
        Collection<PropertyMatcher> filtered = new ArrayList<>();

        for (PropertyMatcher matcher : matchers) {
            if (matcher.getProperty().equals(TAGGABLE_ASPECT_ID)) {
                for (Object tag : matcher.getValues()) {
                    try {
                        Set<String> currentIds = new HashSet<>(getObjectIds((String) tag));

                        if (ids == null) {
                            ids = currentIds;
                        } else {
                            ids.retainAll(currentIds);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                filtered.add(matcher);
            }
        }

        if (ids != null) {
            PropertyMatcher matcher = new PropertyMatcher(
                    PropertyIds.OBJECT_ID,
                    QueryOperator.IN,
                    PropertyType.STRING_SET,
                    ids.toArray()
            );
            filtered.add(matcher);
        }

        return super.search(name, text, filtered, false);
    }

    @Override
    protected SessionParametersFactory getSessionParametersFactory() {
        final Map<String, String> params = super.getSessionParametersFactory().newInstance();

        return new SessionParametersFactory() {
            @Override
            public Map<String, String> newInstance() {
                Map<String, String> sessionParameters = new HashMap<>(params);
                sessionParameters.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
                return sessionParameters;
            }
        };
    }

    /**
     * A {@link PropertyMatcher} for the {@code cm:taggable} Aspect in Alfresco.
     */
    public static class TagMatcher extends PropertyMatcher {

        /**
         * Creates a new {@code TagMatcher} for matching documents.
         *
         * @param values the tags to match
         */
        public TagMatcher(Object... values) {
            super(TAGGABLE_ASPECT_ID, null, null, values);
        }

        /**
         * Creates a new {@code TagMatcher}.
         *
         * @param objectType the type of the object to match
         * @param values     the tags to match
         */
        public TagMatcher(String objectType, Object... values) {
            super(objectType, TAGGABLE_ASPECT_ID, null, null, values);
        }

        /**
         * Creates a new {@code TagMatcher} for matching documents.
         *
         * @param values the tags to match
         */
        public TagMatcher(Collection<String> values) {
            this(values.toArray());
        }

        /**
         * Creates a new {@code TagMatcher}.
         *
         * @param objectType the type of the object to match
         * @param values     the tags to match
         */
        public TagMatcher(String objectType, Collection<String> values) {
            this(objectType, values.toArray());
        }
    }
}
