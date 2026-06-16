package com.okane.service;

import com.okane.dto.requestDto.UpdateClientProfileRequestDto;
import com.okane.dto.responseDto.ClientActivityResponseDto;
import com.okane.dto.responseDto.ClientProfileResponseDto;
import com.okane.pagination.PageResponseDto;

public interface ClientProfileService {

        ClientProfileResponseDto getMyProfile(Long userId);

        ClientProfileResponseDto updateMyProfile(Long userId, UpdateClientProfileRequestDto request);

        PageResponseDto<ClientActivityResponseDto> getMyActivity(Long userId, int page, int size);

    ClientProfileResponseDto getMyProfileByEmail(String email);
    ClientProfileResponseDto updateMyProfileByEmail(String email, com.okane.dto.requestDto.UpdateClientProfileRequestDto request);
    com.okane.pagination.PageResponseDto<com.okane.dto.responseDto.ClientActivityResponseDto> getMyActivityByEmail(String email, int page, int size);
}