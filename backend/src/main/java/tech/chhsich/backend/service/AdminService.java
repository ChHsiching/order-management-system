package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Administrators;
import tech.chhsich.backend.entity.ResponseMessage;

import java.util.List;

public interface AdminService {
    ResponseMessage login(String username, String password);
    
    ResponseMessage updatePassword(String username, String oldPassword, String newPassword);
    
    List<Administrators> getAllMembers();
    
    ResponseMessage deleteMember(String username);
    
    Administrators getMemberByUsername(String username);
}
