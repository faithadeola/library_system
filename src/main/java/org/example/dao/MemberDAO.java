package org.example.dao;

import org.example.models.Member;
import java.util.List;

public interface MemberDAO {
    void addMember(Member member);
    Member getMemberById(int id);
    List<Member> getAllMembers();
    void updateMember(Member member);
    void deleteMember(int id);
}
