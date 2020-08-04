import React from 'react';
import styled from 'styled-components';

import Select from 'components/common/Select';

export const MS_DAY = 24 * 60 * 60 * 1000;
export const MS_HOUR = 60 * 60 * 1000;
export const MS_MINUTE = 60 * 1000;
export const MS_SECOND = 1000;

const TimeoutSelect = styled(Select)`
  width: 150px;
`;

class TimeoutUnitSelect extends React.Component {
  options = [
    { value: `${MS_SECOND}`, label: 'Seconds' },
    { value: `${MS_MINUTE}`, label: 'Minutes' },
    { value: `${MS_HOUR}`, label: 'Hours' },
    { value: `${MS_DAY}`, label: 'Days' },
  ];

  getValue = () => {
    return this.session_timeout_unit.value;
  };

  render() {
    return (
      <TimeoutSelect type="select"
                     ref={(sessionTimeoutUnit) => { this.session_timeout_unit = sessionTimeoutUnit; }}
                     options={this.options}
                     {...this.props} />
    );
  }
}

export default TimeoutUnitSelect;
