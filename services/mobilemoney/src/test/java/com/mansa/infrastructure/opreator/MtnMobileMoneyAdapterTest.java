// package com.mansa.infrastructure.opreator;

// package com.bankpayx.mobilemoney.infrastructure.operator;

// import com.bankpayx.mobilemoney.application.port.out.MobileMoneyOperatorPort;
// import com.bankpayx.mobilemoney.domain.exception.OperatorUnavailableException;
// import com.bankpayx.mobilemoney.domain.valueobject.*;
// import com.bankpayx.mobilemoney.infrastructure.operator.mtn.MtnApiClient;
// import com.bankpayx.mobilemoney.infrastructure.operator.mtn.MtnMobileMoneyAdapter;
// import com.bankpayx.mobilemoney.infrastructure.operator.mtn.config.MtnProperties;
// import com.bankpayx.mobilemoney.infrastructure.operator.mtn.dto.MtnPaymentResponse;
// import com.bankpayx.mobilemoney.infrastructure.operator.mtn.dto.MtnStatusResponse;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.math.BigDecimal;

// import static org.assertj.core.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class MtnMobileMoneyAdapterTest {

//     @Mock private MtnApiClient mtnApiClient;

//     private MtnProperties mtnProperties;
//     private MtnMobileMoneyAdapter adapter;

//     @BeforeEach
//     void setUp() {
//         mtnProperties = new MtnProperties(
//                 "https://sandbox.momodeveloper.mtn.com",
//                 "test-api-key",
//                 "test-api-secret",
//                 "test-sub-key",
//                 "https://api.bankpayx.com/callbacks/mtn",
//                 "sandbox",
//                 5000,
//                 30000
//         );
//         adapter = new MtnMobileMoneyAdapter(mtnApiClient, mtnProperties);
//     }

//     @Test
//     void shouldReturnMtnAsSupported() {
//         assertThat(adapter.getSupportedOperator()).isEqualTo(OperatorCode.MTN);
//     }

//     @Test
//     void shouldInitiatePaymentSuccessfully() {
//         when(mtnApiClient.requestToPay(any(), anyString()))
//                 .thenReturn(new MtnPaymentResponse("mtn-ref-test-001", "PENDING", null));

//         MobileMoneyOperatorPort.PaymentResult result = adapter.initiatePayment(
//                 TransactionId.generate(),
//                 PhoneNumber.of("+22507123456"),
//                 Money.of(new BigDecimal("5000.00"), "XOF"),
//                 "corr-001"
//         );

//         assertThat(result.success()).isTrue();
//         assertThat(result.operatorReference()).isNotNull();
//         assertThat(result.resultCode()).isEqualTo("PENDING");
//     }

//     @Test
//     void shouldThrowOperatorUnavailableWhenApiCallFails() {
//         when(mtnApiClient.requestToPay(any(), anyString()))
//                 .thenThrow(new RuntimeException("Connection refused"));

//         assertThatThrownBy(() -> adapter.initiatePayment(
//                 TransactionId.generate(),
//                 PhoneNumber.of("+22507123456"),
//                 Money.of(new BigDecimal("5000.00"), "XOF"),
//                 "corr-002"
//         )).isInstanceOf(OperatorUnavailableException.class)
//           .hasMessageContaining("MTN");
//     }

//     @Test
//     void shouldMapMtnSuccessfulStatusToSucceeded() {
//         when(mtnApiClient.getRequestToPayStatus("mtn-ref-001"))
//                 .thenReturn(new MtnStatusResponse(
//                         "5000", "XOF", "tx-ext-001", null,
//                         "SUCCESSFUL", null, "mtn-financial-001"
//                 ));

//         MobileMoneyOperatorPort.StatusResult result = adapter.checkPaymentStatus(
//                 TransactionId.generate(),
//                 OperatorReference.of("mtn-ref-001"),
//                 "corr-003"
//         );

//         assertThat(result.status()).isEqualTo(TransactionStatus.SUCCEEDED);
//         assertThat(result.operatorReference()).isPresent();
//         assertThat(result.operatorReference().get().value()).isEqualTo("mtn-financial-001");
//     }

//     @Test
//     void shouldMapMtnFailedStatusToFailed() {
//         when(mtnApiClient.getRequestToPayStatus("mtn-ref-002"))
//                 .thenReturn(new MtnStatusResponse(
//                         "5000", "XOF", "tx-ext-002", null,
//                         "FAILED",
//                         new MtnStatusResponse.MtnFailureReason("PAYER_LIMIT_REACHED", "Daily limit exceeded"),
//                         null
//                 ));

//         MobileMoneyOperatorPort.StatusResult result = adapter.checkPaymentStatus(
//                 TransactionId.generate(),
//                 OperatorReference.of("mtn-ref-002"),
//                 "corr-004"
//         );

//         assertThat(result.status()).isEqualTo(TransactionStatus.FAILED);
//         assertThat(result.resultCode()).isEqualTo("PAYER_LIMIT_REACHED");
//         assertThat(result.resultMessage()).isEqualTo("Daily limit exceeded");
//     }

//     @Test
//     void shouldReturnFalseForCancellation() {
//         boolean cancelled = adapter.cancelPayment(
//                 TransactionId.generate(),
//                 OperatorReference.of("mtn-ref-003"),
//                 "corr-005"
//         );
//         assertThat(cancelled).isFalse();
//     }
// }
