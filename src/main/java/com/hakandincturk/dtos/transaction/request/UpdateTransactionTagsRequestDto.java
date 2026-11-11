package com.hakandincturk.dtos.transaction.request;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Replaces the full tag set of a transaction; current tags missing from the request are unlinked.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionTagsRequestDto {
  private List<Long> tagIds = new ArrayList<>();

  /** Names typed by the user; a name matching an existing tag links that tag instead of creating a new one. */
  private List<String> newTags = new ArrayList<>();
}
