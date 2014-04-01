package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.DocumentUploader;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.StringUtils;
import com.vaadin.data.Container.Filterable;
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

    // AlfrescoClient 
    private static AlfrescoClient client;

    // TopLayout 
    private HorizontalLayout topLayout;
    private Image logo;
    private Label title;

    // MiddleLayout 
    private HorizontalLayout middleLayout;
    private Image addFolderImage;
    private Image uploadImage;
    private TextField filter;

    // BottomLayout 
    private HorizontalSplitPanel bottomLayout;
    private TableComponent table;
    private Tree tree;
    private static List<String> treeItemExpanded;

    public HomeView() {

        setSizeFull();
        setMargin(true);
        addStyleName("dashboard-view");

        client = ((AppUI) UI.getCurrent()).getClient();

        // TOPLAYOUT: Image logo + Label title 
        topLayout = new HorizontalLayout();
        topLayout.addStyleName("toolbar");
        topLayout.setWidth(layoutWidth);
        topLayout.setSpacing(true);
        addComponent(topLayout);

        // Image logo 
        logo = new Image(null, new ThemeResource(logoImagePath));
        logo.setHeight(logoSize);
        logo.setWidth(logoSize);
        topLayout.addComponent(logo);
        topLayout.setComponentAlignment(logo, Alignment.TOP_LEFT);

        // Label title 
        title = new Label("My Alfresco");
        title.addStyleName("h1");
        title.setSizeUndefined();
        topLayout.addComponent(title);
        topLayout.setComponentAlignment(title, Alignment.TOP_LEFT);
        topLayout.setExpandRatio(title, 1);

        // MIDDLELAYOUT: Image addFolderImage + Image uploadImage + TextField filter  
        middleLayout = new HorizontalLayout();
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
        filter = new TextField();
        filter.setWidth(filterWidth);
        filter.focus();
        filter.setInputPrompt("Filter");
        filter.addListener(filterListener);
        middleLayout.addComponent(filter);
        middleLayout.setComponentAlignment(filter, Alignment.MIDDLE_RIGHT);

        // BOTTOMLAYOUT: Tree tree + TableComponent table  
        bottomLayout = new HorizontalSplitPanel();
        bottomLayout.setStyleName("toolbar");
        bottomLayout.setSizeFull();
        bottomLayout.setSplitPosition(15, Sizeable.UNITS_PERCENTAGE);
        addComponent(bottomLayout);
        setExpandRatio(bottomLayout, 3);

        // Tree tree 
        tree = new Tree();
        tree.setSizeFull();
        createTree(tree);
        tree.setSelectable(false);
        tree.addListener(treeListener);
        bottomLayout.setFirstComponent(tree);

        // TableComponent table 
        table = new TableComponent(client, tree);
        table.setImmediate(true);
        bottomLayout.setSecondComponent(table);
    }

    /**
     * Listener for filter texfield. returns Documents and Folders filter by "Name"
     */
    TextChangeListener filterListener = new TextChangeListener() {

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
    ClickListener addFolderListener = new ClickListener() {

        private static final long serialVersionUID = 1L;

        private TextField name;
        private Window window;
        private Button addFolder;

        public void buttonClick(ClickEvent event) {

            window.close();

            // Add a new folder to table
            if (name.getValue() != null && event.getButton() == addFolder) {
                FolderView newFolder;
                String path = client.getCurrentFolder().getPath();
                if (!client.exists(path, name.getValue())) {
                    newFolder = client.createFolder(path, name.getValue());
                } else {
                    String regexName = name.getValue();
                    do {
                        regexName = StringUtils.renameFolder(regexName);
                    } while (client.exists(path + regexName));

                    newFolder = client.createFolder(path, regexName);
                }
                table.addItemToTableComponent(newFolder);
            }
        }
    };


    /**
     * Listener upload document and add folder image. Open window dialog.
     */
    MouseEvents.ClickListener actionListener = new MouseEvents.ClickListener() {

        private static final long serialVersionUID = 1L;

        private Window window;
        private VerticalLayout background;
        private FormLayout informationLayout;
        private HorizontalLayout footer;
        private GridLayout gridLayout;
        private Button close;

        // addFolder 
        private TextField name;
        private Button addFolder;
        private TextArea description;

        //upload 
        private OptionGroup v;
        private FolderView folderView;
        private String path;
        private DocumentUploader receiver;
        private HorizontalLayout uploadHorizontal;
        private Upload upload;
        private Label textualProgress;
        private ProgressIndicator progressBar;
        private Image info;
        private Label state;
        private Label fileName;

        public void click(com.vaadin.event.MouseEvents.ClickEvent event) {

            // addFolderImage 
            if (event.getComponent().equals(addFolderImage)) {

                window = new Window("Add new folder");
                window.setResizable(false);
                window.center();
                window.addStyleName("edit-dashboard");
                UI.getCurrent().addWindow(window);

                background = new VerticalLayout();

                informationLayout = new FormLayout();
                informationLayout.setSizeUndefined();
                informationLayout.setMargin(true);

                name = new TextField("Name");
                name.setRequired(true);
                name.setSizeFull();
                informationLayout.addComponent(name);

                description = new TextArea("Description");
                description.setSizeFull();
                informationLayout.addComponent(description);

                gridLayout = new GridLayout(5, 1);
                gridLayout.setSpacing(true);
                gridLayout.setMargin(true);

                addFolder = new Button("Add");
                addFolder.addClickListener(addFolderListener);
                addFolder.addStyleName("default");

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
                informationLayout.setSizeUndefined();
                informationLayout.setMargin(true);

                uploadHorizontal = new HorizontalLayout();
                uploadHorizontal.addStyleName("upload-layout");

                upload = new Upload("", receiver);
                upload.setImmediate(false);
                upload.setButtonCaption("Start upload");
                upload.addSucceededListener(receiver);
                uploadHorizontal.addComponent(upload);

                v = new OptionGroup("Select Version");
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
                info.setDescription("in minor, in major");
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
                progressBar.setVisible(false);
                informationLayout.addComponent(progressBar);

                // textual progress  
                textualProgress = new Label();
                informationLayout.addComponent(textualProgress);

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
                    private Tag t;

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
                        System.out.println("path upload: " + path);
                        t = new Tag(path, event.getFilename(), client);
                        informationLayout.addComponent(t);
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
     * Listener for tree. Highlight selected item. Populate table from selected item.
     */
    ItemClickListener treeListener = new ItemClickEvent.ItemClickListener() {

        private static final long serialVersionUID = 1L;

        String itemId = null;
        String path = null;


        public void itemClick(ItemClickEvent event) {
            // Pick only left mouse clicks 
            itemId = (String) event.getItemId();
            if (event.getButton() == ItemClickEvent.BUTTON_LEFT) {
                path = getTreePath(tree, itemId);
                //update folderComponent + tree 
                table.populateTable(path);
                updateTree(tree, itemId);
            }
            tree.select(itemId);
        }
    };

    /**
     * Create tree.
     *
     * @param tree tree to populate
     */
    public static void createTree(Tree tree) {
        //add root 
        treeItemExpanded = new ArrayList<>();
        tree.addItem("Company Home");

        //create hierarchy from current folder 
        String path = client.getCurrentFolder().getPath();
        if (path.equals("/")) {
            tree.select(client.getCurrentFolder().getName());
            tree.expandItem(client.getCurrentFolder().getId());
        }
        ArrayList<FolderView> parents = StringUtils.getAllParentFolder(path, client);
        for (FolderView parent : parents) {
            createTree(tree, parent.getName(), parent);
        }
    }

    /**
     * Add hierarchy from current folder.
     *
     * @param tree       tree to populate
     * @param parentName name of parent node in the tree
     * @param currentFolder current folder to populate
     */
    public static void createTree(Tree tree, String parentName, FolderView currentFolder) {
        Collection<FileView> currentFiles = currentFolder.getChildren();
        for (FileView file : currentFiles) {
            if (file.isFolder()) {
                tree.addItem(file.getName());
                tree.setParent(file.getName(), parentName);
                if (((FolderView) file).getChildren().isEmpty()) {
                    tree.setChildrenAllowed(file.getName(), false);
                }
            }
            //highlight current folder in tree 
            tree.select(currentFolder.getName());
        }
        if (treeItemExpanded.contains(parentName)) {
            //if expanded, collapse 
            tree.collapseItem(parentName);
            treeItemExpanded.remove(parentName);
        } else {
            tree.expandItem(parentName);
            treeItemExpanded.add(parentName);
        }

    }

    /**
     * Update tree from table or from tree listener.
     * Tree is already create.
     *
     * @param tree       tree to populate
     * @param parentName name of parent node in the tree
     */
    public static void updateTree(Tree tree, String parentName) {
        createTree(tree, parentName, client.getCurrentFolder());
    }

    /**
     * Return path of folder selected.
     *
     * @param tree   tree selected
     * @param itemId name of selected node
     */
    public static String getTreePath(Tree tree, String itemId) {
        if (itemId.equals("Company Home")) {
            return "/";
        }
        String path = "/" + itemId;
        while (!tree.getParent(itemId).equals("Company Home") && tree.getParent(itemId) != null) {
            itemId = (String) tree.getParent(itemId);
            path = "/" + itemId + path;
        }
        return path;
    }

    @Override
    public void enter(ViewChangeEvent event) {

    }
}
