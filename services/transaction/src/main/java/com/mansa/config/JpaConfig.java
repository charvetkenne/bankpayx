package com.mansa.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.mansa.adapters.outbound.persistence")
@EnableJpaRepositories(basePackages = "com.mansa.adapters.outbound.persistence")
public class JpaConfig {}
