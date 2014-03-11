package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.DocumentUploader;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.StartedEvent;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@SuppressWarnings("deprecation")
public class HomeView  extends VerticalLayout implements View  {

	private static final long serialVersionUID = 1L;

	private static AlfrescoClient client;

	private HorizontalLayout topLayout;
	private Label title;

	private HorizontalLayout middleLayout;
	private Image addFolderImage;
	private Image uploadImage;
	private TextField filter;

	private HorizontalSplitPanel bottomLayout;

	private TableComponent table;
	private Tree tree;

	private Object [] p={"image","name"};
	private boolean [] o={true,true};

	public HomeView() {

		setSizeFull();
		addStyleName("dashboard-view");
		client = ((AppUI)UI.getCurrent()).getClient();

		// TOPLAYOUT	

		//pannello titolo
		topLayout = new HorizontalLayout();
		topLayout.setWidth("100%");
		topLayout.setSpacing(true);
		topLayout.addStyleName("toolbar");
		addComponent(topLayout);
		title = new Label("My Alfresco");
		title.setSizeUndefined();
		title.addStyleName("h1");
		topLayout.addComponent(title);
		topLayout.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
		topLayout.setExpandRatio(title, 1);

		//middle: BreadCrumb + SearchBar + Icone createFolder,upload
		middleLayout = new HorizontalLayout();
		middleLayout.setWidth("100%");
		middleLayout.setSpacing(true);
		middleLayout.addStyleName("toolbar");
		addComponent(middleLayout);




		// ADD FOLDER
		addFolderImage = new Image(null, new ThemeResource("img/arrow-up.png"));
		addFolderImage.setHeight("34px");
		addFolderImage.setWidth("34px");
		addFolderImage.addClickListener(addFolderListener);
		middleLayout.addComponent(addFolderImage);
		middleLayout.setComponentAlignment(addFolderImage, Alignment.MIDDLE_LEFT);

		//-- UPLOAD --
		uploadImage = new Image(null, new ThemeResource("img/upload-icon.png"));
		uploadImage.setHeight("34px");
		uploadImage.setWidth("34px");
		uploadImage.addClickListener(uploadListener);
		middleLayout.addComponent(uploadImage);
		middleLayout.setComponentAlignment(uploadImage, Alignment.MIDDLE_LEFT);

		// SearchBar

		filter = new TextField();
		filter.setWidth("300px");
		filter.focus();
		middleLayout.addComponent(filter);
		middleLayout.setComponentAlignment(filter, Alignment.MIDDLE_RIGHT);

		// bottom: splitPanel con tree e table
		bottomLayout = new HorizontalSplitPanel();
		bottomLayout.setStyleName("toolbar");
		bottomLayout.setSizeFull(); //mette barra in split panel
		bottomLayout.setSplitPosition(15, Sizeable.UNITS_PERCENTAGE);
		addComponent(bottomLayout);
		setExpandRatio(bottomLayout, 3);

		//--- TREE ---
		tree = new Tree();
		tree.addItem("Repository");
		createTree(tree, "Repository");
		tree.setSizeFull();		
		tree.expandItem("Repository");
		tree.addListener(treeListener);
		bottomLayout.setFirstComponent(tree);

		// --- TABLE ---
		table = new TableComponent(null, client, tree);
		table.setImmediate(true);
		bottomLayout.setSecondComponent(table);

		table.sort(p, o);

		filter.addListener(new TextChangeListener() {
			private static final long serialVersionUID = 1048639156493298177L;

			SimpleStringFilter filterString = null;

			public void textChange(TextChangeEvent event) {
				Filterable f = table.getContainerDataSource();

				// Remove old filter
				if (filterString != null)
					f.removeContainerFilter(filterString);

				// Set new filter for the "Name" column

				filterString = new SimpleStringFilter("name", event.getText(),true, false);
				f.addContainerFilter(filterString);
			}


		});


	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public static void createTree(Tree tree, String parentId){	
		Collection<FileView> currentFiles = client.getCurrentFolder().getChildren();
		for(FileView file : currentFiles){
			if(file.isFolder()){
				tree.addItem(file.getName());
				tree.setParent(file.getName(), parentId);
				if(((FolderView) file).getChildren().isEmpty()){
					tree.setChildrenAllowed(file.getName(), false);
				}
			}
		}
	}

	//	BreadcrumbClickListener cl = new BreadcrumbClickListener(){

	//	};


	MouseEvents.ClickListener uploadListener = new MouseEvents.ClickListener() {

		private static final long serialVersionUID = 1L;

		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
			//create and add window
			final Window window = new Window();
			window.center();
			final VerticalLayout verticalLayout = new VerticalLayout();      
			UI.getCurrent().addWindow(window);
			//create component
			final Label state = new Label();
			final Label fileName = new Label();
			final Label textualProgress = new Label();
			final ProgressIndicator pi = new ProgressIndicator();
			final Button close = new Button("Finish");
			
			// find parameters
			FolderView folderView = client.getCurrentFolder();
			final String path = folderView.getPath();
			final DocumentUploader receiver = new DocumentUploader(client, path) {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				protected void onCmisUploadReceived(DocumentView documentView) {
					// TODO Auto-generated method stub

				}
				
			};
						
			//upload component        
			final Upload upload = new Upload("Select file to upload", receiver);
			//!!!!!! very important
			upload.setImmediate(false);
			upload.setButtonCaption("Upload File");
			upload.addSucceededListener(receiver);

            verticalLayout.addComponent(upload);
			verticalLayout.setComponentAlignment(upload, Alignment.MIDDLE_CENTER);
			verticalLayout.addComponent(new Label());	

			//details
			Panel p = new Panel("Status");
			p.setSizeUndefined();
			FormLayout l = new FormLayout();
			l.setMargin(true);
			p.setContent(l);
			HorizontalLayout stateLayout = new HorizontalLayout();
			stateLayout.setSpacing(true);
			stateLayout.addComponent(state);
			stateLayout.setCaption("Current state");
			state.setValue("Idle");
			l.addComponent(stateLayout);
			fileName.setCaption("File name");
			l.addComponent(fileName);
			pi.setCaption("Progress");
			pi.setVisible(false);
			l.addComponent(pi);
			textualProgress.setVisible(false);
			l.addComponent(textualProgress);
			close.setVisible(false);
			l.addComponent(close);
			verticalLayout.addComponent(p);
			verticalLayout.setComponentAlignment(p, Alignment.BOTTOM_CENTER);
			
			upload.addListener(new Upload.StartedListener() {

				private static final long serialVersionUID = 1L;

				public void uploadStarted(final StartedEvent event) {
                    if(client.exists(path, event.getFilename())){
                        receiver.setVersioningState(VersioningState.MAJOR);
					}
					pi.setValue(0f);
					pi.setVisible(true);
					pi.setPollingInterval(500); // hit server frequently to get
					textualProgress.setVisible(true);
					// updates to client
					state.setValue("Uploading");
					fileName.setValue(event.getFilename());
				}
			});

			upload.addListener(new Upload.ProgressListener() {

				private static final long serialVersionUID = 1L;

				public void updateProgress(long readBytes, long contentLength) {
					// this method gets called several times during the update
					pi.setValue(readBytes / (float) contentLength);
					textualProgress.setValue("Processed " + readBytes
							+ " bytes of " + contentLength);
				}

			});

			upload.addListener(new Upload.FinishedListener() {

				private static final long serialVersionUID = 1L;

				public void uploadFinished(FinishedEvent event) {
					state.setValue("Idle");
					pi.setVisible(false);
					textualProgress.setVisible(false);
//					System.out.println("receiver name: "+ receiver.getFileName());
//					System.out.println("receiver version: "+ receiver.getDocumentView().getVersionLabel());
					table.populateTable(path);
					close.setVisible(true);
				}
			});

			close.addClickListener(new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					// TODO Auto-generated method stub
					window.close();
				}
			});

