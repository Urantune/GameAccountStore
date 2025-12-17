package webBackEnd.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public Type findById(UUID id) {
        return typeRepositories.findByTypeId(id);
    }


    public Type findByTypeName(String name) {
        return  typeRepositories.findByTypeName(name);
    }

    public List<Type> getAllType(){
        return typeRepositories.findAll();
    }



}
