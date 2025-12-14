package com.example.datacountwebmongo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Field;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class BaseEntity {

    // MongoDB field mapping - Python'daki field isimleriyle eşleşmeli
    @Field("DocType")
    private String docType;

    @Field("acType")
    private String acType;

    @Field("Category")
    private String category;

    @Field("safahatId")
    private String safahatId;

    private DocLocation docLocation;
}