package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.vaadin.data.util.DefaultItemSorter;

import java.io.Serializable;
import java.util.Comparator;


public class CaseInsensitiveItemSorter extends DefaultItemSorter {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a CaseInsensitiveItemSorter that uses a case-insensitive sorter for string property values,
     * and the default otherwise.
     */
    public CaseInsensitiveItemSorter() {
        super(new CaseInsensitivePropertyValueComparator());
    }

    /**
     * Provides a case-insensitive comparator used for comparing string {@link Property} values.
     * The <code>CaseInsensitivePropertyValueComparator</code> assumes all objects it
     * compares can be cast to Comparable.
     */
    public static class CaseInsensitivePropertyValueComparator implements Comparator<Object>, Serializable {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            int r = 0;
            // Normal non-null comparison
            if (o1 != null && o2 != null) {
                if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                    if (((Integer) o1) <= ((Integer) o2))
                        return -1;
                    else
                        return 1;
                } else {
                    if ((o1 instanceof String) && (o2 instanceof String)) {
                        return ((String) o1).compareToIgnoreCase((String) o2);
                    } else {
                        // Assume the objects can be cast to Comparable, throw
                        // ClassCastException otherwise.
                        r = ((Comparable<Object>) o1).compareTo(o2);
                    }
                }
            } else if (o1 == o2) {
                // Objects are equal if both are null
                r = 0;
            } else {
                if (o1 == null) {
                    r = -1; // null is less than non-null
                } else {
                    r = 1; // non-null is greater than null
                }
            }
            return r;
        }
    }
}
