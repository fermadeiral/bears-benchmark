/*
 * Copyright (C) 2012-2016 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.define('vieweradmin.components.AttributeSource', {

    extend: "Ext.ux.b3p.CrudGrid",

    config: {
        gridurl: "",
        editurl: "",
        deleteurl: "",
        itemname: "attribuutbronnen",
        editattributesurl: ""
    },

    constructor: function(config) {
        this.initConfig(config);
        vieweradmin.components.AttributeSource.superclass.constructor.call(this, this.config);
        vieweradmin.components.Menu.setActiveLink('menu_attribuutbronnen');
    },

    getGridColumns: function() {
        return [
            {
                id: 'status',
                text: "Status",
                dataIndex: 'status',
                flex: 1,
                renderer: function(value) {
                    if(value === "ok") {
                        return '<span class="status_ok">GOED</span>';
                    }
                    return '<span class="status_error">FOUT</span>';
                }
            },{
                id: 'name',
                text: "Naam",
                dataIndex: 'name',
                flex: 1,
                filter: {
                    xtype: 'textfield'
                }
            },{
                id: 'url',
                text: "Bron URL",
                dataIndex: 'url',
                flex: 1,
                filter: {
                    xtype: 'textfield'
                }
            },{
                id: 'protocol',
                text: "Type",
                dataIndex: 'protocol',
                flex: 1,
                filter: {
                    xtype: 'textfield'
                }
            },{
                id: 'edit',
                header: '',
                dataIndex: 'id',
                width: 300,
                sortable: false,
                hideable: false,
                menuDisabled: true,
                renderer: (function(value) {
                     return [
                         Ext.String.format('<a href="{0}?featureSourceId={1}">Attributen bewerken</a>', this.config.editattributesurl, value),
                         Ext.String.format('<a href="#" class="editobject">Bewerken</a>'),
                         Ext.String.format('<a href="#" class="removeobject">Verwijderen</a>')
                     ].join(" | ");
                }).bind(this)
            }
        ];
    },

    getDefaultSortColumn: function() {
        return 1;
    },

    getGridModel: function() {
        return [
            {name: 'id', type: 'int' },
            {name: 'status', type: 'string'},
            {name: 'name', type: 'string'},
            {name: 'url', type: 'string'},
            {name: 'protocol', type: 'string'}
        ];
    },

    removeConfirmMessage: function(record) {
        return ["Weet u zeker dat u de attribuutbron ", record.get("name"), " wilt verwijderen?"].join("");
    },

    getEditUrl: function(record) {
        return this.createUrl(this.config.editurl, { featureSource: record.get('id') });
    },

    getRemoveUrl: function(record) {
        return this.createUrl(this.config.deleteurl, { featureSource: record.get('id') });
    }

});