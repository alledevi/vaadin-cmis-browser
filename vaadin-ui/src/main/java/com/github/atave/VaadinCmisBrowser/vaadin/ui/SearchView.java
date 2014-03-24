package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.PropertyMatcher;
import com.github.atave.VaadinCmisBrowser.cmis.api.PropertyType;
import com.github.atave.VaadinCmisBrowser.cmis.api.QueryOperator;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Position;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


public class SearchView extends VerticalLayout implements View  {

	private static final long serialVersionUID = 1L;

	private AlfrescoClient client;
	private TableComponent table;

	private HorizontalLayout topSearchLayout;
	private VerticalLayout middleSearchLayout;
	private VerticalLayout middleResultLayout;
	private HorizontalLayout dateLayout;
	private HorizontalLayout topResultLayout;

	private Label advancedSearch;
	private Label resultSearch;
	private TextField keyWords;
	private TextField name;
	private TextArea text;
	private TextField author;
	private ComboBox mimeType;
	private DateField fromDate;
	private DateField toDate;
	private TextField mod;
	private Button searchButton;
	private Button returnButton;

	public SearchView() {

		setSizeFull();
		addStyleName("search-view");
		client = ((AppUI)UI.getCurrent()).getClient();

		topSearchLayout = new HorizontalLayout();
		topSearchLayout.setWidth("100%");
		topSearchLayout.setSpacing(true);
		topSearchLayout.addStyleName("toolbar");
		addComponent(topSearchLayout);

		Image logo = new Image(null, new ThemeResource("img/alfresco.png"));
		logo.setHeight("45px");
		logo.setWidth("45px");
		topSearchLayout.addComponent(logo);

		// advancedSearch : title
		advancedSearch = new Label("Ricerca Avanzata");
		advancedSearch.addStyleName("h1");
		advancedSearch.setSizeUndefined();
		topSearchLayout.addComponent(advancedSearch);
		topSearchLayout.setComponentAlignment(advancedSearch, Alignment.MIDDLE_LEFT);
		topSearchLayout.setExpandRatio(advancedSearch, 1);

		middleSearchLayout = new VerticalLayout();
		middleSearchLayout.setSpacing(true);
		middleSearchLayout.setMargin(true);
		middleSearchLayout.setWidth("70%");
		middleSearchLayout.setHeight("100%");
		addComponent(middleSearchLayout);
		setExpandRatio(middleSearchLayout, 4);

		// Tag
		
		FormLayout tagLayout = new FormLayout();
		tagLayout.setSpacing(true);
		middleSearchLayout.addComponent(tagLayout);
		keyWords = new TextField("Parole chiave");
		tagLayout.addComponent(keyWords);
		keyWords.setWidth("90%");;
//		middleSearchLayout.setExpandRatio(tagLayout, 1);

		FormLayout advancedLayout = new FormLayout();
		advancedLayout.setSpacing(true);
		advancedLayout.setSizeFull();
		middleSearchLayout.addComponent(advancedLayout);
		middleSearchLayout.setExpandRatio(advancedLayout, 3);

		name = new TextField("Nome");
		advancedLayout.addComponent(name);
		name.setWidth("50%");

		author = new TextField("Autore");
		advancedLayout.addComponent(author);
		author.setWidth("50%");

		text = new TextArea("Contenuto");
		text.setWidth("90%");
		advancedLayout.addComponent(text);

		mimeType = new ComboBox("Mimetype");
		advancedLayout.addComponent(mimeType);
		mimeType.setWidth("50%");

		dateLayout = new HorizontalLayout();
		dateLayout.setSpacing(true);
		dateLayout.setCaption("Data di modifica");
		advancedLayout.addComponent(dateLayout);

		fromDate = new DateField("Da");
		dateLayout.addComponent(fromDate);

		toDate = new DateField("A");
		dateLayout.addComponent(toDate);

		mod = new TextField("Modificatore");
		advancedLayout.addComponent(mod);
		mod.setWidth("50%");

		searchButton = new Button("Search");
		searchButton.addStyleName("default");
		searchButton.setIcon(new ThemeResource("img/search36.png"));
		middleSearchLayout.addComponent(searchButton);
		middleSearchLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);
//		middleSearchLayout.setExpandRatio(searchButton, 1);

		searchButton.addClickListener(searchListener);

		// resultLayout risultati ricerca: resultSearch + table + returnButton 
		topResultLayout = new HorizontalLayout();
		topResultLayout.setVisible(false);
		topResultLayout.setWidth("100%");
		topResultLayout.setSpacing(true);
		topResultLayout.addStyleName("toolbar");
		addComponent(topResultLayout);
		

