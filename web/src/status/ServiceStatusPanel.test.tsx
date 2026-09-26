// Copyright (c) 2026 Amit Chougule. All rights reserved.
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ServiceStatusPanel } from './ServiceStatusPanel.tsx';

const GATEWAY = [{ name: 'rx-gateway', baseUrl: 'https://gateway.example' }];
const FAST = { intervalMs: 10, deadlineMs: 200, requestTimeoutMs: 50 };

function healthResponse(status: string) {
  return new Response(JSON.stringify({ status }), { status: 200 });
}

describe('ServiceStatusPanel', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('[REQ-UI-006] shows waking up, then up once /health answers', async () => {
    const fetchMock = vi
      .fn()
      .mockRejectedValueOnce(new TypeError('network error'))
      .mockResolvedValue(healthResponse('UP'));
    vi.stubGlobal('fetch', fetchMock);

    render(<ServiceStatusPanel services={GATEWAY} pollOptions={FAST} />);

    expect(screen.getByRole('heading', { name: 'Waking up services…' })).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('rx-gateway');
    expect(screen.getByRole('status')).toHaveTextContent('Waking up…');

    expect(await screen.findByText('Up')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Service status' })).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith('https://gateway.example/health', expect.anything());
  });

  it('[REQ-UI-006] reports unreachable after the deadline and can retry', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response('', { status: 503 }));
    vi.stubGlobal('fetch', fetchMock);

    render(<ServiceStatusPanel services={GATEWAY} pollOptions={FAST} />);

    expect(await screen.findByText('Unreachable')).toBeInTheDocument();

    fetchMock.mockResolvedValue(healthResponse('UP'));
    await userEvent.click(screen.getByRole('button', { name: 'Retry' }));

    expect(await screen.findByText('Up')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Retry' })).not.toBeInTheDocument();
  });

  it('[REQ-UI-006] treats a non-UP body as not ready', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(healthResponse('DOWN')));

    render(<ServiceStatusPanel services={GATEWAY} pollOptions={FAST} />);

    expect(await screen.findByText('Unreachable')).toBeInTheDocument();
  });

  it('says so when no services are configured', () => {
    render(<ServiceStatusPanel services={[]} />);

    expect(
      screen.getByText('No backend services are configured for this build.'),
    ).toBeInTheDocument();
  });
});
