package io.github.cursodesouza.msclientes.application;

import com.fasterxml.jackson.annotation.OptBoolean;
import io.github.cursodesouza.msclientes.domain.Cliente;
import io.github.cursodesouza.msclientes.infra.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;

    public List<Cliente> getAllClientes (){
        return repository.findAll();
    }

    @Transactional
    public Cliente save (Cliente cliente){
        return  repository.save(cliente);
    }

    public Optional<Cliente> getByCpf(String cpf){
        return repository.findByCpf(cpf);
    }

}
