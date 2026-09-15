package com.lesofn.archforge.cms.internal.service

import com.lesofn.archforge.cms.api.dao.CmsArticleRepository
import com.lesofn.archforge.cms.api.dao.CmsCategoryRepository
import com.lesofn.archforge.cms.api.domain.CmsArticle
import com.lesofn.archforge.cms.api.domain.CmsCategory
import com.lesofn.archforge.cms.api.enums.CmsArticleStatus
import com.lesofn.archforge.cms.api.errors.CmsException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class CmsArticleServiceSpec extends Specification {

    CmsArticleRepository articleRepository = Mock()
    CmsCategoryRepository categoryRepository = Mock()
    CmsArticleServiceImpl service = new CmsArticleServiceImpl(articleRepository, categoryRepository)

    def "create persists draft after validating category"() {
        given:
        def article = new CmsArticle(id: null, categoryId: 2L, title: "t", slug: "s", content: "c", status: CmsArticleStatus.DRAFT)
        categoryRepository.findById(2L) >> Optional.of(new CmsCategory())
        articleRepository.findBySlugAndStatusAndDeletedFalse("s", _) >> Optional.empty()
        articleRepository.save(_) >> { CmsArticle saved -> saved.setId(9L); saved }

        when:
        def created = service.create(article)

        then:
        created.id == 9L
        created.status == CmsArticleStatus.DRAFT
    }

    def "publish and offline follow the article state machine"() {
        given:
        def article = new CmsArticle(id: 1L, categoryId: 2L, title: "t", slug: "s", content: "c", status: CmsArticleStatus.DRAFT)
        articleRepository.findById(1L) >> Optional.of(article)
        articleRepository.save(_) >> { CmsArticle saved -> saved }

        when:
        def published = service.publish(1L)

        then:
        published.status == CmsArticleStatus.PUBLISHED

        when:
        def offline = service.offline(1L)

        then:
        offline.status == CmsArticleStatus.OFFLINE
    }

    def "delete missing article throws"() {
        given:
        articleRepository.findById(88L) >> Optional.empty()

        when:
        service.delete(88L)

        then:
        thrown(CmsException)
    }

    def "pagePublished delegates to repository"() {
        given:
        def pageable = PageRequest.of(0, 10)
        articleRepository.findAll(_, pageable) >> new PageImpl<CmsArticle>([])

        expect:
        service.pagePublished(pageable, null, null).totalElements == 0
    }
}
