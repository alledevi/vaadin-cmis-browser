package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.CmisTree;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.DocumentUploader;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.StartedEvent;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("deprecation")
/**
 * Home view. The first tab of application 
 *
 */
public class HomeView extends VerticalLayout implements View {

	private static final long serialVersionUID = 1L;
	private static final String logoSize = "45px";
	private static final String imageSize = "34px";
	private static final String layoutWidth = "100%";
	private static final String filterWidth = "300px";
	private static final String iconSize = "16px";
	private static final String minor = "Minor";
	private static final String major = "Major";
	private static final String infoIcon = "img/info-icon.png";
	private static final String logoImagePath = "img/alfresco.png";
	private static final String addFolderImagePath = "img/addfolder-icon.png";
	private static final String uploadImagePath = "img/upload-icon.png";
	private static final String removeImage = "img/remove.png";

	// AlfrescoClient 
	private static AlfrescoClient client;

    private final Image addFolderImage;
	private final Image uploadImage;
    private Button addFolder;
	private Button close;
	private Window window;
	private TextField name;


	private HorizontalLayout tagLayout;
	private TextField addTagTextField;
	private Button addTagButton;
	private ArrayList<String> tags = new ArrayList<>();
	private Table tableTag;
	private Integer i = 0;
	private Image removeTag;
	private Label fileName;
	private String path;


    private TableComponent table;
	private final CmisTree tree;

