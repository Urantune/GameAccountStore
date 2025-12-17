package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Staff;
import webBackEnd.repository.StaffRepositories;

import java.util.List;
import java.util.UUID;

@Service
public class AdministratorService {

    @Autowired
    private StaffRepositories staffRepositories;

    public Staff getStaffByID(UUID id){
        return  staffRepositories.findById(id).orElse(null);
    }

    public List<Staff> getAll(){
        return staffRepositories.findAll();
    }
}
