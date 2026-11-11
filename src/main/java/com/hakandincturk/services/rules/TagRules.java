package com.hakandincturk.services.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.ConflictException;
import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Tag;
import com.hakandincturk.repositories.TagRepository;
import com.hakandincturk.utils.TagNameUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagRules {

  private final TagRepository tagRepository;

  public Tag checkTagExistAndGet(Long id){
    Optional<Tag> dbTag = tagRepository.findByIdAndIsRemovedFalse(id);
    if(dbTag.isEmpty()){
      throw new NotFoundException("Etiket bulunamadı");
    }
    return dbTag.get();
  }

  public Tag checkUserTagExistAndGet(Long userId, Long tagId){
    Optional<Tag> dbTag = tagRepository.findByIdAndUserIdAndIsRemovedFalse(tagId, userId);
    if(dbTag.isEmpty()){
      throw new NotFoundException("Etiket bulunamadı");
    }
    return dbTag.get();
  }

  public List<Tag> checkAllIdsAndGet(List<Long> ids){
    if(ids == null || ids.isEmpty()){
        return new ArrayList<>();
    }

    List<Tag> dbTags = tagRepository.findAllByIdInAndIsRemovedFalse(ids);
    if(dbTags.size() != ids.size()){
      throw new NotFoundException("Aranan etiketlerin hepsi bulunamadı");
    }

    return dbTags;
  }

  /**
   * Throws when any id does not belong to the user, so a transaction can never be linked to another user's tag.
   * Repeated ids are not an error.
  */
  public List<Tag> checkAllUserTagIdsAndGet(Long userId, List<Long> ids){
    if(ids == null || ids.isEmpty()){
      return new ArrayList<>();
    }

    List<Long> distinctIds = ids.stream().distinct().toList();
    List<Tag> dbTags = tagRepository.findAllByIdInAndUserIdAndIsRemovedFalse(distinctIds, userId);
    if(dbTags.size() != distinctIds.size()){
      throw new NotFoundException("Aranan etiketlerin hepsi bulunamadı");
    }

    return dbTags;
  }

  /**
   * Throws when the user already has an active tag with the same name, ignoring case and extra whitespace.
   * @param excludedTagId The tag being renamed, so it does not collide with itself; null for a new tag
  */
  public void checkTagNameNotDuplicate(Long userId, String name, Long excludedTagId){
    String normalizedName = TagNameUtils.normalize(name);

    boolean exists = tagRepository.findAllByUserIdAndIsRemovedFalse(userId).stream()
      .filter(tag -> excludedTagId == null || !excludedTagId.equals(tag.getId()))
      .anyMatch(tag -> TagNameUtils.normalize(tag.getName()).equals(normalizedName));

    if(exists){
      throw new ConflictException("Bu isimde bir etiket zaten var");
    }
  }
}
