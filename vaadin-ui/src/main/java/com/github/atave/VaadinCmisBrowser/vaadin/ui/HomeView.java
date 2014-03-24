package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.DocumentUploader;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.StringUtils;
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
import com.vaadin.ui.Button.ClickListener;
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
	private TextField name;
	private Window window;
	private Button addFolder;

	private TableComponent table;
	private Tree tree;

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
		addFolderImage = new Image(null, new ThemeResource("img/addfolder-icon.png"));
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
	public void enter(ViewChangeEvent event) {}

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
		private OptionGroup v;

		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {

			window = new Window("Select file to upload ");
			window.setResizable(false);
			window.center();
			window.addStyleName("edit-dashboard");
			UI.getCurrent().addWindow(window);

			// find document to upload
			FolderView folderView = client.getCurrentFolder();
			final String path = folderView.getPath();

			final DocumentUploader receiver = new DocumentUploader(client, path) {	
				private static final long serialVersionUID = 1L;	
				@Override
				protected void onCmisUploadReceived(DocumentView documentView) {					
				}				
			};

			// set background and information
			VerticalLayout background = new VerticalLayout();		

			FormLayout informationLayout = new FormLayout();
			informationLayout.setSizeUndefined();
			informationLayout.setMargin(true);

			Upload upload = new Upload("", receiver);
			upload.setImmediate(false);	
			upload.setButtonCaption("Start upload");
			upload.addSucceededListener(receiver);
			informationLayout.addComponent(upload);
			
			v = new OptionGroup("Select Version");
			v.addItem("Minor");
			v.addItem("Major");
			v.setMultiSelect(false);
			v.setImmediate(true);
			informationLayout.addComponent(v);
			
			final Label state = new Label();
			state.setCaption("Current state: ");
			state.setValue("Idle");
			informationLayout.addComponent(state);

			final Label fileName = new Label();
			fileName.setCaption("Upload file: ");
			informationLayout.addComponent(fileName);

			// progress bar
			final ProgressIndicator progressBar = new ProgressIndicator();
			progressBar.setCaption("Progress: ");
			progressBar.setVisible(false);
			informationLayout.addComponent(progressBar);

			// textual progress
			final Label textualProgress = new Label();
			informationLayout.addComponent(textualProgress);

			// close button
			GridLayout gridLayout = new GridLayout(5,1);
			gridLayout.setSizeFull();
			gridLayout.setSpacing(true);
			gridLayout.setMargin(true);
			final Button close = new Button("Finish");
			close.setVisible(false);
			close.addStyleName("default");
			gridLayout.addComponent(close,2,0);

			//set footer
			HorizontalLayout footer = new HorizontalLayout();
			footer.setMargin(true);
			footer.setSpacing(true);
			footer.addStyleName("footer");
			footer.setWidth("100%");
			footer.addComponent(gridLayout);
			
			upload.addListener(new Upload.StartedListener() {

				private static final long serialVersionUID = 1L;

				public void uploadStarted(final StartedEvent event) {
					if(client.exists(path, event.getFilename())){
						if(v.getValue().toString().equals("Minor")){
							receiver.setVersioningState(VersioningState.MINOR);
						}
						else if(v.getValue().toString().equals("Major")){
							receiver.setVersioningState(VersioningState.MAJOR);
						}
					}
					progressBar.setValue(0f);
					progressBar.setVisible(true);
					progressBar.setPollingInterval(500); // hit server frequently to get
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
					progressBar.setValue(readBytes / (float) contentLength);
					textualProgress.setValue("Processed " + readBytes
							+ " bytes of " + contentLength);
				}

			});

			upload.addListener(new Upload.FinishedListener() {

				private static final long serialVersionUID = 1L;

				public void uploadFinished(FinishedEvent event) {
					state.setValue("Idle");
					progressBar.setVisible(false);
					textualProgress.setVisible(false);
					table.populateTable(path);
					close.setVisible(true);
				}
			});

			close.addClickListener(new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					window.close();
				}
			});

			// add all layouts to background
			background.addComponent(informationLayout);
			background.addComponent(footer);
			window.setContent(background);	

		}
	};

	ValueChangeListener versionListener = new ValueChangeListener() {

		private static final long serialVersionUID = 1L;

		@Override
		public void valueChange(ValueChangeEvent event) {


		}
	};


	MouseEvents.ClickListener addFolderListener = new MouseEvents.ClickListener() {

		private static final long serialVersionUID = 1L;

		@Override
		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
			window = new Window("Add new folder");
			window.setResizable(false);
			window.center();
			window.addStyleName("edit-dashboard");
			UI.getCurrent().addWindow(window);

			VerticalLayout background = new VerticalLayout();		

			FormLayout informationLayout = new FormLayout();
			informationLayout.setSizeUndefined();
			informationLayout.setMargin(true);
			name = new TextField("Name");
			name.setRequired(true);
			name.setSizeFull();
			informationLayout.addComponent(name);
			TextArea description = new TextArea("Description");
			description.setSizeFull();
			informationLayout.addComponent(description);

			GridLayout gridLayout = new GridLayout(5,1);
			gridLayout.setSpacing(true);
			gridLayout.setMargin(true);
			addFolder = new Button("Add");
			addFolder.addClickListener(nameListener);
			addFolder.addStyleName("default");
			Button close = new Button("Close");
			close.addClickListener(nameListener);
			close.addStyleName("default");
			gridLayout.addComponent(addFolder,1,0,2,0);
			gridLayout.addComponent(close,3,0,4,0);

			HorizontalLayout footer = new HorizontalLayout();
			footer.setMargin(true);
			footer.setSpacing(true);
			footer.addStyleName("footer");
			footer.setWidth("100%");
			footer.addComponent(gridLayout);		

			background.addComponent(informationLayout);
			background.addComponent(footer);
			window.setContent(background); 
		}
	};

	ClickListener nameListener = new ClickListener(){
		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(ClickEvent event) {
			
			window.close();
			
			if(!name.getValue().equals(null) && event.getButton() == addFolder){
				FolderView newFolder = null;
				String path = client.getCurrentFolder().getPath();
				
				if(!client.exists(path + name.getValue())){
					newFolder = client.createFolder(path, name.getValue());			
				} else {
					String regexName = name.getValue();
					do {
						regexName = StringUtils.renameFolder(regexName);
					} while(client.exists(path + regexName));	
					
					newFolder = client.createFolder(path, regexName);
				}		
				table.addItemToFolderComponent(newFolder);
			}
			
		}
	};


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
