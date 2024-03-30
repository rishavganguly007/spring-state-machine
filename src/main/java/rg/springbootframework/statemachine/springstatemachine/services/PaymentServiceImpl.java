package rg.springbootframework.statemachine.springstatemachine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import rg.springbootframework.statemachine.springstatemachine.domain.Payment;
import rg.springbootframework.statemachine.springstatemachine.domain.PaymentEvent;
import rg.springbootframework.statemachine.springstatemachine.domain.PaymentRepository;
import rg.springbootframework.statemachine.springstatemachine.domain.PaymentState;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_ID_HEADER = "payment_id" ;
    @Autowired
    final PaymentRepository paymentRepository;
    @Autowired
    final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    @Autowired
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;
    @Override
    public Payment newPayment(Payment payment) {

        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {

        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTH_APPROVED);
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {

        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);return null;
    }

    private StateMachine<PaymentState,PaymentEvent> build(Long paymentId) {
        Payment payment = paymentRepository.getReferenceById(paymentId);
        StateMachine<PaymentState,PaymentEvent> sm = stateMachineFactory.getStateMachine(Long.toString(payment.getId()));
        sm.stopReactively();
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    sma.resetStateMachineReactively(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
                });
        sm.startReactively();
        return sm;
    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent pe) {
        Message msg = MessageBuilder.withPayload(paymentId)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();
        sm.sendEvent(msg);
    }
}
