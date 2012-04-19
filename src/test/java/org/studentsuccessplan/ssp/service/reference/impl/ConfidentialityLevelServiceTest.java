package org.studentsuccessplan.ssp.service.reference.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.studentsuccessplan.ssp.dao.reference.ConfidentialityLevelDao;
import org.studentsuccessplan.ssp.model.ObjectStatus;
import org.studentsuccessplan.ssp.model.reference.ConfidentialityLevel;
import org.studentsuccessplan.ssp.service.ObjectNotFoundException;
import org.studentsuccessplan.ssp.util.sort.SortingAndPaging;

public class ConfidentialityLevelServiceTest {

	private ConfidentialityLevelServiceImpl service;
	private ConfidentialityLevelDao dao;

	@Before
	public void setup() {
		service = new ConfidentialityLevelServiceImpl();
		dao = createMock(ConfidentialityLevelDao.class);

		service.setDao(dao);
	}

	@Test
	public void testGetAll() {
		List<ConfidentialityLevel> daoAll = new ArrayList<ConfidentialityLevel>();
		daoAll.add(new ConfidentialityLevel());

		expect(dao.getAll(isA(SortingAndPaging.class)))
				.andReturn(daoAll);

		replay(dao);

		List<ConfidentialityLevel> all = service.getAll(new SortingAndPaging(
				ObjectStatus.ACTIVE));
		assertTrue(all.size() > 0);
		verify(dao);
	}

	@Test
	public void testGet() throws ObjectNotFoundException {
		UUID id = UUID.randomUUID();
		ConfidentialityLevel daoOne = new ConfidentialityLevel(id);

		expect(dao.get(id)).andReturn(daoOne);

		replay(dao);

		assertNotNull(service.get(id));
		verify(dao);
	}

	@Test
	public void testSave() throws ObjectNotFoundException {
		UUID id = UUID.randomUUID();
		ConfidentialityLevel daoOne = new ConfidentialityLevel(id);

		expect(dao.get(id)).andReturn(daoOne);
		expect(dao.save(daoOne)).andReturn(daoOne);

		replay(dao);

		assertNotNull(service.save(daoOne));
		verify(dao);
	}

	@Test
	public void testDelete() throws ObjectNotFoundException {
		UUID id = UUID.randomUUID();
		ConfidentialityLevel daoOne = new ConfidentialityLevel(id);

		expect(dao.get(id)).andReturn(daoOne).times(2);
		expect(dao.save(daoOne)).andReturn(daoOne);
		expect(dao.get(id)).andReturn(null);

		replay(dao);

		service.delete(id);

		boolean found = true;
		try {
			service.get(id);
		} catch (ObjectNotFoundException e) {
			found = false;
		}

		assertFalse(found);
		verify(dao);
	}

}
