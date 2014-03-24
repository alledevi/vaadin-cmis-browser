package com.github.atave.VaadinCmisBrowser.vaadin.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	public static String DIGIT_PATTERN = "(\\d+)"; // find one or more digit
	public static String BRACKET_PATTERN = "(\\p{Punct})"; // find one punctuation character like ",&(! 
	public static String PUNCT_PATTERN = "(\\p{Punct}+)"; // find one or more punctuation character
	public static String PUNCT_REGEX = PUNCT_PATTERN + ".*" ; // find one or more punctuation character follow by char or digit
	public static String VERSION_PATTERN = BRACKET_PATTERN + DIGIT_PATTERN + BRACKET_PATTERN; // (1) (2) (12)
	public static String VERSION_REGEX = ".*" + VERSION_PATTERN; // name(1), name(2)


	public static String renameFolder(String name){
		Boolean match = Pattern.matches(VERSION_REGEX, name);
		Pattern pattern = null;
		Matcher matcher = null;
		
		if(match){ // do another copy
			
			String version = null;
			pattern = Pattern.compile(DIGIT_PATTERN);
			matcher = pattern.matcher(name);
			// find new version
			if (matcher.find()) {
				version = matcher.group();
			} 					
			Integer i = Integer.valueOf(version.toString());
			i ++;
			// delete old version
			pattern = Pattern.compile(VERSION_PATTERN);
			matcher = pattern.matcher(name);
			if (matcher.find()) {
				name = name.replace(matcher.group(), "");
			} 		
			name = name + "(" + i + ")";
		} else{
			// do first copy
			name = name + "(1)";
		}
		return name;
	}

	// clean string if starts with punctuation character
	public static String startsWithPunct(String text){
		Boolean match = Pattern.matches(PUNCT_REGEX, text);
		if(match){
			Pattern pattern = Pattern.compile(PUNCT_PATTERN);
			Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				text = text.replace(matcher.group(), "");
				return text;
			} 			
		}
		return text;
	}

}
