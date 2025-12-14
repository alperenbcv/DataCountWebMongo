package com.example.datacountwebmongo.entity;

import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Document(collection = "resmiGazeteOcr")
public class ResmiGazeteEntity extends BaseEntity {
    @Id
    public String _id;
}
