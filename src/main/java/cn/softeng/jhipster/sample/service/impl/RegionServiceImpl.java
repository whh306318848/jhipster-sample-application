package cn.softeng.jhipster.sample.service.impl;

import cn.softeng.jhipster.sample.domain.Region;
import cn.softeng.jhipster.sample.repository.RegionRepository;
import cn.softeng.jhipster.sample.repository.search.RegionSearchRepository;
import cn.softeng.jhipster.sample.service.RegionService;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link cn.softeng.jhipster.sample.domain.Region}.
 */
@Service
@Transactional
public class RegionServiceImpl implements RegionService {

    private static final Logger LOG = LoggerFactory.getLogger(RegionServiceImpl.class);

    private final RegionRepository regionRepository;

    private final RegionSearchRepository regionSearchRepository;

    public RegionServiceImpl(RegionRepository regionRepository, RegionSearchRepository regionSearchRepository) {
        this.regionRepository = regionRepository;
        this.regionSearchRepository = regionSearchRepository;
    }

    @Override
    public Region save(Region region) {
        LOG.debug("Request to save Region : {}", region);
        region = regionRepository.save(region);
        regionSearchRepository.index(region);
        return region;
    }

    @Override
    public Region update(Region region) {
        LOG.debug("Request to update Region : {}", region);
        region = regionRepository.save(region);
        regionSearchRepository.index(region);
        return region;
    }

    @Override
    public Optional<Region> partialUpdate(Region region) {
        LOG.debug("Request to partially update Region : {}", region);

        return regionRepository
            .findById(region.getId())
            .map(existingRegion -> {
                if (region.getRegionName() != null) {
                    existingRegion.setRegionName(region.getRegionName());
                }

                return existingRegion;
            })
            .map(regionRepository::save)
            .map(savedRegion -> {
                regionSearchRepository.index(savedRegion);
                return savedRegion;
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> findAll() {
        LOG.debug("Request to get all Regions");
        return regionRepository.findAll();
    }

    /**
     *  Get all the regions where Country is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Region> findAllWhereCountryIsNull() {
        LOG.debug("Request to get all regions where Country is null");
        return StreamSupport.stream(regionRepository.findAll().spliterator(), false).filter(region -> region.getCountry() == null).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Region> findOne(Long id) {
        LOG.debug("Request to get Region : {}", id);
        return regionRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Region : {}", id);
        regionRepository.deleteById(id);
        regionSearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> search(String query) {
        LOG.debug("Request to search Regions for query {}", query);
        try {
            return StreamSupport.stream(regionSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
