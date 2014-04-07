package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.CmisTree;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.DocumentDownloader;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table.Align;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Component with all possible user action in table. It is in the last column in TableComponent
 *
 * @param tree     the tree in home view
 * @param path     path of document or folder witch actionComponent refers to
 * @param itemId   table row
 * @param client   Alfresco client
 * @param isFolder true if actionConponent refers to folder, false otherwise
 */

public class TableActionComponent extends CustomComponent {

	private static final long serialVersionUID = 1L;
	private static final String imageHeight = "34px";
	private static final String imageWidth = "34px";
	private static final String deleteImagePath = "img/delete-icon.png";
	private static final String downloadImagePath = "img/download-icon.png";
	private static final String moreInformationImagePath = "img/info-icon.png";
	private static final String removeImage = "img/remove.png";

	private CmisTree tree;
	private String path;
	private Integer itemId;
	private Table table;
	private AlfrescoClient client;
	private Boolean isFolder;

	// panel components
	private Panel panel;
	private HorizontalLayout layoutFolder;
	private HorizontalLayout layoutDocument;
	private Image deleteImage;
	private Image downloadImage;
	private Image moreInformationImage;
	private HorizontalLayout tagLayout;
	private TextField addTagTextField;
	private Button addTagButton;
	private ArrayList<String> tags = new ArrayList<String>();
	private Table tableTag;
	private Integer i = 0;
	private Image removeTag;

	//windows components
	private Window window;
	private Button yes, no;
	private Link downloadLink;
	private String requestedVersion;


	public TableActionComponent(CmisTree tree, final String path, final Integer itemId, final Table table,
			final AlfrescoClient client, final Boolean isFolder) {
		this.tree = tree;
		this.table = table;
		this.client = client;
		this.isFolder = isFolder;
		this.itemId = itemId;
		this.path = path;

		// A layout structure used for composition
		panel = new Panel();
		panel.setData(itemId);
		layoutFolder = new HorizontalLayout();
		layoutDocument = new HorizontalLayout();

		// button for delete document and folder
		deleteImage = new Image(null, new ThemeResource(deleteImagePath));
		deleteImage.setHeight(imageHeight);
		deleteImage.setWidth(imageWidth);
		deleteImage.addClickListener(deleteListener);

		// button for download document
		downloadImage = new Image(null, new ThemeResource(downloadImagePath));
		downloadImage.setHeight(imageHeight);
		downloadImage.setWidth(imageWidth);
		downloadImage.addClickListener(downloadListener);

		// button for get more information about document or folder
		moreInformationImage = new Image(null, new ThemeResource(moreInformationImagePath));
		moreInformationImage.setHeight(imageHeight);
		moreInformationImage.setWidth(imageWidth);
		moreInformationImage.addClickListener(moreInformationListener);

		panel.addClickListener(selectPanelListener);

		// add component to panel. If file is folder don't add downloadComponent
		if (isFolder) {
			panel.setContent(layoutFolder);
			layoutFolder.addComponent(deleteImage);
			layoutFolder.setComponentAlignment(deleteImage, Alignment.BOTTOM_LEFT);
			layoutFolder.addComponent(moreInformationImage);
			layoutFolder.setComponentAlignment(moreInformationImage, Alignment.MIDDLE_CENTER);
		} else {
			panel.setContent(layoutDocument);
			layoutDocument.addComponent(deleteImage);
			layoutDocument.setComponentAlignment(deleteImage, Alignment.BOTTOM_LEFT);
			layoutDocument.addComponent(downloadImage);
			layoutDocument.setComponentAlignment(downloadImage, Alignment.MIDDLE_CENTER);
			layoutDocument.addComponent(moreInformationImage);
			layoutDocument.setComponentAlignment(moreInformationImage, Alignment.MIDDLE_RIGHT);
		}
		setCompositionRoot(panel);
	}

