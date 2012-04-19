package org.studentsuccessplan.ssp.service.reference.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.studentsuccessplan.ssp.dao.reference.EthnicityDao;
import org.studentsuccessplan.ssp.model.ObjectStatus;
import org.studentsuccessplan.ssp.model.reference.Ethnicity;
import org.studentsuccessplan.ssp.service.ObjectNotFoundException;
import org.studentsuccessplan.ssp.service.reference.EthnicityService;
import org.studentsuccessplan.ssp.util.sort.SortingAndPaging;

@Service
@Transactional
public class EthnicityServiceImpl implements EthnicityService {

	@Autowired
	private EthnicityDao dao;

	@Override
	public List<Ethnicity> getAll(SortingAndPaging sAndP) {
		return dao.getAll(sAndP);
	}

	@Override
	public Ethnicity get(UUID id) throws ObjectNotFoundException {
		Ethnicity obj = dao.get(id);
		if (null == obj) {
			throw new ObjectNotFoundException(id, "Ethnicity");
		}

		return obj;
	}

	@Override
	public Ethnicity create(Ethnicity obj) {
		return dao.save(obj);
	}

	@Override
	public Ethnicity save(Ethnicity obj) throws ObjectNotFoundException {
		Ethnicity current = get(obj.getId());

		current.setName(obj.getName());
		current.setDescription(obj.getDescription());
		current.setObjectStatus(obj.getObjectStatus());

		return dao.save(current);
	}

	@Override
	public void delete(UUID id) throws ObjectNotFoundException {
		Ethnicity current = get(id);

		if (null != current) {
			current.setObjectStatus(ObjectStatus.DELETED);
			save(current);
		}
	}

	protected void setDao(EthnicityDao dao) {
		this.dao = dao;
	}
}
