package com.code.springmvcgenerator.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@Entity
public class Variable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dataType;
    private String name;
    private String namePlural;

    @ManyToOne
    @JoinColumn(name = "entity_detail_id")
    @JsonBackReference(value = "variableJbr")
    private EntityDetail entityDetail;
}