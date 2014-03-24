package com.github.atave.VaadinCmisBrowser.vaadin.ui; 
  
import java.io.IOException; 
import java.util.ArrayList; 
import java.util.Collections; 
  
import com.github.atave.VaadinCmisBrowser.cmis.api.DocumentView; 
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView; 
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView; 
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient; 
import com.github.atave.VaadinCmisBrowser.vaadin.utils.DocumentDownloader; 
import com.vaadin.data.Property.ValueChangeEvent; 
import com.vaadin.data.Property.ValueChangeListener; 
import com.vaadin.event.MouseEvents.ClickListener; 
import com.vaadin.event.ShortcutAction.KeyCode; 
import com.vaadin.event.ShortcutListener; 
import com.vaadin.server.StreamResource; 
import com.vaadin.server.ThemeResource; 
import com.vaadin.ui.Alignment; 
import com.vaadin.ui.Button; 
import com.vaadin.ui.Button.ClickEvent; 
import com.vaadin.ui.ComboBox; 
import com.vaadin.ui.CustomComponent; 
import com.vaadin.ui.FormLayout; 
import com.vaadin.ui.GridLayout; 
import com.vaadin.ui.HorizontalLayout; 
import com.vaadin.ui.Image; 
import com.vaadin.ui.Label; 
import com.vaadin.ui.Link; 
import com.vaadin.ui.Panel; 
import com.vaadin.ui.Table; 
import com.vaadin.ui.Table.Align; 
import com.vaadin.ui.TextField; 
import com.vaadin.ui.Tree; 
import com.vaadin.ui.UI; 
import com.vaadin.ui.VerticalLayout; 
import com.vaadin.ui.Window; 
  
public class TableActionComponent extends CustomComponent { 
      
    private static final long serialVersionUID = 1L; 
      
    Button yes, no; 
    Table table; 
    AlfrescoClient client; 
    Boolean isFolder; 
    Integer itemId; 
    String path; 
    Window window; 
    Panel queryPanel; 
    String requestedVersion; 
    Link downloadLink = new Link(); 
    Table tableTag; 
    Integer i=0; 
    Image removeTag; 
    Tree tree; 
  
