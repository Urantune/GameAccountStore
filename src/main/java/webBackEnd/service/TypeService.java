package webBackEnd.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.DTO.CategoryCountDTO;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.Type;
import webBackEnd.repository.GameAccountRepositories;
import webBackEnd.repository.TypeRepositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TypeService {

    @Autowired
    private TypeRepositories typeRepositories;

    @Autowired
    private GameAccountRepositories gameAccountRepositories;

    public List<CategoryCountDTO> getCategoriesWithCount() {

        List<Type> categories = typeRepositories.findAll();
        List<Object[]> counts = gameAccountRepositories.countProductsByType();

        Map<UUID, Long> map = new HashMap<>();
        for (Object[] row : counts) {
            map.put((UUID) row[0], (Long) row[1]);   // ✔ row[0] là UUID
        }

        return categories.stream()
                .map(c -> new CategoryCountDTO(
                        c,
                        map.getOrDefault(c.getId(), 0L)
                ))
                .toList();
    }
}
