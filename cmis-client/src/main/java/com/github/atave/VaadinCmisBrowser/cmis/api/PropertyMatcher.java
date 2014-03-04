package com.github.atave.VaadinCmisBrowser.cmis.api;


import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * A bundle of information used to build WHERE clauses in queries.
 *
 * @see com.github.atave.VaadinCmisBrowser.cmis.api.QueryBuilder
 */
public class PropertyMatcher {

    private final String objectType;
    private final String property;
    private final QueryOperator operator;
    private final PropertyType propertyType;
    private final Object[] values;

    /**
     * Creates a new {@code PropertyMatcher}.
     *
     * @param objectType   the type of the object to match
     * @param property     the name of the property
     * @param operator     an operator supported by the type of the property
     * @param propertyType the type of the property
     * @param values       the value(s) of the property
     */
    public PropertyMatcher(String objectType, String property, QueryOperator operator, PropertyType propertyType, Object... values) {
        this.property = property;
        this.operator = operator;
        this.propertyType = propertyType;
        this.values = values;
        this.objectType = objectType;
    }

    /**
     * Creates a new {@code PropertyMatcher} for matching documents.
     *
     * @param property     the name of the property
     * @param operator     an operator supported by the type of the property
     * @param propertyType the type of the property
     * @param values       the value(s) of the property
     */
    public PropertyMatcher(String property, QueryOperator operator, PropertyType propertyType, Object... values) {
        this(BaseTypeId.CMIS_DOCUMENT.value(), property, operator, propertyType, values);
    }

    /**
     * Returns the type of the property.
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Returns the the name of the property.
     */
    public String getProperty() {
        return property;
    }

    /**
     * Returns an operator supported by the type of the property.
     */
    public QueryOperator getOperator() {
        return operator;
    }

    /**
     * Returns the type of the property.
     */
    public PropertyType getPropertyType() {
        return propertyType;
    }

    /**
     * Returns the value(s) of the property.
     */
    public Object[] getValues() {
        return values;
    }

    /**
     * Translates this {@code PropertyMatcher} in a WHERE clause.
     *
     * @param session the session to use
     * @return a WHERE clause as a {@code String}
     */
    String getFragment(Session session) {
        return operator.getFragment(this, session);
    }
}
