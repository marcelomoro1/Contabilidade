package com.trabalho.apihamburgueria.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "produto")
public class Produto {

    public Produto() {
    }

    public Produto(Long id, String nome, String descricao, String imagem, Double valor, int quantidade) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.imagem = imagem;
        this.valor = valor;
        this.quantidade = quantidade;
    }

    public Produto(String nome, String descricao, Double valor, String imagem, int quantidade) {
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
        this.imagem = imagem;
        this.quantidade = quantidade;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private Double valor;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false)
    private String imagem;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", valor=" + valor +
                ", quantidade=" + quantidade +
                ", imagem='" + imagem + '\'' +
                '}';
    }
}
