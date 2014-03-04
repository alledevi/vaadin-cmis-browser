package com.github.atave.VaadinCmisBrowser.cmis.api;


import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;

import java.util.Arrays;
import java.util.Date;

import static com.github.atave.VaadinCmisBrowser.cmis.api.QueryOperator.*;

/**
 * An enumeration of property types.
 */
public enum PropertyType {

    STRING(EQUALS, NOT_EQUALS, LIKE, NOT_LIKE, CONTAINS),
    STRING_SET(IN, NOT_IN, ANY_IN),

    NUMBER(EQUALS, NOT_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS),
    NUMBER_SET(IN, NOT_IN, ANY_IN),

    BOOLEAN(EQUALS),

    DATETIME(EQUALS, NOT_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS),
    DATETIME_SET(IN, NOT_IN, ANY_IN),

    ID(EQUALS, NOT_EQUALS),
    ID_SET(IN, NOT_IN, ANY_IN),

    URI(EQUALS, NOT_EQUALS, LIKE, NOT_LIKE),
    URI_SET(IN, NOT_IN, ANY_IN);

    private final QueryOperator[] supportedOperators;

    PropertyType(QueryOperator... supportedOperators) {
        this.supportedOperators = supportedOperators;
    }

    /**
     * Returns the operators supported by this property type.
     */
    public QueryOperator[] getSupportedOperators() {
        return supportedOperators;
    }

    /**
     * Returns whether the specified operator is supported.
     */
    public boolean supports(QueryOperator operator) {
        for (QueryOperator queryOperator : supportedOperators) {
            if (queryOperator == operator) {
                return true;
            }
        }
        return false;
    }

    /**
     * Casts an array.
     *
     * @param values the array to cast
     * @param cl     {@code T[].class}
     * @param <T>    the type to cast to
     * @return a casted array
     */
    private <T> T[] convertValues(Object[] values, Class<? extends T[]> cl) {
        return Arrays.copyOf(values, values.length, cl);
    }

    /**
     * Formats the values of a query fragment.
     */
    String format(String fragment, Object[] values, Session session) {
        QueryStatement stmt = session.createQueryStatement(fragment);

        switch (this) {
            case STRING:
            case URI:
            case ID:
            case STRING_SET:
            case URI_SET:
            case ID_SET:
                stmt.setString(1, convertValues(values, String[].class));
                break;

            case NUMBER:
            case NUMBER_SET:
                stmt.setNumber(1, convertValues(values, Number[].class));
                break;

            case BOOLEAN:
                stmt.setBoolean(1, (boolean[]) values[0]);
                break;

            case DATETIME:
            case DATETIME_SET:
                stmt.setDateTimeTimestamp(1, convertValues(values, Date[].class));
                break;
        }

        return stmt.toQueryString();
    }
}
