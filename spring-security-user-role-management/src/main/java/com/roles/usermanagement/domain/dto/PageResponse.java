package com.roles.usermanagement.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// [Nueva Funcionalidad: U-21/T-21/A-25 - DTO genérico de paginación] - Estandariza respuestas paginadas de API para todas las entidades - Conecta con: Repositorios JPA y Controladores REST
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
