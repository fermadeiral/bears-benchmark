/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2015, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gml2.simple;

import org.geotools.gml2.GML;
import org.geotools.xsd.Encoder;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Encodes a GML2 multipoint
 *
 * @author Justin Deoliveira, OpenGeo
 * @author Andrea Aime - GeoSolutions
 */
class MultiPointEncoder extends GeometryEncoder<MultiPoint> {

    static final QualifiedName MULTI_POINT = new QualifiedName(GML.NAMESPACE, "MultiPoint", "gml");

    static final QualifiedName POINT_MEMBER =
            new QualifiedName(GML.NAMESPACE, "pointMember", "gml");

    PointEncoder pe;

    QualifiedName multiPoint;

    QualifiedName pointMember;

    protected MultiPointEncoder(Encoder encoder, String gmlPrefix) {
        super(encoder);
        pe = new PointEncoder(encoder, gmlPrefix);
        multiPoint = MULTI_POINT.derive(gmlPrefix);
        pointMember = POINT_MEMBER.derive(gmlPrefix);
    }

    @Override
    public void encode(MultiPoint geometry, AttributesImpl atts, GMLWriter handler)
            throws Exception {
        handler.startElement(multiPoint, atts);

        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            handler.startElement(pointMember, null);
            pe.encode((Point) geometry.getGeometryN(i), null, handler);
            handler.endElement(pointMember);
        }

        handler.endElement(multiPoint);
    }
}
