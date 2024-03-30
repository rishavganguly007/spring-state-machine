package rg.springbootframework.statemachine.springstatemachine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import rg.springbootframework.statemachine.springstatemachine.domain.Payment;
import rg.springbootframework.statemachine.springstatemachine.domain.PaymentEvent;
import rg.springbootframework.statemachine.springstatemachine.domain.PaymentRepository;
import rg.springbootframework.statemachine.springstatemachine.domain.PaymentState;

import java.util.Optional;

@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {
    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message, Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine, StateMachine<PaymentState, PaymentEvent> rootStateMachine) {
        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(Long.class.cast(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L)))
                    .ifPresent(paymentId -> {
                        Payment payment = paymentRepository.getReferenceById(paymentId);
                        payment.setState(state.getId());
                        paymentRepository.save(payment);
                    });
        });
    }
}
