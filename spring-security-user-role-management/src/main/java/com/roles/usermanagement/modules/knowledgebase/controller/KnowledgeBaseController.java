package com.roles.usermanagement.modules.knowledgebase.controller;

import com.roles.usermanagement.domain.dto.PageResponse;
import com.roles.usermanagement.modules.knowledgebase.dto.KbArticleRequest;
import com.roles.usermanagement.modules.knowledgebase.dto.KbArticleResponse;
import com.roles.usermanagement.modules.knowledgebase.dto.KbCategoryResponse;
import com.roles.usermanagement.modules.knowledgebase.service.KnowledgeBaseService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [Nueva Funcionalidad: U-08/U-09/U-20/A-03/U-30 - Controlador REST de base de conocimiento] - Endpoints para artículos públicos, sugerencias en vivo, categorías, FAQ y votos
@RestController
@RequestMapping("/api/v1/kb")
@RequiredArgsConstructor
@Tag(name = "Knowledge Base", description = "Base de conocimiento, artículos de autoayuda, sugerencias en vivo y FAQ")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class KnowledgeBaseController {

    private final KnowledgeBaseService kbService;

    @GetMapping("/articles")
    @Operation(summary = "Listar artículos publicados de la base de conocimiento con paginación (MOD-27, U-08)")
    public ResponseEntity<PageResponse<KbArticleResponse>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(kbService.getPublishedArticles(page, size));
    }

    @GetMapping("/articles/search")
    @Operation(summary = "Buscar artículos de conocimiento por palabras clave (MOD-27, U-09)")
    public ResponseEntity<List<KbArticleResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(kbService.searchArticles(query));
    }

    @GetMapping("/articles/suggest")
    @Operation(summary = "Sugerencias inteligentes de artículos al redactar título del ticket (MOD-27, U-20)")
    public ResponseEntity<List<KbArticleResponse>> suggest(@RequestParam String title) {
        return ResponseEntity.ok(kbService.suggestArticles(title));
    }

    @GetMapping("/articles/{id}")
    @Operation(summary = "Obtener detalle completo de un artículo e incrementar contador de lecturas (MOD-27)")
    public ResponseEntity<KbArticleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kbService.getArticleById(id));
    }

    @PostMapping("/articles/{id}/vote")
    @Operation(summary = "Votar utilidad de un artículo (útil / no útil) (MOD-28, U-21)")
    public ResponseEntity<Void> vote(@PathVariable Long id, @RequestParam boolean helpful) {
        kbService.voteArticle(id, helpful);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/articles")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @Operation(summary = "Crear nuevo artículo de conocimiento (ADMIN o TECHNICIAN) (MOD-29, T-12, A-03)")
    public ResponseEntity<KbArticleResponse> createArticle(@Valid @RequestBody KbArticleRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kbService.createArticle(request, auth.getName()));
    }

    @GetMapping("/categories")
    @Operation(summary = "Listar categorías activas de la base de conocimiento (A-04)")
    public ResponseEntity<List<KbCategoryResponse>> getCategories() {
        return ResponseEntity.ok(kbService.getCategories());
    }

    @GetMapping("/faq")
    @Operation(summary = "Portal de preguntas frecuentes / FAQ más consultadas (MOD-51, U-30)")
    public ResponseEntity<List<KbArticleResponse>> getFaq() {
        return ResponseEntity.ok(kbService.getFaqArticles());
    }
}
