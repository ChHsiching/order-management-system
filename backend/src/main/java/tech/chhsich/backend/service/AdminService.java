package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.entity.ResponseMessage;

import java.util.List;

public interface AdminService {
    ResponseMessage login(String username, String password);

    ResponseMessage updatePassword(String username, String oldPassword, String newPassword);

    List<Administrator> getAllMembers();

    ResponseMessage deleteMember(String username);

    Administrator getMemberByUsername(String username);
}
