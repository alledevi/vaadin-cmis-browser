package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.CmisTree;
import com.github.atave.VaadinCmisBrowser.vaadin.utils.Thumbnail;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Table.Align;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

import java.sql.Timestamp;
import java.util.Collection;

/**
 * Custom table component
 *
 * @param tree   the tree in home view associate with table
 * @param client Alfresco client
 */

public class TableComponent extends CustomComponent {

    private static final long serialVersionUID = 1L;
    private static final String imageWidth = "34px";
    private static final String folderImagePath = "img/folder-icon.png";

    private final AlfrescoClient client;
    private final CmisTree tree;
    private final Table table;

    public TableComponent(AlfrescoClient client, CmisTree tree) {
        this.client = client;
        this.tree = tree;

        Panel panel = new Panel();
        VerticalLayout layout = new VerticalLayout();
        panel.setContent(layout);

        table = new Table();
        table.setSizeFull();
        table.addStyleName("borderless");
        table.setSelectable(true);
        table.setMultiSelect(false);
        table.setImmediate(true);

        //add property to table
        table.addContainerProperty("image", Image.class, null);
        table.addContainerProperty("name", String.class, null);
        table.addContainerProperty("created on", Timestamp.class, null);
        table.addContainerProperty("modified on", Timestamp.class, null);
        table.addContainerProperty("created by", String.class, null);
        table.addContainerProperty("modified by", String.class, null);
        table.addContainerProperty("path", String.class, null);
        table.addContainerProperty("isFolder", Integer.class, null);
        table.addContainerProperty("action", TableActionComponent.class, null);

        //set table cell alignment
        table.setColumnAlignment("image", Align.CENTER);
        table.setColumnAlignment("name", Align.CENTER);
        table.setColumnAlignment("created on", Align.CENTER);
        table.setColumnAlignment("created by", Align.CENTER);
        table.setColumnAlignment("modified on", Align.CENTER);
        table.setColumnAlignment("modified by", Align.CENTER);
        table.setColumnAlignment("path", Align.CENTER);
        table.setColumnAlignment("isFolder", Align.CENTER);
        table.setColumnAlignment("action", Align.CENTER);
        table.setPageLength(table.size());

        //set table column expand ratio
        table.setColumnExpandRatio("name", 4);
        table.setColumnExpandRatio("created on", 3);
        table.setColumnExpandRatio("modified on", 3);
        table.setColumnExpandRatio("action", 2);

        //build table from current folder
        String path = client.getCurrentFolder().getPath();
        populateTable(path);

        //hide unused column
        table.setColumnCollapsingAllowed(true);
        table.setColumnCollapsed("created by", true);
        table.setColumnCollapsed("modified by", true);
        table.setColumnCollapsed("path", true);
        table.setColumnCollapsed("isFolder", true);
        table.addItemClickListener(itemListener);

        layout.addComponent(table);
        setCompositionRoot(panel);
    }


    /**
     * Set right icon to file view in a row.
     *
     * @param isFolder true if image refers to folder, false otherwise
     * @param fileId   id of file witch image refers to
     * @param itemId   table row
     */
    public Image getFolderIcon(Boolean isFolder, final String fileId, Integer itemId) {
        Image icon;
        if (isFolder) {
            icon = new Image("a", new ThemeResource(folderImagePath));
            icon.setData(itemId);
            icon.setWidth(imageWidth);
        } else {
            icon = new Thumbnail(client, fileId);
            icon.setData(itemId);
            icon.setWidth(imageWidth);
        }
        icon.addClickListener(iconListener);
        return icon;
    }

