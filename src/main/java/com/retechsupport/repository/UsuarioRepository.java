package com.retechsupport.repository;

import com.retechsupport.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Usuario findByNombreAndPassword(String nombre, String password);
}