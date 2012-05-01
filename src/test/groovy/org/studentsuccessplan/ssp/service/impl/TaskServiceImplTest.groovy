package org.studentsuccessplan.ssp.service.impl

import static org.junit.Assert.*

import java.util.UUID

import org.junit.Test
import org.junit.Before
import org.studentsuccessplan.ssp.dao.TaskDao
import org.studentsuccessplan.ssp.model.ObjectStatus
import org.studentsuccessplan.ssp.model.Person
import org.studentsuccessplan.ssp.model.Task
import org.studentsuccessplan.ssp.model.reference.Challenge
import org.studentsuccessplan.ssp.model.reference.ChallengeReferral


class TaskServiceImplTest {
	//the task that will get returned from the mock dao
	private Task testTask1

	private Person testStudent = new Person(id:UUID.randomUUID());

	private List<Task> all

	//a mock dao for the service to call;
	private def dao = [
		get:{UUID id -> testTask1},
		save:{Task task -> return task},
		getAllForPersonId:{personId, sAndP -> return all}
	] as TaskDao

	private TaskServiceImpl service

	@Before
	void setup(){
		service = new TaskServiceImpl(dao:dao)
		testTask1 = new Task(id:UUID.randomUUID(),
				challenge: new Challenge(name:"testChallenge"),
				challengeReferral: new ChallengeReferral(),
				description:"test description",
				objectStatus:ObjectStatus.ACTIVE,
				person: testStudent,
				createdBy: testStudent);
		all = [testTask1]

	}

	@Test
	void markTaskComplete() {
		service.markTaskComplete(testTask1)
		assertNotNull(testTask1.getCompletedDate())
	}

	@Test
	void markTaskIncomplete(){
		service.markTaskIncomplete(testTask1)
		assertNull(testTask1.getCompletedDate())
	}

	@Test
	void markTaskCompletion_true(){
		service.markTaskCompletion(testTask1, true)
		assertNotNull(testTask1.getCompletedDate());
	}

	@Test
	void markTaskCompletion_false(){
		service.markTaskCompletion(testTask1, false)
		assertNull(testTask1.getCompletedDate())
	}

	@Test
	void setReminderSentDateToToday() {
		service.setReminderSentDateToToday(testTask1)
		assertNotNull(testTask1.getReminderSentDate())
	}

	@Test
	void save(){
		Task newTask = service.save(testTask1)
		assertEquals(newTask.getId(), testTask1.getId())
		assertEquals(newTask.getChallenge(), testTask1.getChallenge())
		assertEquals(newTask.getChallengeReferral(), testTask1.getChallengeReferral())
		assertEquals(newTask.description, testTask1.description)
	}

	@Test
	void getAllGroupedByTaskGroup(){
		all << new Task(id:UUID.randomUUID(),
				challenge: new Challenge(name:"testChallenge"),
				challengeReferral: new ChallengeReferral(),
				description:"test description",
				objectStatus:ObjectStatus.ACTIVE,
				person: testStudent,
				createdBy: testStudent);
		all << new Task(id:UUID.randomUUID(),
				challenge: new Challenge(name:"testChallenge2"),
				challengeReferral: new ChallengeReferral(),
				description:"test description",
				objectStatus:ObjectStatus.ACTIVE,
				person: testStudent,
				createdBy: testStudent);
		all << new Task(id:UUID.randomUUID(),
				description:"test description",
				objectStatus:ObjectStatus.ACTIVE,
				person: testStudent,
				createdBy: testStudent);

		Map<String, List<Task>> grouped = service.getAllGroupedByTaskGroup(testStudent, null);
		assertEquals(2,grouped["testChallenge"].size());
		assertEquals(1,grouped["testChallenge2"].size());
		assertEquals(1,grouped[Task.CUSTOM_GROUP_NAME].size());
	}
}