package com.example.proyek.service;

import com.example.proyek.model.User;
import com.example.proyek.repository.UserRepository;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> listAllUser() {
        return userRepository.findAll();
    }

    public void saveUser(User user) {
    	if (isUsernameUnique(user.getUsername())) {
            userRepository.save(user);
        } else {
            // Handle the case where the username is not unique (throw an exception or perform other actions)
            throw new IllegalArgumentException("Username is not unique");
        }
    }
    private boolean isUsernameUnique(String username) {
        // Check if the username already exists in the database
        return userRepository.findByUsername(username) == null;
    }

    public User getUser(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public String loginUser(User user, HttpSession session) {
        User dbUser = userRepository.findByUsername(user.getUsername());
        if ("baak".equals(user.getUsername()) && "admin".equals(user.getPassword())) {
            User adminUser = new User();
            adminUser.setUsername("BAAK");
            session.setAttribute("user", adminUser);
            return "admin-index";
        } else if (dbUser != null && user.getPassword().equals(dbUser.getPassword())) {
            session.setAttribute("user", dbUser);
            return "mahasiswa-index";
        }
        return "Username atau password salah";
    }


    
}
