package webBackEnd.DTO;

import webBackEnd.entity.Type;

public class CategoryCountDTO {


    private Type type;
    private Long count;

    public CategoryCountDTO(Type type, Long count) {
        this.type = type;
        this.count = count;
    }

    public Type getCategory() {
        return type;
    }

    public void setCategory(Type type) {
        this.type = type;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
