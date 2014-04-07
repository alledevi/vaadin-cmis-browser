package com.github.atave.VaadinCmisBrowser.utils;

import org.apache.chemistry.opencmis.commons.impl.json.JSONAware;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParseException;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple REST client.
 */
public class RestClient {

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";

    private static final Map<String, Class<?>> methodsMap = new HashMap<>();

    static {
        methodsMap.put(GET, HttpGet.class);
        methodsMap.put(POST, HttpPost.class);
        methodsMap.put(PUT, HttpPut.class);
        methodsMap.put(DELETE, HttpDelete.class);
    }

    private final HttpHost target;
    private final AuthScope authScope;
    private final CredentialsProvider credentialsProvider;
    private final AuthCache authCache;

    /**
     * Creates a new {@code RestClient} with no set credentials.
     *
     * @param hostname the hostname (IP or DNS name)
     * @param port     the port number.
     *                 <code>-1</code> indicates the scheme default port.
     * @param scheme   the name of the scheme.
     *                 <code>null</code> indicates the
     *                 {@link HttpHost#DEFAULT_SCHEME_NAME default scheme}
     */
    public RestClient(String hostname, int port, String scheme) {
        target = new HttpHost(hostname, port, scheme);
        authScope = new AuthScope(target.getHostName(), target.getPort());
        credentialsProvider = new BasicCredentialsProvider();

        authCache = new BasicAuthCache();
        AuthScheme authScheme = new BasicScheme();
        authCache.put(target, authScheme);
    }

    /**
     * Creates a new {@code RestClient} with the provided credentials.
     *
     * @param hostname the hostname (IP or DNS name)
     * @param port     the port number.
     *                 <code>-1</code> indicates the scheme default port.
     * @param scheme   the name of the scheme.
     *                 <code>null</code> indicates the
     *                 {@link HttpHost#DEFAULT_SCHEME_NAME default scheme}
     */
    public RestClient(String hostname, int port, String scheme, String username, String password) {
        this(hostname, port, scheme);

        setCredentials(new UsernamePasswordCredentials(username, password));
    }

    /**
     * Sets the credentials for the default {@link AuthScope}.
     *
     * @param credentials the authentication credentials
     */
    public void setCredentials(Credentials credentials) {
        credentialsProvider.setCredentials(authScope, credentials);
    }

    /**
     * Factory method for {@code CloseableHttpClient}s.
     */
    protected CloseableHttpClient buildHttpClient() {
        return HttpClientBuilder.create().build();
    }

    /**
     * Factory method for {@code HttpUriRequest}s.
     */
    protected HttpUriRequest buildRequest(String method, String url, JSONAware data) {
        Class<?> cl = methodsMap.get(method);

        if (cl == null) {
            throw new RuntimeException("Method " + method + " cannot be handled");
        }

        HttpUriRequest request = null;

        try {
            request = (HttpUriRequest) cl.getConstructor(String.class).newInstance(url);
            request.setHeader("Accept", "application/json");

            if (request instanceof HttpEntityEnclosingRequest && data != null) {
                request.setHeader("Content-type", "application/json");
                StringEntity entity = new StringEntity(data.toJSONString());
                ((HttpEntityEnclosingRequest) request).setEntity(entity);
            }

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException
                | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return request;
    }

    /**
     * Factory method for {@code HttpClientContext}s.
     */
    protected HttpClientContext buildContext() {
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credentialsProvider);
        context.setAuthCache(authCache);
        return context;
    }

    protected <T> T process(String method, String url, JSONAware data, final EntityHandler<T> entityHandler) throws IOException {
        ResponseHandler<T> responseHandler = new ResponseHandler<T>() {
            @Override
            public T handleResponse(HttpResponse response) throws IOException {
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode >= 300) {
                    throw new HttpResponseException(statusCode, statusLine.getReasonPhrase());
                }

                InputStream content = response.getEntity().getContent();

                if (entityHandler != null) {
                    try {
                        return entityHandler.handle(content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }
        };

        HttpClientContext context = buildContext();
        CloseableHttpClient httpClient = buildHttpClient();
        HttpUriRequest request = buildRequest(method, url, data);

        return httpClient.execute(target, request, responseHandler, context);
    }

    public <T> T get(String url, final EntityHandler<T> entityHandler) throws IOException {
        return process(GET, url, null, entityHandler);
    }

    public <T> T post(String url, JSONAware data, final EntityHandler<T> entityHandler) throws IOException {
        return process(POST, url, data, entityHandler);
    }

    public <T> T put(String url, JSONAware data, final EntityHandler<T> entityHandler) throws IOException {
        return process(PUT, url, data, entityHandler);
    }

    public <T> T delete(String url, final EntityHandler<T> entityHandler) throws IOException {
        return process(DELETE, url, null, entityHandler);
    }

    public static interface EntityHandler<T> {
        T handle(InputStream inputStream) throws Exception;
    }

    public abstract static class JSONHandler<T, J> implements EntityHandler<T> {

        protected abstract T handleJSON(J parsedJSON);

        @Override
        public T handle(InputStream inputStream) throws IOException, JSONParseException {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                JSONParser parser = new JSONParser();
                return handleJSON((J) parser.parse(reader));
            }
        }
    }
}
