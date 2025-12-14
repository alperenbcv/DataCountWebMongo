package com.example.datacountwebmongo.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Document(collection = "jurisprudence")
public class JurisprudenceEntity extends BaseEntity {

    @Id
    private String id;

}