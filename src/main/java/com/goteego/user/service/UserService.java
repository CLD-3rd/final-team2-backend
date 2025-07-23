package com.goteego.user.service;

import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.user.domain.User;
import com.goteego.user.dto.UserDto;
import com.goteego.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateRefreshToken(Long userId, String newToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        user.setRefreshToken(newToken); // setter 필요
    }

    public List<UserDto> findAll() {
        List<User> findUsers = userRepository.findAll();
        return findUsers.stream()
                .map(user -> new UserDto(user.getId(), user.getNickname()))
                .collect(Collectors.toList());
    }

}