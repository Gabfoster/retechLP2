package com.retechsupport.controller;

import com.retechsupport.entity.*;
import com.retechsupport.repository.*;
import com.retechsupport.service.TicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private EstadoRepository estadoRepository;
    @Autowired
    private TecnicoRepository tecnicoRepository;
    @Autowired
    private HistorialTicketRepository historialRepository;
    @Autowired
    private TicketService ticketService;

    // Vista Administrador: Listar tickets
    @GetMapping("/admin")
    public String listarAdmin(Model model, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null || !"Admin".equals(user.getRol())) return "redirect:/login";

        List<Ticket> tickets = ticketRepository.findAll();
        model.addAttribute("tickets", tickets);
        model.addAttribute("usuario", user);
        return "admin-tickets";
    }

    // Vista Técnico de Soporte: Listar tickets asignados
    @GetMapping("/soporte")
    public String listarSoporte(Model model, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null || !"Soporte".equals(user.getRol())) return "redirect:/login";

        List<Ticket> tickets = ticketRepository.findAll();
        model.addAttribute("tickets", tickets);
        model.addAttribute("usuario", user);
        return "soporte-tickets";
    }

    // Formulario Nuevo Ticket (Admin)
    @GetMapping("/nuevo")
    public String nuevoTicketForm(Model model, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null || !"Admin".equals(user.getRol())) return "redirect:/login";

        model.addAttribute("ticket", new Ticket());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("tecnicos", tecnicoRepository.findAll());
        model.addAttribute("estados", estadoRepository.findAll());
        return "nuevo-ticket";
    }

    // Guardar Nuevo Ticket (Admin)
    @PostMapping("/guardar")
    public String guardarTicket(@ModelAttribute Ticket ticket, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/login";

        ticket.setNumeroTicket("RT00000" + (ticketRepository.count() + 1) + ".26");
        ticket.setFechaCreacion(LocalDateTime.now());
        
        Estado estadoPendiente = estadoRepository.findById(1).orElse(null);
        ticket.setEstado(estadoPendiente);
        ticket.setUsuarioCreador(user);

        Ticket savedTicket = ticketRepository.save(ticket);

        // Registro inicial en el historial
        HistorialTicket historial = new HistorialTicket(savedTicket, estadoPendiente, LocalDateTime.now(), "Ticket registrado", user.getNombre());
        historialRepository.save(historial);

        return "redirect:/tickets/admin";
    }

    // Ver Detalle e Historial
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Integer id, Model model, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/login";

        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        List<HistorialTicket> historial = historialRepository.findByTicketIdOrderByFechaCambioDesc(id);

        model.addAttribute("ticket", ticket);
        model.addAttribute("historial", historial);
        return "detalle-ticket";
    }

    // Formulario Atender Ticket (Caso Transaccional)
    @GetMapping("/atender/{id}")
    public String atenderForm(@PathVariable Integer id, Model model, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/login";

        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        List<HistorialTicket> historial = historialRepository.findByTicketIdOrderByFechaCambioDesc(id);

        model.addAttribute("ticket", ticket);
        model.addAttribute("estados", estadoRepository.findAll());
        model.addAttribute("tecnicos", tecnicoRepository.findAll());
        model.addAttribute("historial", historial);
        return "atender-ticket";
    }

    // Procesar Atención de Ticket (Invoca el servicio @Transactional)
    @PostMapping("/atender")
    public String procesarAtencion(@RequestParam Integer id,
                                   @RequestParam Integer idEstado,
                                   @RequestParam Integer idTecnico,
                                   @RequestParam String observacion,
                                   HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/login";

        ticketService.atenderTicket(id, idEstado, idTecnico, observacion, user.getNombre());

        if ("Admin".equals(user.getRol())) {
            return "redirect:/tickets/admin";
        } else {
            return "redirect:/tickets/soporte";
        }
    }

    // Eliminar Ticket
    @GetMapping("/eliminar/{id}")
    public String eliminarTicket(@PathVariable Integer id, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null || !"Admin".equals(user.getRol())) return "redirect:/login";

        List<HistorialTicket> hist = historialRepository.findByTicketIdOrderByFechaCambioDesc(id);
        historialRepository.deleteAll(hist);
        ticketRepository.deleteById(id);

        return "redirect:/tickets/admin";
    }
}