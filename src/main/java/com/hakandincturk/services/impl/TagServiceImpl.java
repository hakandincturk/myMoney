package com.hakandincturk.services.impl;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hakandincturk.core.specs.TagSpecification;
import com.hakandincturk.dtos.tag.request.FilterListUserTags;
import com.hakandincturk.dtos.tag.response.ListUserTagsDto;
import com.hakandincturk.dtos.tag.response.ListUserTagsWithTransactionCountDto;
import com.hakandincturk.mappers.TagMapper;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.repositories.TagRepository;
import com.hakandincturk.repositories.TransactionTagRepository;
import com.hakandincturk.services.abstracts.TagService;
import com.hakandincturk.services.rules.TagRules;
import com.hakandincturk.utils.PaginationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

  private final TagRepository tagRepository;
  private final TransactionTagRepository transactionTagRepository;
  private final TagMapper tagMapper;
  private final TagRules tagRules;

  @Override
  public Page<ListUserTagsDto> listUserTags(Long userId, FilterListUserTags body) {

    Pageable pageable = PaginationUtils.toPageable(body);
    Specification<Tag> specs = TagSpecification.filter(userId, body);
    Page<Tag> tags = tagRepository.findAll(specs, pageable);

    return tags.map(tagMapper::toListUserTagsDto);
  }

  @Override
  public Page<ListUserTagsWithTransactionCountDto> listUserTagsWithTransactionCount(Long userId, FilterListUserTags body) {
    Pageable pageable = PaginationUtils.toPageable(body);
    Specification<Tag> specs = TagSpecification.filter(userId, body);
    Page<ListUserTagsWithTransactionCountDto> result = tagRepository.findWithTransactionCount(specs, pageable);
    return result;
  }

  @Override
  @Transactional
  public void deleteTag(Long userId, Long tagId) {
    Tag tag = tagRules.checkUserTagExistAndGet(userId, tagId);

    List<TransactionTag> transactionTags = transactionTagRepository.findAllByTagIdAndIsRemovedFalse(tagId);
    if (transactionTags != null && !transactionTags.isEmpty()) {
      transactionTags.forEach(transactionTag -> transactionTag.setRemoved(true));
      transactionTagRepository.saveAll(transactionTags);
    }

    tag.setRemoved(true);
    tagRepository.save(tag);
  }
}
