package com.roles.usermanagement.modules.knowledgebase.controller;

import com.roles.usermanagement.domain.dto.PageResponse;
import com.roles.usermanagement.modules.knowledgebase.dto.KbArticleRequest;
import com.roles.usermanagement.modules.knowledgebase.dto.KbArticleResponse;
import com.roles.usermanagement.modules.knowledgebase.dto.KbCategoryResponse;
import com.roles.usermanagement.modules.knowledgebase.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [Nueva Funcionalidad: U-08/U-09/U-20/A-03 - Controlador REST de base de conocimiento] - Endpoints para artículos públicos, sugerencias en vivo, categorías y votos - Conecta con: KnowledgeBaseService
@RestController
@RequestMapping("/api/v1/kb")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService kbService;

    @GetMapping("/articles")
    public ResponseEntity<PageResponse<KbArticleResponse>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(kbService.getPublishedArticles(page, size));
    }

    @GetMapping("/articles/search")
    public ResponseEntity<List<KbArticleResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(kbService.searchArticles(query));
    }

    @GetMapping("/articles/suggest")
    public ResponseEntity<List<KbArticleResponse>> suggest(@RequestParam String title) {
        return ResponseEntity.ok(kbService.suggestArticles(title));
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<KbArticleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kbService.getArticleById(id));
    }

    @PostMapping("/articles/{id}/vote")
    public ResponseEntity<Void> vote(@PathVariable Long id, @RequestParam boolean helpful) {
        kbService.voteArticle(id, helpful);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/articles")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<KbArticleResponse> createArticle(@Valid @RequestBody KbArticleRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kbService.createArticle(request, auth.getName()));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<KbCategoryResponse>> getCategories() {
        return ResponseEntity.ok(kbService.getCategories());
    }
}
