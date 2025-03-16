package org.example.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.database.DatabaseConnection;
import org.example.models.Member;
import org.example.utils.Logger;

public class MemberDAOImpl implements MemberDAO {
    private final List<Member> memberList = new ArrayList<>();
    private static final String FILE_PATH = "members.txt";
    
    public MemberDAOImpl() {
        loadMembersFromFile();

    }

    private void saveMemberToFile(Member member) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(member.getId() + "," + 
                       member.getName() + "," + 
                       member.getEmail() + "," + 
                       member.getPhone());
            writer.newLine();
            System.out.println("📂 Member saved to file.");
        } catch (IOException e) {
            System.out.println(" Error writing member to file: " + e.getMessage());
        }
    }
    

    private void loadMembersFromFile() {
        File file = new File(FILE_PATH);
        
        if (!file.exists()) {
            System.out.println(" Members file not found. Will be created when adding members.");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            memberList.clear();
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        Member member = new Member(id, parts[1], parts[2], parts[3]);
                        memberList.add(member);
                    } catch (NumberFormatException e) {
                        System.out.println(" Invalid member ID format in file: " + parts[0]);
                    }
                }
            }
            
            System.out.println("📂 Loaded " + memberList.size() + " members from file.");
        } catch (IOException e) {
            System.out.println(" Error reading members from file: " + e.getMessage());
        }
    }
    

    private void updateMemberInFile(Member updatedMember) {
        File file = new File(FILE_PATH);
        List<String> fileLines = new ArrayList<>();
        boolean memberFound = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        if (id == updatedMember.getId()) {

                            line = updatedMember.getId() + "," + 
                                   updatedMember.getName() + "," + 
                                   updatedMember.getEmail() + "," + 
                                   updatedMember.getPhone();
                            memberFound = true;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(" Invalid member ID format in file: " + parts[0]);
                    }
                }
                fileLines.add(line);
            }
            
        } catch (IOException e) {
            System.out.println(" Error reading file: " + e.getMessage());
            return;
        }
        
        if (!memberFound) {

            fileLines.add(updatedMember.getId() + "," + 
                        updatedMember.getName() + "," + 
                        updatedMember.getEmail() + "," + 
                        updatedMember.getPhone());
        }
        

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : fileLines) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println(" Member updated in file.");
        } catch (IOException e) {
            System.out.println(" Error updating member in file: " + e.getMessage());
        }
    }
    

    private void deleteMemberFromFile(int memberId) {
        File file = new File(FILE_PATH);
        List<String> fileLines = new ArrayList<>();
        boolean memberFound = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        if (id == memberId) {
                            memberFound = true;
                            continue;
                        }
                    } catch (NumberFormatException e) {

                    }
                }
                fileLines.add(line);
            }
            
        } catch (IOException e) {
            System.out.println(" Error reading file: " + e.getMessage());
            return;
        }
        

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : fileLines) {
                writer.write(line);
                writer.newLine();
            }
            
            if (memberFound) {
                System.out.println(" Member deleted from file.");
            } else {
                System.out.println(" Member with ID " + memberId + " not found in file.");
            }
        } catch (IOException e) {
            System.out.println(" Error updating file: " + e.getMessage());
        }
    }

    @Override
    public void addMember(Member member) {

        String query = "INSERT INTO members (member_id, name, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, member.getId());
            stmt.setString(2, member.getName());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getPhone());
            stmt.executeUpdate();
            

            memberList.add(member);
            

            saveMemberToFile(member);
            
            System.out.println(" Member added successfully!");
            Logger.log("Added member: " + member.getName() + " (ID: " + member.getId() + ")");
        } catch (SQLException e) {
            System.out.println(" Error adding member to database: " + e.getMessage());
        }
    }

    @Override
    public Member getMemberById(int id) {
        for (Member member : memberList) {
            if (member.getId() == id) {
                return member;
            }
        }


        String query = "SELECT * FROM members WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Member member = new Member(
                        rs.getInt("member_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone")
                );

                memberList.add(member);
                return member;
            }
        } catch (SQLException e) {
            System.out.println(" Error retrieving member from database: " + e.getMessage());
        }
        
        return null;
    }

    @Override
    public List<Member> getAllMembers() {

        memberList.clear();
        

        String query = "SELECT * FROM members";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Member member = new Member(
                        rs.getInt("member_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone")
                );
                memberList.add(member);
            }
        } catch (SQLException e) {
            System.out.println(" Error loading members from database: " + e.getMessage());


            if (memberList.isEmpty()) {
                loadMembersFromFile();
            }
        }
        
        return new ArrayList<>(memberList);
    }

    @Override
    public void updateMember(Member member) {

        String query = "UPDATE members SET name = ?, email = ?, phone = ? WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setInt(4, member.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {

                for (int i = 0; i < memberList.size(); i++) {
                    if (memberList.get(i).getId() == member.getId()) {
                        memberList.set(i, member);
                        break;
                    }
                }
                

                updateMemberInFile(member);
                
                System.out.println(" Member updated successfully!");
                Logger.log("Updated member: " + member.getName() + " (ID: " + member.getId() + ")");
            } else {
                System.out.println("Member with ID " + member.getId() + " not found in database.");
            }
        } catch (SQLException e) {
            System.out.println(" Error updating member in database: " + e.getMessage());
        }
    }

    @Override
    public void deleteMember(int id) {

        String query = "DELETE FROM members WHERE member_id = ?";
        boolean deleteSuccessful = false;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                deleteSuccessful = true;
                

                memberList.removeIf(member -> member.getId() == id);
                

                deleteMemberFromFile(id);
                
                System.out.println(" Member deleted successfully!");
                Logger.log("Deleted member with ID: " + id);
            } else {
                System.out.println(" Member with ID " + id + " not found in database.");
            }
        } catch (SQLException e) {
            System.out.println(" Error deleting member from database: " + e.getMessage());
        }
    }
}
