package com.mansa.config;


import com.mansa.infrastructure.operator.cinetpay.CinetPayFallbackAdapter;
import com.mansa.application.port.out.MobileMoneyOperatorPort;
import com.mansa.domain.valueobject.OperatorCode;
// import com.mansa.infrastructure.operator.cinetpay.CinetPayFallbackAdapter;
import com.mansa.infrastructure.operator.mtn.MtnMobileMoneyAdapter;
import com.mansa.infrastructure.operator.notchpay.NotchPayMobileMoneyAdapter;
import com.mansa.infrastructure.operator.orange.OrangeMobileMoneyAdapter;
import com.mansa.infrastructure.operator.wave.WaveMobileMoneyAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Registers all operator adapters in the routing map.
 * To add a new operator: create an adapter implementing MobileMoneyOperatorPort
 * and add it here. The application service requires no modification.
 * This is the Open/Closed Principle at the configuration level.
 */
@Configuration
public class OperatorRouterConfig {

    @Bean
    public Map<OperatorCode, MobileMoneyOperatorPort> operatorAdapters(
            MtnMobileMoneyAdapter mtnAdapter,
            OrangeMobileMoneyAdapter orangeAdapter,
            WaveMobileMoneyAdapter waveAdapter,
            CinetPayFallbackAdapter cinetPayAdapter,
            NotchPayMobileMoneyAdapter notchpayAdapter  ) {

        return Map.of(
                OperatorCode.MTN, mtnAdapter,
                OperatorCode.ORANGE, orangeAdapter,
                OperatorCode.WAVE, waveAdapter,
                OperatorCode.CINETPAY, cinetPayAdapter,
                OperatorCode.NOTCHPAY, notchpayAdapter 
        );
    }
}
