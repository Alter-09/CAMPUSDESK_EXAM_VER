package com.roles.usermanagement.persistance.mapper;

import com.roles.usermanagement.domain.dto.UserRoleDto;
import com.roles.usermanagement.persistance.entity.UserRoleEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-11T17:14:15-0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class UserRoleMapperImpl implements UserRoleMapper {

    @Override
    public UserRoleDto toUserRoleDto(UserRoleEntity userRoleEntity) {
        if ( userRoleEntity == null ) {
            return null;
        }

        UserRoleDto userRoleDto = new UserRoleDto();

        userRoleDto.setUsername( userRoleEntity.getUsername() );
        userRoleDto.setRole( userRoleEntity.getRole() );

        return userRoleDto;
    }

    @Override
    public List<UserRoleDto> toUserRolesDto(List<UserRoleEntity> userRoleEntities) {
        if ( userRoleEntities == null ) {
            return null;
        }

        List<UserRoleDto> list = new ArrayList<UserRoleDto>( userRoleEntities.size() );
        for ( UserRoleEntity userRoleEntity : userRoleEntities ) {
            list.add( toUserRoleDto( userRoleEntity ) );
        }

        return list;
    }

    @Override
    public UserRoleEntity toUserRoleEntity(UserRoleDto userRoleDto) {
        if ( userRoleDto == null ) {
            return null;
        }

        UserRoleEntity userRoleEntity = new UserRoleEntity();

        userRoleEntity.setUsername( userRoleDto.getUsername() );
        userRoleEntity.setRole( userRoleDto.getRole() );

        return userRoleEntity;
    }
}
