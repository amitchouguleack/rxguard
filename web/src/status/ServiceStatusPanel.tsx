// Copyright (c) 2026 Amit Chougule. All rights reserved.
import type { ServiceConfig } from './services.ts';
import {
  DEFAULT_POLL_OPTIONS,
  useServiceStatus,
  type PollOptions,
  type ServiceState,
} from './useServiceStatus.ts';

const LABELS: Record<ServiceState, string> = {
  waking: 'Waking up…',
  up: 'Up',
  unreachable: 'Unreachable',
};

interface Props {
  services: readonly ServiceConfig[];
  pollOptions?: PollOptions;
}

/** Shows why the first load is slow: free-tier services sleep when idle (REQ-UI-006). */
export function ServiceStatusPanel({ services, pollOptions = DEFAULT_POLL_OPTIONS }: Props) {
  const { states, retry } = useServiceStatus(services, pollOptions);

  if (services.length === 0) {
    return (
      <section className="status-panel" aria-labelledby="status-heading">
        <h2 id="status-heading">Service status</h2>
        <p>No backend services are configured for this build.</p>
      </section>
    );
  }

  const values = Object.values(states);
  const waking = values.includes('waking');
  const anyUnreachable = values.includes('unreachable');

  return (
    <section className="status-panel" aria-labelledby="status-heading">
      <h2 id="status-heading">{waking ? 'Waking up services…' : 'Service status'}</h2>
      <p>
        This demo runs on free hosting. Services sleep after 15 minutes without traffic, so the
        first visit can take a minute or more while they start.
      </p>
      <ul className="status-list" role="status" aria-live="polite">
        {services.map((service) => {
          const state = states[service.name] ?? 'waking';
          return (
            <li key={service.name} className={`status-item status-${state}`}>
              {state === 'waking' && <span className="spinner" aria-hidden="true" />}
              <span className="status-name">{service.name}</span>
              <span className="status-value">{LABELS[state]}</span>
            </li>
          );
        })}
      </ul>
      {anyUnreachable && !waking && (
        <button type="button" onClick={retry}>
          Retry
        </button>
      )}
    </section>
  );
}
