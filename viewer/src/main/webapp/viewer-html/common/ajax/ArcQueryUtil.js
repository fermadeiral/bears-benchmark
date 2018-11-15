/* 
 * Copyright (C) 2012-2013 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.define("viewer.ArcQueryUtil", {
    config: {
        actionbeanUrl: null
    },
    constructor: function(config) {        
        this.initConfig(config);
        if(this.config.actionbeanUrl == null) {
            this.config.actionbeanUrl = actionBeans["arcqueryutil"];
        }        
    },
    cqlToArcXMLSpatialQuery: function(cql, successFunction, failureFunction) {
        
        Ext.Ajax.request({
            url: this.config.actionbeanUrl,
            params: { cql: cql },
            success: function(result) {
                var response = Ext.JSON.decode(result.responseText);
                
                if(response.success) {
                    successFunction(response.SPATIALQUERY);
                } else {
                    if(failureFunction != undefined) {
                        failureFunction(response.error);
                    }
                }
            },
            failure: function(result) {
                if(failureFunction != undefined) {
                    failureFunction("Ajax request failed with status " + result.status + " " + result.statusText + ": " + result.responseText);
                }
            }
        });
    },
    
    cqlToArcXMLWhere: function(cql, successFunction, failureFunction) {
        Ext.Ajax.request({
            url: this.config.actionbeanUrl,
            params: { cql: cql, whereOnly: true },
            success: function(result) {
                var response = Ext.JSON.decode(result.responseText);
                
                if(response.success) {
                    successFunction(response.where);
                } else {
                    if(failureFunction != undefined) {
                        failureFunction(response.error);
                    }
                }
            },
            failure: function(result) {
                if(failureFunction != undefined) {
                    failureFunction("Ajax request failed with status " + result.status + " " + result.statusText + ": " + result.responseText);
                }
            }
        });
    },
    
    cqlToArcFIDS : function (cql, appLayer, successFunction, failureFunction){
        Ext.Ajax.request({
            url: this.config.actionbeanUrl,
            params: { cql: cql, getObjectIds : true,appLayer: appLayer, application: appId },
            success: function(result) {
                var response = Ext.JSON.decode(result.responseText);
                
                if(response.success) {
                    successFunction(response.objectIds,response.objectIdFieldName);
                } else {
                    if(failureFunction != undefined) {
                        failureFunction(response.message);
                    }
                }
            },
            failure: function(result) {
                if(failureFunction != undefined) {
                    failureFunction("Ajax request failed with status " + result.status + " " + result.statusText + ": " + result.responseText);
                }
            }
        });
    }
});
