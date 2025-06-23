package com.example.wp.service;

import com.example.wp.model.UserEntity;
import com.example.wp.model.Workspace;
import com.example.wp.repository.UserRepository;
import com.example.wp.repository.WorkspaceRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;

    public UserServiceImpl(UserRepository userRepository, WorkspaceRepository workspaceRepository) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Optional<UserEntity> findByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public void saveuser(UserEntity newUser) {
        userRepository.save(newUser);
    }

    public List<Workspace> getWorkspacesForUser(UserEntity user) {
        return workspaceRepository.findWorkspacesByUserWithMemberships(user);
    }

}
