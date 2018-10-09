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

<stripes:url var="url" beanclass="nl.b3p.viewer.admin.stripes.IndexActionBean"/>
<html>
    <head>
        <%-- We need to redirect to a JSP page which was forwarded to from Stripes
             for the Stripesstuff SecurityManager to work
        --%>
        <meta http-equiv="refresh" content="0;url=${url}">
    </head>
    <body>
    </body>
</html>        