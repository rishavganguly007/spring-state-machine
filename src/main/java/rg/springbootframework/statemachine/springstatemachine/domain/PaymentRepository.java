package rg.springbootframework.statemachine.springstatemachine.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
