package org.example.services;

import org.example.dao.MemberDAO;
import org.example.models.Member;
import org.example.utils.Logger;

import java.util.List;
import java.util.Random;

public class MemberService {
    private final MemberDAO memberDAO;

    public MemberService(MemberDAO memberDAO) {
        this.memberDAO = memberDAO;
    }

    public void addMember(String name, String email, String phone) {
        // Generate a random member ID between 1000 and 9999
        Random random = new Random();
        int memberId = 1000 + random.nextInt(9000);
        
        Member member = new Member(memberId, name, email, phone);
        memberDAO.addMember(member);
        
        Logger.log("Added new member: " + name + " (ID: " + memberId + ")");
        System.out.println("✅ Member added successfully with ID: " + memberId);
    }

    public void updateMember(int memberId, String name, String email, String phone) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            System.out.println("❌ Member with ID " + memberId + " not found.");
            return;
        }
        
        Member updatedMember = new Member(memberId, name, email, phone);
        memberDAO.updateMember(updatedMember);
        
        Logger.log("Updated member details for ID: " + memberId);
        System.out.println("✅ Member updated successfully.");
    }

    public void deleteMember(int memberId) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            System.out.println("❌ Member with ID " + memberId + " not found.");
            return;
        }
        
        memberDAO.deleteMember(memberId);
        
        Logger.log("Deleted member with ID: " + memberId);
        System.out.println("✅ Member deleted successfully.");
    }

    public Member getMemberById(int memberId) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            System.out.println("❌ Member with ID " + memberId + " not found.");
        }
        return member;
    }

    public List<Member> getAllMembers() {
        List<Member> members = memberDAO.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("ℹ️ No members found in the system.");
        }
        return members;
    }
}
