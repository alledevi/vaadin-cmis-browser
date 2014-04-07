package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.PropertyMatcher;
import com.github.atave.VaadinCmisBrowser.cmis.api.PropertyType;
import com.github.atave.VaadinCmisBrowser.cmis.api.QueryOperator;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.MimeTypes;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Search view. The second tab of application
 */

public class SearchView extends VerticalLayout implements View {

    private static final long serialVersionUID = 1L;
    private static final String layoutWidth = "100%";
    private static final String Text1Width = "50%";
    private static final String formWidth = "70%";
    private static final String Text2Width = "90%";
    private static final String logoSize = "45px";
    private static final String searchImage = "img/search36.png";
    private static final String logoImage = "img/alfresco.png";
    private static final String backImage = "img/back.png";

    // AlfrescoClient
    private final AlfrescoClient client;

    // SearchLayout
    private final HorizontalLayout topSearchLayout;
    private final VerticalLayout middleSearchLayout;
    private final FormLayout bottomSearchLayout;
    private final Collection<Field> inputFields = new ArrayList<>();
    private final TextField keyWords;
    private final TextField name;
    private final TextField author;
    private final TextArea text;
    private final DateField creationDate;
    private final DateField modDate;
    private final TextField mod;
    private final ComboBox mimeType;
    private final Button searchButton;

    // ResultLayout
    private final HorizontalLayout topResultLayout;
    private final HorizontalLayout middleResultLayout;
    private final VerticalLayout bottomResultLayout;
    private final Label numberResults;
    private final TableComponent table;
    private final Button returnButton;


