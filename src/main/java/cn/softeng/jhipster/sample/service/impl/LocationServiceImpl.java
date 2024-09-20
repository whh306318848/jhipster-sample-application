package cn.softeng.jhipster.sample.service.impl;

import cn.softeng.jhipster.sample.domain.Location;
import cn.softeng.jhipster.sample.repository.LocationRepository;
import cn.softeng.jhipster.sample.repository.search.LocationSearchRepository;
import cn.softeng.jhipster.sample.service.LocationService;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link cn.softeng.jhipster.sample.domain.Location}.
 */
@Service
@Transactional
public class LocationServiceImpl implements LocationService {

    private static final Logger LOG = LoggerFactory.getLogger(LocationServiceImpl.class);

    private final LocationRepository locationRepository;

    private final LocationSearchRepository locationSearchRepository;

    public LocationServiceImpl(LocationRepository locationRepository, LocationSearchRepository locationSearchRepository) {
        this.locationRepository = locationRepository;
        this.locationSearchRepository = locationSearchRepository;
    }

    @Override
    public Location save(Location location) {
        LOG.debug("Request to save Location : {}", location);
        location = locationRepository.save(location);
        locationSearchRepository.index(location);
        return location;
    }

    @Override
    public Location update(Location location) {
        LOG.debug("Request to update Location : {}", location);
        location = locationRepository.save(location);
        locationSearchRepository.index(location);
        return location;
    }

    @Override
    public Optional<Location> partialUpdate(Location location) {
        LOG.debug("Request to partially update Location : {}", location);

        return locationRepository
            .findById(location.getId())
            .map(existingLocation -> {
                if (location.getStreetAddress() != null) {
                    existingLocation.setStreetAddress(location.getStreetAddress());
                }
                if (location.getPostalCode() != null) {
                    existingLocation.setPostalCode(location.getPostalCode());
                }
                if (location.getCity() != null) {
                    existingLocation.setCity(location.getCity());
                }
                if (location.getStateProvince() != null) {
                    existingLocation.setStateProvince(location.getStateProvince());
                }

                return existingLocation;
            })
            .map(locationRepository::save)
            .map(savedLocation -> {
                locationSearchRepository.index(savedLocation);
                return savedLocation;
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Location> findAll() {
        LOG.debug("Request to get all Locations");
        return locationRepository.findAll();
    }

    /**
     *  Get all the locations where Department is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Location> findAllWhereDepartmentIsNull() {
        LOG.debug("Request to get all locations where Department is null");
        return StreamSupport.stream(locationRepository.findAll().spliterator(), false)
            .filter(location -> location.getDepartment() == null)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Location> findOne(Long id) {
        LOG.debug("Request to get Location : {}", id);
        return locationRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Location : {}", id);
        locationRepository.deleteById(id);
        locationSearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Location> search(String query) {
        LOG.debug("Request to search Locations for query {}", query);
        try {
            return StreamSupport.stream(locationSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
