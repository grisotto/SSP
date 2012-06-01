Ext.define('Ssp.controller.ToolsViewController', {
	extend: 'Deft.mvc.ViewController',
    mixins: [ 'Deft.mixin.Injectable'],
    inject: {
    	apiProperties: 'apiProperties',
        person: 'currentPerson',
        formUtils: 'formRendererUtils',
        appEventsController: 'appEventsController'
    },
    config: {
    	personViewHistoryUrl: '',
    },
    control: {
		view: {
			itemclick: 'onItemClick',
			viewready: 'onViewReady'
		}
		
	},
	
	init: function() {
		return this.callParent(arguments);
    }, 
    
    onViewReady: function(comp, obj){
    	var personId = this.person.get('id');
    	this.personViewHistoryUrl = this.apiProperties.getItemUrl('personViewHistory');
		this.personViewHistoryUrl = this.personViewHistoryUrl.replace('{id}',personId);
 
    	this.appEventsController.assignEvent({eventName: 'viewHistory', callBackFunc: this.onViewHistory, scope: this});
		
    	this.appEventsController.assignEvent({eventName: 'loadPerson', callBackFunc: this.onLoadPerson, scope: this});
		/*
    	this.appEventsController.getApplication().addListener('loadPerson', function(){
			// this.person.get('tools') );
			this.getView().getSelectionModel().select(0);
			this.loadTool('profile');
		},this);
		*/
    },

    destroy: function() {
    	this.appEventsController.removeEvent({eventName: 'viewHistory', callBackFunc: this.onViewHistory, scope: this});
    	this.appEventsController.removeEvent({eventName: 'loadPerson', callBackFunc: this.onLoadPerson, scope: this});

        return this.callParent( arguments );
    },
    
    onLoadPerson: function(){
		// this.person.get('tools') );
		this.getView().getSelectionModel().select(0);
		this.loadTool('profile');    	
    },
    
	onItemClick: function(grid,record,item,index){ 
		this.loadTool( record.get('toolType') );		
	},
	
	loadTool: function( toolType ) {	
		var comp = this.formUtils.loadDisplay('tools',toolType, true, {});
	},

    onViewHistory: function(button) {
		Ext.Msg.alert('Attention','ToolsViewController->viewHistory. This item is completed in the ui. Uncomment to display the History Report when it is complete.');
    	/*
    	var url = this.apiProperties.createUrl( this.personViewHistoryUrl );
        this.apiProperties.makeRequest({
    		url: url,
    		method: 'GET',
    		jsonData: jsonData,
    		successFunc: function(){
    			// handle response here
    		}
    	});
    	*/
    }
});