    public SearchView() {

        setSizeFull();
        setMargin(true);
        addStyleName("search-view");
//        addStyleName("sidebar");
        
        Button homeButton = ((AppUI) UI.getCurrent()).getButtonHome();
        ((AppUI) UI.getCurrent()).getButtonSearch().addStyleName("selected");
        homeButton.removeStyleName("selected");

        client = ((AppUI) UI.getCurrent()).getClient();

        // TOPSEARCHLAYOUT: image logoSearch + label advancedSearch
        topSearchLayout = new HorizontalLayout();
        topSearchLayout.addStyleName("toolbar");
        topSearchLayout.setWidth(layoutWidth);
        topSearchLayout.setSpacing(true);
        addComponent(topSearchLayout);

        // Image logoSearch
        Image logoSearch = new Image(null, new ThemeResource(logoImage));
        logoSearch.setHeight(logoSize);
        logoSearch.setWidth(logoSize);
        topSearchLayout.addComponent(logoSearch);
        topSearchLayout.setComponentAlignment(logoSearch, Alignment.MIDDLE_LEFT);

        // Label advancedSearch
        Label advancedSearch = new Label("Advanced Search");
        advancedSearch.addStyleName("h1");
        advancedSearch.setSizeUndefined();
        topSearchLayout.addComponent(advancedSearch);
        topSearchLayout.setComponentAlignment(advancedSearch, Alignment.MIDDLE_LEFT);
        topSearchLayout.setExpandRatio(advancedSearch, 1);

        // MIDDLESEARCHLAYOUT: FormLayout tagLayout + FormLayout advancedLayout + FormLayout bottomSearchLayout
        middleSearchLayout = new VerticalLayout();
        middleSearchLayout.setSpacing(true);
        middleSearchLayout.setWidth(formWidth);
        middleSearchLayout.setMargin(true);
        middleSearchLayout.addShortcutListener(enter);
        addComponent(middleSearchLayout);
        setExpandRatio(middleSearchLayout, 4);

        Label l = new Label("You can perform searches by entering one or more pieces of information about a document.");
        l.setSizeUndefined();
        middleSearchLayout.addComponent(l);

        // FormLayout tagLayout: TextField keyWords
        FormLayout tagLayout = new FormLayout();
        tagLayout.addStyleName("f");
        tagLayout.setSpacing(true);
        middleSearchLayout.addComponent(tagLayout);

        // TextField KeyWords
        keyWords = new TextField("Keywords: ");
        tagLayout.addComponent(keyWords);
        keyWords.setWidth(Text2Width);
        keyWords.setImmediate(true);
        keyWords.setDescription("Enter one or more tag of the document to search");
        keyWords.addTextChangeListener(textListener);

		/* FormLayout advancedLayout: TextField name, author, mod
                                        TextArea text
							  		  DateField creationDate, modDate
		 */
        FormLayout advancedLayout = new FormLayout();
        advancedLayout.addStyleName("f1");
        advancedLayout.setSpacing(true);
        middleSearchLayout.addComponent(advancedLayout);
        middleSearchLayout.setExpandRatio(advancedLayout, 4);

        // TextField name
        name = new TextField("Name: ");
        advancedLayout.addComponent(name);
        name.setWidth(Text1Width);
        name.setImmediate(true);
        name.setDescription("Enter the Name of the document to search");
        name.addTextChangeListener(textListener);
        inputFields.add(name);

        // TextField author
        author = new TextField("Author: ");
        advancedLayout.addComponent(author);
        author.setWidth(Text1Width);
        author.setImmediate(true);
        author.setDescription("Enter the Author of the document to search");
        author.addTextChangeListener(textListener);
        inputFields.add(author);

        // TextField mod
        mod = new TextField("Modifier: ");
        advancedLayout.addComponent(mod);
        mod.setWidth(Text1Width);
        mod.setImmediate(true);
        mod.setDescription("Enter the Modifier of the document to search");
        mod.addTextChangeListener(textListener);
        inputFields.add(mod);

        // TextArea text
        text = new TextArea("Content: ");
        text.setWidth(Text2Width);
        text.setImmediate(true);
        text.setDescription("Enter a word contained in the document to search");
        advancedLayout.addComponent(text);
        text.addTextChangeListener(textListener);
        inputFields.add(text);

        // DateField creationDate
        creationDate = new DateField("Created Before: ");
        advancedLayout.addComponent(creationDate);
        creationDate.setImmediate(true);
        creationDate.setDescription("Enter the creation date of the document to search");
        creationDate.addValueChangeListener(dateMimeListener);
        inputFields.add(creationDate);

        // DateField modDate
        modDate = new DateField("Modified Before: ");
        advancedLayout.addComponent(modDate);
        modDate.setImmediate(true);
        modDate.setDescription("Enter the modified date of the document to search");
        modDate.addValueChangeListener(dateMimeListener);
        inputFields.add(modDate);
        
        // TextField mimeType

        mimeType = new ComboBox("MimeType: ");
        for (String mimeItem: MimeTypes.getExtensions())
        	mimeType.addItem(mimeItem);        	
        advancedLayout.addComponent(mimeType);
        mimeType.setWidth(Text1Width);
        mimeType.setImmediate(true);
        mimeType.setDescription("Enter the MimeType of the document to search");
        mimeType.addValueChangeListener(dateMimeListener);
        inputFields.add(mimeType);

        // BOTTOMSEARCHLAYOUT: Button searchButton
        bottomSearchLayout = new FormLayout();
        bottomSearchLayout.addStyleName("f2");
        bottomSearchLayout.setSizeUndefined();
        middleSearchLayout.addComponent(bottomSearchLayout);

        // Button searchButton
        searchButton = new Button("Search");
        searchButton.addStyleName("default1");
        searchButton.setIcon(new ThemeResource(searchImage));
        searchButton.setEnabled(false);
        bottomSearchLayout.addComponent(searchButton);
        bottomSearchLayout.setComponentAlignment(searchButton, Alignment.TOP_RIGHT);
        searchButton.addClickListener(searchListener);

        // TOPRESULTLAYOUT: Image logoResult + Label resultSearch
        topResultLayout = new HorizontalLayout();
        topResultLayout.addStyleName("toolbar");
        topResultLayout.setWidth(layoutWidth);
        topResultLayout.setSpacing(true);
        topResultLayout.setVisible(false);
        addComponent(topResultLayout);

        // Image logoResult
        Image logoResult = new Image(null, new ThemeResource(logoImage));
        logoResult.setHeight(logoSize);
        logoResult.setWidth(logoSize);
        topResultLayout.addComponent(logoResult);
        topResultLayout.setComponentAlignment(logoResult, Alignment.MIDDLE_LEFT);

        // Label resultSearch
        Label resultSearch = new Label("Advanced Search Results");
        resultSearch.addStyleName("h1");
        resultSearch.setSizeUndefined();
        topResultLayout.addComponent(resultSearch);
        topResultLayout.setComponentAlignment(resultSearch, Alignment.MIDDLE_LEFT);
        topResultLayout.setExpandRatio(resultSearch, 1);

        // MIDDLERESULTLAYOUT: Label numberResults + Button returnButton
        middleResultLayout = new HorizontalLayout();
        middleResultLayout.setSpacing(true);
        middleResultLayout.setMargin(true);
        middleResultLayout.setWidth(layoutWidth);
        middleResultLayout.setVisible(false);
        addComponent(middleResultLayout);
        setExpandRatio(middleResultLayout, 1);

        // Label numberResults
        numberResults = new Label();
        numberResults.addStyleName("h2");
        middleResultLayout.addComponent(numberResults);
        middleResultLayout.setComponentAlignment(numberResults, Alignment.MIDDLE_LEFT);

        // Button returnButton
        returnButton = new Button("Return to advanced search");
        returnButton.addStyleName("link");
        returnButton.setIcon(new ThemeResource(backImage));
        middleResultLayout.addComponent(returnButton);
        middleResultLayout.setComponentAlignment(returnButton, Alignment.MIDDLE_RIGHT);
        returnButton.addClickListener(searchListener);

        // BOTTOMRESULTLAYOUT: TableComponent Table
        bottomResultLayout = new VerticalLayout();
        bottomResultLayout.setSpacing(true);
        bottomResultLayout.setMargin(true);
        bottomResultLayout.setVisible(false);
        addComponent(bottomResultLayout);
        setExpandRatio(bottomResultLayout, 5);

        // TableComponent Table
        table = new TableComponent(client, null);
        table.pageLength(9);
        bottomResultLayout.addComponent(table);
        bottomResultLayout.setComponentAlignment(table, Alignment.TOP_CENTER);
    }

