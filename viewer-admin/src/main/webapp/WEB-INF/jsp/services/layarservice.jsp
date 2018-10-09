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
        <title>Layar services</title>
    </stripes:layout-component>
    <stripes:layout-component name="header">
        <jsp:include page="/WEB-INF/jsp/header.jsp"/>
    </stripes:layout-component>
    <stripes:layout-component name="body">
        <div id="content">
            <stripes:errors/>
            <stripes:messages/>
            <h1>Layar services<a href="#Layarservices_Beheren_Help" title="Help" class="helplink"></a></h1>  
            <div style="margin-top: 35px; margin-bottom: -20px;">
                Gebruik de volgende URL in Layar: <code><c:out value="${actionBean.url}"/></code>
            </div>               
            <div id="grid-container" class="documenten">

            </div>
            <div id="form-container" class="documenten">
                <iframe src="<stripes:url beanclass="nl.b3p.viewer.admin.stripes.LayarServiceActionBean" event="cancel"/>" id="editFrame" frameborder="0"></iframe>
            </div>
        
            <script type="text/javascript">
                var gridurl = '<stripes:url beanclass="nl.b3p.viewer.admin.stripes.LayarServiceActionBean" event="getGridData"/>';
                var editurl = '<stripes:url beanclass="nl.b3p.viewer.admin.stripes.LayarServiceActionBean" event="edit"/>';
                var deleteurl = '<stripes:url beanclass="nl.b3p.viewer.admin.stripes.LayarServiceActionBean" event="delete"/>';
                vieweradmin.components.Menu.setActiveLink('menu_layarservices');
            </script>
            <script type="text/javascript" src="${contextPath}/resources/js/services/layarservice.js"></script>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
