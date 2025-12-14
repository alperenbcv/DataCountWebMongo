package com.example.datacountwebmongo.dto;

import com.example.datacountwebmongo.entity.DocLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataCounterResult {
    private String docType;
    private String subType;
    private String description;
    private Long webCount;
    private Long dbCount;
    private Long difference;
    private String status;

    public static DataCounterResult of(String docType, String subType, String description,
                                       Long webCount, Long dbCount) {
        long diff = (webCount != null && dbCount != null) ? webCount - dbCount : 0;
        String status = "UNKNOWN";

        if (webCount != null && dbCount != null) {
            if (diff == 0) {
                status = "SYNCED";
            } else if (diff > 0) {
                status = "MISSING_IN_DB";
            } else {
                status = "EXTRA_IN_DB";
            }
        }

        return DataCounterResult.builder()
                .docType(docType)
                .subType(subType)
                .description(description)
                .webCount(webCount)
                .dbCount(dbCount)
                .difference(diff)
                .status(status)
                .build();
    }
}