    public TableActionComponent(Tree tree, final String path, final Integer itemId , final Table table, final AlfrescoClient client, final Boolean isFolder) { 
        this.tree = tree; 
        this.table = table; 
        this.client = client; 
        this.isFolder = isFolder; 
        this.itemId = itemId; 
        this.path = path; 
  
  
        // A layout structure used for composition 
        Panel panel = new Panel(); 
        //which rows it refers to  
        panel.setData(itemId); 
        HorizontalLayout layoutFolder = new HorizontalLayout(); 
        HorizontalLayout layoutDocument = new HorizontalLayout(); 
          
  
        //button for delete document 
        Image deleteDocument = new Image(null, new ThemeResource( 
                "img/delete-icon.png")); 
        deleteDocument.setHeight("34px"); 
        deleteDocument.setWidth("34px"); 
  
        ClickListener deleteDocumentListener = new ClickListener() { 
  
            private static final long serialVersionUID = 1L; 
  
            @Override
            public void click(com.vaadin.event.MouseEvents.ClickEvent event) { 
                window = new Window("Delete. Are you sure? "); 
                window.setResizable(false); 
                window.center(); 
                window.addStyleName("edit-dashboard"); 
                UI.getCurrent().addWindow(window); 
                  
                VerticalLayout background = new VerticalLayout();        
                  
                FormLayout informationLayout = new FormLayout(); 
                informationLayout.setSizeUndefined(); 
                informationLayout.setMargin(true); 
                                  
                GridLayout gridLayout = new GridLayout(5,1); 
                gridLayout.setSizeFull(); 
                gridLayout.setSpacing(true); 
                gridLayout.setMargin(true); 
  
                yes = new Button("yes"); 
                yes.addStyleName("default"); 
                no = new Button("no"); 
                no.addStyleName("default"); 
                yes.addClickListener(deleteDocumentButtonListener); 
                no.addClickListener(deleteDocumentButtonListener); 
                gridLayout.addComponent(yes,1,0,2,0); 
                gridLayout.addComponent(no,3,0,4,0);                 
                informationLayout.addComponent(gridLayout); 
                  
                //set footer 
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
        deleteDocument.addClickListener(deleteDocumentListener); 
  
        //button for download document 
        Image documentDownload = new Image(null, new ThemeResource( 
                "img/download-icon.png")); 
        documentDownload.setHeight("34px"); 
        documentDownload.setWidth("34px"); 
  
        ClickListener documentDownloadListener = new ClickListener() { 
  
            private static final long serialVersionUID = 1L; 
  
            @Override
            public void click(com.vaadin.event.MouseEvents.ClickEvent event) { 
                window = new Window("Select version: "); 
                window.setResizable(false); 
                window.center(); 
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
                for(String version : document.getAllVersions()){ 
                    versions.addItem(version); 
                } 
                informationLayout.addComponent(versions);                
                  
                downloadLink.setCaption("Start download"); 
                downloadLink.setEnabled(false); 
                informationLayout.addComponent(downloadLink);        
  
                //set footer 
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
        documentDownload.addClickListener(documentDownloadListener); 
  
        //button for more information 
        Image moreInformation = new Image(null, new ThemeResource("img/info-icon.png")); 
        moreInformation.setHeight("34px"); 
        moreInformation.setWidth("34px"); 
          
        //open window for more information 
        ClickListener moreInformationListener = new ClickListener() { 
            private static final long serialVersionUID = 1L; 
  
            @SuppressWarnings("deprecation") 
            public void click(com.vaadin.event.MouseEvents.ClickEvent event) { 
                  
                window = new Window("More information"); 
                window.setResizable(false); 
                window.center(); 
                window.addStyleName("edit-dashboard"); 
                UI.getCurrent().addWindow(window);   
                  
                VerticalLayout background = new VerticalLayout(); 
                  
                //general information 
                FormLayout informationLayout = new FormLayout(); 
                informationLayout.setSizeUndefined(); 
                informationLayout.setMargin(true); 
                Label createdBy = new Label((String) table.getContainerProperty(itemId, "created by").getValue()); 
                createdBy.setCaption("created by: ");
                informationLayout.addComponent(createdBy); 
                
                Label modifiedBy = new Label((String) table.getContainerProperty(itemId, "modified by").getValue()); 
                modifiedBy.setCaption("modified by: ");
                informationLayout.addComponent(modifiedBy); 
                
                Label path1 = new Label((String) table.getContainerProperty(itemId, "path").getValue()); 
                path1.setCaption("path: ");
                informationLayout.addComponent(path1); 
                  
                //panel for add tag 
                HorizontalLayout tagLayout = new HorizontalLayout(); 
                tagLayout.setMargin(false); 
                tagLayout.setSpacing(true); 
                tagLayout.setWidth("100%");
                tagLayout.setCaption("Tag: ");
                final TextField tf = new TextField(); 
                tf.setInputPrompt("Add new tag"); 
                tagLayout.addComponent(tf); 
                final Button add = new Button("Add"); 
                add.addStyleName("default"); 
                tagLayout.addComponent(add);     
                informationLayout.addComponent(tagLayout); 
                  
                //table with tag 
                tableTag = new Table(); 
                tableTag.setSelectable(false); 
                tableTag.setImmediate(true); 
                tableTag.addContainerProperty("name", String.class, null); 
                tableTag.addContainerProperty("image", Image.class, null); 
                tableTag.setColumnAlignment("image", Align.CENTER); 
                tableTag.setColumnAlignment("name", Align.CENTER); 
                tableTag.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN); 
                tableTag.setWidth("100%"); 
                tableTag.addStyleName("borderless"); 
                tableTag.setPageLength(5); 
                informationLayout.addComponent(tableTag); 
                  
                //button for remove tag 
                removeTag = new Image(null, new ThemeResource("img/remove.png")); 
                removeTag.addClickListener(removeTagListener); 
  
                // find tag and populate table 
                ArrayList<String> tags = new ArrayList<String>();        
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
                  
                // shortcut for add tag 
                final ShortcutListener enter = new ShortcutListener("", KeyCode.ENTER, null) { 
                            private static final long serialVersionUID = 1L; 
                    @Override
                    public void handleAction(Object sender, Object target) { 
                        add.click(); 
                    } 
                }; 
                  
                add.addClickListener(new Button.ClickListener() { 
                    private static final long serialVersionUID = 1L; 
  
                    @Override
                    public void buttonClick(ClickEvent event) { 
                        if(!tf.getValue().equals("")){ 
                        removeTag = new Image(tf.getValue(), new ThemeResource("img/remove.png")); 
                        removeTag.addClickListener(removeTagListener); 
                        removeTag.setData(i); 
                        tableTag.addItem(new Object[] {tf.getValue(), removeTag},i++);       
                        try { 
                            client.createTag(tf.getValue()); 
                            FileView file = client.getFile(path); 
                            String id = null; 
                            if(file.isDocument()){ 
                                DocumentView document = client.getDocument(path); 
                                id = document.getId(); 
                            } 
                            else if(file.isFolder()){ 
                                FolderView folder = client.getFolder(path); 
                                id = folder.getId(); 
                            } 
                          
                            client.addTags(id, Collections.singleton(tf.getValue())); 
                        } catch (IOException e) { 
                            e.printStackTrace(); 
                        } 
                        } 
                    } 
                }); 
  
                add.addShortcutListener(enter); 
                  
                //set footer 
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
        moreInformation.addClickListener(moreInformationListener); 
  
  
        panel.addClickListener(selectPanelListener); 
        if(isFolder){ 
            panel.setContent(layoutFolder); 
            layoutFolder.addComponent(deleteDocument); 
            layoutFolder.setComponentAlignment(deleteDocument, Alignment.BOTTOM_LEFT); 
            layoutFolder.addComponent(moreInformation); 
            layoutFolder.setComponentAlignment(moreInformation, Alignment.MIDDLE_CENTER); 
        } 
        else{ 
            panel.setContent(layoutDocument); 
            layoutDocument.addComponent(deleteDocument); 
            layoutDocument.setComponentAlignment(deleteDocument, Alignment.BOTTOM_LEFT); 
            layoutDocument.addComponent(documentDownload); 
            layoutDocument.setComponentAlignment(documentDownload, Alignment.MIDDLE_CENTER); 
            layoutDocument.addComponent(moreInformation); 
            layoutDocument.setComponentAlignment(moreInformation, Alignment.MIDDLE_RIGHT); 
              
        } 
          
        setCompositionRoot(panel); 
  
    } 
      
    ClickListener removeTagListener = new ClickListener() { 
  
        private static final long serialVersionUID = 1L; 
  
        //select table row 
        public void click(com.vaadin.event.MouseEvents.ClickEvent event) { 
            try { 
                Integer id = (Integer)((Image) event.getComponent()).getData(); 
                String tag = event.getComponent().getCaption().toString(); 
                System.out.println(tag); 
                client.deleteTag(tag); 
                tableTag.removeItem(id); 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
              
        } 
    }; 
  
    //lister for highlight row 
        ClickListener selectPanelListener = new ClickListener() { 
            private static final long serialVersionUID = 1L; 
  
            //select table row 
            public void click(com.vaadin.event.MouseEvents.ClickEvent event) { 
                Integer id = (Integer)((Panel) event.getComponent()).getData(); 
                table.select(id); 
            } 
        }; 
    Button.ClickListener deleteDocumentButtonListener = new Button.ClickListener() { 
        private static final long serialVersionUID = 1L; 
  
        public void buttonClick(ClickEvent event) { 
            if(event.getButton().equals(yes)){ 
                String parent = ""; 
                // Delete all versions of a document             
                if (isFolder){ 
                    FolderView folder = client.getFolder(path); 
                    parent = folder.getParent().getName(); 
                    System.out.println(parent); 
                    client.deleteFolder(folder); 
                    tree.removeItem(folder.getName()); 
                          
                } else { 
                    DocumentView document = client.getDocument(path); 
                    client.deleteDocument(document.asDocument()); 
                } 
                table.removeItem(itemId); 
  
            }    
            window.close(); 
        } 
    }; 
      
      
    //select which version download 
    ValueChangeListener versionListener = new ValueChangeListener() { 
        private static final long serialVersionUID = 1L; 
  
        public void valueChange(ValueChangeEvent event) { 
            requestedVersion = event.getProperty().getValue().toString(); 
              
            if (requestedVersion == "" || requestedVersion == null){ 
                requestedVersion = "1.0"; 
            } 
            DocumentView document = client.getDocument(path);    
            document = document.getObjectOfVersion(requestedVersion); 
            // Create a stream resource 
            StreamResource.StreamSource source = new DocumentDownloader(document); 
            StreamResource resource = new StreamResource(source, document.getName()); 
            // Let the user download the document 
            downloadLink.setResource(resource); 
            downloadLink.setEnabled(true); 
            downloadLink.setTargetName("_blank"); 
            downloadLink.setVisible(true); 
        } 
    }; 
  
  
}