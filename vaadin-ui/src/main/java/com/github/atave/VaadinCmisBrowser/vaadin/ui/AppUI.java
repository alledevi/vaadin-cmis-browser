package com.github.atave.VaadinCmisBrowser.vaadin.ui;

import com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient;
import com.github.atave.VaadinCmisBrowser.cmis.impl.AlfrescoClient;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

import javax.servlet.annotation.WebServlet;
import java.util.HashMap;
import java.util.Iterator;

@Theme("dashboard")
@SuppressWarnings("serial")

/**
 * AppUI. Principal class of application 
 *
 */

public class AppUI extends UI {

    // AlfrescoClient
    private AlfrescoClient client;
    
    Button buttonHome;
    Button buttonSearch;
    private Tree tree = new Tree();

    //getter and setter tree
    public Tree getTree() {
        return tree;
    }

    public void setTree(Tree tree) {
        this.tree = tree;
    }
    
    public Button getButtonHome() {
        return buttonHome;
    }

    public void setButtonHome(Button button) {
        this.buttonHome = button;
    }
    
    public Button getButtonSearch() {
        return buttonSearch;
    }

    public void setButtonSearch(Button button) {
        this.buttonSearch = button;
    }
    

    public AlfrescoClient getClient() {
        return client;
    }

    public void setClient(AlfrescoClient client) {
        this.client = client;
    }

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = AppUI.class, widgetset = "com.github.atave.VaadinCmisBrowser.vaadin.ui.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    private CssLayout root;
    private CssLayout content;

    // init
    private Label bg;

    // buildLoginView
    private VerticalLayout loginLayout;
    private CssLayout loginPanel;
    private HorizontalLayout labels;
    private Label welcome;
    private Label title;
    private HorizontalLayout fields;
    private TextField usernameTf;
    private PasswordField passwordTf;
    private Button signin;

    //buildMainView
    private Navigator nav;
    private String user;
    private HorizontalLayout hl;
    private VerticalLayout menuLayout;
    private CssLayout brandingLayout;
    private Label logo;
    private CssLayout menu;
    private VerticalLayout userMenu;
    private Image profilePic;
    private Label userName;
    private Button exit;


    HashMap<String, Class<? extends View>> routes = new HashMap<String, Class<? extends View>>() {
        {
            put("/home", HomeView.class);
            put("/search", SearchView.class);
        }
    };

    HashMap<String, Button> viewNameToMenuButton = new HashMap<String, Button>();

    protected void init(VaadinRequest request) {

        content = new CssLayout();

        root = new CssLayout();
        root.addStyleName("root");
        root.setSizeFull();
        setContent(root);

        bg = new Label();
        bg.setSizeUndefined();
        bg.addStyleName("login-bg");
        root.addComponent(bg);

        buildLoginView(false);
    }

