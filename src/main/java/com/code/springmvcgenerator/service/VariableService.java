package com.code.springmvcgenerator.service;

import com.code.springmvcgenerator.entity.Relation;
import com.code.springmvcgenerator.entity.Variable;
import com.code.springmvcgenerator.repository.RelationRepository;
import com.code.springmvcgenerator.repository.VariableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VariableService {

    private final VariableRepository variableRepository;

    @Autowired
    public VariableService(VariableRepository variableRepository) {
        this.variableRepository = variableRepository;
    }

    public Variable save(Variable variable) {
        return variableRepository.save(variable);
    }

    public Variable findById(Long id) {
        return variableRepository.findById(id).orElseThrow();
    }
}