    /**
     * Listener for texfield and textarea. Enable/Disable searchButton
     */
    private final TextChangeListener textListener = new TextChangeListener() {

        private static final long serialVersionUID = 1L;

        public void textChange(TextChangeEvent event) {
            updateSearchButtonState(event.getComponent(), event.getText());
        }
    };

    /**
     * Listener for datefield. Enable/Disable searchButton
     */
    private final ValueChangeListener dateMimeListener = new ValueChangeListener() {

        private static final long serialVersionUID = 1L;

        public void valueChange(ValueChangeEvent event) {
            updateSearchButtonState(null, event.getProperty().getValue());
        }
    };

    /**
     * Listener for searchButton and returnButton.
     */
    private final Button.ClickListener searchListener = new Button.ClickListener() {

        private static final long serialVersionUID = 1L;

        public void buttonClick(ClickEvent event) {

            if (event.getButton().equals(searchButton)) {
                Collection<PropertyMatcher> matchers = new ArrayList<>();
                ItemIterable<DocumentView> results;
                String nameDocument = name.getValue();
                String textDocument = text.getValue();

                if (name.getValue().equals(""))
                    nameDocument = null;

                if (text.getValue().equals(""))
                    textDocument = null;

                if (!author.getValue().equals(""))
                    matchers.add(new PropertyMatcher(PropertyIds.CREATED_BY, QueryOperator.EQUALS, PropertyType.STRING, author.getValue()));

                if (!mod.getValue().equals(""))
                    matchers.add(new PropertyMatcher(PropertyIds.LAST_MODIFIED_BY, QueryOperator.EQUALS, PropertyType.STRING, mod.getValue()));

                if (creationDate.getValue() != null)
                    matchers.add(new PropertyMatcher(PropertyIds.CREATION_DATE, QueryOperator.LESS_THAN_OR_EQUALS, PropertyType.DATETIME, creationDate.getValue()));

                if (modDate.getValue() != null)
                    matchers.add(new PropertyMatcher(PropertyIds.LAST_MODIFICATION_DATE, QueryOperator.LESS_THAN_OR_EQUALS, PropertyType.DATETIME, modDate.getValue()));

                if (mimeType.getValue() != null)
                    matchers.add(new PropertyMatcher(PropertyIds.CONTENT_STREAM_MIME_TYPE, QueryOperator.EQUALS, PropertyType.STRING, MimeTypes.getMimeType(mimeType.getValue().toString())));
                
                if (!keyWords.getValue().isEmpty()) {
                    String toParse = keyWords.getValue().trim();
                    Collection<String> tags = new ArrayList<>();

                    StringBuilder sb = new StringBuilder();

                    final char space = ' ';
                    final char single_quote = '\'';
                    final char double_quote = '"';
                    final char escape = '\\';

                    char separator = space;
                    final String separators = "" + single_quote + space + double_quote;

                    for (int i = 0; i < toParse.length(); ++i) {
                        char ch = toParse.charAt(i);
                        if (ch != escape) {
                            if (separators.contains(toParse.subSequence(i, i+1))) {
                                // it's a possible separator
                                if (sb.length() != 0) {
                                    // we are inside a tag
                                    if (ch == separator && toParse.charAt(i-1) != escape) {
                                        // the current tag is closed
                                        tags.add(sb.toString());
                                        sb.setLength(0);
                                        separator = space;
                                    } else {
                                        // it's part of the current tag
                                        sb.append(ch);
                                    }
                                } else if (i == 0 || toParse.charAt(i-1) != escape) {
                                    // change the separator
                                    separator = ch;
                                } else {
                                    // escaped separator is part of a new tag
                                    sb.append(ch);
                                }
                            } else {
                                // it's part of the current tag
                                sb.append(ch);
                            }
                        }
                    }

                    if (sb.length() > 0) {
                        tags.add(sb.toString());
                    }

                    matchers.add(new AlfrescoClient.TagMatcher(tags));
                }

                table.clearTable();
                topSearchLayout.setVisible(false);
                middleSearchLayout.setVisible(false);
                bottomSearchLayout.setVisible(false);
                topResultLayout.setVisible(true);
                middleResultLayout.setVisible(true);
                bottomResultLayout.setVisible(true);

                // start the search
                results = client.search(nameDocument, textDocument, matchers);

                // Add all document to table
                int num = 0;
                for (DocumentView document : results) {
                    table.addItemToTableComponent(document);
                    // num: number of results
                    num++;
                }

                if (num == 1)
                    numberResults.setValue(num + " search result");
                else
                    numberResults.setValue(num + " search results");

            } else if (event.getButton().equals(returnButton)) {
                topResultLayout.setVisible(false);
                middleResultLayout.setVisible(false);
                bottomResultLayout.setVisible(false);
                topSearchLayout.setVisible(true);
                middleSearchLayout.setVisible(true);
                bottomSearchLayout.setVisible(true);
            }
        }
    };

    /**
     * ShortcutListener enter for middleSearchLayout
     */
    private final ShortcutListener enter = new ShortcutListener("Search", KeyCode.ENTER, null) {

        private static final long serialVersionUID = 1L;

        public void handleAction(Object sender, Object target) {

            // searchButton must be Enable
            if (searchButton.isEnabled()) {
                searchButton.click();
            }
        }
    };

    @Override
    public void enter(ViewChangeEvent event) {

    }

    private boolean isValid(Object value) {
        if (value instanceof String) {
            return !((String) value).isEmpty();
        } else {
            return value != null;
        }
    }

    private void updateSearchButtonState(Component component, Object value) {
        if (!isValid(value)) {
            for (Field field : inputFields) {
                if (field != component && isValid(field.getValue())) {
                    searchButton.setEnabled(true);
                    return;
                }
            }

            searchButton.setEnabled(false);
        } else {
            searchButton.setEnabled(true);
        }
    }
}
