package com.github.atave.VaadinCmisBrowser.vaadin.utils;

import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A {@code Tree} specialized in handling {@link FolderView} items.
 */
public class CmisTree extends Tree {

    /**
     * Constructs a {@code CmisTree}.
     */
    public CmisTree() {
        super();

        setSelectable(false);

        this.addCollapseListener(new CollapseListener() {
            @Override
            public void nodeCollapse(CollapseEvent event) {
                removeChildren((FolderView) event.getItemId());
            }
        });
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId) {
        setChildrenAllowed(newParentId, true);
        return super.setParent(itemId, newParentId);
    }

    private void updateChildrenAllowed(FolderView folder) {
        setChildrenAllowed(folder, !folder.getFolders().isEmpty());
    }

    /**
     * Create a new item into container. The created new item is returned and
     * ready for setting property values. if the creation fails, null is
     * returned. In case the container already contains the item, null is
     * returned.
     *
     * @param folder the folder to add to the tree
     * @return the created item with the given id, or null in case of failure.
     */
    public Item add(FolderView folder) {
        Item retval = super.addItem(folder);
        updateChildrenAllowed(folder);

        if (!folder.isRootFolder()) {
            setParent(folder, folder.getParent());
        }

        return retval;
    }

    /**
     * Removes the folder from the Container.
     *
     * @param folder the folder to remove
     * @param parent the parent of the removed folder
     */
    public void remove(FolderView folder, FolderView parent) {
        removeChildren(folder);
        super.removeItem(folder);

        if (!folder.isRootFolder()) {
            updateChildrenAllowed(parent);
        }
    }

    /**
     * Expands an item.
     *
     * @param folder the folder to expand
     */
    public void expand(FolderView folder) {
        Collection<FolderView> children = folder.getFolders();

        if (!hasChildren(folder) && !children.isEmpty()) {
            for (FolderView child : children) {
                super.addItem(child);
                setParent(child, folder);
                updateChildrenAllowed(child);
            }
        }

        updateChildrenAllowed(folder);
        super.expandItem(folder);
        select(folder);
    }

    /**
     * Recursively expands the specified folder and its parents.
     *
     * @param folder the folder to show
     */
    public void show(FolderView folder) {
        if (!folder.isRootFolder()) {
            List<FolderView> parents = new ArrayList<>();

            FolderView parent = folder;
            do {
                parent = parent.getParent();
                parents.add(parent);
            } while (!parent.isRootFolder());

            Collections.reverse(parents);

            for (FolderView toExpand : parents) {
                expand(toExpand);
            }
        }

        expand(folder);
    }

    private void removeChildren(FolderView folder) {
        Collection<Object> children = new ArrayList<>();
        if (hasChildren(folder)) {
            children.addAll(getChildren(folder));
        }

        while (!children.isEmpty()) {
            Collection<Object> toRemove = new ArrayList<>(children);
            children.clear();

            for (Object childId : toRemove) {
                if (hasChildren(childId)) {
                    children.addAll(getChildren(childId));
                }
                super.removeItem(childId);
            }
        }
    }

    @Override
    public void addItemClickListener(final ItemClickEvent.ItemClickListener listener) {
        super.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                if (event.getButton() == ItemClickEvent.BUTTON_LEFT) {
                    FolderView folder = (FolderView) event.getItemId();
                    expand(folder);
                    listener.itemClick(event);
                }
            }
        });
    }
}
