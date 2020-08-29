/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.ssp.service.impl;

import org.apache.commons.lang.StringUtils;
import org.jasig.ssp.dao.JournalEntryDao;
import org.jasig.ssp.dao.PersonDao;
import org.jasig.ssp.model.JournalEntry;
import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.service.AbstractRestrictedPersonAssocAuditableService;
import org.jasig.ssp.service.JournalEntryService;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonProgramStatusService;
import org.jasig.ssp.transferobject.reports.*;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.jasig.ssp.web.api.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
@Transactional
public class JournalEntryServiceImpl
        extends AbstractRestrictedPersonAssocAuditableService<JournalEntry>
        implements JournalEntryService {

    private final JournalEntryDao dao;

    private final PersonProgramStatusService personProgramStatusService;

    private final PersonDao personDao;

    @Autowired
    public JournalEntryServiceImpl(JournalEntryDao dao, PersonProgramStatusService personProgramStatusService, PersonDao personDao) {
        this.dao = dao;
        this.personProgramStatusService = personProgramStatusService;
        this.personDao = personDao;
    }

    @Override
    protected JournalEntryDao getDao() {
        return dao;
    }

    @Override
    public JournalEntry create(final JournalEntry obj)
            throws ObjectNotFoundException, ValidationException {
        return getJournalEntry(obj);
    }

    @Override
    public JournalEntry save(final JournalEntry obj)
            throws ObjectNotFoundException, ValidationException {
        return getJournalEntry(obj);
    }

    private JournalEntry getJournalEntry(JournalEntry obj) {
        final JournalEntry journalEntry = getDao().save(obj);
        checkForTransition(journalEntry);
        return journalEntry;
    }

    private void checkForTransition(final JournalEntry journalEntry) {
        // search for a JournalStep that indicates a transition
        journalEntry.getJournalEntryDetails().stream()
                .filter(detail -> detail.getJournalStepJournalStepDetail().getJournalStep()
                        .isUsedForTransition())
                .findFirst()
                .ifPresent(detail -> setTransitionForStudent(journalEntry));
    }

    private void setTransitionForStudent(JournalEntry journalEntry) throws RuntimeException {
        try {
            personProgramStatusService.setTransitionForStudent(journalEntry
                    .getPerson());
        } catch (ObjectNotFoundException e) {
            throw new RuntimeException((e.getObjectId() + " " + e.getName()));
        } catch (ValidationException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Long getCountForCoach(Person coach, Date createDateFrom, Date createDateTo, List<UUID> studentTypeIds) {
        return dao.getJournalCountForCoach(coach, createDateFrom, createDateTo, studentTypeIds);
    }

    @Override
    public Long getStudentCountForCoach(Person coach, Date createDateFrom, Date createDateTo, List<UUID> studentTypeIds) {
        return dao.getStudentJournalCountForCoach(coach, createDateFrom, createDateTo, studentTypeIds);
    }

    @Override
    public PagingWrapper<EntityStudentCountByCoachTO> getStudentJournalCountForCoaches(EntityCountByCoachSearchForm form) {
        return dao.getStudentJournalCountForCoaches(form);
    }

    @Override
    public PagingWrapper<JournalStepStudentReportTO> getJournalStepStudentReportTOsFromCriteria(JournalStepSearchFormTO personSearchForm,
                                                                                                SortingAndPaging sAndP) {
        return dao.getJournalStepStudentReportTOsFromCriteria(personSearchForm,
                sAndP);
    }

    @Override
    public List<JournalCaseNotesStudentReportTO> getJournalCaseNoteStudentReportTOsFromCriteria(JournalStepSearchFormTO personSearchForm, SortingAndPaging sAndP) throws ObjectNotFoundException {
        final List<JournalCaseNotesStudentReportTO> personsWithJournalEntries = dao.getJournalCaseNoteStudentReportTOsFromCriteria(personSearchForm, sAndP);
        final Map<String, JournalCaseNotesStudentReportTO> map = personsWithJournalEntries.stream()
                .collect(toMap(JournalCaseNotesStudentReportTO::getSchoolId, Function.identity()));

        final SortingAndPaging personSAndP = SortingAndPaging.createForSingleSortAll(ObjectStatus.ACTIVE, "lastName", "DESC");
        final PagingWrapper<BaseStudentReportTO> persons = personDao.getStudentReportTOs(personSearchForm, personSAndP);

        if (persons == null) {
            return personsWithJournalEntries;
        }

        persons.forEach(person -> {
                    if (!map.containsKey(person.getSchoolId()) && StringUtils.isNotBlank(person.getCoachSchoolId())) {
                        boolean addStudent = isAddStudent(personSearchForm, person);
                        if (addStudent) {
                            final JournalCaseNotesStudentReportTO entry = new JournalCaseNotesStudentReportTO(person);
                            personsWithJournalEntries.add(entry);
                            map.put(entry.getSchoolId(), entry);
                        }
                    }
                }

        );
        sortByStudentName(personsWithJournalEntries);

        return personsWithJournalEntries;
    }

    private boolean isAddStudent(JournalStepSearchFormTO personSearchForm, BaseStudentReportTO person) {
        return personSearchForm.getJournalSourceIds() == null || getDao().getJournalCountForPersonForJournalSourceIds(person.getId(), personSearchForm.getJournalSourceIds()) != 0;
    }

    private static void sortByStudentName(List<JournalCaseNotesStudentReportTO> toSort) {
        toSort.sort((p1, p2) -> {

            int value = p1.getLastName().compareToIgnoreCase(
                    p2.getLastName());
            if (value != 0)
                return value;

            value = p1.getFirstName().compareToIgnoreCase(
                    p2.getFirstName());
            if (value != 0)
                return value;
            if (p1.getMiddleName() == null && p2.getMiddleName() == null)
                return 0;
            if (p1.getMiddleName() == null)
                return -1;
            if (p2.getMiddleName() == null)
                return 1;
            return p1.getMiddleName().compareToIgnoreCase(
                    p2.getMiddleName());
        });
    }

}