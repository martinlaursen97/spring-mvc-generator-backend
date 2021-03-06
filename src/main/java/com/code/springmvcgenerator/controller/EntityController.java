package com.code.springmvcgenerator.controller;

import com.code.springmvcgenerator.entity.Entity;
import com.code.springmvcgenerator.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entities")
@CrossOrigin
public class EntityController {

    private final EntityService entityService;

    @Autowired
    public EntityController(EntityService entityService) {
        this.entityService = entityService;
    }

    @PostMapping
    public ResponseEntity<Entity> create(@RequestBody Entity entity) {
        Entity savedEntity = entityService.save(entity);
        return ResponseEntity.ok()
                .body(savedEntity);
    }

    @GetMapping
    public ResponseEntity<List<Entity>> fetchEntities() {
        List<Entity> entities = entityService.findAll();
        return ResponseEntity.ok()
                .body(entities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entity> fetchById(@PathVariable Long id) {
        Entity foundEntity = entityService.findById(id);
        return ResponseEntity.ok()
                .body(foundEntity);
    }

    @GetMapping("/project/{id}")
    public ResponseEntity<List<Entity>> fetchEntitiesByProjectId(@PathVariable Long id) {
        List<Entity> found = entityService.findByProjectId(id);
        return ResponseEntity.ok()
                .body(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Entity> updateById(@RequestBody Entity entity, @PathVariable Long id) {
        Entity entityToUpdate = entityService.findById(id);
        entityToUpdate.setName(entity.getName());
        entityToUpdate.setHasCreate(entity.getHasCreate());
        entityToUpdate.setHasRead(entity.getHasRead());
        entityToUpdate.setHasUpdate(entity.getHasUpdate());
        entityToUpdate.setHasDelete(entity.getHasDelete());
        entityToUpdate.setRelations(entity.getRelations());
        entityToUpdate.setVariables(entity.getVariables());

        Entity updatedEntity = entityService.update(entityToUpdate);
        return ResponseEntity.ok()
                .body(updatedEntity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable Long id) {
        entityService.deleteById(id);
        return ResponseEntity.ok()
                .build();
    }
}
