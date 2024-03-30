package rg.springbootframework.statemachine.springstatemachine.services;

import org.springframework.statemachine.StateMachine;
import rg.springbootframework.statemachine.springstatemachine.domain.Payment;
import rg.springbootframework.statemachine.springstatemachine.domain.PaymentEvent;
import rg.springbootframework.statemachine.springstatemachine.domain.PaymentState;

public interface PaymentService {

    Payment newPayment(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);
}
