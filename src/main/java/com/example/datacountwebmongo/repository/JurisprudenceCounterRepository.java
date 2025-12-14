package com.example.datacountwebmongo.repository;

import com.example.datacountwebmongo.entity.JurisprudenceEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface JurisprudenceCounterRepository extends MongoRepository<JurisprudenceEntity, String> {

    // === GENEL SORGULAR ===
    long countByDocType(String docType);
    long countByDocTypeAndAcType(String docType, String acType);
    long countByDocTypeAndCategory(String docType, String category);

    // === CA (Rekabet Kurumu) - safahatId exists/not exists ===
    @Query(value = "{ 'DocType': ?0, 'safahatId': { $exists: false } }", count = true)
    long countByDocTypeAndSafahatIdNotExists(String docType);

    @Query(value = "{ 'DocType': ?0, 'safahatId': { $exists: true } }", count = true)
    long countByDocTypeAndSafahatIdExists(String docType);
}