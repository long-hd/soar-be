package com.hdl.soar.framework.jpa.core.repository;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class SoftDeleteRepository <T extends BasePO, ID> extends SimpleJpaRepository<T, ID> {

    public SoftDeleteRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void deleteById(ID id) {
        T entity = findById(id).orElseThrow(() ->
                new EntityNotFoundException("Entity not found: " + id));
        entity.setDeleted(true);
        save(entity);  // UPDATE ... SET deleted = true
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(T entity) {
        entity.setDeleted(true);
        save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void deleteAll(Iterable<? extends T> entities) {
        entities.forEach(this::delete);
    }

}
