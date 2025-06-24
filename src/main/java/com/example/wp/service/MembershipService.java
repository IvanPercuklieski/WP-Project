package com.example.wp.service;

import com.example.wp.model.Membership;
import com.example.wp.repository.MembershipRepository;
import org.springframework.stereotype.Service;

@Service
public class MembershipService {
    private final MembershipRepository membershipRepository;

    public MembershipService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }


    public void addMembership(Membership membership) {
        membershipRepository.save(membership);
    }
}