			//			FinishedListener finishedListener = new FinishedListener() {			
			//				@Override
			//				public void uploadFinished(FinishedEvent event) {
			//					// TODO Auto-generated method stub
			//					table.populateTable(path);
			//					window.close();
			//				}
			//			};
			//			upload.addFinishedListener(finishedListener);


			//set size
			window.setHeight("300px");
			window.setWidth("500px");
			window.setContent(verticalLayout);

		}
	};

	ValueChangeListener versionListener = new ValueChangeListener() {

		@Override
		public void valueChange(ValueChangeEvent event) {


		}
	};


	MouseEvents.ClickListener addFolderListener = new MouseEvents.ClickListener() {

		private static final long serialVersionUID = 1L;

		@Override
		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
			// TODO Auto-generated method stub
			String path = client.getCurrentFolder().getPath();
			client.createFolder(path, "New folder");
			table.populateTable(path);

		}};


		// popola tabella + tree
		ItemClickListener treeListener = new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 1L;
			List<String> itemExpanded = new ArrayList<>();
			String itemId = null;
			String path = null;


			public void itemClick(ItemClickEvent event) {
				// Pick only left mouse clicks
				itemId = (String) event.getItemId();
				if (event.getButton() == ItemClickEvent.BUTTON_LEFT){
					path = getTreePath(tree, itemId);
					//update folderComponent + tree
					table.populateTable(path);
					createTree(tree, itemId);
					if(itemExpanded.contains(itemId)){
						//if expanded, collapse
						tree.collapseItem(itemId);
						itemExpanded.remove(itemId);
					}else{
						tree.expandItem(itemId);
						itemExpanded.add(itemId);
					}				
				}
			}
		};

		//		private boolean filterByProperty(String prop, Item item, String text) {
		//			if (item == null || item.getItemProperty(prop) == null
		//					|| item.getItemProperty(prop).getValue() == null)
		//				return false;
		//			String val = item.getItemProperty(prop).getValue().toString().trim()
		//					.toLowerCase();
		//			if (val.startsWith(text.toLowerCase().trim()))
		//				return true;
		//			// String[] parts = text.split(" ");
		//			// for (String part : parts) {
		//			// if (val.contains(part.toLowerCase()))
		//			// return true;
		//			//
		//			// }
		//			return false;
		//		}

		public static String getTreePath(Tree tree, String itemId){
			//return path of folder selected
			if(itemId.equals("Repository")){
				return "/";
			}		
			String path = "/" + itemId;
			while(!tree.getParent(itemId).equals("Repository") && tree.getParent(itemId) != null ){
				itemId = (String) tree.getParent(itemId);
				path = "/" + itemId +  path;
			}		
			return path;
		}
}
