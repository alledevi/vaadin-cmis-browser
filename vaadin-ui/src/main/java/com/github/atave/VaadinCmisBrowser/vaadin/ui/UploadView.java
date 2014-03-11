package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient;
import com.github.atave.VaadinCmisBrowser.cmis.api.FileView;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
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
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class UploadView  extends VerticalLayout implements View  {
//	AlfrescoClient client;
	CmisClient client;

	public UploadView() {
		setSizeFull();
//		addStyleName("dashboard-view");
		client = ((AppUI)UI.getCurrent()).getClient();
		client.navigateTo("/");

		//layout
		VerticalLayout uploadLayout = new VerticalLayout();
		uploadLayout.setWidth("100%");
		uploadLayout.setSpacing(true);
//		uploadLayout.addStyleName("toolbar");
		
		

	
		Upload upload = new Upload("Upload Document Here", null);
		upload.setSizeFull();
		upload.setButtonCaption("Start Upload");
		upload.setStyleName("default");

//		// Put the upload component in a panel
//		Panel panel = new Panel("Cool Image Storage");
//		panel.addComponent(upload);
		uploadLayout.addComponent(upload);
		
		addComponent(uploadLayout);
		setComponentAlignment(uploadLayout, Alignment.MIDDLE_CENTER);


//		// Show uploaded file in this placeholder
		final Embedded image = new Embedded("Uploaded Image");
		image.setVisible(false);
//		panel.addComponent(image);

		// Implement both receiver that saves upload in a file and
		// listener for successful upload
		class ImageUploader implements Receiver, SucceededListener {
			private static final long serialVersionUID = -1276759102490466761L;

			public File file;

			public OutputStream receiveUpload(String filename, String mimeType) {
				// Create upload stream
				FileOutputStream fos = null; // Output stream to write to
				try {
					// Open the file for writing.
					file = new File("C:/Users/Alessia/Documents/TesinaBasiDiDati/" + filename);
					fos = new FileOutputStream(file);
				} catch (final java.io.FileNotFoundException e) {
					Notification.show(
							"Could not open file<br/>", e.getMessage(),
							Notification.TYPE_ERROR_MESSAGE);
					return null;
				}
				return fos; // Return the output stream to write to
			}

			public void uploadSucceeded(SucceededEvent event) {
				// Show the uploaded file in the image viewer
				image.setVisible(true);
//				image.setSource(new FileResource(file, getApplication()));
			}

		};

		final ImageUploader uploader = new ImageUploader(); 
		upload.setReceiver(uploader);
		upload.addListener(uploader);
		// END-EXAMPLE: component.upload.basic

		// Create uploads directory
		File uploads = new File("C:/Users/Alessia/Documents/TesinaBasiDiDati");
		if (!uploads.exists() && !uploads.mkdir())
			uploadLayout.addComponent(new Label("ERROR: Could not create upload dir"));

//		((VerticalLayout) uploadLayout.getContent()).setSpacing(true);
		uploadLayout.setWidth("-1");
	
}

@Override
public void enter(ViewChangeEvent event) {
	// TODO Auto-generated method stub		
}
}
