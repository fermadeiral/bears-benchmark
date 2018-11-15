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
        <title>Boomstructuur met kaarten</title>
    </stripes:layout-component>

    <stripes:layout-component name="header">
        <jsp:include page="/WEB-INF/jsp/header.jsp"/>
    </stripes:layout-component>

    <stripes:layout-component name="body">

        <div id="content">
            <h1>
                Boomstructuur met kaarten: <c:out value="${actionBean.application.name}"/> <c:if test="${!empty actionBean.application.version}">(v${actionBean.application.version})</c:if>
                <a href="#Boomstructuur_Applicatie_Help" title="Help" class="helplink"></a>
            </h1>

            <c:choose>
                <c:when test="${!empty actionBean.application.details['isMashup'] && actionBean.application.details['isMashup'].value }">
                    <span class="status_error">Applicatie is een mashup. Kaartboom kan niet bewerkt worden!</span>
                </div>
            </c:when>
            <c:otherwise>

                <div id="tree-container"></div>
                <div id="form-container" class="services">
                    <iframe src="" id="editFrame" frameborder="0"></iframe>
                </div>
            </div>

            <script type="text/javascript">
                var imagesPath = "${contextPath}/resources/images/";
            
                var rootName = <js:quote value="${actionBean.rootLevel.name}"/>;
                var rootId = 'n${actionBean.rootLevel.id}';
                var actionBeans = {
                    "appTree": <js:quote><stripes:url beanclass="nl.b3p.viewer.admin.stripes.ApplicationTreeActionBean"/></js:quote>,
                    "appTreeLevel": <js:quote><stripes:url beanclass="nl.b3p.viewer.admin.stripes.ApplicationTreeLevelActionBean"/></js:quote>,
                    "appTreeLayer": <js:quote><stripes:url beanclass="nl.b3p.viewer.admin.stripes.ApplicationTreeLayerActionBean"/></js:quote>
                };
                </script>
                <script type="text/javascript" src="${contextPath}/resources/js/application/applicationTree.js"></script>
        </c:otherwise>
    </c:choose>
    <script type="text/javascript">
        vieweradmin.components.Menu.setActiveLink('menu_boomstructuur');
    </script>
</stripes:layout-component>

</stripes:layout-render>
