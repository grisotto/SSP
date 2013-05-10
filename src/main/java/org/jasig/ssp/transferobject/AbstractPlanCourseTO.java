/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.ssp.transferobject;

import org.jasig.ssp.model.AbstractPlan;
import org.jasig.ssp.model.AbstractPlanCourse;

public class AbstractPlanCourseTO<P extends AbstractPlan ,T extends AbstractPlanCourse<P>> extends
		AbstractAuditableTO<T> implements TransferObject<T> {

	private String termCode;

	private String courseCode;

	private String formattedCourse;

	private String courseTitle;

	private String courseDescription;

	private Integer creditHours;

	private Boolean isDev;

	private Integer orderInTerm;

	/**
	 * Empty constructor.
	 */
	public AbstractPlanCourseTO() {
		super();
	}

	@Override
	public void from(T model) {
		super.from(model);
		this.setCourseCode(model.getCourseCode());
		this.setCourseDescription(model.getCourseDescription());
		this.setCourseTitle(model.getCourseTitle());
		this.setCreditHours(model.getCreditHours());
		this.setFormattedCourse(model.getFormattedCourse());
		this.setIsDev(model.isDev());
		this.setOrderInTerm(model.getOrderInTerm());
		this.setTermCode(model.getTermCode());
	}

	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	public String getCourseCode() {
		return courseCode;
	}

	public void setCourseCode(String courseCode) {
		this.courseCode = courseCode;
	}

	public String getFormattedCourse() {
		return formattedCourse;
	}

	public void setFormattedCourse(String formattedCourse) {
		this.formattedCourse = formattedCourse;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public String getCourseDescription() {
		return courseDescription;
	}

	public void setCourseDescription(String courseDescription) {
		this.courseDescription = courseDescription;
	}

	public Integer getCreditHours() {
		return creditHours;
	}

	public void setCreditHours(Integer creditHours) {
		this.creditHours = creditHours;
	}

	public Boolean isDev() {
		return isDev;
	}

	public void setIsDev(Boolean isDev) {
		this.isDev = isDev;
	}

	public Integer getOrderInTerm() {
		return orderInTerm;
	}

	public void setOrderInTerm(Integer orderInTerm) {
		this.orderInTerm = orderInTerm;
	}

}
