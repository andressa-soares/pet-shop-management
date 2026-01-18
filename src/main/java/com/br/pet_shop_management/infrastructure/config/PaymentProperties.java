package com.br.pet_shop_management.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "payment.card")
public class PaymentProperties {
    private BigDecimal interestPerExtraInstallment = new BigDecimal("0.02");
}