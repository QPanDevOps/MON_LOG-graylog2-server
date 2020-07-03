/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.security.shares;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.graylog2.utilities.GRN;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;

@AutoValue
@JsonAutoDetect
public abstract class EntityShareRequest {
    @JsonProperty("selected_grantees")
    public abstract ImmutableMap<GRN, GRN> selectedGrantees();

    public Set<GRN> grantees() {
        return selectedGrantees().keySet();
    }

    public Set<GRN> capabilities() {
        return ImmutableSet.copyOf(selectedGrantees().values());
    }

    @JsonCreator
    public static EntityShareRequest create(@JsonProperty("selected_grantees") Map<GRN, GRN> selectedGrantees) {
        return new AutoValue_EntityShareRequest(ImmutableMap.copyOf(firstNonNull(selectedGrantees, Collections.emptyMap())));
    }
}