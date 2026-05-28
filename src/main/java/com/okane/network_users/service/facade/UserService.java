package com.okane.network_users.service.facade;

import com.okane.network_users.controller.dto.requestDto.CreateUserRequestDto;
import com.okane.network_users.controller.dto.requestDto.UpdateUserRequestDto;
import com.okane.network_users.controller.dto.requestDto.UpdateUserStatusRequestDto;
import com.okane.pagination.PageResponseDto;
import com.okane.network_users.controller.dto.responseDto.UserResponseDto;
import com.okane.shared.Role;

public interface UserService {
    PageResponseDto<UserResponseDto> getAllUsers(Role role, Boolean active, Long agenceId,
                                                 int page, int size, String sort);

    UserResponseDto getUserById(Long id);

    UserResponseDto createUser(CreateUserRequestDto request);

    UserResponseDto updateUser(Long id, UpdateUserRequestDto request);

    UserResponseDto updateUserStatus(Long id, UpdateUserStatusRequestDto request);

    void deleteUser(Long id);

}
