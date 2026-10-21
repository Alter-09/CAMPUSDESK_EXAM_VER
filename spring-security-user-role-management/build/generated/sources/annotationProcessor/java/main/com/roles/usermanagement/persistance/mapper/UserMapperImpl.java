package com.roles.usermanagement.persistance.mapper;

import com.roles.usermanagement.domain.dto.UserDto;
import com.roles.usermanagement.domain.dto.UserRoleDto;
import com.roles.usermanagement.persistance.entity.UserEntity;
import com.roles.usermanagement.persistance.entity.UserRoleEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-18T11:30:47-0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toUserDto(UserEntity userEntity) {
        if ( userEntity == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.setUsername( userEntity.getUsername() );
        userDto.setEmail( userEntity.getEmail() );
        userDto.setLocked( userEntity.getLocked() );
        userDto.setDisabled( userEntity.getDisabled() );
        userDto.setMustChangePassword( userEntity.getMustChangePassword() );
        userDto.setPassword( userEntity.getPassword() );
        userDto.setRoles( userRoleEntityListToUserRoleDtoList( userEntity.getRoles() ) );

        userDto.setRole( roleName(userEntity) );
        userDto.setAdditionalPermissions( permissionNames(userEntity) );

        return userDto;
    }

    @Override
    public List<UserDto> toUsersDto(List<UserEntity> userEntities) {
        if ( userEntities == null ) {
            return null;
        }

        List<UserDto> list = new ArrayList<UserDto>( userEntities.size() );
        for ( UserEntity userEntity : userEntities ) {
            list.add( toUserDto( userEntity ) );
        }

        return list;
    }

    @Override
    public UserEntity toUserEntity(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setUsername( userDto.getUsername() );
        userEntity.setMustChangePassword( userDto.getMustChangePassword() );
        userEntity.setEmail( userDto.getEmail() );
        userEntity.setLocked( userDto.getLocked() );
        userEntity.setDisabled( userDto.getDisabled() );
        userEntity.setPassword( userDto.getPassword() );
        userEntity.setRoles( userRoleDtoListToUserRoleEntityList( userDto.getRoles() ) );

        return userEntity;
    }

    protected UserRoleDto userRoleEntityToUserRoleDto(UserRoleEntity userRoleEntity) {
        if ( userRoleEntity == null ) {
            return null;
        }

        UserRoleDto userRoleDto = new UserRoleDto();

        userRoleDto.setUsername( userRoleEntity.getUsername() );
        userRoleDto.setRole( userRoleEntity.getRole() );

        return userRoleDto;
    }

    protected List<UserRoleDto> userRoleEntityListToUserRoleDtoList(List<UserRoleEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<UserRoleDto> list1 = new ArrayList<UserRoleDto>( list.size() );
        for ( UserRoleEntity userRoleEntity : list ) {
            list1.add( userRoleEntityToUserRoleDto( userRoleEntity ) );
        }

        return list1;
    }

    protected UserRoleEntity userRoleDtoToUserRoleEntity(UserRoleDto userRoleDto) {
        if ( userRoleDto == null ) {
            return null;
        }

        UserRoleEntity userRoleEntity = new UserRoleEntity();

        userRoleEntity.setUsername( userRoleDto.getUsername() );
        userRoleEntity.setRole( userRoleDto.getRole() );

        return userRoleEntity;
    }

    protected List<UserRoleEntity> userRoleDtoListToUserRoleEntityList(List<UserRoleDto> list) {
        if ( list == null ) {
            return null;
        }

        List<UserRoleEntity> list1 = new ArrayList<UserRoleEntity>( list.size() );
        for ( UserRoleDto userRoleDto : list ) {
            list1.add( userRoleDtoToUserRoleEntity( userRoleDto ) );
        }

        return list1;
    }
}
