package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient;
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class HomeView  extends VerticalLayout implements View  {
	CmisClient client;
	FolderComponent table;
	Tree tree;
	
	public HomeView() {
		setSizeFull();
		addStyleName("dashboard-view");
		client = ((AppUI)UI.getCurrent()).getClient();
		client.navigateTo("/");

		//pannello intestazione e ricerca
		HorizontalLayout top = new HorizontalLayout();
		top.setWidth("100%");
		top.setSpacing(true);
		top.addStyleName("toolbar");

		addComponent(top);
		//		setExpandRatio(top, 1);

		//pannello albero e tabella
		final Label title = new Label("My Alfresco");
		title.setSizeUndefined();
		title.addStyleName("h1");
		top.addComponent(title);
		top.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
		top.setExpandRatio(title, 1);

		//middle: menù ricerca
		HorizontalLayout middle = new HorizontalLayout();
		middle.setWidth("100%");
		middle.setSpacing(true);
		middle.addStyleName("toolbar");
		addComponent(middle);

		//--- MENU DIRECTORY ---
		MenuBar menuBar = new MenuBar();
		middle.addComponent(menuBar);
		middle.setComponentAlignment(menuBar, Alignment.MIDDLE_LEFT);
		menuBar.setStyleName("h1");
		//		menuBar.setWidth("60%");
		middle.setExpandRatio(menuBar, 2);
		MenuBar.MenuItem dir1 = menuBar.addItem("directory1", null, null);
		MenuBar.MenuItem dir2 = menuBar.addItem("directory2", null, null);
		MenuBar.MenuItem dir3 = menuBar.addItem("directory3", null, null);

		MenuBar.Command command = new MenuBar.Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub

			}
		};

		// search alto destra
		final ComboBox documentSelectHome = new ComboBox();
		ArrayList<String> documents = new ArrayList();
		documents.add("alessia");
		documents.add("federica");
		for (String document : documents) {
			documentSelectHome.addItem(document);
		}
		documentSelectHome.setWidth("300px");
		middle.addComponent(documentSelectHome);
		middle.setComponentAlignment(documentSelectHome, Alignment.MIDDLE_RIGHT);
		//		documentSelectHome.addShortcutListener(new ShortcutListener("Add",
		//				KeyCode.ENTER, null) {
		//
		//			@Override
		////			public void handleAction(Object sender, Object target) {
		////				addSelectedMovie(documentSelectHome);
		////			}
		//		});

		Button add = new Button("Search");
		add.addStyleName("default");
		add.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				//                addSelectedMovie(movieSelect);
			}
		});
		middle.addComponent(add);


		//--- TREE ---
		tree = new Tree();
		tree.addItem("/");
		createTree(tree, "/");
		tree.setSizeFull();		
		tree.addListener(treeListener);


		// --- TABLE ---
		table = new FolderComponent(null, client);
		table.setImmediate(true);
		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		splitPanel.setStyleName("toolbar");
		splitPanel.setSizeFull(); //mette barra in split panel
		splitPanel.setSplitPosition(15, Sizeable.UNITS_PERCENTAGE);
		splitPanel.setFirstComponent(tree);
		splitPanel.setSecondComponent(table);
		addComponent(splitPanel);
		setExpandRatio(splitPanel, 3);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public void createTree(Tree tree, String path){	
		Collection<FileView> files1 = client.getCurrentFolder().getChildren();
		for(FileView file1 : files1){
			if(file1.isDocument()){
//				System.out.println("documento " + " " + file1.getName());
				tree.addItem(file1.getName());
				tree.setParent(file1.getName(), path);
			}
			else if(file1.isFolder()){
//				System.out.println("cartella " + " " + file1.getName());
				tree.addItem(file1.getName());
				tree.setParent(file1.getName(), path);
				client.navigateTo(file1.getName());
//				System.out.println("prima " + client.getCurrentFolder().getPath());
//				System.out.println(file1.getName());
				createTree(tree, file1.getName());
//				System.out.println("dopo " + client.getCurrentFolder().getPath());
				client.navigateTo("..");
			}
		}
	}
	
	ItemClickListener treeListener = new ItemClickEvent.ItemClickListener() {
		private static final long serialVersionUID = 1L;
		List<String> itemExpanded = new ArrayList<>();
		String itemId = null;
		String path = null;
		@SuppressWarnings("deprecation")
		public void itemClick(ItemClickEvent event) {
			// Pick only left mouse clicks
			itemId = (String) event.getItemId();
			if (event.getButton() == ItemClickEvent.BUTTON_LEFT && event.isDoubleClick()){
				path = getTreePath(tree, itemId);
				//update folder component
				table.populateTable(path);
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
		if(itemId.equals("/")){
			return "/";
		}		
		String path = "/" + itemId;
		while(!tree.getParent(itemId).equals("/") && tree.getParent(itemId) != null ){
			itemId = (String) tree.getParent(itemId);
			path = "/" + itemId +  path;
		}		
		return path;
	}
}
