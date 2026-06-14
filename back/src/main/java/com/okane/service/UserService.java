package com.okane.service;

import com.okane.dto.requestDto.CreateUserRequestDto;
import com.okane.dto.requestDto.UpdateUserRequestDto;
import com.okane.dto.requestDto.UpdateUserStatusRequestDto;
import com.okane.pagination.PageResponseDto;
import com.okane.dto.responseDto.UserResponseDTO;
import com.okane.entity.enums.Role;

public interface UserService {
    PageResponseDto<UserResponseDTO> getAllUsers(Role role, Boolean active, Long agenceId,
                                                 int page, int size, String sort);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO createUser(CreateUserRequestDto request);

    UserResponseDTO updateUser(Long id, UpdateUserRequestDto request);

    UserResponseDTO updateUserStatus(Long id, UpdateUserStatusRequestDto request);

    void deleteUser(Long id);

}
