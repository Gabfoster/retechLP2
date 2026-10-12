package com.retechsupport.service;

import com.retechsupport.entity.*;
import com.retechsupport.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private HistorialTicketRepository historialRepository;
    @Autowired
    private EstadoRepository estadoRepository;
    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Transactional
    public void atenderTicket(Integer idTicket, Integer idEstado, Integer idTecnico, String observacion, String usuarioLogueado) {
        Ticket ticket = ticketRepository.findById(idTicket).orElseThrow();
        Estado estado = estadoRepository.findById(idEstado).orElseThrow();
        Tecnico tecnico = tecnicoRepository.findById(idTecnico).orElseThrow();

        ticket.setEstado(estado);
        ticket.setTecnico(tecnico);
        ticket.setObservacionSoporte(observacion);
        ticket.setFechaAtencion(LocalDateTime.now());
        ticketRepository.save(ticket);

        HistorialTicket historial = new HistorialTicket(ticket, estado, LocalDateTime.now(), observacion, usuarioLogueado);
        historialRepository.save(historial);
    }
}