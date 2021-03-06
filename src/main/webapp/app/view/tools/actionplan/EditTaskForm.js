/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
Ext.define('Ssp.view.tools.actionplan.EditTaskForm', {
    extend: 'Ext.window.Window',
    alias: 'widget.edittaskform',
    mixins: ['Deft.mixin.Injectable', 'Deft.mixin.Controllable'],
    controller: 'Ssp.controller.tool.actionplan.EditTasksFormViewController',
    inject: {
        columnRendererUtils: 'columnRendererUtils',
        appEventsController: 'appEventsController',
        confidentialityLevelsStore: 'confidentialityLevelsAllUnpagedStore',
        textStore: 'sspTextStore'
    },
    height: 375,
    width: 600,
    itemId: 'editAPWindow',
    resizable: true,
    
    initComponent: function(){
        var me = this;
        
        Ext.applyIf(me, {
            items: [{
                xtype: 'form',
                height: '100%',
                width: '100%',
                layout: {
                    align: 'stretch',
                    type: 'vbox'
                },
                bodyPadding: 10,
                title: me.textStore.getValueByCode('ssp.label.action-plan.edit-task-form.title','Edit Task') ,
                dockedItems: [{
                    xtype: 'toolbar',
                    dock: 'top',
                    items: [{
                        xtype: 'button',
                        text: me.textStore.getValueByCode('ssp.label.save-button','Save'),
                        itemId: 'editActionPlanButton'
                    }, {
                        xtype: 'button',
                        text: me.textStore.getValueByCode('ssp.label.cancel-button','Cancel'),
                        listeners: {
                            click: function(){
                                me = this;
                                me.close();
                            },
                            scope: me
                        }
                    }]
                }],
                items: [/*{
                    xtype: 'fieldcontainer',
                    fieldLabel: '',
                    width: '95%',
                    layout: {
                        type: 'hbox'
                    },
                    items: [*/{
                        xtype: 'textfield',
                        width: '99%',
                        fieldLabel: me.textStore.getValueByCode('ssp.label.action-plan.edit-task-form.name','Name'),
                        name: 'name',
						itemId: 'name'
                    }/*]
                }*/, {
                    xtype: 'textareafield',
                    width: '95%',
                    fieldLabel: me.textStore.getValueByCode('ssp.label.action-plan.edit-task-form.description','Description'),
                    maxLength: 1000,
                    allowBlank: false,
                    name: 'description',
					itemId: 'description'
                }, {
                    xtype: 'textfield',
                    fieldLabel: 'Link (No HTML)',
                    inputAttrTpl: " data-qtip='" + me.textStore.getValueByCode('ssp.tooltip.action-plan.add-task-form.link', 'Example: https://www.sample.com  <br /> No HTML markup e.g. &quot;&lt; a href=...&gt;&quot; ') + "'",
                    name: 'link',
                    maxLength: 256,
                    allowBlank: true,
                    width: '95%'
                }, {
                    xtype: 'fieldcontainer',
                    fieldLabel: '',
                    width: '50%',
                    layout: {
                        type: 'hbox'
                    },
                    items: [{
                        width: '100%',
                        xtype: 'datefield',
                        fieldLabel: me.textStore.getValueByCode('ssp.label.action-plan.edit-task-form.dueDate','Target Date'),
                        altFormats: me.textStore.getValueByCode('ssp.alt-format.action-plan.edit-task-form.dueDate','m/d/Y|m-d-Y'),
                        name: 'dueDate',
                        itemId: 'actionPlanDueDate',
                        allowBlank: false,
						format: me.textStore.getValueByCode('ssp.format.action-plan.edit-task-form.format','m/d/Y'),
						minValue: Ext.Date.format(new Date(), me.textStore.getValueByCode('ssp.format.action-plan.edit-task-form.dueDate','m/d/Y')),
						minText: me.textStore.getValueByCode('ssp.min-text.action-plan.edit-task-form.dueDate','Cannot have a due date before today!'),
                        showToday: false, // else 'today' would be browser-local 'today'
                        listeners: {
                            render: function(field){
                                Ext.create('Ext.tip.ToolTip', {
                                    target: field.getEl(),
                                    html: me.textStore.getValueByCode('ssp.tooltip.action-plan.edit-task-form.dueDate','Use this to set the target completion date in the institution\'s time zone.')
                                });
                            }
                        }
                    }]
                }, {
                    xtype: 'combobox',
                    itemId: 'confidentialityLevel',
                    name: 'confidentialityLevelId',
                    fieldLabel: 'Confidentiality Level',
                    emptyText: me.textStore.getValueByCode('ssp.empty-text.action-plan.edit-task-form.confidentiality','Select One'),
                    store: me.confidentialityLevelsStore,
                    valueField: 'id',
                    displayField: 'name',
                    typeAhead: true,
                    queryMode: 'local',
                    allowBlank: false,
                    forceSelection: true,
                    width: '70%'
                
                }]
            }]
        });
        
        me.callParent(arguments);
    }
    
});
