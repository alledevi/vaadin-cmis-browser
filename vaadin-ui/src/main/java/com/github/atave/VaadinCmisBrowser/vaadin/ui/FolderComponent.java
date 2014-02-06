package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;

import com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient;
import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.google.gwt.core.client.Callback;
import com.vaadin.ui.Image;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

public class FolderComponent extends CustomComponent {
	public final Table table;
	public CmisClient client;
//	public Callback clickHandler;
	
	public FolderComponent(String path, CmisClient client) {
		this.client = client;
		
//		this.addListener(listener);
//		new LayoutEvents.LayoutClickListener() { 
//			public void layoutClick(LayoutEvents.LayoutClickEvent event) {
//			clickHandler.execute(event);
//			}
//		});
		

		// A layout structure used for composition
		Panel panel = new Panel();
		VerticalLayout layout = new VerticalLayout();
		panel.setContent(layout);

		table = new Table();
		// table.setPageLength(table.size()); //ok
		table.setSizeFull();
		table.addStyleName("borderless");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		// table.setContainerDataSource(ExampleUtil.getISO3166Container());
		// table.setVisibleColumns(new Object[] {
		// ExampleUtil.iso3166_PROPERTY_NAME,
		// ExampleUtil.iso3166_PROPERTY_SHORT });
		table.addContainerProperty("image", Image.class, null);
		table.addContainerProperty("panel", Panel.class, null);
		table.addContainerProperty("download", DownloadDocumentComponent.class, null);
		table.setColumnAlignment("image", Align.CENTER);
		table.setColumnAlignment("panel", Align.CENTER);
		table.setColumnAlignment("download", Align.CENTER);
		// nasconde header tabella
		table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		// riordino delle colonne
		table.setColumnReorderingAllowed(true);
		// possibilità di aggiungere o togliere colonne a piacere
		table.setColumnCollapsingAllowed(false);

		//--RIEMPIO TABELLA ROOT--
		path = client.getCurrentFolder().getPath();
		System.out.println("folderComponent:" + path);
		populateTable(path);

		// table.setColumnAlignment(ExampleUtil.iso3166_PROPERTY_SHORT,
		// Align.CENTER);
		// table.setColumnWidth("panel", 700);
		// table.setColumnWidth("button", 200);

		// table.setColumnExpandRatio("button", 1);
		// table.setColumnWidth(ExampleUtil.iso3166_PROPERTY_SHORT, 70);
		//
		// table.setRowHeaderMode(RowHeaderMode.ICON_ONLY);
		// table.setItemIconPropertyId(ExampleUtil.iso3166_PROPERTY_FLAG);
		//
		// final Action actionMark = new Action("Mark");
		// final Action actionUnmark = new Action("Unmark");

		// table.addActionHandler(new Action.Handler() {
		// @Override
		// public Action[] getActions(final Object target, final Object sender)
		// {
		// if (markedRows.contains(target)) {
		// return new Action[] { actionUnmark };
		// } else {
		// return new Action[] { actionMark };
		// }
		// }

		// @Override
		// public void handleAction(final Action action, final Object sender,
		// final Object target) {
		// if (actionMark == action) {
		// markedRows.add(target);
		// } else if (actionUnmark == action) {
		// markedRows.remove(target);
		// }
		// table.markAsDirtyRecursive();
		// Notification.show("Marked rows: " + markedRows,
		// Type.TRAY_NOTIFICATION);
		// }
		//
		// });

		// table.setCellStyleGenerator(new CellStyleGenerator() {
		// @Override
		// public String getStyle(final Table source, final Object itemId,
		// final Object propertyId) {
		// String style = null;
		// if (propertyId == null && markedRows.contains(itemId)) {
		// // no propertyId, styling a row
		// style = "marked";
		// }
		// return style;
		// }
		//
		// });

		// TODO aggiungere scroll
		table.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(final ValueChangeEvent event) {
				final Object itemId = event.getProperty().getValue();
				// // ((Panel) table.getContainerProperty(itemId,
				// "panel")).setHeight("200px");;
				//				 table.getContainerProperty(itemId, "panel");
				// Notification.show("Value changed:" +
				// table.getContainerProperty(itemId, "panel").getValue() ,
				// Type.TRAY_NOTIFICATION);
			}
		});

		layout.addComponent(table);

		// Set the size as undefined at all levels
		// panel.getContent().setSizeFull();
		// panel.setSizeFull();
		// setSizeFull();

		// The composition root MUST be set
		setCompositionRoot(panel);

	}

	public Panel getInformationPanel(String name, GregorianCalendar calendar, String author, final String fileId) {
		final Panel panel = new Panel();
		final VerticalLayout layout = new VerticalLayout();

		LayoutClickListener listener = new  LayoutClickListener() {
			private static final long serialVersionUID = 5527999180793601282L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.isDoubleClick()){
					FileView fileView = client.getFile(fileId);
					if(fileView.isFolder()){
						populateTable(fileView.getPath());
					}
				} else {
					table.select(fileId);
					Notification.show("CLICCATO una volta ");
				}
			}
		};

		layout.addLayoutClickListener(listener);
		panel.setContent(layout);
		long millis = calendar.getTimeInMillis();
		Timestamp date = new Timestamp(millis);
		layout.addComponent(new Label("nome: " + name));
		layout.addComponent(new Label("modificato il: " + date));
		layout.addComponent(new Label("autore: " + author));
		// image for arrow
		final Image imageDown = new Image(null, new ThemeResource(
				"img/arrow-down.png"));
		imageDown.setHeight("24px");
		imageDown.setWidth("24px");
		layout.addComponent(imageDown);
		layout.setComponentAlignment(imageDown, Alignment.BOTTOM_RIGHT);
		final Image imageUp = new Image(null, new ThemeResource(
				"img/arrow-up.png"));
		imageUp.setVisible(false);
		imageUp.setHeight("24px");
		imageUp.setWidth("24px");
		layout.addComponent(imageUp);
		layout.setComponentAlignment(imageUp, Alignment.BOTTOM_RIGHT);

		ClickListener arrowListener = new ClickListener() {
			Boolean flag = true;
			Label label = new Label("info aggiuntive ");
			@Override
			public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
				if (flag) {
					layout.addComponent(label);
					imageDown.setVisible(false);
					imageUp.setVisible(true);
					flag = false;
				} else {
					layout.removeComponent(label);
					imageDown.setVisible(true);
					imageUp.setVisible(false);
					flag = true;
				}

			}
		};
		imageDown.addClickListener(arrowListener);
		imageUp.addClickListener(arrowListener);
		return panel;
	}

	public Image getFolderIcon(Boolean isFolder) {
		Image icon;
		if (isFolder) {
			icon = new Image(null, new ThemeResource("img/open_folder.png"));
			icon.setWidth("34px");
		} else {
			icon = new Image(null, new ThemeResource("img/profile-pic.png"));
			icon.setWidth("34px");
		}
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
				System.out.println(client.getCurrentFolder().toString());
				FolderView currentFolder = client.getCurrentFolder();
				Collection<FileView> files = currentFolder.getChildren();
				for(FileView file : files){
					if(file.isDocument()){
						table.addItem(
								new Object[] {
										getFolderIcon(false),
										getInformationPanel(
												file.getName(),
												file.getLastModificationDate(),
												file.getCreatedBy(),
												file.getId()), 
												new DownloadDocumentComponent(file.getId()) },
												Integer.valueOf(file.getId()));
					}else{
						table.addItem(
								new Object[] {
										getFolderIcon(true),
										getInformationPanel(
												file.getName(),
												file.getLastModificationDate(),
												file.getCreatedBy(),
												file.getId()), 
												new DownloadDocumentComponent(file.getId()) },
												Integer.valueOf(file.getId()));
					}			
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
				for(FileView file : files){
					if(file.isDocument()){
						table.addItem(
								new Object[] {
										getFolderIcon(false),
										getInformationPanel(
												file.getName(),
												file.getLastModificationDate(),
												file.getCreatedBy(),
												file.getId()), 
												new DownloadDocumentComponent(file.getId()) },
												Integer.valueOf(file.getId()));
					}else{
						table.addItem(
								new Object[] {
										getFolderIcon(true),
										getInformationPanel(
												file.getName(),
												file.getLastModificationDate(),
												file.getCreatedBy(),
												file.getId()), 
												new DownloadDocumentComponent(file.getId()) },
												Integer.valueOf(file.getId()));
					}			
				}
			}
		}
	}

}
