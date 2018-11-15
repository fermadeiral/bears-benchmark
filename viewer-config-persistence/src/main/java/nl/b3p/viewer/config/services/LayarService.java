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
package nl.b3p.viewer.config.services;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Jytte Schaeffer
 */

@Entity
public class LayarService {
    @Id
    private Long id;
    
    @Basic(optional=false)
    @Column(unique=true)
    private String name;
    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="layarService")
    private List<LayarSource> layarSources = new ArrayList<LayarSource>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<LayarSource> getLayarSources() {
        return layarSources;
    }

    public void setLayarSources(List<LayarSource> layarSources) {
        this.layarSources = layarSources;
    }
}
