package com.example.airline.service;

import com.example.airline.dto.UserDTO;
import com.example.airline.entity.User;
import com.example.airline.entity.Wallet;
import com.example.airline.exception.ResourceNotFoundException;
import com.example.airline.repository.UserRepository;
import com.example.airline.repository.WalletRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public UserService(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public User createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create user
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user = userRepository.save(user);

        // Create wallet for user
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setBalance(0.0);
        walletRepository.save(wallet);

        return user;
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        User user = getUser(id);

        // Check if new email is already taken by another user
        if (!user.getEmail().equals(userDTO.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        BeanUtils.copyProperties(userDTO, user);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUser(id);
        userRepository.delete(user);
    }
}
