package org.jasig.ssp.dao;

import java.util.UUID;

import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jasig.ssp.model.Goal;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.springframework.stereotype.Repository;

/**
 * Data access class for the Goal entity.
 */
@Repository
public class GoalDao
		extends AbstractAuditableCrudDao<Goal>
		implements AuditableCrudDao<Goal> {

	/**
	 * Construct an instance with the specified specific class for use by base
	 * class methods.
	 */
	public GoalDao() {
		super(Goal.class);
	}

	/**
	 * Gets all instances for the specified {@link Person}, filtered by the
	 * specified filters.
	 * 
	 * @param personId
	 *            Person identifier
	 * @param sAndP
	 *            Sorting and paging filters, if any
	 * @return All instance for the specified Person with any specified filters
	 *         applied.
	 */
	@SuppressWarnings("unchecked")
	public PagingWrapper<Goal> getAllForPersonId(final UUID personId,
			final SortingAndPaging sAndP) {
		final long totalRows = (Long) createCriteria()
				.add(Restrictions.eq("person.id", personId))
				.setProjection(Projections.rowCount())
				.uniqueResult();

		return new PagingWrapper<Goal>(totalRows,
				createCriteria(sAndP)
						.add(Restrictions.eq("person.id", personId))
						.list());
	}
}