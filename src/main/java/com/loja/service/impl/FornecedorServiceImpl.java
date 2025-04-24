package com.loja.service.impl;

import com.loja.model.Fornecedor;
import com.loja.repository.FornecedorRepository;
import com.loja.service.FornecedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FornecedorServiceImpl implements FornecedorService {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Fornecedor> listarTodos() {
        return fornecedorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Fornecedor buscarPorId(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
    }

    @Override
    @Transactional
    public Fornecedor salvar(Fornecedor fornecedor) {
        if (fornecedor.getId() == null && fornecedorRepository.existsByCnpj(fornecedor.getCnpj())) {
            throw new RuntimeException("CNPJ já cadastrado");
        }
        return fornecedorRepository.save(fornecedor);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        fornecedorRepository.deleteById(id);
    }
} 