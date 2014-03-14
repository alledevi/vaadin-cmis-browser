package com.github.atave.VaadinCmisBrowser.cmis.impl;


import com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient;
import com.github.atave.VaadinCmisBrowser.cmis.api.RepositoryView;
import com.github.atave.VaadinCmisBrowser.cmis.api.SessionParametersFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient} that authenticates
 * with a username and password.
 */
public abstract class HttpAuthCmisClient extends CmisClient {

    private final String user;
    private final String password;
    private final String bindingUrl;

    /**
     * Constructs an {@code HttpAuthCmisClient} with the provided credentials.
     */
    protected HttpAuthCmisClient(String username, String password, String bindingUrl) {
        this.user = username;
        this.password = password;
        this.bindingUrl = bindingUrl;

        // Smart autoconnect
        Collection<RepositoryView> repositories = getRepositories();
        if (repositories.size() == 1) {
            connect(repositories.iterator().next().getId());
        }
    }

    /**
     * Returns the client's user.
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the client's password.
     */
    protected String getPassword() {
        return password;
    }

    /**
     * Returns the AtomPub binding URL for this client.
     */
    protected String getBindingUrl() {
        return bindingUrl;
    }

    @Override
    protected SessionParametersFactory getSessionParametersFactory() {
        return new HttpAuthSessionParametersFactory();
    }

    /**
     * A {@link com.github.atave.VaadinCmisBrowser.cmis.api.SessionParametersFactory} for Basic HTTP authentication.
     */
    private class HttpAuthSessionParametersFactory extends SessionParametersFactory {
        @Override
        public Map<String, String> newInstance() {
            Map<String, String> parameters = new HashMap<>();

            // Credentials
            parameters.put(SessionParameter.USER, user);
            parameters.put(SessionParameter.PASSWORD, password);

            // Connection settings
            BindingType binding;
            String url;

            if (getBindingUrl().contains("atom")) {
                binding = BindingType.ATOMPUB;
                url = SessionParameter.ATOMPUB_URL;
            } else {
                binding = BindingType.BROWSER;
                url = SessionParameter.BROWSER_URL;
            }

            parameters.put(SessionParameter.BINDING_TYPE, binding.value());
            parameters.put(url, getBindingUrl());

            return parameters;
        }
    }
}
