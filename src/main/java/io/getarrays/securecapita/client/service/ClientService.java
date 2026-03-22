package io.getarrays.securecapita.client.service;

import io.getarrays.securecapita.client.Client;
import io.getarrays.securecapita.client.dto.ClientDto;
import io.getarrays.securecapita.client.repository.ClientRepository;
import io.getarrays.securecapita.domain.PageResponseDto;
import io.getarrays.securecapita.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Transactional
    public ClientDto createClient(ClientDto clientDto) {
        Client client = createOrUpdateClient(null, clientDto);
        return entityToDto(clientRepository.save(client));
    }

    public PageResponseDto<ClientDto> getAllClients(int page, int size) {
        Page<Client> clientPage = clientRepository.findAll(PageRequest.of(page, size));
        return new PageResponseDto<>(
                clientPage.getContent().stream().map(this::entityToDto).toList(),
                clientPage
        );
    }

    public ClientDto getClientById(Long id) {
        return entityToDto(findClientByIdOrThrow(id));
    }

    @Transactional
    public ClientDto updateClient(Long id, ClientDto clientDto) {
        Client client = createOrUpdateClient(id, clientDto);
        return entityToDto(clientRepository.save(client));
    }

    @Transactional
    public boolean deleteClient(Long id) {
        if (clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
            return true;
        }
        throw new ResourceNotFoundException("Client not found with id " + id);
    }

    private Client createOrUpdateClient(Long id, ClientDto dto) {
        Client client = null;
        if (id != null) {
            client = findClientByIdOrThrow(id);
        }
        if (dto != null) {
            if (client == null) {
                client = new Client();
            }
            client.setName(dto.getName());
            client.setSurname(dto.getSurname());
            client.setPhoneNumber(dto.getPhoneNumber());
        }
        return client;
    }

    private ClientDto entityToDto(Client entity) {
        return ClientDto.toDto(entity);
    }

    private Client findClientByIdOrThrow(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id " + id));
    }
}

