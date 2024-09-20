package cn.softeng.jhipster.sample.service.impl;

import cn.softeng.jhipster.sample.domain.JobHistory;
import cn.softeng.jhipster.sample.repository.JobHistoryRepository;
import cn.softeng.jhipster.sample.repository.search.JobHistorySearchRepository;
import cn.softeng.jhipster.sample.service.JobHistoryService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link cn.softeng.jhipster.sample.domain.JobHistory}.
 */
@Service
@Transactional
public class JobHistoryServiceImpl implements JobHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(JobHistoryServiceImpl.class);

    private final JobHistoryRepository jobHistoryRepository;

    private final JobHistorySearchRepository jobHistorySearchRepository;

    public JobHistoryServiceImpl(JobHistoryRepository jobHistoryRepository, JobHistorySearchRepository jobHistorySearchRepository) {
        this.jobHistoryRepository = jobHistoryRepository;
        this.jobHistorySearchRepository = jobHistorySearchRepository;
    }

    @Override
    public JobHistory save(JobHistory jobHistory) {
        LOG.debug("Request to save JobHistory : {}", jobHistory);
        jobHistory = jobHistoryRepository.save(jobHistory);
        jobHistorySearchRepository.index(jobHistory);
        return jobHistory;
    }

    @Override
    public JobHistory update(JobHistory jobHistory) {
        LOG.debug("Request to update JobHistory : {}", jobHistory);
        jobHistory = jobHistoryRepository.save(jobHistory);
        jobHistorySearchRepository.index(jobHistory);
        return jobHistory;
    }

    @Override
    public Optional<JobHistory> partialUpdate(JobHistory jobHistory) {
        LOG.debug("Request to partially update JobHistory : {}", jobHistory);

        return jobHistoryRepository
            .findById(jobHistory.getId())
            .map(existingJobHistory -> {
                if (jobHistory.getStartDate() != null) {
                    existingJobHistory.setStartDate(jobHistory.getStartDate());
                }
                if (jobHistory.getEndDate() != null) {
                    existingJobHistory.setEndDate(jobHistory.getEndDate());
                }
                if (jobHistory.getLanguage() != null) {
                    existingJobHistory.setLanguage(jobHistory.getLanguage());
                }

                return existingJobHistory;
            })
            .map(jobHistoryRepository::save)
            .map(savedJobHistory -> {
                jobHistorySearchRepository.index(savedJobHistory);
                return savedJobHistory;
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobHistory> findAll(Pageable pageable) {
        LOG.debug("Request to get all JobHistories");
        return jobHistoryRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobHistory> findOne(Long id) {
        LOG.debug("Request to get JobHistory : {}", id);
        return jobHistoryRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete JobHistory : {}", id);
        jobHistoryRepository.deleteById(id);
        jobHistorySearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobHistory> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of JobHistories for query {}", query);
        return jobHistorySearchRepository.search(query, pageable);
    }
}
