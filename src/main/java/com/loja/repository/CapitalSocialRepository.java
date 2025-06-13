package com.loja.repository;

import com.loja.model.CapitalSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CapitalSocialRepository extends JpaRepository<CapitalSocial, Long> {
    Optional<CapitalSocial> findTopByOrderByDataAberturaDesc();
}