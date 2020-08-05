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
package org.graylog2.migrations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.bson.types.ObjectId;
import org.graylog.grn.GRNRegistry;
import org.graylog.security.DBGrantService;
import org.graylog.security.permissions.GRNPermission;
import org.graylog.testing.mongodb.MongoDBFixtures;
import org.graylog.testing.mongodb.MongoDBInstance;
import org.graylog2.Configuration;
import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.MongoConnection;
import org.graylog2.database.PersistedServiceImpl;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.database.users.User;
import org.graylog2.shared.bindings.providers.ObjectMapperProvider;
import org.graylog2.shared.security.Permissions;
import org.graylog2.shared.security.RestPermissions;
import org.graylog2.shared.users.Role;
import org.graylog2.shared.users.UserService;
import org.graylog2.users.RoleService;
import org.graylog2.users.RoleServiceImpl;
import org.graylog2.users.UserImpl;
import org.graylog2.users.UserServiceImplTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.annotation.Nullable;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class V20200803120800_MigrateRolesToGrantsTest {
    private V20200803120800_MigrateRolesToGrants migration;
    private RoleService roleService;
    private ObjectMapper objectMapper;

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public final MongoDBInstance mongodb = MongoDBInstance.createForClass();

    @Mock
    private Permissions permissions;

    @Mock
    private Validator validator;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapperProvider().get();
        final MongoJackObjectMapperProvider mongoJackObjectMapperProvider = new MongoJackObjectMapperProvider(objectMapper);
        when(permissions.readerBasePermissions()).thenReturn(ImmutableSet.of());
        when(validator.validate(any())).thenReturn(ImmutableSet.of());

        roleService = new RoleServiceImpl(mongodb.mongoConnection(), mongoJackObjectMapperProvider, permissions, validator);

        UserService userService = new TestUserService(mongodb.mongoConnection());
        final GRNRegistry grnRegistry = GRNRegistry.createWithBuiltinTypes();
        DBGrantService dbGrantService = new DBGrantService(mongodb.mongoConnection(), mongoJackObjectMapperProvider, grnRegistry);
        migration = new V20200803120800_MigrateRolesToGrants(roleService, userService, dbGrantService, grnRegistry, "admin");
    }

    @Test
    @MongoDBFixtures("V20200803120800_MigrateRolesToGrantsTest.json")
    public void upgrade() {
        migration.upgrade();

        // TODO add assertions
    }

    public static class TestUserService extends PersistedServiceImpl implements UserService {

        final UserImpl.Factory userFactory;
        protected TestUserService(MongoConnection mongoConnection) {
            super(mongoConnection);
            final Permissions permissions = new Permissions(ImmutableSet.of(new RestPermissions()));
            userFactory = new UserServiceImplTest.UserImplFactory(new Configuration(), permissions);
        }

        @Nullable
        @Override
        public User load(String username) {
            return null;
        }

        @Override
        public int delete(String username) {
            return 0;
        }

        @Override
        public User create() {
            return null;
        }

        @Override
        public List<User> loadAll() {
            return null;
        }

        @Override
        public User getAdminUser() {
            return null;
        }

        @Override
        public Optional<User> getRootUser() {
            return Optional.empty();
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public Collection<User> loadAllForRole(Role role) {
            final String roleId = role.getId();
            final DBObject query = BasicDBObjectBuilder.start(UserImpl.ROLES, new ObjectId(roleId)).get();

            final List<DBObject> result = query(UserImpl.class, query);
            if (result == null || result.isEmpty()) {
                return Collections.emptySet();
            }
            final Set<User> users = Sets.newHashSetWithExpectedSize(result.size());
            for (DBObject dbObject : result) {
                //noinspection unchecked
                users.add(userFactory.create((ObjectId) dbObject.get("_id"), dbObject.toMap()));
            }
            return users;
        }

        @Override
        public Set<String> getRoleNames(User user) {
            return null;
        }

        @Override
        public List<Permission> getPermissionsForUser(User user) {
            return null;
        }

        @Override
        public List<WildcardPermission> getWildcardPermissionsForUser(User user) {
            return null;
        }

        @Override
        public List<GRNPermission> getGRNPermissionsForUser(User user) {
            return null;
        }

        @Override
        public Set<String> getUserPermissionsFromRoles(User user) {
            return null;
        }

        @Override
        public void dissociateAllUsersFromRole(Role role) {
            final Collection<User> usersInRole = loadAllForRole(role);
            // remove role from any user still assigned
            for (User user : usersInRole) {
                if (user.isLocalAdmin()) {
                    continue;
                }
                final HashSet<String> roles = Sets.newHashSet(user.getRoleIds());
                roles.remove(role.getId());
                user.setRoleIds(roles);
                try {
                    save(user);
                } catch (ValidationException e) {
                    throw new RuntimeException("Unable to remove role " + role.getName() + " from user " + user, e);
                }
            }
        }
    }
}
