package com.tnf.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tnf.auth.entity.Credential;

public interface CredentialRepository extends MongoRepository<Credential, String> {
    boolean existsByEmail(String email);
    Optional<Credential> findByEmail(String email);
}
