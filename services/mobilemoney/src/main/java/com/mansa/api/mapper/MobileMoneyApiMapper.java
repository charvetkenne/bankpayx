package com.mansa.api.mapper;

import com.mansa.api.response.*;
import com.mansa.application.usecase.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MobileMoneyApiMapper {

    @Mapping(target = "message", expression = "java(result.isDuplicate() ? \"Duplicate request — existing transaction returned\" : \"Payment initiated successfully\")")
    PaymentInitiationResponse toResponse(InitiatePaymentUseCase.InitiatePaymentResult result);

    TransactionStatusResponse toResponse(CheckTransactionStatusUseCase.TransactionStatusResult result);

}