		Image logoResult = new Image(null, new ThemeResource("img/alfresco.png"));
		logoResult.setHeight("45px");
		logoResult.setWidth("45px");
		topResultLayout.addComponent(logoResult);

		resultSearch = new Label("Risultati Ricerca Avanzata");
		resultSearch.addStyleName("h1");
		resultSearch.setSizeUndefined();
		topResultLayout.addComponent(resultSearch);
		topResultLayout.setComponentAlignment(resultSearch, Alignment.MIDDLE_LEFT);
		topResultLayout.setExpandRatio(resultSearch, 1);
		
		middleResultLayout = new VerticalLayout();
		middleResultLayout.setSpacing(true);
		middleResultLayout.setMargin(true);
		middleResultLayout.setSizeFull();
		middleResultLayout.setVisible(false);
		addComponent(middleResultLayout);
		setExpandRatio(middleResultLayout, 2);

		returnButton  = new Button("Ritorna alla ricerca");
		returnButton.setIcon(new ThemeResource("img/back.png"));
		returnButton.addStyleName("link");
		middleResultLayout.addComponent(returnButton);
		middleResultLayout.setComponentAlignment(returnButton, Alignment.TOP_RIGHT);
		middleResultLayout.setExpandRatio(returnButton, 1);
		returnButton.addClickListener(searchListener);
		
		// table
		table = new TableComponent(null,client,null);
		table.pageLength(12);
		table.setWidth("90%");
		middleResultLayout.addComponent(table);
		middleResultLayout.setComponentAlignment(table, Alignment.TOP_CENTER);

		

	}

	Button.ClickListener searchListener = new Button.ClickListener() {

		private static final long serialVersionUID = 1L;

		@SuppressWarnings("deprecation")
		@Override
		public void buttonClick(ClickEvent event) {

			// searchButton
			if(event.getButton().equals(searchButton)){
			
				Collection<PropertyMatcher> matchers = new ArrayList<>();
                ItemIterable<DocumentView> results;
				String nameDocument = name.getValue();
				String textDocument = text.getValue();

				if(name.getValue().equals(""))
					nameDocument = null;

				if(text.getValue().equals(""))
					textDocument = null;	

				if(!author.getValue().equals(""))
					matchers.add(new PropertyMatcher(PropertyIds.CREATED_BY, QueryOperator.EQUALS, PropertyType.STRING, author.getValue()));


				if(!mod.getValue().equals(""))
					matchers.add(new PropertyMatcher(PropertyIds.LAST_MODIFIED_BY, QueryOperator.EQUALS, PropertyType.STRING, mod.getValue()));

				if(fromDate.getValue() != null)
					matchers.add(new PropertyMatcher(PropertyIds.LAST_MODIFICATION_DATE, QueryOperator.GREATER_THAN_OR_EQUALS, PropertyType.DATETIME, fromDate.getValue()));

				if(toDate.getValue() != null)
					matchers.add(new PropertyMatcher(PropertyIds.LAST_MODIFICATION_DATE, QueryOperator.LESS_THAN_OR_EQUALS, PropertyType.DATETIME, toDate.getValue()));

                if(!keyWords.getValue().equals("")) {
                    matchers.add(new AlfrescoClient.TagMatcher(keyWords.getValue().split("\\s+")));
                }

				if(keyWords.getValue().equals("") && nameDocument == null && 
						textDocument == null && matchers.isEmpty()){
					// Notification with default settings for a warning
					Notification notif = new Notification("Error!","Inserire almeno un valore",Notification.TYPE_WARNING_MESSAGE);

					// Customize it
					notif.setDelayMsec(2000);
					notif.setPosition(Position.MIDDLE_CENTER);
//					notif.setStyleName("error");
					
					// Show it in the page
					notif.show(Page.getCurrent());
					
				} else {

					table.clearTable();
					topSearchLayout.setVisible(false);
					middleSearchLayout.setVisible(false);
					topResultLayout.setVisible(true);
					middleResultLayout.setVisible(true);
                    results = client.search(nameDocument, textDocument, matchers);


					for(DocumentView document : results) {
                        table.addItemToFolderComponent(document);
					}

					matchers.removeAll(matchers);

				}

            }



			// returnButton
			else if(event.getButton().equals(returnButton)){
				topResultLayout.setVisible(false);
				middleResultLayout.setVisible(false);
				topSearchLayout.setVisible(true);
				middleSearchLayout.setVisible(true);
			}
		}
	};

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