    /**
     * Listener for icon. Highlight selected row. If double click on a folder, open it.
     */
    private final MouseEvents.ClickListener iconListener = new MouseEvents.ClickListener() {
        private static final long serialVersionUID = 1L;

        public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
            // select row
            Integer itemId = (Integer) ((Image) event.getComponent()).getData();
            table.select(itemId);
            //open folder
            if (event.isDoubleClick()) {
                String path = ((String) table.getContainerProperty(itemId, "path").getValue());
                FileView fileView = client.getFile(path);
                if (fileView.isFolder()) {
                    tree.show(fileView.asFolder());
                    client.navigateTo(path);
                    populateTable(fileView.getPath());
                }
            }
        }
    };

    /**
     * Listener for row. If double click on a folder, open it.
     */
    private final ItemClickListener itemListener = new ItemClickListener() {
        private static final long serialVersionUID = 1L;

        public void itemClick(ItemClickEvent event) {
            if (event.isDoubleClick()) {
                Object itemId = event.getItemId();
                String path = ((String) table.getContainerProperty(itemId, "path").getValue());
                FileView fileView = client.getFile(path);
                if (fileView.isFolder()) {
                    tree.show(fileView.asFolder());
                    client.navigateTo(path);
                    populateTable(fileView.getPath());
                }
            }


        }
    };

    /**
     * Return empty table.
     */
    public void clearTable() {
        table.removeAllItems();
    }

    /**
     * Override defined page length.
     *
     * @param n number of visible row
     */
    public void pageLength(int n) {
        table.setPageLength(n);
    }


    /**
     * Populate table with element of a folder.
     *
     * @param path path of folder
     */
    public void populateTable(String path) {
        table.removeAllItems();

        if (path != null) {
            //populate table only if path is a folder
            if ((client.getFile(path)).isFolder()) {
                client.navigateTo(path);
                FolderView currentFolder = client.getCurrentFolder();
                Collection<FileView> files = currentFolder.getChildren();
                int i = 0;
                for (FileView file : files) {
                    long creationDateMillis = file.getCreationDate().getTimeInMillis();
                    Timestamp creationDate = new Timestamp(creationDateMillis);
                    long modificationDateMillis = file.getLastModificationDate().getTimeInMillis();
                    Timestamp modificationDate = new Timestamp(modificationDateMillis);
                    if (file.isDocument()) {
                        table.addItem(
                                new Object[]{
                                        getFolderIcon(false, file.getId(), i),
                                        file.getName(),
                                        creationDate,
                                        modificationDate,
                                        file.getCreatedBy(),
                                        file.getLastModifiedBy(),
                                        file.getPath(),
                                        1,
                                        new TableActionComponent(tree, file.getPath(), i, table, client, false)},
                                i
                        );

                    } else {
                        table.addItem(
                                new Object[]{
                                        getFolderIcon(true, file.getId(), i),
                                        file.getName(),
                                        creationDate,
                                        modificationDate,
                                        file.getCreatedBy(),
                                        file.getLastModifiedBy(),
                                        file.getPath(),
                                        0,
                                        new TableActionComponent(tree, file.getPath(), i, table, client, true)},
                                i
                        );
                    }
                    ++i;
                }
            }
        }

        sortTable();

    }

    /**
     * Add one item to table.
     *
     * @param idOrPath path or id of item
     */
    public void addItemToTableComponent(String idOrPath) {
        FileView file = client.getFile(idOrPath);
        addItemToTableComponent(file);
    }

    /**
     * Add one item to table.
     *
     * @param file fileView of item
     */
    public void addItemToTableComponent(FileView file) {
        // Globally filter files shown
        String path = file.getPath();
        String objectTypeId = file.getProperty(PropertyIds.OBJECT_TYPE_ID);
        boolean isFolder = file.isFolder();

        if (path == null || !(isFolder || objectTypeId.equals(BaseTypeId.CMIS_DOCUMENT.value()))) {
            return;
        }
        // Add the file
        long creationDateMillis = file.getCreationDate().getTimeInMillis();
        Timestamp creationDate = new Timestamp(creationDateMillis);
        long modificationDateMillis = file.getLastModificationDate().getTimeInMillis();
        Timestamp modificationDate = new Timestamp(modificationDateMillis);
        int rowIndex = table.size() + 1;
        table.setImmediate(true);

        table.addItem(new Object[]{
                getFolderIcon(isFolder, file.getId(), rowIndex),
                file.getName(),
                creationDate,
                modificationDate,
                file.getCreatedBy(),
                file.getLastModifiedBy(),
                file.getPath(),
                isFolder ? 0 : 1,
                new TableActionComponent(tree, file.getPath(), rowIndex, table, client, isFolder)
        }, rowIndex);

        table.refreshRowCache();
        sortTable();
    }

    /**
     * Returns filterable container.
     */
    public Filterable getContainerDataSource() {
        return (Filterable) table.getContainerDataSource();
    }

    /**
     * Returns table sorted for isFolder field.
     */
    public void sortTable() {
        IndexedContainer container = (IndexedContainer) table.getContainerDataSource();
        CaseInsensitiveItemSorter d = new CaseInsensitiveItemSorter();
        container.setItemSorter(d);
        boolean[] b = {true, true};
        Object[] o = {"isFolder", "name"};
        container.sort(o, b);
        table.setContainerDataSource(container);
        table.setColumnCollapsingAllowed(true);
        table.setColumnCollapsed("created by", true);
        table.setColumnCollapsed("modified by", true);
        table.setColumnCollapsed("path", true);
        table.setColumnCollapsed("isFolder", true);
    }

}