    public HomeView() {

		setSizeFull();
		setMargin(true);
		addStyleName("dashboard-view");

		Button homeButton = ((AppUI) UI.getCurrent()).getButtonHome();
		Button searchButton = ((AppUI) UI.getCurrent()).getButtonSearch(); 
		homeButton.addStyleName("selected");
		searchButton.removeStyleName("selected");

		client = ((AppUI) UI.getCurrent()).getClient();

		// TOPLAYOUT: Image logo + Label title 
        HorizontalLayout topLayout = new HorizontalLayout();
		topLayout.addStyleName("toolbar");
		topLayout.setWidth(layoutWidth);
		topLayout.setSpacing(true);
		addComponent(topLayout);

		// Image logo 
        Image logo = new Image(null, new ThemeResource(logoImagePath));
		logo.setHeight(logoSize);
		logo.setWidth(logoSize);
		topLayout.addComponent(logo);
		topLayout.setComponentAlignment(logo, Alignment.TOP_LEFT);

		// Label title 
        Label title = new Label("My Alfresco");
		title.addStyleName("h1");
		title.setSizeUndefined();
		topLayout.addComponent(title);
		topLayout.setComponentAlignment(title, Alignment.TOP_LEFT);
		topLayout.setExpandRatio(title, 1);

		// MIDDLELAYOUT: Image addFolderImage + Image uploadImage + TextField filter  
        HorizontalLayout middleLayout = new HorizontalLayout();
		middleLayout.addStyleName("toolbar");
		middleLayout.setSpacing(true);
		middleLayout.setSizeUndefined();
		addComponent(middleLayout);
		setComponentAlignment(middleLayout, Alignment.TOP_RIGHT);

		// Image addFolderImage  
		addFolderImage = new Image(null, new ThemeResource(addFolderImagePath));
		addFolderImage.setHeight(imageSize);
		addFolderImage.setWidth(imageSize);
		addFolderImage.addClickListener(actionListener);
		middleLayout.addComponent(addFolderImage);
		middleLayout.setComponentAlignment(addFolderImage, Alignment.MIDDLE_RIGHT);

		// Image uploadImage 
		uploadImage = new Image(null, new ThemeResource(uploadImagePath));
		uploadImage.setHeight(imageSize);
		uploadImage.setWidth(imageSize);
		uploadImage.addClickListener(actionListener);
		middleLayout.addComponent(uploadImage);
		middleLayout.setComponentAlignment(uploadImage, Alignment.MIDDLE_RIGHT);

		// TextField SearchBar  
        TextField filter = new TextField();
		filter.setWidth(filterWidth);
		filter.focus();
		filter.setInputPrompt("Filter");
		filter.addListener(filterListener);
		middleLayout.addComponent(filter);
		middleLayout.setComponentAlignment(filter, Alignment.MIDDLE_RIGHT);

		// BOTTOMLAYOUT: Tree tree + TableComponent table  
        HorizontalSplitPanel bottomLayout = new HorizontalSplitPanel();
		bottomLayout.setStyleName("toolbar");
		bottomLayout.setSizeFull();
		bottomLayout.setSplitPosition(15, Sizeable.UNITS_PERCENTAGE);
		addComponent(bottomLayout);
		setExpandRatio(bottomLayout, 3);

		// Tree
		tree = new CmisTree();
		tree.setSizeFull();
        bottomLayout.setFirstComponent(tree);

        tree.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                FolderView folder = (FolderView) event.getItemId();
                table.populateTable(folder.getPath());
            }
        });

        // Bootstrap tree
        tree.add(client.getFolder("/"));
        tree.show(client.getCurrentFolder());

		// TableComponent table 
		table = new TableComponent(client, tree);
		table.setImmediate(true);
		bottomLayout.setSecondComponent(table);

	}

	/**
	 * Listener for filter texfield. returns Documents and Folders filter by "Name"
	 */
    private final TextChangeListener filterListener = new TextChangeListener() {

		private static final long serialVersionUID = 1L;
		private SimpleStringFilter filterString = null;
		private Filterable f;

		public void textChange(TextChangeEvent event) {

			f = table.getContainerDataSource();

			// Remove old filter  
			if (filterString != null)
				f.removeContainerFilter(filterString);

			// Set new filter for the "Name" column  
			filterString = new SimpleStringFilter("name", event.getText(), true, false);
			f.addContainerFilter(filterString);
		}
	};

	/**
	 * Listener for button in window add folder. Add a new folder or close window.
	 */
    private final ClickListener addFolderListener = new ClickListener() {

		private static final long serialVersionUID = 1L;

		public void buttonClick(ClickEvent event) {

			window.close();

            String folderName = name.getValue();

			// Add a new folder to table
			if (!folderName.isEmpty() && event.getButton() == addFolder) {
				FolderView newFolder;
				String path = client.getCurrentFolder().getPath();
				if (!client.exists(path, folderName)) {
					newFolder = client.createFolder(path, name.getValue());
				} else {
                    String modifiedFolderName;
                    int count = 0;

					do {
                        ++count;
                        modifiedFolderName = folderName + " (" + count + ")";
					} while (client.exists(path, modifiedFolderName));

					newFolder = client.createFolder(path, modifiedFolderName);
                }

                tree.add(newFolder);
				table.addItemToTableComponent(newFolder);
			}
		}
	};


	/**
	 * Listener upload document and add folder image. Open window dialog.
	 */
    private final MouseEvents.ClickListener actionListener = new MouseEvents.ClickListener() {

		private static final long serialVersionUID = 1L;


		private VerticalLayout background;
		private FormLayout informationLayout;
		private HorizontalLayout footer;
		private GridLayout gridLayout;

		//upload 
		private OptionGroup v;
		private FolderView folderView;
		private DocumentUploader receiver;
		private HorizontalLayout uploadHorizontal;
		private Upload upload;
		private Label textualProgress;
		private ProgressIndicator progressBar;
		private Image info;
		private Label state;


		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {

			// addFolderImage 
			if (event.getComponent().equals(addFolderImage)) {

				window = new Window("Add new folder");
				window.setResizable(false);
				window.center();
				window.setModal(true);
				window.addStyleName("edit-dashboard");
				UI.getCurrent().addWindow(window);

				background = new VerticalLayout();

				informationLayout = new FormLayout();
				informationLayout.setSizeUndefined();
				informationLayout.setSizeFull();
				informationLayout.setMargin(true);

				name = new TextField("Name");
				name.setRequired(true);
				name.setSizeFull();
				name.setWidth("90%");
				name.focus();
				name.addTextChangeListener(nameListener);
				informationLayout.addComponent(name);
				informationLayout.setComponentAlignment(name, Alignment.MIDDLE_CENTER);
				name.addShortcutListener(enterFolder);

				gridLayout = new GridLayout(5, 1);
				gridLayout.setSpacing(true);
				gridLayout.setMargin(true);

				addFolder = new Button("Add");
				addFolder.addClickListener(addFolderListener);
				addFolder.addStyleName("default");
				addFolder.setEnabled(false);

				close = new Button("Close");
				close.addClickListener(addFolderListener);
				close.addStyleName("default");
				gridLayout.addComponent(addFolder, 1, 0, 2, 0);
				gridLayout.addComponent(close, 3, 0, 4, 0);

				footer = new HorizontalLayout();
				footer.setMargin(true);
				footer.setSpacing(true);
				footer.addStyleName("footer");
				footer.setWidth("100%");
				footer.addComponent(gridLayout);

				background.addComponent(informationLayout);
				background.addComponent(footer);
				window.setContent(background);
			}

			// uploadImage 
			else if (event.getComponent().equals(uploadImage)) {

				window = new Window("Select file to upload ");
				window.setResizable(false);
				window.center();
				window.setModal(true);
				window.addStyleName("edit-dashboard");
				UI.getCurrent().addWindow(window);

				// find document to upload  
				folderView = client.getCurrentFolder();
				path = folderView.getPath();

				receiver = new DocumentUploader(client, path) {

					private static final long serialVersionUID = 1L;

					protected void onCmisUploadReceived(DocumentView documentView) {

					}
				};

				// set background and information  
				background = new VerticalLayout();

				informationLayout = new FormLayout();
				informationLayout.setMargin(true);

				uploadHorizontal = new HorizontalLayout();
				uploadHorizontal.addStyleName("upload-layout");

				upload = new Upload("", receiver);
				upload.setImmediate(false);
				upload.setButtonCaption("Start upload");
				upload.addSucceededListener(receiver);
				uploadHorizontal.addComponent(upload);

				v = new OptionGroup("New Version");
				v.addItem(minor);
				v.addItem(major);
				upload.submitUpload();
				v.setValue(minor);
				v.setMultiSelect(false);
				v.setImmediate(true);
				uploadHorizontal.addComponent(v);

				info = new Image(null, new ThemeResource(infoIcon));
				info.setWidth(iconSize);
				info.setHeight(iconSize);
				info.setDescription("Given a Version expressed as MAJOR.MINOR:</br>" +
						"- Minor increases MINOR (es. 1.0 -> 1.1)</br>" +
						"- Major increases MAJOR and sets MINOR to 0 (es. 1.1 -> 2.0)");
				uploadHorizontal.addComponent(info);

				state = new Label();
				state.setCaption("Current state: ");
				state.setValue("Idle");
				informationLayout.addComponent(state);

				fileName = new Label();
				fileName.setCaption("Upload file: ");
				informationLayout.addComponent(fileName);

				// progress bar  
				progressBar = new ProgressIndicator();
				progressBar.setCaption("Progress: ");
				progressBar.setVisible(true);
				informationLayout.addComponent(progressBar);

				// textual progress  
				textualProgress = new Label();
				informationLayout.addComponent(textualProgress);

				// tagLayout: TextField tf + Button add
				tagLayout = new HorizontalLayout();
				tagLayout.setSpacing(true);
				tagLayout.setWidth("100%");
				tagLayout.setCaption("Tag: ");
				tagLayout.setEnabled(false);
				informationLayout.addComponent(tagLayout);

				// TextField addTagTextField
				addTagTextField = new TextField();
				addTagTextField.setInputPrompt("Add new tag");
				tagLayout.addComponent(addTagTextField);

				// Table tableTag
				tableTag = new Table();
				tableTag.setSelectable(false);
				tableTag.setImmediate(true);
				tableTag.addContainerProperty("name", String.class, null);
				tableTag.addContainerProperty("image", Image.class, null);
				tableTag.setColumnAlignment("image", Align.CENTER);
				tableTag.setColumnAlignment("name", Align.CENTER);
				tableTag.setColumnExpandRatio("name", 7);
				tableTag.setColumnExpandRatio("image", 1);
				tableTag.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
				tableTag.setWidth("240px");
				tableTag.addStyleName("borderless");
				tableTag.setPageLength(5);
				informationLayout.addComponent(tableTag);

				//button for remove tag
				removeTag = new Image(null, new ThemeResource(removeImage));
				removeTag.addClickListener(removeTagListener);


				// Button add
				addTagButton = new Button("Add");
				addTagButton.addStyleName("default");
				tagLayout.addComponent(addTagButton);
				addTagButton.addClickListener(addListener);
				addTagButton.addShortcutListener(enterTag);


				// close button  
				gridLayout = new GridLayout(5, 1);
				gridLayout.setSizeFull();
				gridLayout.setSpacing(true);
				gridLayout.setMargin(true);
				close = new Button("Finish");
				close.setVisible(false);
				close.addStyleName("default");
				gridLayout.addComponent(close, 2, 0);

				//set footer  
				footer = new HorizontalLayout();
				footer.setMargin(true);
				footer.setSpacing(true);
				footer.addStyleName("footer");
				footer.setWidth(layoutWidth);
				footer.addComponent(gridLayout);

				upload.addListener(new Upload.StartedListener() {

					private static final long serialVersionUID = 1L;

					public void uploadStarted(final StartedEvent event) {
						if (client.exists(path, event.getFilename())) {
							if (v.getValue().toString().equals(minor)) {
								receiver.setVersioningState(VersioningState.MINOR);
							} else if (v.getValue().toString().equals(major)) {
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

						// find tag and populate table
						if (client.exists(path, event.getFilename())) {
							try {
								FileView file = client.getFile(path, event.getFilename());
								if (file.isDocument()) {
									tags = (ArrayList<String>) client.getTags(client.getDocument(path, event.getFilename()).getId());
								} 
							} catch (IOException e1) {
								e1.printStackTrace();
							}

							for (String t : tags) {
								removeTag = new Image(t, new ThemeResource(removeImage));
								removeTag.addClickListener(removeTagListener);
								removeTag.setData(i);
								tableTag.addItem(new Object[]{t, removeTag}, i++);
							}
						}
						tagLayout.setEnabled(true);

					}
				});

				upload.addListener(new Upload.ProgressListener() {

					private static final long serialVersionUID = 1L;

					public void updateProgress(long readBytes, long contentLength) {

						// this method gets called several times during the update  
						progressBar.setValue(readBytes / (float) contentLength);
						textualProgress.setValue("Processed " + readBytes + " bytes of " + contentLength);
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

					public void buttonClick(ClickEvent event) {
						window.close();
					}
				});

				// add all layouts to background  
				background.addComponent(uploadHorizontal);
				background.addComponent(informationLayout);
				background.addComponent(footer);
				window.setContent(background);
			}
		}
	};

	/**
	 * Listener for addTagButton. Add tag to selected document or folder.
	 */
    private final Button.ClickListener addListener = new Button.ClickListener() {

		private static final long serialVersionUID = 1L;
		private FileView file;
		private String id = null;
		private DocumentView document;

		public void buttonClick(ClickEvent event) {
			Boolean exist = false;
			file = client.getFile(path, fileName.getValue());

			if (!addTagTextField.getValue().equals("")){
				try {
					tags = (ArrayList<String>) client.getTags(file.getId());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				for (String t : tags) {
					if(addTagTextField.getValue().equals(t)){
						exist = true;
						break;
					}
				}
				if(!exist) {
					removeTag = new Image(addTagTextField.getValue(), new ThemeResource(removeImage));
					removeTag.addClickListener(removeTagListener);
					removeTag.setData(i);
					tableTag.addItem(new Object[]{addTagTextField.getValue(), removeTag}, i++);
					try {
						client.createTag(addTagTextField.getValue());
						//					file = client.getFile(path + fileName.getValue());
						// file is a document
						if (file.isDocument()) {
							document = client.getDocument(path, fileName.getValue());
							id = document.getId();
						}
						client.addTags(id, Collections.singleton(addTagTextField.getValue()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	};

	/**
	 * Listener for removeTag. Remove selected tag from tableTag and from repository.
	 */
    private final MouseEvents.ClickListener removeTagListener = new MouseEvents.ClickListener() {


		private static final long serialVersionUID = 1L;
		private Integer id;
		private String tag;

		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
			try {
				//select table row
				id = (Integer) ((Image) event.getComponent()).getData();
				tag = event.getComponent().getCaption();

				// remove tag from item and table
				client.removeTags(client.getFile(fileName.getValue()).getId(), Collections.singleton(tag));
				tableTag.removeItem(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	private final TextChangeListener nameListener = new TextChangeListener() {

		private static final long serialVersionUID = 1L;

		public void textChange(TextChangeEvent event) {
			if(event.getText().equals(""))
				addFolder.setEnabled(false);
			else
				addFolder.setEnabled(true);
		}
	};

	private final ShortcutListener enterFolder = new ShortcutListener("Search", KeyCode.ENTER, null) {

		private static final long serialVersionUID = 1L;

		public void handleAction(Object sender, Object target) {

			addFolder.click();

		}
	};

	// shortcutListener enter: shortcut for add tag
	private final ShortcutListener enterTag = new ShortcutListener("", KeyCode.ENTER, null) {
		private static final long serialVersionUID = 1L;

		@Override
		public void handleAction(Object sender, Object target) {
			addTagButton.click();
		}
	};

	@Override
	public void enter(ViewChangeEvent event) {

	}
}