    private void buildLoginView(boolean exit) {
        if (exit)
            root.removeAllComponents();

        addStyleName("login");

        loginLayout = new VerticalLayout();
        loginLayout.setSizeFull();
        loginLayout.addStyleName("login-layout");
        root.addComponent(loginLayout);

        loginPanel = new CssLayout();
        loginPanel.addStyleName("login-panel");

        labels = new HorizontalLayout();
        labels.setWidth("100%");
        labels.setMargin(true);
        labels.addStyleName("labels");
        loginPanel.addComponent(labels);

        welcome = new Label("Welcome");
        welcome.setSizeUndefined();
        welcome.addStyleName("h4");
        labels.addComponent(welcome);
        labels.setComponentAlignment(welcome, Alignment.MIDDLE_LEFT);

        title = new Label("My Alfresco");
        title.setSizeUndefined();
        title.addStyleName("h2");
        title.addStyleName("light");
        labels.addComponent(title);
        labels.setComponentAlignment(title, Alignment.MIDDLE_RIGHT);

        fields = new HorizontalLayout();
        fields.setSpacing(true);
        fields.setMargin(true);
        fields.addStyleName("fields");

        usernameTf = new TextField("Username");
        usernameTf.focus();
        fields.addComponent(usernameTf);

        passwordTf = new PasswordField("Password");
        fields.addComponent(passwordTf);

        signin = new Button("Sign In");
        signin.addStyleName("default");
        fields.addComponent(signin);
        fields.setComponentAlignment(signin, Alignment.BOTTOM_LEFT);
        signin.addClickListener(signinListener);
        signin.addShortcutListener(enter);

        loginPanel.addComponent(fields);

        loginLayout.addComponent(loginPanel);
        loginLayout.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);
    }

    private void buildMainView(CmisClient client) {

        nav = new Navigator(this, content);
        for (String route : routes.keySet()) {
            nav.addView(route, routes.get(route));
        }

        removeStyleName("login");
        root.removeComponent(loginLayout);

        //brandingLayout
        brandingLayout = new CssLayout();
        brandingLayout.addStyleName("branding");
        logo = new Label("<span>My</span>Alfresco", ContentMode.HTML);
        logo.setSizeUndefined();
        brandingLayout.addComponent(logo);

        // userMenu
        userMenu = new VerticalLayout();
        userMenu.setSizeUndefined();
        userMenu.addStyleName("user");
        profilePic = new Image(null, new ThemeResource("img/profile-pic.png"));
        profilePic.setWidth("34px");
        userMenu.addComponent(profilePic);
        userName = new Label(user);
        userName.setSizeUndefined();
        userMenu.addComponent(userName);
        exit = new NativeButton("Exit");
        exit.addStyleName("icon-cancel");
        exit.setDescription("Sign Out");
        userMenu.addComponent(exit);
        exit.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                buildLoginView(true);
            }
        });

        //menu
        menu = new CssLayout();
        menu.addStyleName("menu");
        menu.setHeight("100%");
        menu.removeAllComponents();
        final String viewHome = "home";
        buttonHome = new NativeButton(viewHome.substring(0, 1).toUpperCase() 
        		+ viewHome.substring(1).replace('-', ' '));
        buttonHome.addStyleName("icon-" + viewHome);
        buttonHome.addClickListener(new ClickListener() {
        	@Override
        	public void buttonClick(ClickEvent event) {
        		clearMenuSelection();
        		event.getButton().addStyleName("selected");
        		if (!nav.getState().equals("/" + viewHome))
        			nav.navigateTo("/" + viewHome);
        	}
        });
        menu.addComponent(buttonHome);
        viewNameToMenuButton.put("/" + viewHome, buttonHome);
        
        final String viewSearch = "search";
        buttonSearch = new NativeButton(viewSearch.substring(0, 1).toUpperCase() 
        		+ viewSearch.substring(1).replace('-', ' '));
        buttonSearch.addStyleName("icon-" + viewSearch);
        buttonSearch.addClickListener(new ClickListener() {
        	@Override
        	public void buttonClick(ClickEvent event) {
        		clearMenuSelection();
        		event.getButton().addStyleName("selected");
        		if (!nav.getState().equals("/" + viewSearch))
        			nav.navigateTo("/" + viewSearch);
        	}
        });
        menu.addComponent(buttonSearch);
        viewNameToMenuButton.put("/" + viewSearch, buttonSearch);
        
        
        

        String f = Page.getCurrent().getUriFragment();
        if (f != null && f.startsWith("!")) {
            f = f.substring(1);
        }
        if (f == null || f.equals("") || f.equals("/")) {
            nav.navigateTo("/home");
            menu.getComponent(0).addStyleName("selected");
        } else {
            nav.navigateTo(f);
            viewNameToMenuButton.get(f).addStyleName("selected");
        }

        // menuLayout
        menuLayout = new VerticalLayout();
        menuLayout.addStyleName("sidebar");
        menuLayout.setWidth(null);
        menuLayout.setHeight("100%");
        menuLayout.addComponent(brandingLayout);
        menuLayout.addComponent(menu);
        menuLayout.setExpandRatio(menu, 1);
        menuLayout.addComponent(userMenu);

        // hl
        hl = new HorizontalLayout();
        hl.setSizeFull();
        hl.addStyleName("main-view");
        hl.addComponent(menuLayout);
        hl.addComponent(content);
        content.setSizeFull();
        content.addStyleName("view-content");
        hl.setExpandRatio(content, 1);

        root.addComponent(hl);
    }

    /**
     * Listener for Button "signin"
     */
    ClickListener signinListener = new ClickListener() {

        private Label error;
        private String password;

        public void buttonClick(ClickEvent event) {
            try {
                user = usernameTf.getValue();
                password = passwordTf.getValue();
                client = new AlfrescoClient(user, password);
                signin.removeShortcutListener(enter);
                buildMainView(client);
            } catch (CmisBaseException e) {
                if (loginPanel.getComponentCount() > 2) {
                    loginPanel.removeComponent(loginPanel.getComponent(2));
                }
                // Add new error message
                error = new Label("Wrong username or password.", ContentMode.HTML);
                error.addStyleName("error");
                error.setSizeUndefined();
                error.addStyleName("light");
                error.addStyleName("v-animate-reveal");

                loginPanel.addComponent(error);
                usernameTf.focus();
            }

        }
    };

    @SuppressWarnings("deprecation")
    private void clearMenuSelection() {
        for (Iterator<Component> it = menu.getComponentIterator(); it.hasNext(); ) {
            Component next = it.next();
            if (next instanceof NativeButton) {
                next.removeStyleName("selected");
            } else if (next instanceof DragAndDropWrapper) {
                ((DragAndDropWrapper) next).iterator().next().removeStyleName("selected");
            }
        }
    }

    /**
     * ShortcutListener enter for Button "signin"
     */
    final ShortcutListener enter = new ShortcutListener("Sign In", KeyCode.ENTER, null) {
        @Override
        public void handleAction(Object sender, Object target) {
            signin.click();
        }
    };
}