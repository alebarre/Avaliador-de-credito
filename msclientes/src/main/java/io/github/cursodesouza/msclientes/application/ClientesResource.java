package io.github.cursodesouza.msclientes.application;

import io.github.cursodesouza.msclientes.application.representation.ClienteSaveRequest;
import io.github.cursodesouza.msclientes.domain.Cliente;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("clientes")
public class ClientesResource {

    private int cont = 0;

    private final ClienteService service;

    @GetMapping("/todos")
    public List<Cliente> todosClientes (){
        return service.getAllClientes();
    }

    @GetMapping
    public String status(){
        this.cont++;
        log.info("Obtendo status do microservice de Clientes..." + this.cont);
        return "ok";
    }

    @PostMapping
    public ResponseEntity save (@RequestBody ClienteSaveRequest request){
        Cliente cliente = request.toModel();
        service.save(cliente);
        URI headerLocation = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .query("cpf={cpf}")
                .buildAndExpand(cliente.getCpf())
                .toUri();
        return ResponseEntity.created(headerLocation).build();
    }

    @GetMapping(params="cpf")
    public ResponseEntity dadosDoCliente(@RequestParam("cpf") String cpf){
        var cliente = service.getByCpf(cpf);
        if(cliente.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cliente);
    }
}
