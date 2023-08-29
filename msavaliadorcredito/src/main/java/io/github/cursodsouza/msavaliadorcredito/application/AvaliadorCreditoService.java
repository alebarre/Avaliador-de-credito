package io.github.cursodsouza.msavaliadorcredito.application;

import feign.FeignException;
import io.github.cursodsouza.msavaliadorcredito.application.ex.DadosClienteNotFoundException;
import io.github.cursodsouza.msavaliadorcredito.application.ex.ErroComunicacoIcroservicesException;
import io.github.cursodsouza.msavaliadorcredito.application.ex.ErroSolicitacaoCartaoException;
import io.github.cursodsouza.msavaliadorcredito.domain.model.Cartao;
import io.github.cursodsouza.msavaliadorcredito.domain.model.CartaoAprovado;
import io.github.cursodsouza.msavaliadorcredito.domain.model.CartaoCliente;
import io.github.cursodsouza.msavaliadorcredito.domain.model.DadosCliente;
import io.github.cursodsouza.msavaliadorcredito.domain.model.DadosSolicitacaoEmissaoCartao;
import io.github.cursodsouza.msavaliadorcredito.domain.model.ProtocoloSolicitacaoCartao;
import io.github.cursodsouza.msavaliadorcredito.domain.model.RetornoAvaliacaoCliente;
import io.github.cursodsouza.msavaliadorcredito.domain.model.SituacaoCliente;
import io.github.cursodsouza.msavaliadorcredito.infra.clients.CartoesResourceClient;
import io.github.cursodsouza.msavaliadorcredito.infra.clients.ClienteResourceClient;
import io.github.cursodsouza.msavaliadorcredito.infra.mqueue.SolicitacaoEmissaoCartaoPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

    private final ClienteResourceClient clienteResourceClient;
    private final CartoesResourceClient cartoesResourceClient;
    private final SolicitacaoEmissaoCartaoPublisher emissaoCartaoPublisher;

    //Comunicação síncrona, simpples.
    // Obter dados do cliente - MSCLIENTE
    //obter cartões do Cliente - MSCARTOES
    public SituacaoCliente obterSituacaoCliente (String cpf) throws DadosClienteNotFoundException, ErroComunicacoIcroservicesException {

        try {
            ResponseEntity<DadosCliente> dadosClienteResponse = clienteResourceClient.dadosDoCliente(cpf);
            ResponseEntity<List<CartaoCliente>> dadosCartaoResponse = cartoesResourceClient.getCartoesByCliente(cpf);

            return SituacaoCliente
                    .builder()
                    .cliente(dadosClienteResponse.getBody())
                    .cartoes((dadosCartaoResponse.getBody()))
                    .build();
        }catch (FeignException e){
            int status = e.status();
            if (HttpStatus.NOT_FOUND.value() == status){
                throw new DadosClienteNotFoundException();
            }
            throw new ErroComunicacoIcroservicesException(e.getMessage(), status);
        }
    }
    
    public RetornoAvaliacaoCliente realizarAvaliacao(String cpf, Long renda) throws DadosClienteNotFoundException, ErroComunicacoIcroservicesException {
    	
        try { 
        	ResponseEntity<DadosCliente> dadosClienteResponse = clienteResourceClient.dadosDoCliente(cpf);
        	ResponseEntity<List<Cartao>> cartaoResponse = cartoesResourceClient.getCartoesRendaAte(renda);
        	
        	List<Cartao> cartoes = cartaoResponse.getBody();
        	var listaCartoesAprovados = cartoes.stream().map(cartao -> {
        		
        		DadosCliente dadosCliente = dadosClienteResponse.getBody();
        		
        		BigDecimal limiteBasico = cartao.getLimiteBasico();
        		BigDecimal idadeBD = BigDecimal.valueOf(dadosCliente.getIdade());
        		var fator = idadeBD.divide(BigDecimal.valueOf(10));
        		BigDecimal limiteAprovado = fator.multiply(limiteBasico);
        		
        		
        		CartaoAprovado cartaoAprovado = new CartaoAprovado();
        		cartaoAprovado.setNome(cartao.getNome());
        		cartaoAprovado.setBandeira(cartao.getBandeira());
        		cartaoAprovado.setLimiteAprovado(limiteAprovado);
        		
        		return cartaoAprovado;
        		
        	}).collect(Collectors.toList());
        	
        	return new RetornoAvaliacaoCliente(listaCartoesAprovados);
        	
        }catch (FeignException e){
            int status = e.status();
            if (HttpStatus.NOT_FOUND.value() == status){
                throw new DadosClienteNotFoundException();
            }
            throw new ErroComunicacoIcroservicesException(e.getMessage(), status);
        }
    }
    
    public ProtocoloSolicitacaoCartao solicitarEmissaoCartao(DadosSolicitacaoEmissaoCartao dados) {
    	try {
			emissaoCartaoPublisher.solicitaCartao(dados);
			var protocolo = UUID.randomUUID().toString();
			return new ProtocoloSolicitacaoCartao(protocolo);
		} catch (Exception e) {
			throw new ErroSolicitacaoCartaoException(e.getMessage());	
		}	
    }
    
    
}
