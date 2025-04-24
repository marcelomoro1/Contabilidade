package com.loja.service.impl;

import com.loja.model.Cliente;
import com.loja.repository.ClienteRepository;
import com.loja.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    @Override
    @Transactional
    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() == null && clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new RuntimeException("Já existe um cliente cadastrado com este CPF");
        }
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        clienteRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCpf(String cpf) {
        return clienteRepository.existsByCpf(cpf);
    }

    @Override
    @Transactional(readOnly = true)
    public long countClientes() {
        return clienteRepository.count();
    }
} 