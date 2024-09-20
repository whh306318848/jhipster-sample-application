package cn.softeng.jhipster.sample.service;

import cn.softeng.jhipster.sample.domain.Location;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link cn.softeng.jhipster.sample.domain.Location}.
 */
public interface LocationService {
    /**
     * Save a location.
     *
     * @param location the entity to save.
     * @return the persisted entity.
     */
    Location save(Location location);

    /**
     * Updates a location.
     *
     * @param location the entity to update.
     * @return the persisted entity.
     */
    Location update(Location location);

    /**
     * Partially updates a location.
     *
     * @param location the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Location> partialUpdate(Location location);

    /**
     * Get all the locations.
     *
     * @return the list of entities.
     */
    List<Location> findAll();

    /**
     * Get all the Location where Department is {@code null}.
     *
     * @return the {@link List} of entities.
     */
    List<Location> findAllWhereDepartmentIsNull();

    /**
     * Get the "id" location.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Location> findOne(Long id);

    /**
     * Delete the "id" location.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the location corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    List<Location> search(String query);
}
