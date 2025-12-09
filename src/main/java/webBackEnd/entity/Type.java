package webBackEnd.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "Type")
public class Type {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "typeId", columnDefinition = "uniqueidentifier")
    private UUID id;
    @Column(name = "description")
    private String description;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "typeName")
    private String typeName;

    public Type() {
    }

    public Type(UUID id, String description, String status, String typeName) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.typeName = typeName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}