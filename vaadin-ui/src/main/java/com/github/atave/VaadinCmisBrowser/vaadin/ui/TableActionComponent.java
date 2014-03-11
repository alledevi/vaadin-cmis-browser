package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient;
import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.DocumentDownloader;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TableActionComponent extends CustomComponent {
	private static final long serialVersionUID = 1L;
	Button yes, no;
	Table table;
	CmisClient client;
	Boolean isFolder;
	Integer itemId;
	String path;
	Window window;
	Panel queryPanel;
	String requestedVersion;
	Link downloadLink = new Link();

	public TableActionComponent(final String path, final Integer itemId , final Table table, final CmisClient client, final Boolean isFolder) {
		this.table = table;
		this.client = client;
		this.isFolder = isFolder;
		this.itemId = itemId;
		this.path = path;
	

		// A layout structure used for composition
		Panel panel = new Panel();
		//which rows it refers to 
		panel.setData(itemId);
		HorizontalLayout layout = new HorizontalLayout();
		panel.setContent(layout);

		//button for delete document
		Image deleteDocument = new Image(null, new ThemeResource(
				"img/delete-icon.png"));
		deleteDocument.setHeight("34px");
		deleteDocument.setWidth("34px");
		queryPanel = new Panel();
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		yes = new Button("yes");
		no = new Button("no");
		yes.addClickListener(buttonListener);
		no.addClickListener(buttonListener);
		horizontalLayout.addComponent(yes);
		horizontalLayout.setComponentAlignment(yes, Alignment.MIDDLE_LEFT);
		horizontalLayout.addComponent(no);
		horizontalLayout.setComponentAlignment(no, Alignment.MIDDLE_RIGHT);
		queryPanel.setContent(horizontalLayout);

		ClickListener deleteDocumentListener = new ClickListener() {
			@Override
			public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
				//create and add window
				window = new Window();
				window.center();
				final VerticalLayout verticalLayout = new VerticalLayout();      
				UI.getCurrent().addWindow(window);
				Label question = new Label("Delete: Are you sure?");
				verticalLayout.addComponent(question);
				verticalLayout.addComponent(queryPanel);
				verticalLayout.setComponentAlignment(queryPanel, Alignment.BOTTOM_CENTER);
				window.setHeight("150px");
				window.setWidth("500px");
				window.setContent(verticalLayout);
			}
		};
		deleteDocument.addClickListener(deleteDocumentListener);

		//button for download document
		Image documentDownload = new Image(null, new ThemeResource(
				"img/download-icon.png"));
		documentDownload.setHeight("34px");
		documentDownload.setWidth("34px");
		
		ClickListener documentDownloadListener = new ClickListener() {
			@Override
			public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
				window = new Window();
				window.center();
				final VerticalLayout verticalLayout = new VerticalLayout();      
				UI.getCurrent().addWindow(window);
				//get document
				DocumentView document = client.getDocument(path);	
				ComboBox versions = new ComboBox("Select version: ");
				versions.setImmediate(true);
				for(String version : document.getAllVersions()){
					versions.addItem(version);
				}
				
				versions.addValueChangeListener(versionListener);				
				verticalLayout.addComponent(versions);
				downloadLink.setCaption("Start download");
				downloadLink.setEnabled(false);
				verticalLayout.addComponent(downloadLink);		
				window.setHeight("150px");
				window.setWidth("500px");
				window.setContent(verticalLayout);			
			}
		};
		documentDownload.addClickListener(documentDownloadListener);

		//button for more information
		Image moreInformation = new Image(null, new ThemeResource(
				"img/info-icon.png"));
		moreInformation.setHeight("34px");
		moreInformation.setWidth("34px");
		ClickListener moreInformationListener = new ClickListener() {
			@Override
			public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
				Item item = table.getItem(itemId);
				Label createdBy = new Label("created by: " + (String) table.getContainerProperty(itemId, "created by").getValue());
				Label modifiedBy = new Label("modified by: " + (String) table.getContainerProperty(itemId, "modified by").getValue());
				Label path = new Label("path: " + (String) table.getContainerProperty(itemId, "path").getValue());
				
				window = new Window();
				window.center();
				final VerticalLayout verticalLayout = new VerticalLayout();      
				UI.getCurrent().addWindow(window);
				Label title = new Label("More information");
				title.setHeight("10px");
				verticalLayout.addComponent(title);		
				verticalLayout.setComponentAlignment(title, Alignment.TOP_CENTER);
				verticalLayout.addComponent(createdBy);
				verticalLayout.addComponent(modifiedBy);
				verticalLayout.addComponent(path);
				
				window.setHeight("150px");
				window.setWidth("500px");
				window.setContent(verticalLayout);
			}
		};
		moreInformation.addClickListener(moreInformationListener);


		panel.addClickListener(panelListener);
		layout.addComponent(deleteDocument);
		layout.setComponentAlignment(deleteDocument, Alignment.BOTTOM_LEFT);
		layout.addComponent(documentDownload);
		layout.setComponentAlignment(documentDownload, Alignment.MIDDLE_CENTER);
		layout.addComponent(moreInformation);
		layout.setComponentAlignment(moreInformation, Alignment.MIDDLE_RIGHT);
		setCompositionRoot(panel);

	}

	ClickListener panelListener = new ClickListener() {
		//select table row
		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
			Integer id = (Integer)((Panel) event.getComponent()).getData();
			table.select(id);
		}
	};

	Button.ClickListener buttonListener = new Button.ClickListener() {

		@Override
		public void buttonClick(ClickEvent event) {
			// TODO Auto-generated method stub
			if(event.getButton().equals(yes)){
				// Delete all versions of a document			
				if (isFolder){
					FolderView folder = client.getFolder(path);
					client.deleteFolder(folder);			
				} else {
					DocumentView document = client.getDocument(path);
					client.deleteDocument(document.asDocument());
				}
				table.removeItem(itemId);
			}	

			window.close();
		}
	};
	
	ValueChangeListener versionListener = new ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			requestedVersion = event.getProperty().getValue().toString();
			
			if (requestedVersion == "" || requestedVersion == null){
				requestedVersion = "1.0";
			}
			DocumentView document = client.getDocument(path);	
			document = document.getObjectOfVersion(requestedVersion);
			// Create a stream resource
			StreamResource.StreamSource source = new DocumentDownloader(document);
			StreamResource resource = new StreamResource(source, document.getName());
			// Let the user download the document
			downloadLink.setResource(resource);
			downloadLink.setEnabled(true);
			downloadLink.setTargetName("_blank");
			downloadLink.setVisible(true);
		}
	};


}