	/**
	 * Listener for highlight selected row.
	 */
	ClickListener selectPanelListener = new ClickListener() {
		private static final long serialVersionUID = 1L;

		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
			Integer id = (Integer) ((Panel) event.getComponent()).getData();
			table.select(id);
		}
	};


	/**
	 * Listener for deleteImage. Ask confirm for delete document or folder.
	 */
	ClickListener deleteListener = new ClickListener() {
		private static final long serialVersionUID = 1L;

		@Override
		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
			window = new Window("Delete. Are you sure? ");
			window.setResizable(false);
			window.center();
			window.setModal(true);
			window.addStyleName("edit-dashboard");
			UI.getCurrent().addWindow(window);

			VerticalLayout background = new VerticalLayout();

			FormLayout informationLayout = new FormLayout();
			informationLayout.setSizeUndefined();
			informationLayout.setMargin(true);

			GridLayout buttonLayout = new GridLayout(5, 1);
			buttonLayout.setSizeFull();
			buttonLayout.setSpacing(true);
			buttonLayout.setMargin(true);

			yes = new Button("yes");
			yes.addStyleName("default");
			no = new Button("no");
			no.addStyleName("default");
			yes.addClickListener(deleteButtonListener);
			no.addClickListener(deleteButtonListener);
			buttonLayout.addComponent(yes, 1, 0, 2, 0);
			buttonLayout.addComponent(no, 3, 0, 4, 0);
			informationLayout.addComponent(buttonLayout);

			// set footer
			HorizontalLayout footer = new HorizontalLayout();
			footer.setMargin(true);
			footer.setSpacing(true);
			footer.addStyleName("footer");
			footer.setWidth("100%");

			background.addComponent(informationLayout);
			background.addComponent(footer);
			window.setContent(background);
		}
	};

	/**
	 * Listener for button in deleteListener.
	 */
	Button.ClickListener deleteButtonListener = new Button.ClickListener() {
		private static final long serialVersionUID = 1L;

		public void buttonClick(ClickEvent event) {
			if (event.getButton().equals(yes)) {
                // Delete all versions of a document
				if (isFolder) {
                    FolderView folder = client.getFolder(path);
                    FolderView parent = folder.getParent();
                    client.deleteFolder(folder);
                    tree.remove(folder, parent);
				} else {
					DocumentView document = client.getDocument(path);
					client.deleteDocument(document.asDocument());
				}
				table.removeItem(itemId);
			}
			window.close();
		}
	};

	/**
	 * Listener for downloadImage. Starts download of document after select version.
	 */
	ClickListener downloadListener = new ClickListener() {
		private static final long serialVersionUID = 1L;

		@Override
		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
			window = new Window("Select version: ");
			window.setResizable(false);
			window.center();
			window.setModal(true);
			window.addStyleName("edit-dashboard");
			UI.getCurrent().addWindow(window);

			VerticalLayout background = new VerticalLayout();

			FormLayout informationLayout = new FormLayout();
			informationLayout.setSizeUndefined();
			informationLayout.setMargin(true);

			DocumentView document = client.getDocument(path);
			ComboBox versions = new ComboBox();
			versions.setImmediate(true);
			versions.addValueChangeListener(versionListener);
			for (String version : document.getAllVersions()) {
				versions.addItem(version);
			}
			informationLayout.addComponent(versions);

			downloadLink = new Link();
			downloadLink.setCaption("Start download");
			downloadLink.setEnabled(false);
			informationLayout.addComponent(downloadLink);

			// set footer
			HorizontalLayout footer = new HorizontalLayout();
			footer.setMargin(true);
			footer.setSpacing(true);
			footer.addStyleName("footer");
			footer.setWidth("100%");

			background.addComponent(informationLayout);
			background.addComponent(footer);
			window.setContent(background);
		}
	};

	/**
	 * Listener for select version in downloadListener.
	 */
	ValueChangeListener versionListener = new ValueChangeListener() {
		private static final long serialVersionUID = 1L;

		public void valueChange(ValueChangeEvent event) {
			requestedVersion = event.getProperty().getValue().toString();

			if (requestedVersion == "" || requestedVersion == null) {
				requestedVersion = "1.0";
			}
			DocumentView document = client.getDocument(path);
			document = document.getObjectOfVersion(requestedVersion);
			// Create a stream resource
			StreamResource.StreamSource source = new DocumentDownloader(
					document);
			StreamResource resource = new StreamResource(source,
					document.getName());
			// Let the user download the document
			downloadLink.setResource(resource);
			downloadLink.setEnabled(true);
			downloadLink.setTargetName("_blank");
			downloadLink.setVisible(true);
		}
	};

	/**
	 * Listener for moreInformationImage. Show information about document and folder.
	 * Show possibility to add tag.
	 */
	ClickListener moreInformationListener = new ClickListener() {
		private static final long serialVersionUID = 1L;


		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
			window = new Window("More information");
			window.setResizable(false);
			window.center();
			window.setModal(true);
			window.addStyleName("edit-dashboard");
			UI.getCurrent().addWindow(window);

			VerticalLayout background = new VerticalLayout();

			FormLayout informationLayout = new FormLayout();
			informationLayout.setSizeUndefined();
			informationLayout.setMargin(true);
			Label createdBy = new Label((String) table
					.getContainerProperty(itemId, "created by").getValue());
			createdBy.setCaption("created by: ");
			informationLayout.addComponent(createdBy);
			Label createdByOn = new Label(table
					.getContainerProperty(itemId, "created on").getValue()
					.toString());
			createdByOn.setCaption("on: ");
			informationLayout.addComponent(createdByOn);
			Label modifiedBy = new Label((String) table
					.getContainerProperty(itemId, "modified by").getValue());
			modifiedBy.setCaption("modified by: ");
			informationLayout.addComponent(modifiedBy);
			Label modifiedByOn = new Label(table
					.getContainerProperty(itemId, "modified on").getValue()
					.toString());
			modifiedByOn.setCaption("on: ");
			informationLayout.addComponent(modifiedByOn);
			// add information on version only if it is document
			if (client.getFile(path).isDocument()) {
				DocumentView documentView = client.getDocument(path);
				Label latestVersion = new Label(
						documentView.getVersionLabel());
				latestVersion.setCaption("latest version: ");
				informationLayout.addComponent(latestVersion);
			}
			Label pathInformation = new Label(path);
			pathInformation.setCaption("path: ");
			informationLayout.addComponent(pathInformation);

			// Add a tag component if file is a Docuemnt
			if(client.getFile(path).isDocument()){

				tagLayout = new HorizontalLayout();
				tagLayout.setSpacing(true);
				tagLayout.setWidth("100%");
				tagLayout.setCaption("Tag: ");
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

				// find tag and populate table         
				try { 
					FileView file = client.getFile(path); 
					if(file.isDocument()){ 
						tags = (ArrayList<String>) client.getTags(client.getDocument(path).getId()); 
					} 
					else if(file.isFolder()){ 
						tags = (ArrayList<String>) client.getTags(client.getFolder(path).getId()); 
					} 
				} catch (IOException e1) { 
					e1.printStackTrace(); 
				} 

				for(String t : tags){ 
					removeTag = new Image(t, new ThemeResource("img/remove.png")); 
					removeTag.addClickListener(removeTagListener); 
					removeTag.setData(i); 
					tableTag.addItem(new Object[] {t, removeTag},i++); 
				} 

				// Button add
				addTagButton = new Button("Add");
				addTagButton.addStyleName("default");
				tagLayout.addComponent(addTagButton);
				addTagButton.addClickListener(addListener);
				addTagButton.addShortcutListener(enterTag);
			}

			// set footer
			HorizontalLayout footer = new HorizontalLayout();
			footer.setMargin(true);
			footer.setSpacing(true);
			footer.addStyleName("footer");
			footer.setWidth("100%");

			background.addComponent(informationLayout);
			background.addComponent(footer);
			window.setContent(background);

		}
	};

	/**
	 * Listener for addTagButton. Add tag to selected document or folder.
	 */
	Button.ClickListener addListener = new Button.ClickListener() {

		private static final long serialVersionUID = 1L;
		private FileView file; 

		public void buttonClick(ClickEvent event) {
			Boolean exist = false;
			file = client.getFile(path);
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
					removeTag = new Image(addTagTextField.getValue(), new ThemeResource("img/remove.png")); 
					removeTag.addClickListener(removeTagListener); 
					removeTag.setData(i); 
					tableTag.addItem(new Object[] {addTagTextField.getValue(), removeTag},i++);       
					try { 
						client.createTag(addTagTextField.getValue()); 
						String id = null; 
						if(file.isDocument()){ 
							DocumentView document = client.getDocument(path); 
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
	ClickListener removeTagListener = new ClickListener() {

		private static final long serialVersionUID = 1L;
		private Integer id;
		private String tag;

		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
			try {
				//select table row
				id = (Integer) ((Image) event.getComponent()).getData();
				tag = event.getComponent().getCaption();

				// remove tag from item and table
				client.deleteTag(tag);
				tableTag.removeItem(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	// shortcutListener enter: shortcut for add tag
	final ShortcutListener enterTag = new ShortcutListener("", KeyCode.ENTER, null) {
		private static final long serialVersionUID = 1L;

		@Override
		public void handleAction(Object sender, Object target) {
			addTagButton.click();
		}
	};

}