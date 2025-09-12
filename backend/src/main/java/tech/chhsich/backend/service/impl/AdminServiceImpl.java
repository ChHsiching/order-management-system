package tech.chhsich.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.chhsich.backend.mapper.AdminMapper;
import tech.chhsich.backend.pojo.Administrators;
import tech.chhsich.backend.pojo.ResponseMessage;
import tech.chhsich.backend.service.AdminService;

@Service

public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminMapper AdminMapper;
    @Override
    public ResponseMessage login(String username, String password) {
        if(AdminMapper.login1(username, password)==1){
            return ResponseMessage.success();
        } else {
            return ResponseMessage.error("该用户不存在");
        }
    }
}
