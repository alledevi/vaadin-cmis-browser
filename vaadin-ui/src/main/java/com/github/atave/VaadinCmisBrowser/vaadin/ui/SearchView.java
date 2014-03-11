package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import java.util.ArrayList;

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;

import org.apache.chemistry.opencmis.client.api.ItemIterable;

import com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient;
import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView;
import com.github.atave.VaadinCmisBrowser.cmis.api.PropertyType;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.lexaden.breadcrumb.Breadcrumb;
import com.lexaden.breadcrumb.BreadcrumbLayout;
import com.lexaden.breadcrumb.gwt.client.ui.BreadcrumbState;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class SearchView extends VerticalLayout implements View  {

	private static final long serialVersionUID = 1L;
	
	private FormLayout panel;
	private HorizontalLayout dateLayout;
	private VerticalLayout resultLayout;
	
	private Label advancedSearch;
	private TextField keyWords;
	private TextField name;
	private TextField title;
	private TextField description;
	private ComboBox mimeType;
	private DateField fromDate;
	private DateField toDate;
	private TextField mod;
	private Button searchButton;
	private Button returnButton;

	AlfrescoClient client;
//	CmisClient client;
	TableComponent folder;
	PropertyType property;

	public SearchView() {
		setSizeFull();
//		addStyleName("dashboard-view");
		client = ((AppUI)UI.getCurrent()).getClient();
		
		folder = new TableComponent(null, client,null);

		panel = new FormLayout();
		panel.setSpacing(true);
		panel.setSizeFull();
		panel.setMargin(true);  
		
		final Breadcrumb breadCrumb = new Breadcrumb();
		breadCrumb.setShowAnimationSpeed(Breadcrumb.AnimSpeed.SLOW);
		breadCrumb.setHideAnimationSpeed(Breadcrumb.AnimSpeed.SLOW);
		breadCrumb.setUseDefaultClickBehaviour(false);
		Button b = new Button("ciao");
		b.setHeight("45px");
		breadCrumb.addLink(b);
//		breadCrumb.setLinkEnabled(false, 0);
		breadCrumb.setHeight(50, UNITS_PIXELS);
//		breadCrumb.setSizeFull();
		BreadcrumbLayout a = new BreadcrumbLayout();
		a.addComponent(breadCrumb);
		panel.addComponent(a);
		
		breadCrumb.select(0);
        ArrayList<String> breadcrumbList = new ArrayList<>();
        breadcrumbList.add("alessia");
        breadcrumbList.add("federica");
        breadcrumbList.add("alle");
        breadcrumbList.add("fede");
		final int size = breadcrumbList .size();
        for (int i = 0, breadcrumbListSize = breadcrumbList.size(); i < breadcrumbListSize; i++) {
//            String state = breadcrumbList.get(i);
        	breadCrumb.addLink(new Button("i"));
//            breadCrumb.addLink(new Button(getName(state), new ClickListener("cancel", eventProcessor, size - (i + 1))));
        }

		
		advancedSearch = new Label("Ricerca Avanzata");
		advancedSearch.addStyleName("h1");
		panel.addComponent(advancedSearch);

		keyWords = new TextField("Parole chiave");
		panel.addComponent(keyWords);
		keyWords.setWidth("60%");
		
		name = new TextField("Nome");
		panel.addComponent(name);
		name.setWidth("30%");
		
		title = new TextField("Titolo");
		title.setWidth("60%");
		panel.addComponent(title);
		
		description = new TextField("Descrizione");
		panel.addComponent(description);
		description.setWidth("60%");
		
		mimeType = new ComboBox("Mimetype");
		panel.addComponent(mimeType);
		mimeType.setWidth("30%");
				
		dateLayout = new HorizontalLayout();
		dateLayout.setSpacing(true);
	    dateLayout.setCaption("Data di modifica");
		panel.addComponent(dateLayout);
	
		fromDate = new DateField("Da");
		dateLayout.addComponent(fromDate);
		
		toDate = new DateField("A");
		dateLayout.addComponent(toDate);
		
		mod = new TextField("Modificatore");
		panel.addComponent(mod);
		mod.setWidth("30%");
				
		searchButton = new Button("Search");
		searchButton.setIcon(new ThemeResource("img/search36.png"));
		panel.addComponent(searchButton);
		searchButton.addClickListener(clickListener);
		
		resultLayout = new VerticalLayout();
		resultLayout.setVisible(false);
		resultLayout.setSizeFull();
		addComponent(resultLayout);
		
		resultLayout.addComponent(folder);
		
		returnButton  = new Button("Ritorna alla ricerca");
		returnButton.setIcon(new ThemeResource("img/search36.png"));
		resultLayout.addComponent(returnButton);
		returnButton.addClickListener(clickListener);
		
		
		addComponent(panel);	
	}
	
	Button.ClickListener clickListener = new Button.ClickListener() {

		@Override
		public void buttonClick(ClickEvent event) {
			if(event.getButton().equals(searchButton)){
				panel.setVisible(false);
				resultLayout.setVisible(true);
				ItemIterable<DocumentView> files = client.search(name.getValue(), description.getValue());
				System.out.println(description.getValue());
				if (files.getTotalNumItems() >0){
					for(DocumentView file : files){
						System.out.println(file.getName());
						folder.addItemToFolderComponent(file.getId());
					}
				}
//				else
//					folder.removeAllItems();
			}
			else if(event.getButton().equals(returnButton)){
				resultLayout.setVisible(false);
				panel.setVisible(true);
			}
			
			
		}
		
	};

//		type = new ComboBox();
//		advancedLayout.addComponent(type,3,0);
//		type.setImmediate(true);
//		for(PropertyType p : PropertyType.values()){
//			type.addItem(p.name());
//		}
//
//		addOption = new Button("Add Option");
//		advancedLayout.addComponent(addOption,3,1);
//		addOption.addClickListener(optionListener);
//
//		group = new OptionGroup("Opzioni");
//		group.setVisible(false);
//		countOptions = 0;
//
//		type.addListener(new Listener() {
//			@Override
//			public void componentEvent(Event event) {
//				group.setVisible(true);
//				group.removeAllItems();
//				//				System.out.println(type.getValue());
//				PropertyType p = PropertyType.valueOf(type.getValue().toString());
//				if (p.compareTo(property.DATETIME)==0) {
//					System.out.println("Datetime");
//					//					DateField date = new DateField();
//					//					editorLayout.addComponent(date,3,3);
//				}
//
//				QueryOperator[] list = p.getSupportedOperators() ;
//				for(QueryOperator l : list){
//					group.addItem(l.name());
//				}
//			}
//		});
//
//
//		//		metLab = new Label("Tipo");
//		//		editorLayout.addComponent(metLab,0,4);
//		//		
//		//		met = new TextField();
//		//		editorLayout.addComponent(met,1,4);
//
//
//		advancedLayout.addComponent(group,1,1);
//
//
//		searchButton.addClickListener(cl2);
//
//	}
//
//
//
//
//	ClickListener cl = new ClickListener() {
//		Boolean flag = true;
//		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
//			if(flag){
//				advancedLayout.setVisible(true);
//				flag = false;
//			}
//			else{
//				advancedLayout.setVisible(false);
//				flag = true;
//			}
//
//		}
//	};
//
//	Button.ClickListener optionListener = new Button.ClickListener() {
//
//		@Override
//		public void buttonClick(ClickEvent event) {
//			// TODO Auto-generated method stub
//			String option = group.getValue().toString();	
//			String tipo = type.getValue().toString();
//			String value = metValue.getValue().toString();
//
//			System.out.println(value + " " + option + " " + tipo);
//
//			countOptions++;
//
//			Panel p = new Panel();
//			p.setSizeUndefined();
//			p.setId(countOptions.toString());
//			HorizontalLayout hl = new HorizontalLayout();
//			p.setContent(hl);
//
//			l = new Label(value);
//			l.setSizeUndefined();
//			hl.addComponent(l);
//			Image close = new Image(null,new ThemeResource("img/close.png"));
//			close.setId(countOptions.toString());
//			close.setData(value);
//			hl.addComponent(close);
//			close.addClickListener(closeListener);
//			//		close.setWidth("16px");
//			optionLayout.addComponent(p,countOptions,0);
//			optionLayout.setComponentAlignment(p,Alignment.MIDDLE_CENTER);
//
//			
//			pm = new PropertyMatcher("getCreatedBy", QueryOperator.valueOf(group.getValue().toString()) , PropertyType.valueOf(type.getValue().toString()), (Object)metValue.getValue());
//			boolean a = properties.add(pm);
//			System.out.println(a);
//
//
//		}
//	};
//
//	Button.ClickListener cl2 = new Button.ClickListener() {
//		@Override
//		public void buttonClick(ClickEvent event) {
//			// TODO Auto-generated method stub
//
//			String result1 = name.getValue();
//			String result2 = text.getValue();
//
//			System.out.println(result1 + " " + result2);
//
//			if(properties.isEmpty()){
//
//				ItemIterable<DocumentView> files = client.search(result1, result2);
//				System.out.println(files.getTotalNumItems());
//				if (files.getTotalNumItems() >0){
//					for(DocumentView file : files){
//						System.out.println(file.getName());
//						folder.addItemToFolderComponent(file.getId());
//					}
//				}
//				else
//					folder.table.removeAllItems();
//			}
//			else{
//				System.out.println(properties.size());
//				ItemIterable<DocumentView> filesOption = client.search(result1, result2, properties);
//				System.out.println(filesOption.getTotalNumItems());
//				if (filesOption.getTotalNumItems() >0){
//					for(DocumentView file : filesOption){
//						System.out.println(file.getName());
//						folder.addItemToFolderComponent(file.getId());
//					}
//				}
//				else
//					folder.table.removeAllItems();
//			}
//		}
//
//	};
//
//
//	ClickListener closeListener = new ClickListener() {
//
//		@Override
//		public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
//			System.out.println("click");
//			System.out.println(properties.size());
//			Image c = (Image) event.getComponent();
//			optionLayout.removeComponent(Integer.valueOf(c.getId()), 0);
//			System.out.println(c.getData().toString());
////			pm = new PropertyMatcher(property, c.g, valueType, c.getData());
//			properties.remove(c.getData());
//			System.out.println(properties.size());
//			sort();
//		}
//
//	};
//	
//	public void sort(){
//		Integer i;
//		optionLayout.removeAllComponents();
//		for(i=1;i<=properties.size();i++){
//			Panel p1 = new Panel();
//			p1.setSizeUndefined();
//			p1.setId(i.toString());
//			HorizontalLayout hl1 = new HorizontalLayout();
//			p1.setContent(hl1);
//
//			Label l1 = new Label(i.toString());
//			l1.setSizeUndefined();
//			hl1.addComponent(l1);
//			Image close1 = new Image(null,new ThemeResource("img/close.png"));
//			close1.setId(i.toString());
//			hl1.addComponent(close1);
//			close1.addClickListener(closeListener);
//			//		close.setWidth("16px");
//			optionLayout.addComponent(p1,i,0);
//			optionLayout.setComponentAlignment(p1,Alignment.MIDDLE_CENTER);
//
//
//		}
	



	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

}
