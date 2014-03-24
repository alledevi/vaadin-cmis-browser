package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
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

public class TableComponent extends CustomComponent {

	private static final long serialVersionUID = 1L;

	private AlfrescoClient client;

	private Panel panel; 
	private VerticalLayout layout;
	private Table table;
	private Tree tree;

	public TableComponent(String path, AlfrescoClient client, Tree tree) {
		this.client = client;
		this.tree = tree;

		//Layout
		panel = new Panel();
		layout = new VerticalLayout();
		panel.setContent(layout);

		table = new Table();
		table.setPageLength(table.size()); 
		table.setSizeFull();
		table.addStyleName("borderless");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		//add property
		table.addContainerProperty("image", Image.class, null);
		table.addContainerProperty("name", String.class, null);
		table.addContainerProperty("description", String.class, null);
		table.addContainerProperty("created on", Timestamp.class, null);
		table.addContainerProperty("modified on", Timestamp.class, null);
		table.addContainerProperty("created by", String.class, null);
		table.addContainerProperty("modified by", String.class, null);
		table.addContainerProperty("path", String.class, null);
		table.addContainerProperty("isFolder", Integer.class, null);
		table.addContainerProperty("action", TableActionComponent.class, null);
		//set cell alignment
		table.setColumnAlignment("image", Align.CENTER);
		table.setColumnAlignment("name", Align.CENTER);
		table.setColumnAlignment("description", Align.CENTER);
		table.setColumnAlignment("created on", Align.CENTER);
		table.setColumnAlignment("modified on", Align.CENTER);
		table.setColumnAlignment("created by", Align.CENTER);
		table.setColumnAlignment("modified by", Align.CENTER);
		table.setColumnAlignment("path", Align.CENTER);
		table.setColumnAlignment("isFolder", Align.CENTER);
		table.setColumnAlignment("action", Align.CENTER);
		
		//--RIEMPIO TABELLA ROOT--
		path = client.getCurrentFolder().getPath();
		populateTable(path);


		//hide some column
		table.setColumnCollapsingAllowed(true);
		table.setColumnCollapsed("created by", true);
		table.setColumnCollapsed("modified by", true);
		table.setColumnCollapsed("path", true);
		table.addItemClickListener(itemListener);  
		
		layout.addComponent(table);
		setCompositionRoot(panel);
	}




	//select right icon
	public Image getFolderIcon(Boolean isFolder, final String fileId, Integer itemId) {
		Image icon = null;
		if (isFolder) {
			icon = new Image("a", new ThemeResource("img/folder-icon.png"));
			icon.setData(itemId);
			icon.setWidth("34px");
		} else {
			
//			String documentPath = "/path/to/my/document";
//			String documentId = client.getDocument(documentPath).getId();

			icon = new Thumbnail(client, fileId);
			
//			icon = new Thumbnail("document", new ThemeResource("img/document-icon.png"));
			icon.setData(itemId);
			icon.setWidth("34px");
		}
		//open folder
		icon.addClickListener( new MouseEvents.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
				// select row
				Integer id = (Integer)((Image) event.getComponent()).getData();
				table.select(id);
				//open folder
				if(event.isDoubleClick() ){
					//					Object item =  ((ItemClickEvent) event).getItemId();

					String path = ((String) table.getContainerProperty(id, "path").getValue());
					FileView fileView = client.getFile(path);
					if(fileView.isFolder()){
						client.navigateTo(path);
						HomeView.createTree(tree, getParentFolder(fileView.getPath()));
						tree.expandItem(getParentFolder(fileView.getPath()));
						populateTable(fileView.getPath());
					}




				}
			}
		});
		return icon;
	}
	
	public void clearTable(){
		table.removeAllItems();
	}
	
	public void pageLength(){
		table.setPageLength(10);;
	}

	public void populateTable(String path){
		table.removeAllItems();
		if(path == null){
			//display empty table
		} else {
			//populate table only if path is a folder
			if ((client.getFile(path)).isFolder()){
				client.navigateTo(path);
				FolderView currentFolder = client.getCurrentFolder();
				Collection<FileView> files = currentFolder.getChildren();
				int i = 0;
				for(FileView file : files){
					long creationDateMillis = file.getCreationDate().getTimeInMillis();
					Timestamp creationDate = new Timestamp(creationDateMillis);
					long modificationDateMillis = file.getLastModificationDate().getTimeInMillis();
					Timestamp modificationDate = new Timestamp(modificationDateMillis);
					if(file.isDocument()){
						table.addItem(
								new Object[] {
										getFolderIcon(false, file.getId(), i),
										file.getName(),
										file.getDescription(),
										creationDate,
										modificationDate, 
										file.getCreatedBy(),
										file.getLastModifiedBy(),
										file.getPath(),
										1,
										new TableActionComponent(file.getPath(), i, table, client, false)},
										i);
						
					}else{
						table.addItem(
								new Object[] {
										getFolderIcon(true, file.getId(), i),
										file.getName(),
										file.getDescription(),
										creationDate,
										modificationDate,
										file.getCreatedBy(),
										file.getLastModifiedBy(),
										file.getPath(),
										0,
										new TableActionComponent(file.getPath(), i, table, client, true)},
										i);
					}	
					++i;
				}
			}
		}
		IndexedContainer c = (IndexedContainer) table.getContainerDataSource();
		CaseInsensitiveItemSorter d = new CaseInsensitiveItemSorter();
		c.setItemSorter(d);
		boolean[] b = {true, true};
		Object[] o = {"isFolder","name"};
		c.sort(o, b);
		table.setContainerDataSource(c);


	}
