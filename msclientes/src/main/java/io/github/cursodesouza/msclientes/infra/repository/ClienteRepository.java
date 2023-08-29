package io.github.cursodesouza.msclientes.infra.repository;

import io.github.cursodesouza.msclientes.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCpf(String cpf);

    @GetMapping
    List<Cliente> findAll();
}
