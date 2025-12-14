package com.example.datacountwebmongo.repository;

import com.example.datacountwebmongo.entity.YargitayEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface YargitayRepository extends MongoRepository<YargitayEntity, String> {
    // estimated_document_count() -> count() kullanılacak
}