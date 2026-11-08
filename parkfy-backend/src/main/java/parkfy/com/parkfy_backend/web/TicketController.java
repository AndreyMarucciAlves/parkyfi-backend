package parkfy.com.parkfy_backend.web;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import parkfy.com.parkfy_backend.dto.ticket.DadosTickectSaidaDto;
import parkfy.com.parkfy_backend.dto.ticket.TicketSaidaDto;
import parkfy.com.parkfy_backend.dto.ticket.TicketSaveDto;
import parkfy.com.parkfy_backend.service.TicketService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService tickectService;

    @PostMapping("/save")
    public ResponseEntity<Long> save(@Valid @RequestBody  TicketSaveDto ticketSaveDto) {
        Long id = tickectService.registrarEntrada(ticketSaveDto);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/get-dados-saida/{ticketId}")
    public ResponseEntity<DadosTickectSaidaDto> getDadosSaida (@PathVariable Long ticketId) {
        DadosTickectSaidaDto dadosSaida = tickectService.getDadosSaida(ticketId);
        return ResponseEntity.ok(dadosSaida);
    }

    @PostMapping("/registrar-saida")
    public ResponseEntity<Void> registrarSaida(@Valid @RequestBody TicketSaidaDto ticketSaidaDto) {
        tickectService.registrarSaida(ticketSaidaDto);
        return ResponseEntity.ok().build();
    }

}
