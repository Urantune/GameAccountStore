package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Staff;
import webBackEnd.repository.StaffRepositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    public List<Staff> findAll() {
        return staffRepositories.findAll();
    }

    public List<Staff> search(String q) {
        if (q == null || q.trim().isEmpty()) return staffRepositories.findAll();
        return staffRepositories.findByUsernameContainingIgnoreCase(q.trim());
    }

    public Optional<Staff> findById(UUID id) {
        return staffRepositories.findById(id);
    }

    public Staff createStaff(String username, String rawPassword, String status) {
        Staff s = new Staff();
        s.setUsername(username);
        s.setRole("STAFF");
        s.setCreated(LocalDateTime.now());
        s.setStatus(status == null || status.isBlank() ? "ACTIVE" : status);

        if (rawPassword == null) rawPassword = "";
        if (passwordEncoder != null) s.setPassword(passwordEncoder.encode(rawPassword));
        else s.setPassword(rawPassword);

        return staffRepositories.save(s);
    }

    public Staff updateStaff(UUID id, String username, String rawPassword, String status) {
        Staff s = staffRepositories.findById(id).orElseThrow();
        s.setUsername(username);
        s.setRole("STAFF");
        s.setStatus(status);

        if (rawPassword != null && !rawPassword.isBlank()) {
            if (passwordEncoder != null) s.setPassword(passwordEncoder.encode(rawPassword));
            else s.setPassword(rawPassword);
        }

        return staffRepositories.save(s);
    }
}
