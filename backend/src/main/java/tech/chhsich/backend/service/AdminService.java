package tech.chhsich.backend.service;

import org.springframework.web.bind.annotation.ResponseBody;
import tech.chhsich.backend.pojo.Administrators;
import tech.chhsich.backend.pojo.ResponseMessage;

public interface AdminService {
    ResponseMessage login(String username, String password);
}
