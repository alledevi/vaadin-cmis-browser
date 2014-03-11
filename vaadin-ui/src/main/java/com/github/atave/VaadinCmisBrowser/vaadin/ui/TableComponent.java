package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.chemistry.opencmis.commons.impl.jaxb.GetFolderParent;

import com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient;
import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Form;
import com.vaadin.ui.Image;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

public class TableComponent extends CustomComponent {

	private static final long serialVersionUID = 1L;

	private CmisClient client;

	private Panel panel; 
	private VerticalLayout layout;
	private Table table;
	private Tree tree;

	public TableComponent(String path, CmisClient client, Tree tree) {
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
		table.setColumnAlignment("action", Align.CENTER);
		//reordering column
//		table.setColumnReorderingAllowed(true);		


		//--RIEMPIO TABELLA ROOT--
		path = client.getCurrentFolder().getPath();
		populateTable(path);
//		table.setSortContainerPropertyId("name");
//		table.sort();
		


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
			icon = new Image("00folder", new ThemeResource("img/folder-icon.png"));
			icon.setData(itemId);
			icon.setWidth("34px");
		} else {
			icon = new Image("01document", new ThemeResource("img/document-icon.png"));
			icon.setData(itemId);
			icon.setWidth("34px");
		}
		//open folder
		icon.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
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

	public void populateTableFromComboBox(String path){
		table.removeAllItems();
		if(path == null){
			//display empty table
		} else {
			//populate table only if path is a folder
			if ((client.getFile(path)).isFolder()){
				System.out.println(client.getCurrentFolder().toString());
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

	public void addItemToFolderComponent(String fileId){
		FileView fileView = client.getFile(fileId);
		long creationDateMillis = fileView.getCreationDate().getTimeInMillis();
		Timestamp creationDate = new Timestamp(creationDateMillis);
		long modificationDateMillis = fileView.getLastModificationDate().getTimeInMillis();
		Timestamp modificationDate = new Timestamp(modificationDateMillis);
		int i = table.size() +1;
		if (fileView.isFolder()){
			FolderView file = fileView.asFolder();
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
		} else {
			DocumentView file = fileView.asDocument();
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

		}
	}

	public static String getParentFolder(String path){
		String name = null;

		for(String token : path.split("/")){
			name = token;
		}

		return name;
	}

	ItemClickListener itemListener = new ItemClickListener() {

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
	
//	MouseEvents.ClickListener renameListener = new MouseEvents.ClickListener() {
//
//		private static final long serialVersionUID = 1L;
//
//		@Override
//		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
//			// TODO Auto-generated method stub
//			renameFolder(event.getComponent());
//			
//
//		}};


	public Filterable getContainerDataSource() {
		// TODO Auto-generated method stub
		return (Filterable) table.getContainerDataSource();
	}

	public void sort(Object[] properties, boolean[] ordering) {
		// TODO Auto-generated method stub
		table.sort(properties, ordering);
		
	}

//	public void renameFolder() {
//		table.setEditable(true);
//		DefaultFieldFactory f = null;
//		table.setTableFieldFactory(f);
//		
//	}
	
	public String checkOutVersion(String name){
		String version = "1.0";
		
		for(Object id : table.getItemIds()){
			String value = (String) table.getContainerProperty(id, "name").getValue();
			if (name.equals(value)){
				return "other";
			}
		}	
		return "1.0";
	}


}

