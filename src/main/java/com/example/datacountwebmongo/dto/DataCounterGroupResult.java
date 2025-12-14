package com.example.datacountwebmongo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataCounterGroupResult {
    private String source;           // "anayasa", "bam", "brsa", "ca", "cbrt"
    private String sourceName;       // "Anayasa Mahkemesi", "Bölge Adliye Mahkemesi" vs.
    private List<DataCounterResult> results;
    private Long totalWebCount;
    private Long totalDbCount;
    private Long totalDifference;
    private LocalDateTime fetchedAt;

    public static DataCounterGroupResult of(String source, String sourceName,
                                            List<DataCounterResult> results) {
        long totalWeb = results.stream()
                .filter(r -> r.getWebCount() != null)
                .mapToLong(DataCounterResult::getWebCount)
                .sum();

        long totalDb = results.stream()
                .filter(r -> r.getDbCount() != null)
                .mapToLong(DataCounterResult::getDbCount)
                .sum();

        return DataCounterGroupResult.builder()
                .source(source)
                .sourceName(sourceName)
                .results(results)
                .totalWebCount(totalWeb)
                .totalDbCount(totalDb)
                .totalDifference(totalWeb - totalDb)
                .fetchedAt(LocalDateTime.now())
                .build();
    }
}