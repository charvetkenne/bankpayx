// package com.mansa.infrastructure.persistence;


// import com.mansa.domain.aggregate.MobileMoneyTransaction;
// import com.mansa.domain.valueobject.*;
// import com.mansa.infrastructure.persistence.adapter.TransactionPersistenceAdapter;
// import com.mansa.infrastructure.persistence.repository.TransactionJpaRepository;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.context.annotation.Import;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.testcontainers.containers.PostgreSQLContainer;
// import org.testcontainers.junit.jupiter.Container;
// import org.testcontainers.junit.jupiter.Testcontainers;

// import java.math.BigDecimal;
// import java.util.Optional;

// import static org.assertj.core.api.Assertions.*;

// @DataJpaTest
// @Testcontainers
// @ActiveProfiles("test")
// @Import(TransactionPersistenceAdapter.class)
// class TransactionPersistenceAdapterTest {

//     @Container
//     static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
//             .withDatabaseName("mobilemoney_test")
//             .withUsername("test_user")
//             .withPassword("test_pass");

//     @DynamicPropertySource
//     static void configureProperties(DynamicPropertyRegistry registry) {
//         registry.add("spring.datasource.url", postgres::getJdbcUrl);
//         registry.add("spring.datasource.username", postgres::getUsername);
//         registry.add("spring.datasource.password", postgres::getPassword);
//         registry.add("spring.flyway.enabled", () -> "true");
//     }

//     @Autowired
//     private TransactionPersistenceAdapter adapter;

//     @Autowired
//     private TransactionJpaRepository jpaRepository;

//     @Test
//     void shouldSaveAndRetrieveTransaction() {
//         MobileMoneyTransaction tx = MobileMoneyTransaction.initiate(
//                 PhoneNumber.of("+22507654321"),
//                 Money.of(new BigDecimal("25000.00"), "XOF"),
//                 OperatorCode.MTN,
//                 "customer-persist-001",
//                 IdempotencyKey.of("persist-idem-001"),
//                 "corr-persist-001"
//         );

//         MobileMoneyTransaction saved = adapter.save(tx);
//         Optional<MobileMoneyTransaction> found = adapter.findById(saved.getId());

//         assertThat(found).isPresent();
//         MobileMoneyTransaction retrieved = found.get();
//         assertThat(retrieved.getId()).isEqualTo(saved.getId());
//         assertThat(retrieved.getPhoneNumber().value()).isEqualTo("+22507654321");
//         assertThat(retrieved.getAmount().amount()).isEqualByComparingTo(new BigDecimal("25000.00"));
//         assertThat(retrieved.getStatus()).isEqualTo(TransactionStatus.INITIATED);
//         assertThat(retrieved.getOperatorCode()).isEqualTo(OperatorCode.MTN);
//     }

//     @Test
//     void shouldFindByIdempotencyKey() {
//         IdempotencyKey key = IdempotencyKey.of("find-by-idem-002");

//         MobileMoneyTransaction tx = MobileMoneyTransaction.initiate(
//                 PhoneNumber.of("+22507111111"),
//                 Money.of(new BigDecimal("1000.00"), "XOF"),
//                 OperatorCode.WAVE,
//                 "customer-002",
//                 key,
//                 "corr-002"
//         );

//         adapter.save(tx);

//         Optional<MobileMoneyTransaction> found = adapter.findByIdempotencyKey(key);
//         assertThat(found).isPresent();
//         assertThat(found.get().getIdempotencyKey()).isEqualTo(key);
//     }

//     @Test
//     void shouldReturnEmptyWhenTransactionNotFound() {
//         Optional<MobileMoneyTransaction> result =
//                 adapter.findById(TransactionId.generate());
//         assertThat(result).isEmpty();
//     }

//     @Test
//     void shouldPersistStatusTransitions() {
//         MobileMoneyTransaction tx = MobileMoneyTransaction.initiate(
//                 PhoneNumber.of("+22507222222"),
//                 Money.of(new BigDecimal("5000.00"), "XOF"),
//                 OperatorCode.ORANGE,
//                 "customer-003",
//                 IdempotencyKey.of("transition-idem-003"),
//                 "corr-003"
//         );

//         adapter.save(tx);
//         tx.markAsPending();
//         adapter.save(tx);

//         tx.markAsProcessing();
//         adapter.save(tx);

//         tx.markAsSucceeded(OperatorReference.of("orange-ref-final"));
//         adapter.save(tx);

//         Optional<MobileMoneyTransaction> found = adapter.findById(tx.getId());
//         assertThat(found).isPresent();
//         assertThat(found.get().getStatus()).isEqualTo(TransactionStatus.SUCCEEDED);
//         assertThat(found.get().getOperatorReference().value()).isEqualTo("orange-ref-final");
//         assertThat(found.get().getCompletedAt()).isNotNull();
//     }

//     @Test
//     void shouldReturnTrueWhenIdempotencyKeyExists() {
//         IdempotencyKey key = IdempotencyKey.of("exists-idem-004");
//         MobileMoneyTransaction tx = MobileMoneyTransaction.initiate(
//                 PhoneNumber.of("+22507333333"),
//                 Money.of(new BigDecimal("2000.00"), "XOF"),
//                 OperatorCode.MTN,
//                 "customer-004",
//                 key,
//                 "corr-004"
//         );
//         adapter.save(tx);

//         assertThat(adapter.existsByIdempotencyKey(key)).isTrue();
//         assertThat(adapter.existsByIdempotencyKey(IdempotencyKey.of("non-existent-key"))).isFalse();
//     }
// }
