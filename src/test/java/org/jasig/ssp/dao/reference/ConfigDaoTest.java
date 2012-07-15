package org.jasig.ssp.dao.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.model.reference.Config;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.impl.SecurityServiceInTestEnvironment;
import org.jasig.ssp.util.collections.Pair;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortDirection;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Tests for {@link ConfigDao}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("../dao-testConfig.xml")
@TransactionConfiguration
@Transactional
public class ConfigDaoTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConfigDaoTest.class);

	/**
	 * For testing that overriding sort by name instead of tree order works. See
	 * {@link #testGetAllWithSorting()}. "67BD120E-9BE1-11E1-AD1F-0026B9E7FF4C"
	 * is for the "app_title" configuration item.
	 */
	private static final UUID CONFIG_ID_FOR_FIRST_ALPHABETICAL_NAME = UUID
			.fromString("67BD120E-9BE1-11E1-AD1F-0026B9E7FF4C");

	@Autowired
	private transient ConfigDao dao;

	@Autowired
	private transient SecurityServiceInTestEnvironment securityService;

	/**
	 * Setup the security service for the tests
	 */
	@Before
	public void setUp() {
		securityService.setCurrent(new Person(Person.SYSTEM_ADMINISTRATOR_ID));
	}

	/**
	 * Test {@link ConfigDao#save(Config)}
	 * 
	 * @throws ObjectNotFoundException
	 *             Should not be thrown for this test
	 */
	@Test
	public void testSaveNew() throws ObjectNotFoundException {
		UUID savedId;

		Config obj = new Config();
		obj.setName("new name");
		obj.setObjectStatus(ObjectStatus.ACTIVE);
		obj.setDefaultValue("123");
		obj = dao.save(obj);

		savedId = obj.getId();

		LOGGER.debug(obj.toString());

		final Config saved = dao.get(savedId);
		assertNotNull("Reloading did not return the correct saved data.", saved);
		assertNotNull("Reloaded data did not have a set Name property.",
				saved.getName());
		assertEquals("Reloaded data did not have matching data.",
				obj.getName(), saved.getName());

		final Collection<Config> all = dao.getAll(ObjectStatus.ACTIVE)
				.getRows();
		assertNotNull("GetAll() should not have returned a null collection.",
				all);
		assertFalse(
				"GetAll() should not have returned an empty list. (This test assumes some sample reference data exists in the testing database.)",
				all.isEmpty());
		assertList(all);

		dao.delete(saved);
	}

	/**
	 * Test that {@link ConfigDao#get(UUID)} returns null if identifier is not
	 * found.
	 * 
	 * @throws ObjectNotFoundException
	 *             expected
	 */
	@Test(expected = ObjectNotFoundException.class)
	public void testNotFoundIdReturnsNull() throws ObjectNotFoundException {
		final UUID id = UUID.randomUUID();
		final Config config = dao.get(id);

		assertNull(
				"Get() did not return null when a missing identifier was used.",
				config);
	}

	/**
	 * Asserts that list contains objects with non-null identifiers.
	 * 
	 * @param objects
	 *            Collection of objects to assert have non-null identifiers.
	 */
	private void assertList(final Collection<Config> objects) {
		assertFalse("List should not have been empty.", objects.isEmpty());

		for (final Config object : objects) {
			assertNotNull("List item should not have a null id.",
					object.getId());
		}
	}

	/**
	 * Test UUID generation, {@link ConfigDao#save(Config)}.
	 */
	@Test
	public void uuidGeneration() {
		final Config obj = new Config();
		obj.setName("A name");
		obj.setObjectStatus(ObjectStatus.ACTIVE);
		obj.setDefaultValue("");
		dao.save(obj);

		assertNotNull("Transient instance was not assigned a new identifier.",
				obj.getId());
	}

	/**
	 * Tests the {@link ConfigDao#getAll(ObjectStatus)} method. Assumes the test
	 * data includes at least one entry.
	 */
	@Test
	public void testGetAll() {
		final PagingWrapper<Config> data = dao.getAll(new SortingAndPaging(
				ObjectStatus.ALL));
		assertNotNull("Config data should not be null.", data);
		assertFalse("Config data should not be empty.", data.getRows()
				.isEmpty());
	}

	@Test
	public void testGetAllWithSorting() {
		// arrange
		final List<Pair<String, SortDirection>> sorts = Lists.newArrayList();
		sorts.add(new Pair<String, SortDirection>("name", SortDirection.ASC));

		// act
		final PagingWrapper<Config> list = dao
				.getAll(new SortingAndPaging(ObjectStatus.ALL, 0, 10, sorts,
						null, null));

		// assert
		assertFalse("List should not have been empty.", list.getRows()
				.isEmpty());
		assertEquals("Sorted first result ID did not match expected.",
				CONFIG_ID_FOR_FIRST_ALPHABETICAL_NAME, list.getRows()
						.iterator().next().getId());
	}
}