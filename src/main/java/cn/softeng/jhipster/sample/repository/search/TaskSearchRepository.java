package cn.softeng.jhipster.sample.repository.search;

import cn.softeng.jhipster.sample.domain.Task;
import cn.softeng.jhipster.sample.repository.TaskRepository;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Task} entity.
 */
public interface TaskSearchRepository extends ElasticsearchRepository<Task, Long>, TaskSearchRepositoryInternal {}

interface TaskSearchRepositoryInternal {
    Stream<Task> search(String query);

    Stream<Task> search(Query query);

    @Async
    void index(Task entity);

    @Async
    void deleteFromIndexById(Long id);
}

class TaskSearchRepositoryInternalImpl implements TaskSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final TaskRepository repository;

    TaskSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, TaskRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Task> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Task> search(Query query) {
        return elasticsearchTemplate.search(query, Task.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Task entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Task.class);
    }
}
