package com.loja.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "patrimonio")
public class Patrimonio {
    @Id
    private int id;
    private String nome;
    private String descricao;
    private int quantidade;
    private BigDecimal valor;



}
