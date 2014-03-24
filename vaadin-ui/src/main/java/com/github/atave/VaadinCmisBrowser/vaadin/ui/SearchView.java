package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.apache.chemistry.opencmis.client.api.ItemIterable;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.PropertyMatcher;
import com.github.atave.VaadinCmisBrowser.cmis.api.PropertyType;
import com.github.atave.VaadinCmisBrowser.cmis.api.QueryOperator;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class SearchView extends VerticalLayout implements View  {

	private static final long serialVersionUID = 1L;

	private AlfrescoClient client;
	private TableComponent table;

	private FormLayout panel;
	private HorizontalLayout dateLayout;
	private VerticalLayout resultLayout;

	private Label advancedSearch;
	private Label resultSearch;
	private TextField keyWords;
	private TextField name;
	private TextField text;
	private TextField description;
	private ComboBox mimeType;
	private DateField fromDate;
	private DateField toDate;
	private TextField mod;
	private Button searchButton;
	private Button returnButton;

	public SearchView() {

		setSizeFull();
		//		addStyleName("dashboard-view");
		client = ((AppUI)UI.getCurrent()).getClient();

		// panel: advancedSearch + keyWords,name,description,text,mimeType,dateLayout,mod + searchButton
		panel = new FormLayout();
		panel.setSpacing(true);
		panel.setSizeFull();
		panel.setMargin(true);

		// advancedSearch : title
		advancedSearch = new Label("Ricerca Avanzata");
		advancedSearch.addStyleName("h1");
		panel.addComponent(advancedSearch);

		// Tag
		keyWords = new TextField("Parole chiave");
		panel.addComponent(keyWords);
		keyWords.setWidth("60%");

		name = new TextField("Nome");
		panel.addComponent(name);
		name.setWidth("30%");

		description = new TextField("Descrizione");
		panel.addComponent(description);
		description.setWidth("60%");

		text = new TextField("Contenuto");
		text.setWidth("60%");
		panel.addComponent(text);

		mimeType = new ComboBox("Mimetype");
		panel.addComponent(mimeType);
		mimeType.setWidth("30%");

		dateLayout = new HorizontalLayout();
		dateLayout.setSpacing(true);
		dateLayout.setCaption("Data di modifica");
		panel.addComponent(dateLayout);

		fromDate = new DateField("Da");
		dateLayout.addComponent(fromDate);

		toDate = new DateField("A");
		dateLayout.addComponent(toDate);

		mod = new TextField("Modificatore");
		panel.addComponent(mod);
		mod.setWidth("30%");

		searchButton = new Button("Search");
		searchButton.setIcon(new ThemeResource("img/search36.png"));
		panel.addComponent(searchButton);
		searchButton.addClickListener(searchListener);


		// resultLayout risultati ricerca: resultSearch + table + returnButton 
		resultLayout = new VerticalLayout();
		resultLayout.setVisible(false);
		resultLayout.setSizeFull();
		resultLayout.setMargin(true);
		resultLayout.setSpacing(true);
		addComponent(resultLayout);

		resultSearch = new Label("Risultati Ricerca Avanzata");
		resultSearch.addStyleName("h1");
		resultLayout.addComponent(resultSearch);
		resultLayout.setExpandRatio(resultSearch, 1);	

		// table
		table = new TableComponent(null,client,null);
		table.pageLength();
		resultLayout.addComponent(table);
		resultLayout.setExpandRatio(table, 4);
		resultLayout.setComponentAlignment(table, Alignment.TOP_CENTER);

		returnButton  = new Button("Ritorna alla ricerca");
		returnButton.setIcon(new ThemeResource("img/search36.png"));
		resultLayout.addComponent(returnButton);
		resultLayout.setExpandRatio(returnButton, 1);
		returnButton.addClickListener(searchListener);

		addComponent(panel);	
	}

	Button.ClickListener searchListener = new Button.ClickListener() {

		private static final long serialVersionUID = 1L;

		@SuppressWarnings("deprecation")
		@Override
		public void buttonClick(ClickEvent event) {

			// searchButton
			if(event.getButton().equals(searchButton)){

				// da aggiungere: eccezione name e text entrambi nulli e Autore creazione??				
				Collection<PropertyMatcher> matchers = new ArrayList<>();
				PropertyMatcher matcherTag;
				ItemIterable<DocumentView> results;

				String nameDocument = name.getValue();
				String textDocument = text.getValue();

				if(name.getValue().equals(""))
					nameDocument = null;

				if(text.getValue().equals(""))
					textDocument = null;	

				if(!description.getValue().equals(""))
					matchers.add(new PropertyMatcher("cmis:description", QueryOperator.LIKE, PropertyType.STRING, description.getValue()));

				if(!mod.getValue().equals(""))
					matchers.add(new PropertyMatcher("cmis:lastModifiedBy", QueryOperator.EQUALS, PropertyType.STRING, mod.getValue()));

				if(fromDate.getValue() != null)
					matchers.add(new PropertyMatcher("cmis:lastModificationDate", QueryOperator.GREATER_THAN, PropertyType.DATETIME, new Date(2014, 03, 15)));

				if(toDate.getValue() != null)
					matchers.add(new PropertyMatcher("cmis:lastModificationDate", QueryOperator.LESS_THAN, PropertyType.DATETIME, new Date(2014, 03, 17)));

				if(keyWords.getValue().equals("") && nameDocument == null && 
						textDocument == null && matchers.isEmpty()){
					 Notification.show("Error!","Inserire almeno un valore", Notification.TYPE_TRAY_NOTIFICATION);

				}
					
				else{
					table.clearTable();
					panel.setVisible(false);
					resultLayout.setVisible(true);

					// Search for documents by tags
					if(!keyWords.getValue().equals("")){
						matcherTag = new AlfrescoClient.TagMatcher(keyWords.getValue());
						results = client.search(null, null, Collections.singleton(matcherTag));

					}
					else{

						// Search all documents
						if(matchers.isEmpty()){
							results = client.search(nameDocument,textDocument);
						}
						else{
							results = client.search(nameDocument,textDocument,matchers);

						}
					}

					for(DocumentView document : results) {
						if (document.getPath() != null) {
							table.addItemToFolderComponent(document.getPath());
						}
					}

					matchers.removeAll(matchers);

				}
				//	matchers.add(new PropertyMatcher("cmis:createdBy", QueryOperator.EQUALS, PropertyType.STRING, "userA"));
			}

			// returnButton
			else if(event.getButton().equals(returnButton)){
				resultLayout.setVisible(false);
				panel.setVisible(true);
			}
		}
	};

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
