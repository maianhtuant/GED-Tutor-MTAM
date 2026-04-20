package com.gedtutor.service;

import com.gedtutor.dto.RegistrationDto;
import com.gedtutor.model.Role;
import com.gedtutor.model.User;
import com.gedtutor.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegistrationDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        User u = findById(id);
        u.setEnabled(enabled);
        userRepository.save(u);
    }

    @Transactional
    public void setRole(Long id, Role role) {
        User u = findById(id);
        u.setRole(role);
        userRepository.save(u);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User u = findById(id);
        u.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(u);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
