package com.mansa;

/**
 * Hello world!
 */


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.mansa.infrastructure.operator.cinetpay.config.CinetPayProperties;
import com.mansa.infrastructure.operator.mtn.config.MtnProperties;
import com.mansa.infrastructure.operator.notchpay.config.NotchPayProperties;
import com.mansa.infrastructure.operator.orange.config.OrangeProperties;
import com.mansa.infrastructure.operator.wave.config.WaveProperties;

@SpringBootApplication
//@ConfigurationPropertiesScan("com.bankpayx.mobilemoney")
@EnableConfigurationProperties({
    MtnProperties.class,
    OrangeProperties.class,
    WaveProperties.class,
    CinetPayProperties.class,
    NotchPayProperties.class
})
@EnableScheduling
public class MobileMoneyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MobileMoneyApplication.class, args);
    }
}
