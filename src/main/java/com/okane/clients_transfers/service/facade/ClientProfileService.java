// ClientProfileService.java
package com.okane.clients_transfers.service.facade;

import com.okane.clients_transfers.controller.dto.requestDto.UpdateClientProfileRequestDto;
import com.okane.clients_transfers.controller.dto.responseDto.ClientActivityResponseDto;
import com.okane.clients_transfers.controller.dto.responseDto.ClientProfileResponseDto;
import com.okane.pagination.PageResponseDto;

public interface ClientProfileService {

    ClientProfileResponseDto getMyProfile(Long userId);

    ClientProfileResponseDto updateMyProfile(Long userId, UpdateClientProfileRequestDto request);

    PageResponseDto<ClientActivityResponseDto> getMyActivity(Long userId, int page, int size);
}