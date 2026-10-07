package com.mansa.infrastructure.persistence.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
//@EnableJpaRepositories(basePackages = "com.mansa.infrastructure.persistence.repository")
@EnableJpaRepositories(basePackages = "com.mansa.infrastructure")   // couvre Stripe ET PayPal
@EntityScan(basePackages             = "com.mansa.infrastructure")   // couvre TransactionEntity ET PayPalOrderEntity
public class JpaConfiguration {
}
