package com.disenoweb.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.disenoweb.webapp.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
