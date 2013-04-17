/*
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
Ext.define('Ssp.controller.tool.map.SemesterPanelContainerViewController', {
    extend: 'Deft.mvc.ViewController',
    mixins: [ 'Deft.mixin.Injectable' ],
    inject:{
		appEventsController: 'appEventsController',
    	termsStore:'termsStore',
    	mapPlanService:'mapPlanService',
		formUtils: 'formRendererUtils',
		person: 'currentPerson',
        personLite: 'personLite',
    	currentMapPlan: 'currentMapPlan'
    },
    
	control: {
	    	view: {
				afterlayout: {
					fn: 'onAfterLayout',
					single: true
				}
	    	},
	},

	semesterStores: [],
	init: function() {
		var me=this;
		var id = me.personLite.get('id');
	    me.resetForm();
		if(me.termsStore.getTotalCount() == 0)
			me.termsStore.load();
		
		me.appEventsController.assignEvent({eventName: 'onCreateNewMapPlan', callBackFunc: me.onCreateNewMapPlan, scope: me});
		me.appEventsController.assignEvent({eventName: 'onSaveMapPlan', callBackFunc: me.onSaveMapPlan, scope: me});
		me.appEventsController.assignEvent({eventName: 'updateAllPlanHours', callBackFunc: me.updateAllPlanHours, scope: me});
		
		me.termsStore.addListener("load", me.onCreateNewMapPlan, me);
		return me.callParent(arguments);
    },
    resetForm: function() {
        var me = this;
        me.getView().getForm().reset();
    },
    
    newServiceSuccessHandler: function(name, callback, serviceResponses) {
        var me = this;
        return me.newServiceHandler(name, callback, serviceResponses, function(name, serviceResponses, response) {
            serviceResponses.successes[name] = response;
        });
    },

    newServiceFailureHandler: function(name, callback, serviceResponses) {
        var me = this;
        return me.newServiceHandler(name, callback, serviceResponses, function(name, serviceResponses, response) {
            serviceResponses.failures[name] = response;
        });
    },

    newServiceHandler: function(name, callback, serviceResponses, serviceResponsesCallback) {
        return function(r, scope) {
            var me = scope;
            serviceResponses.responseCnt++;
            if ( serviceResponsesCallback ) {
                serviceResponsesCallback.apply(me, [name, serviceResponses, r]);
            }
            if ( callback ) {
                callback.apply(me, [ serviceResponses ]);
            }
            me.afterServiceHandler(serviceResponses);
        };
    },

    getMapPlanServiceSuccess: function(serviceResponses) {
        var me = this;
        var mapResponse = serviceResponses.successes.map;
		if(mapResponse == null)
			me.getMapPlanServiceFailure();
       	 else{
			me.currentMapPlan.populateFromGenericObject(Ext.decode(mapResponse.responseText));
			me.onCreateMapPlan();
			me.populatePlanStores();
		}
    },

    getMapPlanServiceFailure: function() {
		var me = this;
		if(me.termsStore.getTotalCount() == 0)
			me.termsStore.load();
		else{
			me.onCreateMapPlan();
		}
    },
 
	onAfterLayout: function(){
		var me = this;
		console.log('onAfterLayout');
		me.getView().setLoading(true);
		var id = me.personLite.get('id');
	    
	    if (id != "") {
			me.getView().setLoading(true);
			var serviceResponses = {
                failures: {},
                successes: {},
                responseCnt: 0,
                expectedResponseCnt: 1
            }
	    	 me.mapPlanService.get(id, {
	             success: me.newServiceSuccessHandler('map', me.getMapPlanServiceSuccess, serviceResponses),
	             failure: me.newServiceFailureHandler('map', me.getMapPlanServiceFailure, serviceResponses),
	             scope: me
	         });
	    }
		 
	},
	
	getTermsAndStores: function(mapPlan){
		var me = this;
		var terms;
		var stores = [];
		if(mapPlan == null){
			terms = me.termsStore.getCurrentAndFutureTerms();
			terms.forEach(function(term){
				stores[term.get('code')] = new Ssp.store.SemesterCourses();
			});
		} else {
			terms = me.termsStore.getTermsFromTermCodes(me.mapPlanService.getTermCodes(mapPlan));
			terms.forEach(function(term){
				stores[term.get('code')] = new Ssp.store.SemesterCourses();
			});	
		}
		termsStores = new Object();
		termsStores.terms = terms;
		termsStores.stores = stores;
		return termsStores;
	},
	
	onCreateNewMapPlan:function(){
		var me = this;
		me.onCreateMapPlan();
	},
	populatePlanStores:function(){
		var me = this;
		var planCourses = me.currentMapPlan.get('planCourses');
		planCourses.forEach(function(planCourse){
			var termStore = me.semesterStores[planCourse.termCode];
			var semesterCourse = new Ssp.model.tool.map.SemesterCourse();
			semesterCourse.set('title',planCourse.courseTitle);
			semesterCourse.set('code', planCourse.courseCode);
			semesterCourse.set('formattedCourse', planCourse.formattedCourse);
			semesterCourse.set('description', planCourse.courseDescription);
			semesterCourse.set('minCreditHours', planCourse.creditHours);
			semesterCourse.set('termCode', planCourse.termCode);
			semesterCourse.set('isDev',  planCourse.isDev);
			termStore.add(semesterCourse);
		}) 
	},
	
	onCreateMapPlan:function(){
		var me = this;
		var view  = me.getView().getComponent("semestersets");
		if(view == null){
			return;
		}
		view.removeAll(true);
		
		var termsAndStores = me.getTermsAndStores(me.currentMapPlan);
		var terms = termsAndStores.terms;
		me.semesterStores = termsAndStores.stores;
		
		if(terms.length == 0){
			return me.callParent(arguments);
		}
		var startYear =  terms[0].get("reportYear");
		var endYear = startYear + 5;
		var currentYear = startYear;
		var termsets = [];
		termsets[startYear] = [];
		i = k = 0;
		terms.reverse().forEach(function(term){
			if(termsets.hasOwnProperty(term.get("reportYear"))){
				termsets[term.get("reportYear")][termsets[term.get("reportYear")].length] = term;
			}else if(term.get("reportYear") > endYear){
				return termsets;
			}else{
				k = 0;
				termsets[term.get("reportYear")]=[];
				termsets[term.get("reportYear")][0] = term;
			};
		});
		var panels = [];
		termsets.forEach(function(termSet){
			var yearView = view.add(new Ext.form.FieldSet({
				xtype : 'fieldset',
				border: 0,
				title : '',
				padding : '2 2 2 2',
				margin : '0 0 0 0',
				layout : 'hbox',
				autoScroll : true,
				minHeight: 262,
				itemId : 'year' + termSet[0].get("reportYear"),
				flex : 1,
			}));
		
			termSet.forEach(function(term){
				var termCode = term.get('code');
				var panelName = term.get("name");
				me.semesterStores;
				yearView.add(me.createSemesterPanel(panelName, termCode, me.semesterStores[termCode]));
			});
		});
		me.updateAllPlanHours();
		me.getView().setLoading(false);
    },
	
	createSemesterPanel: function(semesterName, termCode, semesterStore){
		var me = this;
		return new Ssp.view.tools.map.SemesterPanel({
			title:semesterName,
			itemId:termCode,
			store: semesterStore
		});
	},
	
	afterServiceHandler: function(serviceResponses) {
        var me = this;
        if ( serviceResponses.responseCnt >= serviceResponses.expectedResponseCnt ) {
            //me.getView().setLoading(false);
        }
    },

	onSaveMapPlan: function(){
		var me = this;
		mapPlanService.save(me.personLite.get('id'), me.semesterStores, me.onSaveComplete);
	},
	
	onSaveComplete: function(){
		var me = this;
		me.getView().setLoading(false);
	},
	
	updateAllPlanHours: function(){
		var me = this;
		var parent =  me.getView();
		var panels = parent.query("semesterpanel");
		var planHours = 0;
		var devHours = 0;
		panels.forEach(function(panel){
			var store = panel.getStore();
			var semesterBottomDock = panel.getDockedComponent("semesterBottomDock");
			var hours = me.updateTermHours(store, semesterBottomDock);
			planHours += hours.planHours;
			devHours += hours.devHours;
		})
		Ext.getCmp('currentTotalPlanCrHrs').setValue(planHours);
		Ext.getCmp('currentPlanTotalDevCrHrs').setValue(devHours);
		
	},
	
	updateTermHours: function(store, semesterBottomDock){
		var models = store.getRange(0);
		var totalHours = 0;
		var totalDevHours = 0;
		models.forEach(function(model){
			totalHours += model.get('minCreditHours');
			if(model.get('isDev')){
				totalDevHours += model.get('minCreditHours');
			}
		});
		var termCreditHours = semesterBottomDock.getComponent('termCrHrs');
		termCreditHours.setText("" + totalHours + "");
		var hours = new Object();
		hours.planHours = totalHours;
		hours.devHours = totalDevHours;
		return hours;
	},

	destroy: function() {
        var me=this;
		me.termsStore.removeListener("load", me.onCreateNewMapPlan, me);
		me.appEventsController.removeEvent({eventName: 'onCreateNewMapPlan', callBackFunc: me.onCreateNewMapPlan, scope: me});
        me.appEventsController.removeEvent({eventName: 'onSaveMapPlan', callBackFunc: me.onSaveMapPlan, scope: me});
		me.appEventsController.removeEvent({eventName: 'updateAllPlanHours', callBackFunc: me.updateAllPlanHours, scope: me});
        return me.callParent( arguments );
    },
});
