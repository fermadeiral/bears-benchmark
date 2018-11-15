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
        <title>Beheeromgeving geo-viewers</title>
    </stripes:layout-component>
    <stripes:layout-component name="header">
        <jsp:include page="/WEB-INF/jsp/header.jsp"/>
    </stripes:layout-component>    
    <stripes:layout-component name="body">
        <c:choose>
            <c:when test="${param.debug}">
                <img class="flamingoLogo" src="${contextPath}/resources/images/FL2.png"/>
            </c:when>
            <c:otherwise>
                <img class="flamingoLogo" src="${contextPath}/resources/images/FlamingoLogo.png"/>
            </c:otherwise>

        </c:choose>
    </stripes:layout-component>
</stripes:layout-render>