package com.hakandincturk.factories;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Users;
import com.hakandincturk.utils.TagNameUtils;

@Component
public class TagFactory {

  public Tag createTag(String name, Users user){
    Tag tag = new Tag();
    tag.setName(TagNameUtils.clean(name));
    tag.setUser(user);
    return tag;
  }

  /**
   * Turns the tag names typed by the user into tags.
   * A name matching an existing tag (after normalization) reuses that tag instead of creating a duplicate,
   * and names repeated in the same request collapse into one tag. Blank names are skipped.
   * Tags without an id in the result are new; saving them is the caller's job.
   * @param names New tag names from the request
   * @param existingUserTags The user's active tags
  */
  public List<Tag> resolveTagsByName(List<String> names, List<Tag> existingUserTags, Users user){
    if(names == null || names.isEmpty()){
      return new ArrayList<>();
    }

    Map<String, Tag> tagsByNormalizedName = new LinkedHashMap<>();
    for (Tag existingTag : existingUserTags) {
      tagsByNormalizedName.putIfAbsent(TagNameUtils.normalize(existingTag.getName()), existingTag);
    }

    Map<String, Tag> resolvedTags = new LinkedHashMap<>();
    for (String name : names) {
      String normalizedName = TagNameUtils.normalize(name);
      if(normalizedName.isEmpty() || resolvedTags.containsKey(normalizedName)){
        continue;
      }

      Tag tag = tagsByNormalizedName.computeIfAbsent(normalizedName, key -> this.createTag(name, user));
      resolvedTags.put(normalizedName, tag);
    }

    return new ArrayList<>(resolvedTags.values());
  }

}
