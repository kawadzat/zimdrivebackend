package io.getarrays.securecapita.client.controller;

import io.getarrays.securecapita.client.dto.ClientDto;
import io.getarrays.securecapita.client.service.ClientService;
import io.getarrays.securecapita.domain.PageResponseDto;
import io.getarrays.securecapita.exception.CustomMessage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    // Create a new client
    @PostMapping
    public ResponseEntity<ClientDto> createClient(@Valid @RequestBody ClientDto clientDto) {
        ClientDto createdClient = clientService.createClient(clientDto);
        return new ResponseEntity<>(createdClient, HttpStatus.CREATED);
    }

    // Get all clients with pagination
    @GetMapping
    public ResponseEntity<PageResponseDto<ClientDto>> getAllClients(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDto<ClientDto> clients = clientService.getAllClients(page, size);
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }

    // Get client by id
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id) {
        return new ResponseEntity<>(clientService.getClientById(id), HttpStatus.OK);
    }

    // Update client by id
    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable Long id,
                                                   @Valid @RequestBody ClientDto clientDto) {
        ClientDto updatedClient = clientService.updateClient(id, clientDto);
        return new ResponseEntity<>(updatedClient, HttpStatus.OK);
    }

    // Delete client by id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return new ResponseEntity<>(new CustomMessage("Client deleted successfully"), HttpStatus.OK);
    }
}


