package com.retechsupport.controller;

import com.retechsupport.entity.Usuario;
import com.retechsupport.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping({"/", "/login"})
    public String index() {
        return "login";
    }

    @PostMapping("/login")
    public String autenticar(@RequestParam String nombre, @RequestParam String password, HttpSession session, Model model) {
        Usuario usuario = usuarioRepository.findByNombreAndPassword(nombre, password);
        if (usuario != null) {
            session.setAttribute("usuarioLogueado", usuario);
            if ("Admin".equals(usuario.getRol())) {
                return "redirect:/tickets/admin";
            } else {
                return "redirect:/tickets/soporte";
            }
        }
        model.addAttribute("error", "Usuario o contraseña incorrectos");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}