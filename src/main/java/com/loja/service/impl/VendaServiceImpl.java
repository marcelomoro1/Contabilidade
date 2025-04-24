package com.loja.service.impl;

import com.loja.model.Venda;
import com.loja.repository.VendaRepository;
import com.loja.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VendaServiceImpl implements VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Override
    @Transactional
    public Venda salvar(Venda venda) {
        if (venda.getId() == null) {
            venda.setDataVenda(LocalDateTime.now());
        }
        venda.calculateTotalValue();
        return vendaRepository.save(venda);
    }

    @Override
    public List<Venda> listarTodas() {
        return vendaRepository.findAll();
    }

    @Override
    public List<Venda> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return vendaRepository.findByDataVendaBetweenOrderByDataVendaDesc(inicio, fim);
    }

    @Override
    public Venda buscarPorId(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        vendaRepository.deleteById(id);
    }

    @Override
    public Long countVendasByData(LocalDate data) {
        return vendaRepository.countByDataVenda(data);
    }

    @Override
    public Double calcularFaturamentoPorData(LocalDate data) {
        return vendaRepository.sumValorTotalByDataVenda(data);
    }
} 