// Copyright (c) 2026 Amit Chougule. All rights reserved.
import { useCallback, useEffect, useState } from 'react';
import type { ServiceConfig } from './services.ts';

export type ServiceState = 'waking' | 'up' | 'unreachable';

export interface PollOptions {
  /** Wait between failed health checks. */
  intervalMs: number;
  /** Give up and show "unreachable" after this long. */
  deadlineMs: number;
  /** Abort a single request after this long (a sleeping service can hold a request open). */
  requestTimeoutMs: number;
}

/** Free Render services take about a minute to wake, so allow three before giving up. */
export const DEFAULT_POLL_OPTIONS: PollOptions = {
  intervalMs: 5_000,
  deadlineMs: 180_000,
  requestTimeoutMs: 30_000,
};

async function isHealthy(baseUrl: string, timeoutMs: number): Promise<boolean> {
  try {
    const response = await fetch(`${baseUrl.replace(/\/$/, '')}/health`, {
      signal: AbortSignal.timeout(timeoutMs),
    });
    if (!response.ok) return false;
    const body = (await response.json()) as { status?: string };
    return body.status === 'UP';
  } catch {
    return false;
  }
}

function allWaking(services: readonly ServiceConfig[]): Record<string, ServiceState> {
  return Object.fromEntries(services.map((service) => [service.name, 'waking' as const]));
}

/** Polls each service's /health until it is up or the deadline passes. */
export function useServiceStatus(
  services: readonly ServiceConfig[],
  options: PollOptions = DEFAULT_POLL_OPTIONS,
) {
  const { intervalMs, deadlineMs, requestTimeoutMs } = options;
  const [states, setStates] = useState(() => allWaking(services));
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let cancelled = false;
    const timers: ReturnType<typeof setTimeout>[] = [];
    const startedAt = Date.now();
    const update = (name: string, state: ServiceState) =>
      setStates((previous) => ({ ...previous, [name]: state }));

    for (const service of services) {
      const poll = async () => {
        const healthy = await isHealthy(service.baseUrl, requestTimeoutMs);
        if (cancelled) return;
        if (healthy) {
          update(service.name, 'up');
        } else if (Date.now() - startedAt >= deadlineMs) {
          update(service.name, 'unreachable');
        } else {
          timers.push(setTimeout(poll, intervalMs));
        }
      };
      void poll();
    }

    return () => {
      cancelled = true;
      timers.forEach(clearTimeout);
    };
  }, [services, intervalMs, deadlineMs, requestTimeoutMs, attempt]);

  const retry = useCallback(() => {
    setStates(allWaking(services));
    setAttempt((value) => value + 1);
  }, [services]);

  return { states, retry };
}
