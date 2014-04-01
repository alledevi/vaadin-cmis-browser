package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table.Align;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Custom tag component
 *
 * @param path   the parentPath of the file
 * @param name   the name of the file
 * @param client Alfresco client
 */

public class Tag extends CustomComponent {

    private static final long serialVersionUID = 1L;
    private static final String tagWidth = "100px";
    private static final String removeImage = "img/remove.png";

    // AlfrescoClient
    private AlfrescoClient client;

    private Panel background;
    private VerticalLayout layout;

    // PathParent and PathFile
    private String pathParent;
    private String pathFile;

    // tagLayout
    private HorizontalLayout tagLayout;
    private TextField addTagTextField;
    private Button addTagButton;

    private ArrayList<String> tags = new ArrayList<String>();
    private Table tableTag;
    private Integer i = 0;
    private Image removeTag;

    @SuppressWarnings("deprecation")
    public Tag(final String path, final String name, final AlfrescoClient client) {
        this.pathParent = path;
        this.client = client;
        this.pathFile = (pathParent.endsWith("/") ? pathParent : pathParent + "/") + name;

        // panel for add tag
        background = new Panel();

        // layout: tagLayout + Table tableTag
        layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(true);

        // tagLayout: TextField tf + Button add
        tagLayout = new HorizontalLayout();
        tagLayout.setMargin(false);
        tagLayout.setSpacing(true);
        tagLayout.setWidth(tagWidth);
        tagLayout.setCaption("Tag: ");
        layout.addComponent(tagLayout);

        // TextField tf
        addTagTextField = new TextField();
        addTagTextField.setInputPrompt("Add new tag");
        tagLayout.addComponent(addTagTextField);

        // Button add
        addTagButton = new Button("Add");
        addTagButton.addStyleName("default");
        tagLayout.addComponent(addTagButton);

        // Table tableTag
        tableTag = new Table();
        tableTag.setSelectable(false);
        tableTag.setImmediate(true);
        tableTag.addContainerProperty("name", String.class, null);
        tableTag.addContainerProperty("image", Image.class, null);
        tableTag.setColumnAlignment("image", Align.CENTER);
        tableTag.setColumnAlignment("name", Align.CENTER);
        tableTag.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
        tableTag.setWidth(tagWidth);
        tableTag.addStyleName("borderless");
        tableTag.setPageLength(5);
        layout.addComponent(tableTag);

        //button for remove tag
        removeTag = new Image(null, new ThemeResource(removeImage));
        removeTag.addClickListener(removeTagListener);

        // find tag and populate table
        if (client.exists(pathParent, name)) {
            try {
                FileView file = client.getFile(pathFile);
                if (file.isDocument()) {
                    tags = (ArrayList<String>) client.getTags(client.getDocument(pathFile).getId());
                } else if (file.isFolder()) {
                    tags = (ArrayList<String>) client.getTags(client.getFolder(pathFile).getId());
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

        // shortcutListener enter: shortcut for add tag
        final ShortcutListener enter = new ShortcutListener("", KeyCode.ENTER, null) {
            private static final long serialVersionUID = 1L;

            @Override
            public void handleAction(Object sender, Object target) {
                addTagButton.click();
            }
        };

        addTagButton.addClickListener(addListener);
        addTagButton.addShortcutListener(enter);

        background.setContent(layout);
        setCompositionRoot(background);
    }

    /**
     * Listener for addTagButton. Add tag to selected document or folder.
     */
    Button.ClickListener addListener = new Button.ClickListener() {

        private static final long serialVersionUID = 1L;
        private FileView file;
        private String id = null;
        private DocumentView document;
        private FolderView folder;

        public void buttonClick(ClickEvent event) {
            if (!addTagTextField.getValue().equals("")) {
                removeTag = new Image(addTagTextField.getValue(), new ThemeResource(removeImage));
                removeTag.addClickListener(removeTagListener);
                removeTag.setData(i);
                tableTag.addItem(new Object[]{addTagTextField.getValue(), removeTag}, i++);
                try {
                    client.createTag(addTagTextField.getValue());
                    file = client.getFile(pathFile);
                    // file is a document
                    if (file.isDocument()) {
                        document = client.getDocument(pathFile);
                        id = document.getId();
                    }
                    // file is a folder
                    else if (file.isFolder()) {
                        folder = client.getFolder(pathFile);
                        id = folder.getId();
                    }
                    client.addTags(id, Collections.singleton(addTagTextField.getValue()));
                } catch (IOException e) {
                    e.printStackTrace();
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
}