//	
//	public void sortTable(Object[] propertyId, boolean[] ascending)
//            throws UnsupportedOperationException {
//        
//		final Container c = table.getContainerDataSource();
//        if (c instanceof Container.Sortable) {
//            final int pageIndex = table.getCurrentPageFirstItemIndex();
//            boolean refreshingPreviouslyEnabled = false;
//            ((Container.Sortable) c).sort(propertyId, ascending);
//            table.setCurrentPageFirstItemIndex(pageIndex);
//            if (refreshingPreviouslyEnabled) {
//                (Table.class).refreshRenderedCells();
//                // Ensure that client gets a response
//                markAsDirty();
//            }
//
//        } else if (c != null) {
//            throw new UnsupportedOperationException(
//                    "Underlying Data does not allow sorting");
//        }
//    }
	
//	public void sortTable(){
//		
//		Collection<Item> folders = null;
//		Collection<Item> documents = null;
//		for(Object i : table.getItemIds()){
//			Image image = (Image) table.getContainerProperty(i, "image").getValue();
//			if(image.getCaption().equals("folder")){
//				folders.add(table.getItem(i));
//			}
//			else
//				documents.add(table.getItem(i));	
//		}
//		
//		ArrayList<String> list = new ArrayList<String>();
//		table.
//		
//		folders.
//		
//
//
//	}

	public void populateTableFromComboBox(String path){
		table.removeAllItems();
		if(path == null){
			//display empty table
		} else {
			//populate table only if path is a folder
			if ((client.getFile(path)).isFolder()){
				FolderView currentFolder = client.getCurrentFolder();
				Collection<FileView> files = currentFolder.getChildren();
				int i = 0;
				for(FileView file : files){
					long creationDateMillis = file.getCreationDate().getTimeInMillis();
					Timestamp creationDate = new Timestamp(creationDateMillis);
					long modificationDateMillis = file.getLastModificationDate().getTimeInMillis();
					Timestamp modificationDate = new Timestamp(modificationDateMillis);
					if(file.isDocument()){
						table.addItem(
								new Object[] {
										getFolderIcon(false, file.getId(), i),
										file.getName(),
										file.getDescription(),
										creationDate,
										modificationDate, 
										file.getCreatedBy(),
										file.getLastModifiedBy(),
										file.getPath(),
										new TableActionComponent(file.getPath(), i, table, client, false)},
										i);
						
					}else{
						table.addItem(
								new Object[] {
										getFolderIcon(true, file.getId(), i),
										file.getName(),
										file.getDescription(),
										creationDate,
										modificationDate,
										file.getCreatedBy(),
										file.getLastModifiedBy(),
										file.getPath(),
										new TableActionComponent(file.getPath(), i, table, client, true)},
										i);
					}	
					++i;
				}
			}
		}
	}

    public void addItemToFolderComponent(String idOrPath) {
        FileView file = client.getFile(idOrPath);
        addItemToFolderComponent(file);
    }

	public void addItemToFolderComponent(FileView file){
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

		int i = table.size() +1;
        table.setImmediate(true);

        table.addItem(new Object[] {
                getFolderIcon(isFolder, file.getId(), i),
                file.getName(),
                file.getDescription(),
                creationDate,
                modificationDate,
                file.getCreatedBy(),
                file.getLastModifiedBy(),
                file.getPath(),
                isFolder ? 0 : 1,
                new TableActionComponent(file.getPath(), i, table, client, isFolder)
        }, i);
    }

	public static String getParentFolder(String path){
		String name = null;

		for(String token : path.split("/")){
			name = token;
		}

		return name;
	}

	ItemClickListener itemListener = new ItemClickListener() {

		private static final long serialVersionUID = 1L;

		@Override
		public void itemClick(ItemClickEvent event) {

			if (event.isDoubleClick())    {
				Object item =  event.getItemId();

				String path = ((String) table.getContainerProperty(item, "path").getValue());
				FileView fileView = client.getFile(path);
				if(fileView.isFolder()){
					client.navigateTo(path);
					HomeView.createTree(tree, getParentFolder(fileView.getPath()));
					tree.expandItem(getParentFolder(fileView.getPath()));
					populateTable(fileView.getPath());
				}
			}


		}
	};

	public Filterable getContainerDataSource() {
		return (Filterable) table.getContainerDataSource();
	}

	public void sort(Object[] properties, boolean[] ordering) {
		table.sort(properties, ordering);

	}

	public String checkOutVersion(String name){
		for(Object id : table.getItemIds()){
			String value = (String) table.getContainerProperty(id, "name").getValue();
			if (name.equals(value)){
				return "other";
			}
		}	
		return "1.0";
	}



}

