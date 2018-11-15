<%--
Copyright (C) 2011-2013 B3Partners B.V.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/jsp/taglibs.jsp"%>

<stripes:layout-render name="/WEB-INF/jsp/templates/ext.jsp">
    <stripes:layout-component name="head">
        <title>Geo service</title>
    </stripes:layout-component>
    <stripes:layout-component name="body">

<div id="formcontent" style="height: 620px">
<stripes:errors/>
<stripes:messages/>
<stripes:form beanclass="nl.b3p.viewer.admin.stripes.GeoServiceActionBean">

    <stripes:hidden name="service"/>
    <stripes:hidden name="sld"/>

    <c:set var="edit" value="${!empty actionBean.sld.id}"/>

    <span id="headertext" style="display: none">
    <c:if test="${!edit}">Nieuwe SLD toevoegen aan <c:out value="${actionBean.service.name}"/></c:if>
    <c:if test="${edit}">SLD <c:out value="${actionBean.sld.title}"/> bewerken van <c:out value="${actionBean.service.name}"/></c:if>
    </span>

    <p>
    <script type="text/javascript">
        
        function checkType() {
            var type = Ext.query("input:checked[name='sldType']")[0].value;
            Ext.fly('external').setVisibilityMode(Ext.Element.DISPLAY).setVisible(type == "external");
            Ext.fly('body').setVisibilityMode(Ext.Element.DISPLAY).setVisible(type == "body");
        }
        Ext.onReady(function() {
            checkType();
            appendPanel('headertext', 'formcontent');
        });
    </script>
    
    Naam *: <stripes:text name="sld.title" maxlength="255" size="30"/><br>
    <br>
    <label><stripes:checkbox name="sld.defaultStyle"/> Standaard SLD voor lagen van deze service die in een applicatie worden gebruikt</label><br>
    <fieldset>
        <legend>Soort</legend>
        <label><stripes:radio name="sldType" value="external" onchange="checkType()"/>Externe SLD</label><br>
        <label><stripes:radio name="sldType" value="body" onchange="checkType()"/>SLD body invoeren</label>
    </fieldset>
    <div id="external">
       URL *: <stripes:text name="sld.externalUrl" maxlength="1000" size="80"/><br>
        Let op: in de externe SLD moeten de lagen uit deze service met <code>&lt;NamedLayer&gt;</code> elementen worden genoemd<br> om effect te hebben.
    </div>
    <div style="margin: 5px 0px 5px 0px;">
        Bij het opslaan van de SLD wordt uit de SLD bepaald per layer welke naam de eerste UserStyle
        heeft. Deze wordt voor ArcGIS Server gebruikt voor WMS requests. Indien een externe SLD is
        gewijzigd opnieuw op "Opslaan" drukken om deze gegevens te updaten.
        <p>
        Voor ArcGIS wilt u mogelijk extra URL parameters voor GetLegendGraphic (bijvoorbeeld WIDTH=200): <stripes:text name="sld.extraLegendParameters" maxlength="255" size="30"/><br/>
    </div>
    <div class="submitbuttons">
        <c:choose>
            <c:when test="${!edit}">
                <stripes:submit name="saveSld" value="Opslaan"/>
            </c:when>
            <c:otherwise>
                <stripes:submit name="saveSld" value="Opslaan"/>
                <stripes:submit name="deleteSld" onclick="return deleteConfirm();" value="Verwijder SLD"/>
                <script type="text/javascript">
                    function deleteConfirm() {
                        return confirm('Weet u zeker dat u deze SLD wilt verwijderen?');
                    }
                </script>
            </c:otherwise>
        </c:choose>
        <stripes:url var="url" beanclass="nl.b3p.viewer.admin.stripes.GeoServiceActionBean">
            <stripes:param name="service" value="${actionBean.service.id}"/>
        </stripes:url>
        <stripes:button name="cancel" class="extlikebutton" value="Annuleren" onclick="window.location.href='${url}'" />
    </div>
    <div id="body">
        SLD document:<br>
        <stripes:submit name="generateSld" value="Maak SLD opzet"/>
        <stripes:submit name="validateSldXml" value="Valideer XML" onclick="return confirm('Let op! Het valideren kan lang duren en heeft internettoegang nodig om de XML schema\\'s op te halen. Wilt u doorgaan?');"/>
        <stripes:submit name="cqlToFilter" value="CQL naar ogc:Filter XML" onclick="doCqlToFilter(); return false;"/>&nbsp;&nbsp; <a href="http://udig.github.com/docs/user/Constraint%20Query%20Language.html" target="_blank">CQL documentatie</a>
        <br>
        <script type="text/javascript">
            var cql = "";
            function doCqlToFilter() {
                var res = prompt('Geef CQL expressie:', cql);
                if(!res) { 
                    return;
                }
                cql = res;
                <stripes:url var="url" beanclass="nl.b3p.viewer.admin.stripes.GeoServiceActionBean" event="cqlToFilter"/>
                Ext.Ajax.request({
                    url: <js:quote value="${url}"/>,
                    params: {cql: cql}, 
                    success: function(result) {
                        var response = Ext.JSON.decode(result.responseText);

                        if(response.success) {
                            Ext.getDom("filterXml").innerHTML = Ext.String.htmlEncode(response.filter);
                            Ext.getDom("cqlText").innerHTML = Ext.String.htmlEncode(cql);
                            Ext.fly('filter').setVisibilityMode(Ext.Element.DISPLAY).setVisible(true);
                        } else {
                            alert(response.error);
                        }
                    },
                    failure: function(result) {
                        alert("Ajax request failed with status " + result.status + " " + result.statusText + ": " + result.responseText);
                    }
                });                
            }
        </script>
        <div id="filter" style="display: none">
            <br>
            <a href="#" onclick="Ext.fly('filter').setVisibilityMode(Ext.Element.DISPLAY).setVisible(false);">Verberg filter</a>
            <br>
            <b>Filter voor expressie: </b><span style="font-family: monospace" id="cqlText"></span>
            <div id="filterXml" style="font-family: monospace; white-space: pre">
                
            </div>
        </div>
        <br>
        <c:choose>
            <c:when test="${actionBean.context.eventName == 'generateSld'}">
                <textarea name="sld.sldBody" rows="20" cols="100" wrap="off" style="font-family: monospace"><c:out value="${actionBean.generatedSld}"/></textarea>
            </c:when>
            <c:otherwise>
                <stripes-dynattr:textarea name="sld.sldBody" rows="20" cols="100" wrap="off" style="font-family: monospace"/>
            </c:otherwise>
        </c:choose>
    </div>
    
</stripes:form>
</div>

    </stripes:layout-component>
</stripes:layout-render>    