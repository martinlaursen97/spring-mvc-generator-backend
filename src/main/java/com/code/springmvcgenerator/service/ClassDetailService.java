package com.code.springmvcgenerator.service;

import com.code.springmvcgenerator.constants.ClassType;
import com.code.springmvcgenerator.entity.ClassDetail;
import com.code.springmvcgenerator.entity.EntityDetail;
import com.code.springmvcgenerator.entity.Relation;
import com.code.springmvcgenerator.entity.Variable;
import com.code.springmvcgenerator.utils.Util;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClassDetailService {

    private final byte SPACING = 4;
    private final String SPACES = " ".repeat(SPACING);

    private List<ClassDetail> getClasses(EntityDetail entityDetail) {
        List<ClassDetail> classes = new ArrayList<>();

        classes.add(toClassByClassType(entityDetail, ClassType.ENTITY));
        classes.add(toClassByClassType(entityDetail, ClassType.CONTROLLER));
        classes.add(toClassByClassType(entityDetail, ClassType.SERVICE));
        classes.add(toClassByClassType(entityDetail, ClassType.REPOSITORY));

        return classes;
    }

    public List<ClassDetail> getAllClasses(List<EntityDetail> entityDetails) {
        List<ClassDetail> classes = new ArrayList<>();

        for (EntityDetail ed : entityDetails) {
            classes.addAll(getClasses(ed));
        }

        return classes;
    }

    private ClassDetail toEntityClass(EntityDetail entityDetail) {
        ClassDetail classDetail = new ClassDetail();

        StringBuilder sb  = new StringBuilder()
                .append("@Getter\n")
                .append("@Setter\n")
                .append("@ToString\n")
                .append("@Entity\n")
                .append("public class ")
                .append(entityDetail.getName())
                .append(" {\n")
                .append(SPACES)
                .append("@Id\n")
                .append(SPACES)
                .append("@Column(name = \"").append(entityDetail.getName().toLowerCase()).append("_id\")\n")
                .append(SPACES)
                .append("@GeneratedValue(strategy = GenerationType.IDENTITY)\n")
                .append(SPACES)
                .append("private Long id;\n");

        // Relationship columns
        for (Relation v : entityDetail.getRelations()) {
            sb.append(SPACES);
            switch (v.getVal1()) {
                case "ManyToOne", "OneToOne" -> sb
                        .append("\n")
                        .append(SPACES)
                        .append("@")
                        .append(v.getVal1())
                        .append("(cascade = CascadeType.MERGE)\n")
                        .append(SPACES)
                        .append("@JoinColumn(name = \"")
                        .append(v.getVal2().toLowerCase())
                        .append("_id\")\n")
                        .append(SPACES)
                        .append("private ")
                        .append(v.getVal2())
                        .append(" ")
                        .append(v.getVal2().toLowerCase())
                        .append(";\n");
                case "OneToMany" -> sb
                        .append("\n")
                        .append(SPACES)
                        .append("@JsonBackReference(value = \"")
                        .append(v.getVal3().toLowerCase())
                        .append("\")\n")
                        .append(SPACES)
                        .append("@")
                        .append(v.getVal1())
                        .append("(mappedBy = \"")
                        .append(entityDetail.getName().toLowerCase())
                        .append("\", cascade = CascadeType.MERGE)\n")
                        .append(SPACES)
                        .append("@ToString.Exclude\n")
                        .append(SPACES)
                        .append("private List<")
                        .append(v.getVal2())
                        .append("> ")
                        .append(v.getVal3().toLowerCase())
                        .append(";\n");
            }
        }

        // Columns
        for (Variable v : entityDetail.getVariables()) {
            sb.append(SPACES);

            if (v.getVal3() != null) {
                sb.append("\n")
                        .append(SPACES)
                        .append("@Column(name = \"")
                        .append(v.getVal3())
                        .append("\")");
            }

            sb.append("\n")
                    .append(SPACES)
                    .append("private ")
                    .append(v.getVal1())
                    .append(" ")
                    .append(v.getVal2())
                    .append(";\n");
        }

        sb.append("}\n");


        classDetail.setName(entityDetail.getName());
        classDetail.setContent(sb.toString());

        return classDetail;
    }

    private ClassDetail toClassByClassType(EntityDetail entityDetail, ClassType classType) {
        String dependency = "";

        switch (classType) {
            case CONTROLLER -> dependency = "Service";
            case SERVICE -> dependency = "Repository";
            case REPOSITORY -> {
                return toRepositoryInterface(entityDetail);
            }
            case ENTITY -> {
                return toEntityClass(entityDetail);
            }
        }

        ClassDetail classDetail = new ClassDetail();
        StringBuilder sb = new StringBuilder();

        String type = Util.capitalize(classType.toString().toLowerCase());
        String entityNameLowerDependency = Util.decapitalize(entityDetail.getName()) + dependency;
        String entityNameLower = Util.decapitalize(entityDetail.getName());
        String entityNamePluralLower = Util.decapitalize(entityDetail.getNamePlural());

        sb.append("@RestController\n")
                .append("@RequestMapping(\"/api/")
                .append(entityDetail.getNamePlural().toLowerCase())
                .append("\")\n")
                .append("@CrossOrigin\n")
                .append("public class ")
                .append(entityDetail.getName())
                .append(type)
                .append(" {\n\n");

        sb.append(SPACES)
                .append("private final ")
                .append(entityDetail.getName())
                .append(dependency)
                .append(" ")
                .append(entityNameLowerDependency)
                .append(";\n\n");

        sb.append(SPACES)
                .append("@Autowired\n")
                .append(SPACES)
                .append("public ")
                .append(entityDetail.getName())
                .append("Controller(")
                .append(entityDetail.getName())
                .append("Service ")
                .append(entityNameLowerDependency)
                .append(") {\n")
                .append(SPACES)
                .append(SPACES)
                .append("this.")
                .append(entityNameLowerDependency)
                .append(" = ")
                .append(entityNameLowerDependency)
                .append(";\n")
                .append(SPACES)
                .append("}\n\n");

        if (entityDetail.getHasCreate()) {
            sb.append(SPACES)
                    .append("@PostMapping\n")
                    .append(SPACES)
                    .append("public ResponseEntity<")
                    .append(entityDetail.getName())
                    .append("> ")
                    .append("create")
                    .append("(@RequestBody ")
                    .append(entityDetail.getName())
                    .append(" ")
                    .append(entityNameLower)
                    .append(") {\n");

            sb.append(SPACES)
                    .append(SPACES)
                    .append(entityDetail.getName())
                    .append(" saved")
                    .append(entityDetail.getName())
                    .append(" = ")
                    .append(entityNameLowerDependency)
                    .append(".save(")
                    .append(entityNameLower)
                    .append(");\n");

            sb.append(SPACES)
                    .append(SPACES)
                    .append("return ResponseEntity.ok()\n")
                    .append(SPACES)
                    .append(SPACES)
                    .append(SPACES)
                    .append(SPACES)
                    .append(".body(")
                    .append("saved")
                    .append(entityDetail.getName())
                    .append(");\n");

            sb.append(SPACES)
                    .append("}\n\n");
        }

        if (entityDetail.getHasRead()) {
            sb.append(SPACES)
                    .append("@GetMapping\n")
                    .append(SPACES)
                    .append("public ResponseEntity<List<")
                    .append(entityDetail.getName())
                    .append(">> ")
                    .append("read")
                    .append("() {\n");

            sb.append(SPACES)
                    .append(SPACES)
                    .append("List<")
                    .append(entityDetail.getName())
                    .append("> ")
                    .append(entityNamePluralLower)
                    .append(" = ")
                    .append(entityNameLowerDependency)
                    .append(".read(")
                    .append(");\n");

            sb.append(SPACES)
                    .append(SPACES)
                    .append("return ResponseEntity.ok()\n")
                    .append(SPACES)
                    .append(SPACES)
                    .append(SPACES)
                    .append(SPACES)
                    .append(".body(")
                    .append(entityNamePluralLower)
                    .append(");\n");

            sb.append(SPACES)
                    .append("}\n\n");
        }

        if (entityDetail.getHasUpdate()) {
            sb.append(SPACES)
                    .append("@PutMapping\n")
                    .append(SPACES)
                    .append("public ResponseEntity<")
                    .append(entityDetail.getName())
                    .append("> ")
                    .append("update")
                    .append("(@RequestBody ")
                    .append(entityDetail.getName())
                    .append(" ")
                    .append(entityNameLower)
                    .append(") {\n");

            sb.append(SPACES)
                    .append(SPACES)
                    .append(entityDetail.getName())
                    .append(" updated")
                    .append(entityDetail.getName())
                    .append(" = ")
                    .append(entityNameLowerDependency)
                    .append(".save(")
                    .append(entityNameLower)
                    .append(");\n");

            sb.append(SPACES)
                    .append(SPACES)
                    .append("return ResponseEntity.ok()\n")
                    .append(SPACES)
                    .append(SPACES)
                    .append(SPACES)
                    .append(SPACES)
                    .append(".body(")
                    .append("updated")
                    .append(entityDetail.getName())
                    .append(");\n");

            sb.append(SPACES)
                    .append("}\n\n");
        }

        if (entityDetail.getHasDelete()) {
            sb.append(SPACES)
                    .append("@DeleteMapping(\"/{id}\")\n")
                    .append(SPACES)
                    .append("public ResponseEntity<")
                    .append(entityDetail.getName())
                    .append("> ")
                    .append("deleteById")
                    .append("(@PathVariable Long id) {\n");

            sb.append(SPACES)
                    .append(SPACES)
                    .append(entityNameLowerDependency)
                    .append(".deleteById(id);\n");

            sb.append(SPACES)
                    .append(SPACES)
                    .append("return ResponseEntity.ok()\n")
                    .append(SPACES)
                    .append(SPACES)
                    .append(SPACES)
                    .append(SPACES)
                    .append(".build();\n");

            sb.append(SPACES)
                    .append("}");
        }

        sb.append("\n}\n");

        classDetail.setName(entityDetail.getName() + type);
        classDetail.setContent(sb.toString());

        return classDetail;
    }

    private ClassDetail toRepositoryInterface(EntityDetail entityDetail) {
        ClassDetail classDetail = new ClassDetail();

        StringBuilder sb = new StringBuilder();

        sb.append("public interface ")
                    .append(entityDetail.getName())
                    .append("Repository extends JpaRepository<")
                    .append(entityDetail.getName())
                    .append(", Long> {\n")
                    .append("}\n");

        classDetail.setName(entityDetail.getName() + "Repository");
        classDetail.setContent(sb.toString());

        return classDetail;
    }
}
