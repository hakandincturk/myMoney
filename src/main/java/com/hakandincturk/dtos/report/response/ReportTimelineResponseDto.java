package com.hakandincturk.dtos.report.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Aylık gelir/gider serisi. Aralıktaki her ay dolu gelir, veri olmayan aylar sıfırlı kayıttır.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportTimelineResponseDto {

  private List<TimelinePointDto> points;

}
