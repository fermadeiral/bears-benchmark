(function ($, molgenis) {
    "use strict";

    var api = new molgenis.RestClient();

    $(function () {
        var onValueChange = function (event) {
            // check if user has read or write permission on entity
            api.getAsync('/api/v1/' + event.value.id + '/meta', {'expand': ['attributes']}).done(function (entity) {
                React.render(molgenis.ui.Form({
                    entity: entity,
                    entityInstance: event.value.id.substring(('sys' + molgenis.packageSeparator + 'set' + molgenis.packageSeparator).length),
                    mode: entity.writable ? 'edit' : 'view',
                    modal: false,
                    enableOptionalFilter: false,
                    enableFormIndex: false
                }), $('#settings-container')[0]);
            });
        };

        var EntitySelectBox = React.render(molgenis.ui.EntitySelectBox({
            entity: 'sys' + molgenis.packageSeparator + 'md' + molgenis.packageSeparator + 'EntityType',
            query: {
                operator: 'NESTED',
                nestedRules: [
                    {
                        field: 'extends',
                        operator: 'EQUALS',
                        value: 'sys' + molgenis.packageSeparator + 'set' + molgenis.packageSeparator + 'settings'
                    },
                    {operator: 'AND'},
                    {operator: 'NOT'},
                    {field: 'isAbstract', operator: 'EQUALS', value: 'true'}
                ]
            },
            mode: 'view',
            multiple: false,
            placeholder: 'Select application or plugin settings',
            focus: true,
            required: true, // do not show clear icon in select
            onValueChange: onValueChange
        }), $('#settings-select-container')[0]);

        // initialize with application settings
        onValueChange({
            value: {
                id: 'sys' + molgenis.packageSeparator + 'set' + molgenis.packageSeparator + 'app',
            }
        });
    });
}($, window.top.molgenis = window.top.molgenis || {}));