package cn.softeng.jhipster.sample.web.rest;

import static cn.softeng.jhipster.sample.domain.JobHistoryAsserts.*;
import static cn.softeng.jhipster.sample.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cn.softeng.jhipster.sample.IntegrationTest;
import cn.softeng.jhipster.sample.domain.JobHistory;
import cn.softeng.jhipster.sample.domain.enumeration.Language;
import cn.softeng.jhipster.sample.repository.JobHistoryRepository;
import cn.softeng.jhipster.sample.repository.search.JobHistorySearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link JobHistoryResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class JobHistoryResourceIT {

    private static final Instant DEFAULT_START_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Language DEFAULT_LANGUAGE = Language.FRENCH;
    private static final Language UPDATED_LANGUAGE = Language.ENGLISH;

    private static final String ENTITY_API_URL = "/api/job-histories";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/job-histories/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private JobHistoryRepository jobHistoryRepository;

    @Autowired
    private JobHistorySearchRepository jobHistorySearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restJobHistoryMockMvc;

    private JobHistory jobHistory;

    private JobHistory insertedJobHistory;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JobHistory createEntity() {
        return new JobHistory().startDate(DEFAULT_START_DATE).endDate(DEFAULT_END_DATE).language(DEFAULT_LANGUAGE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JobHistory createUpdatedEntity() {
        return new JobHistory().startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).language(UPDATED_LANGUAGE);
    }

    @BeforeEach
    public void initTest() {
        jobHistory = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedJobHistory != null) {
            jobHistoryRepository.delete(insertedJobHistory);
            jobHistorySearchRepository.delete(insertedJobHistory);
            insertedJobHistory = null;
        }
    }

    @Test
    @Transactional
    void createJobHistory() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        // Create the JobHistory
        var returnedJobHistory = om.readValue(
            restJobHistoryMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(jobHistory)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            JobHistory.class
        );

        // Validate the JobHistory in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertJobHistoryUpdatableFieldsEquals(returnedJobHistory, getPersistedJobHistory(returnedJobHistory));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedJobHistory = returnedJobHistory;
    }

    @Test
    @Transactional
    void createJobHistoryWithExistingId() throws Exception {
        // Create the JobHistory with an existing ID
        jobHistory.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restJobHistoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(jobHistory)))
            .andExpect(status().isBadRequest());

        // Validate the JobHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllJobHistories() throws Exception {
        // Initialize the database
        insertedJobHistory = jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList
        restJobHistoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(jobHistory.getId().intValue())))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].language").value(hasItem(DEFAULT_LANGUAGE.toString())));
    }

    @Test
    @Transactional
    void getJobHistory() throws Exception {
        // Initialize the database
        insertedJobHistory = jobHistoryRepository.saveAndFlush(jobHistory);

        // Get the jobHistory
        restJobHistoryMockMvc
            .perform(get(ENTITY_API_URL_ID, jobHistory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(jobHistory.getId().intValue()))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.language").value(DEFAULT_LANGUAGE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingJobHistory() throws Exception {
        // Get the jobHistory
        restJobHistoryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingJobHistory() throws Exception {
        // Initialize the database
        insertedJobHistory = jobHistoryRepository.saveAndFlush(jobHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        jobHistorySearchRepository.save(jobHistory);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());

        // Update the jobHistory
        JobHistory updatedJobHistory = jobHistoryRepository.findById(jobHistory.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedJobHistory are not directly saved in db
        em.detach(updatedJobHistory);
        updatedJobHistory.startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).language(UPDATED_LANGUAGE);

        restJobHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedJobHistory.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedJobHistory))
            )
            .andExpect(status().isOk());

        // Validate the JobHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedJobHistoryToMatchAllProperties(updatedJobHistory);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<JobHistory> jobHistorySearchList = Streamable.of(jobHistorySearchRepository.findAll()).toList();
                JobHistory testJobHistorySearch = jobHistorySearchList.get(searchDatabaseSizeAfter - 1);

                assertJobHistoryAllPropertiesEquals(testJobHistorySearch, updatedJobHistory);
            });
    }

    @Test
    @Transactional
    void putNonExistingJobHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        jobHistory.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJobHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, jobHistory.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(jobHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the JobHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchJobHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        jobHistory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJobHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(jobHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the JobHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamJobHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        jobHistory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJobHistoryMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(jobHistory)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the JobHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateJobHistoryWithPatch() throws Exception {
        // Initialize the database
        insertedJobHistory = jobHistoryRepository.saveAndFlush(jobHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the jobHistory using partial update
        JobHistory partialUpdatedJobHistory = new JobHistory();
        partialUpdatedJobHistory.setId(jobHistory.getId());

        partialUpdatedJobHistory.startDate(UPDATED_START_DATE);

        restJobHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedJobHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedJobHistory))
            )
            .andExpect(status().isOk());

        // Validate the JobHistory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertJobHistoryUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedJobHistory, jobHistory),
            getPersistedJobHistory(jobHistory)
        );
    }

    @Test
    @Transactional
    void fullUpdateJobHistoryWithPatch() throws Exception {
        // Initialize the database
        insertedJobHistory = jobHistoryRepository.saveAndFlush(jobHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the jobHistory using partial update
        JobHistory partialUpdatedJobHistory = new JobHistory();
        partialUpdatedJobHistory.setId(jobHistory.getId());

        partialUpdatedJobHistory.startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).language(UPDATED_LANGUAGE);

        restJobHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedJobHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedJobHistory))
            )
            .andExpect(status().isOk());

        // Validate the JobHistory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertJobHistoryUpdatableFieldsEquals(partialUpdatedJobHistory, getPersistedJobHistory(partialUpdatedJobHistory));
    }

    @Test
    @Transactional
    void patchNonExistingJobHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        jobHistory.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJobHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, jobHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(jobHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the JobHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchJobHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        jobHistory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJobHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(jobHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the JobHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamJobHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        jobHistory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJobHistoryMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(jobHistory)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the JobHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteJobHistory() throws Exception {
        // Initialize the database
        insertedJobHistory = jobHistoryRepository.saveAndFlush(jobHistory);
        jobHistoryRepository.save(jobHistory);
        jobHistorySearchRepository.save(jobHistory);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the jobHistory
        restJobHistoryMockMvc
            .perform(delete(ENTITY_API_URL_ID, jobHistory.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(jobHistorySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchJobHistory() throws Exception {
        // Initialize the database
        insertedJobHistory = jobHistoryRepository.saveAndFlush(jobHistory);
        jobHistorySearchRepository.save(jobHistory);

        // Search the jobHistory
        restJobHistoryMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + jobHistory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(jobHistory.getId().intValue())))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].language").value(hasItem(DEFAULT_LANGUAGE.toString())));
    }

    protected long getRepositoryCount() {
        return jobHistoryRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected JobHistory getPersistedJobHistory(JobHistory jobHistory) {
        return jobHistoryRepository.findById(jobHistory.getId()).orElseThrow();
    }

    protected void assertPersistedJobHistoryToMatchAllProperties(JobHistory expectedJobHistory) {
        assertJobHistoryAllPropertiesEquals(expectedJobHistory, getPersistedJobHistory(expectedJobHistory));
    }

    protected void assertPersistedJobHistoryToMatchUpdatableProperties(JobHistory expectedJobHistory) {
        assertJobHistoryAllUpdatablePropertiesEquals(expectedJobHistory, getPersistedJobHistory(expectedJobHistory));
    }
}
