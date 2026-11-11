package com.hakandincturk.utils;

import java.util.Locale;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;

/**
 * Single source of the tag name normalization rule.
 * Two tags are the same when their normalized names match: "Aylık Abonelik" equals " aylık  abonelik".
 */
@UtilityClass
public class TagNameUtils {

  // Lower-cases Turkish letters correctly (İ -> i, I -> ı) regardless of the JVM default locale
  private static final Locale TURKISH_LOCALE = Locale.of("tr");

  private static final Pattern MULTIPLE_WHITESPACE = Pattern.compile("\\s+");

  /**
   * Display name to store: trimmed, inner whitespace collapsed to one space, letter case kept.
  */
  public static String clean(String name){
    if(name == null){
      return "";
    }

    return MULTIPLE_WHITESPACE.matcher(name.trim()).replaceAll(" ");
  }

  /**
   * Comparison key: the cleaned name lower-cased with Turkish rules.
  */
  public static String normalize(String name){
    return clean(name).toLowerCase(TURKISH_LOCALE);
  }

}
