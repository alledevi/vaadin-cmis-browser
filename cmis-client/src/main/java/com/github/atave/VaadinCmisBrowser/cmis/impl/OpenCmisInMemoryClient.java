package com.github.atave.VaadinCmisBrowser.cmis.impl;

import com.github.atave.VaadinCmisBrowser.utils.Config;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;

public class OpenCmisInMemoryClient extends HttpAuthCmisClient {

    public static final String BINDING_PROPERTY = "inmemory.binding";
    public static final String HOSTNAME_PROPERTY = "inmemory.hostname";
    public static final String PORT_PROPERTY = "inmemory.port";

    public static final String BINDING_URL = "http://" + Config.get(HOSTNAME_PROPERTY)
            + ":" + Config.get(PORT_PROPERTY) + Config.get(BINDING_PROPERTY);

    /**
     * Creates a new {@code OpenCmisInMemoryClient}.
     *
     * @param user       the client's user
     * @param password   the client's password
     * @param bindingUrl the client's URL binding
     */
    public OpenCmisInMemoryClient(String user, String password, String bindingUrl) {
        super(user, password, bindingUrl);
    }

    /**
     * Creates a new {@code OpenCmisInMemoryClient} with the default URL binding.
     *
     * @param user     the client's user
     * @param password the client's password
     */
    public OpenCmisInMemoryClient(String user, String password) {
        this(user, password, BINDING_URL);
    }

    /**
     * Returns the {@link org.apache.chemistry.opencmis.client.api.SessionFactory} to use.
     */
    @Override
    protected SessionFactory getSessionFactory() {
        return SessionFactoryImpl.newInstance();
    }
}
