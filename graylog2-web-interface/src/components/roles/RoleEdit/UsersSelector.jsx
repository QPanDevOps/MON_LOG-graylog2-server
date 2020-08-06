// @flow strict
import * as React from 'react';
import { useEffect, useState } from 'react';
import { Formik, Form, Field } from 'formik';
import styled, { css } from 'styled-components';
import type { StyledComponent } from 'styled-components';

import Role from 'logic/roles/Role';
import { type PaginatedListType } from 'components/common/PaginatedItemOverview';
import UserOverview from 'logic/users/UserOverview';
import { UsersActions } from 'stores/users/UsersStore';
import { type ThemeInterface } from 'theme';
import { Button } from 'components/graylog';
import { Select } from 'components/common';

type Props = {
  onSubmit: (user: UserOverview) => Promise<?PaginatedListType>,
  role: Role,
};

const SubmitButton = styled(Button)`
  margin-left: 15px;
`;

const FormElements = styled.div`
  display: flex;
`;

const Errors: StyledComponent<{}, ThemeInterface, HTMLDivElement> = styled.div(({ theme }) => css`
  width: 100%;
  margin-top: 3px;
  color: ${theme.colors.variant.danger};

  > * {
    margin-right: 5px;

    :last-child {
      margin-right: 0;
    }
  }
`);

const SelectOption = styled.div`
  display: flex;
  align-items: center;
`;

const StyledSelect = styled(Select)`
  flex: 1;
`;

const _renderOption = ({ label }: { label: string }) => (
  <SelectOption>{label}</SelectOption>
);

const _isRequired = (field) => (value) => (!value ? `The ${field} is required` : undefined);

const UsersSelector = ({ role, onSubmit }: Props) => {
  const [users, setUsers] = useState([]);
  const [options, setOptions] = useState([]);

  const onUpdate = ({ user }: { user: string }, { resetForm }) => {
    const userOverview = users.find((u) => u.username === user);

    if (!userOverview) {
      throw new Error(`Unable to find user with name ${user} in ${users.map((u) => u.username).join(', ')}`);
    }

    onSubmit(userOverview).then(() => {
      resetForm();
    });
  };

  useEffect(() => {
    const getUnlimited = [1, 0, ''];

    UsersActions.searchPaginated(...getUnlimited)
      .then(({ list }) => {
        if (list) {
          const resultUsers = list
            .filter((u) => !role.users.includes(u.name))
            .map((u) => ({ label: u.name, value: u.name }));

          setOptions(resultUsers);
          setUsers(list);
        }
      });
  }, [role]);

  return (
    <div>
      <Formik onSubmit={onUpdate}
              initialValues={{ user: undefined }}>
        {({ isSubmitting, isValid, errors }) => (
          <Form>
            <FormElements>
              <Field name="user" validate={_isRequired('user')}>
                {({ field: { name, value, onChange } }) => (
                  <StyledSelect inputProps={{ 'arial-label': 'Search for users ' }}
                                onChange={(user) => {
                                  onChange({ target: { value: user, name } });
                                }}
                                optionRenderer={_renderOption}
                                options={options}
                                placeholder="Search for users"
                                value={value} />
                )}
              </Field>
              <SubmitButton bsStyle="success"
                            disabled={isSubmitting || !isValid}
                            title="Assign User"
                            type="submit">
                Assign User
              </SubmitButton>
            </FormElements>
            {errors && (
              <Errors>
                {Object.entries(errors).map(([fieldKey, value]: [string, mixed]) => (
                  <span key={fieldKey}>{String(value)}.</span>
                ))}
              </Errors>
            )}
          </Form>
        )}
      </Formik>
    </div>
  );
};

export default UsersSelector;
