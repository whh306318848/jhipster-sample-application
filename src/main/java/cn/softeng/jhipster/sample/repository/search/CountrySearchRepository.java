package cn.softeng.jhipster.sample.repository.search;

import cn.softeng.jhipster.sample.domain.Country;
import cn.softeng.jhipster.sample.repository.CountryRepository;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Country} entity.
 */
public interface CountrySearchRepository extends ElasticsearchRepository<Country, Long>, CountrySearchRepositoryInternal {}

interface CountrySearchRepositoryInternal {
    Stream<Country> search(String query);

    Stream<Country> search(Query query);

    @Async
    void index(Country entity);

    @Async
    void deleteFromIndexById(Long id);
}

class CountrySearchRepositoryInternalImpl implements CountrySearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final CountryRepository repository;

    CountrySearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, CountryRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Country> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Country> search(Query query) {
        return elasticsearchTemplate.search(query, Country.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Country entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Country.class);
    }
}
