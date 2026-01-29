package com.example.project.interfaces;

import com.example.project.dto.UserRegisterDTO;

public interface Register {
    void registerUser(UserRegisterDTO dto) throws Exception;

}
