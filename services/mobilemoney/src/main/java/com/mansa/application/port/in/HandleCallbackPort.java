package com.mansa.application.port.in;

import com.mansa.application.usecase.HandleCallbackUseCase;

// import com.mansa.application.usecase.HandleCallbackUseCase;

public interface HandleCallbackPort {
    void handleCallback(HandleCallbackUseCase.CallbackCommand command);
}