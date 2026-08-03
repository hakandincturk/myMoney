package com.hakandincturk.dtos.report.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Liste kalemlerinin üzerinde gösterilen sade etiket referansı.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportTagRefDto {

  private Long id;
  private String name;

}
