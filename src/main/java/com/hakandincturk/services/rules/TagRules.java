package com.hakandincturk.services.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Tag;
import com.hakandincturk.repositories.TagRepository;

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
}
