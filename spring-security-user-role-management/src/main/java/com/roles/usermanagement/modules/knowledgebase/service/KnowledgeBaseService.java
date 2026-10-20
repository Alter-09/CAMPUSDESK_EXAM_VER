package com.roles.usermanagement.modules.knowledgebase.service;

import com.roles.usermanagement.domain.dto.PageResponse;
import com.roles.usermanagement.modules.knowledgebase.dto.KbArticleRequest;
import com.roles.usermanagement.modules.knowledgebase.dto.KbArticleResponse;
import com.roles.usermanagement.modules.knowledgebase.dto.KbCategoryResponse;
import com.roles.usermanagement.modules.knowledgebase.entity.KbArticle;
import com.roles.usermanagement.modules.knowledgebase.entity.KbCategory;
import com.roles.usermanagement.modules.knowledgebase.repository.KbArticleRepository;
import com.roles.usermanagement.modules.knowledgebase.repository.KbCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// [Nueva Funcionalidad: U-08/U-09/U-20/U-21/A-03 - Servicio de base de conocimiento] - Publicación, búsqueda, sugerencias en vivo y votación de artículos - Conecta con: KbArticleRepository y KbCategoryRepository
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KbArticleRepository articleRepository;
    private final KbCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<KbArticleResponse> getPublishedArticles(int page, int size) {
        Page<KbArticle> p = articleRepository.findByIsPublishedTrue(PageRequest.of(page, size));
        List<KbArticleResponse> content = p.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), p.isLast());
    }

    @Transactional(readOnly = true)
    public List<KbArticleResponse> searchArticles(String query) {
        return articleRepository.searchPublishedArticles(query)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<KbArticleResponse> suggestArticles(String title) {
        return articleRepository.suggestArticlesByTitle(title, PageRequest.of(0, 5))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public KbArticleResponse getArticleById(Long id) {
        KbArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado"));
        article.setViewsCount(article.getViewsCount() + 1);
        return toResponse(article);
    }

    @Transactional
    public void voteArticle(Long id, boolean helpful) {
        articleRepository.findById(id).ifPresent(a -> {
            if (helpful) {
                a.setHelpfulVotes(a.getHelpfulVotes() + 1);
            } else {
                a.setUnhelpfulVotes(a.getUnhelpfulVotes() + 1);
            }
        });
    }

    @Transactional
    public KbArticleResponse createArticle(KbArticleRequest request, String authorUsername) {
        KbCategory category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId()).orElse(null);
        }

        KbArticle article = KbArticle.builder()
                .title(request.title())
                .content(request.content())
                .category(category)
                .authorUsername(authorUsername)
                .isPublished(request.isPublished() == null || request.isPublished())
                .viewsCount(0)
                .helpfulVotes(0)
                .unhelpfulVotes(0)
                .build();

        return toResponse(articleRepository.save(article));
    }

    @Transactional(readOnly = true)
    public List<KbCategoryResponse> getCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(c -> new KbCategoryResponse(c.getId(), c.getName(), c.getDescription(), c.getIcon(), c.getDisplayOrder(), c.getIsActive()))
                .toList();
    }

    private KbArticleResponse toResponse(KbArticle a) {
        return new KbArticleResponse(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getCategory() == null ? null : a.getCategory().getId(),
                a.getCategory() == null ? null : a.getCategory().getName(),
                a.getAuthorUsername(),
                a.getIsPublished(),
                a.getViewsCount(),
                a.getHelpfulVotes(),
                a.getUnhelpfulVotes(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
