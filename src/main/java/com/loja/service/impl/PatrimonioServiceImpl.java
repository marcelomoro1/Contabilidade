package com.loja.service.impl;

import com.loja.model.Patrimonio;
import com.loja.repository.PatrimonioRepository;
import com.loja.service.PatrimonioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PatrimonioServiceImpl implements PatrimonioService {

    @Autowired
    private PatrimonioRepository patrimonioRepository;

    @Override
    @Transactional
    public Patrimonio salvar(Patrimonio patrimonio) {
        // Garante que o valor total é calculado antes de salvar
        if (patrimonio.getValorUnitario() != null && patrimonio.getQuantidade() != null) {
            patrimonio.setValorTotal(patrimonio.getValorUnitario().multiply(BigDecimal.valueOf(patrimonio.getQuantidade())));
        } else {
            patrimonio.setValorTotal(BigDecimal.ZERO); // Ou lance uma exceção, se esses campos forem obrigatórios
        }
        return patrimonioRepository.save(patrimonio);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        patrimonioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patrimonio> listarTodos() {
        return patrimonioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patrimonio> buscarPorId(Long id) {
        return patrimonioRepository.findById(id);
    }
}