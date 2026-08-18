package com.lesofn.archforge.blog.internal.service

import com.lesofn.archforge.blog.api.dao.BlogArticleRepository
import com.lesofn.archforge.blog.api.dao.BlogCategoryRepository
import com.lesofn.archforge.blog.api.domain.BlogArticle
import com.lesofn.archforge.blog.api.domain.BlogCategory
import com.lesofn.archforge.blog.api.enums.BlogArticleStatus
import com.lesofn.archforge.blog.api.errors.BlogException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class BlogArticleServiceSpec extends Specification {

    BlogArticleRepository articleRepository = Mock()
    BlogCategoryRepository categoryRepository = Mock()
    BlogArticleServiceImpl service = new BlogArticleServiceImpl(articleRepository, categoryRepository)

    def "create persists draft after validating category"() {
        given:
        def article = new BlogArticle(id: null, categoryId: 2L, title: "t", slug: "s", content: "c", status: BlogArticleStatus.DRAFT)
        categoryRepository.findById(2L) >> Optional.of(new BlogCategory())
        articleRepository.findBySlugAndStatusAndDeletedFalse("s", _) >> Optional.empty()
        articleRepository.save(_) >> { BlogArticle saved -> saved.setId(9L); saved }

        when:
        def created = service.create(article)

        then:
        created.id == 9L
        created.status == BlogArticleStatus.DRAFT
    }

    def "publish and offline follow the article state machine"() {
        given:
        def article = new BlogArticle(id: 1L, categoryId: 2L, title: "t", slug: "s", content: "c", status: BlogArticleStatus.DRAFT)
        articleRepository.findById(1L) >> Optional.of(article)
        articleRepository.save(_) >> { BlogArticle saved -> saved }

        when:
        def published = service.publish(1L)

        then:
        published.status == BlogArticleStatus.PUBLISHED

        when:
        def offline = service.offline(1L)

        then:
        offline.status == BlogArticleStatus.OFFLINE
    }

    def "delete missing article throws"() {
        given:
        articleRepository.findById(88L) >> Optional.empty()

        when:
        service.delete(88L)

        then:
        thrown(BlogException)
    }

    def "pagePublished delegates to repository"() {
        given:
        def pageable = PageRequest.of(0, 10)
        articleRepository.findAll(_, pageable) >> new PageImpl<BlogArticle>([])

        expect:
        service.pagePublished(pageable, null, null).totalElements == 0
    }
}
