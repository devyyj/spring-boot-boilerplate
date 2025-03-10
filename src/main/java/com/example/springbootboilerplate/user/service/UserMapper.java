package com.example.springbootboilerplate.user.service;

import com.example.springbootboilerplate.user.domain.User;
import com.example.springbootboilerplate.user.repository.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")  // Spring Bean으로 등록
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // UserEntity → User 변환
    @Mapping(source = "id", target = "id")
    User toDomain(UserEntity entity);

    // User → UserEntity 변환
    @Mapping(source = "id", target = "id")
    UserEntity toEntity(User user);
}
