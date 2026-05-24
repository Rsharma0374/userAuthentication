package com.guardianservices.userAuthentication.web.mapper;

import com.guardianservices.userAuthentication.domain.model.User;
import com.guardianservices.userAuthentication.web.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between User entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface UserMapper {


    /**
     * Converts User entity to UserResponse DTO
     *
     * @param user user entity
     * @return user response DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "roles", target = "roles")
    @Mapping(source = "products", target = "products")
    UserResponse toResponse(User user);
}
