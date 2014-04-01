package com.github.atave.VaadinCmisBrowser.vaadin.utils;

import com.github.atave.VaadinCmisBrowser.cmis.api.CmisClient;
import com.github.atave.VaadinCmisBrowser.cmis.api.FolderView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String DIGIT_PATTERN = "(\\d+)"; // find one or more digit
    public static String BRACKET_PATTERN = "(\\p{Punct})"; // find one punctuation character like ",&(!
    public static String PUNCT_PATTERN = "(\\p{Punct}+)"; // find one or more punctuation character
    public static String PUNCT_REGEX = PUNCT_PATTERN + ".*"; // find one or more punctuation character follow by char or digit
    public static String VERSION_PATTERN = BRACKET_PATTERN + DIGIT_PATTERN + BRACKET_PATTERN; // (1) (2) (12)
    public static String VERSION_REGEX = ".*" + VERSION_PATTERN; // name(1), name(2)


    /**
     * return folder name or folder name with number of same copy.
     * example folder, folder (1), folder (2)...
     *
     * @param name the name of folder
     */
    public static String renameFolder(String name) {
        Boolean match = Pattern.matches(VERSION_REGEX, name);
        Pattern pattern;
        Matcher matcher;
        if (match) {
            // do copy
            String version = null;
            pattern = Pattern.compile(DIGIT_PATTERN);
            matcher = pattern.matcher(name);
            // find new version
            if (matcher.find()) {
                version = matcher.group();
            }
            Integer i = Integer.valueOf(version);
            i++;
            // delete old version
            pattern = Pattern.compile(VERSION_PATTERN);
            matcher = pattern.matcher(name);
            if (matcher.find()) {
                name = name.replace(matcher.group(), "");
            }
            name = name + "(" + i + ")";
        } else {
            // first copy
            name = name + "(1)";
        }
        return name;
    }

    /**
     * return clean string if it starts with punctuation character.
     *
     * @param text string to clean
     */
    public static String startsWithPunct(String text) {
        Boolean match = Pattern.matches(PUNCT_REGEX, text);
        if (match) {
            Pattern pattern = Pattern.compile(PUNCT_PATTERN);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                text = text.replace(matcher.group(), "");
                return text;
            }
        }
        return text;
    }

    /**
     * return list of parent folder from root to folderPath.
     *
     * @param folderPath path of folder
     * @param client     Alfresco client
     */
    public static ArrayList<FolderView> getAllParentFolder(String folderPath, CmisClient client) {
        ArrayList<FolderView> parents = new ArrayList<FolderView>();
        String treePath = "";
        for (String token : folderPath.split("/")) {
            treePath = treePath + token + "/";
            FolderView folderView = client.getFolder(treePath);
            parents.add(folderView);
        }
        return parents;
    }

}
