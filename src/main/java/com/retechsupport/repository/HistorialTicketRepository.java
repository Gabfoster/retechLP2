package com.retechsupport.repository;

import com.retechsupport.entity.HistorialTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialTicketRepository extends JpaRepository<HistorialTicket, Integer> {
    List<HistorialTicket> findByTicketIdOrderByFechaCambioDesc(Integer ticketId);
}