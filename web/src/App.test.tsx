// Copyright (c) 2026 Amit Chougule. All rights reserved.
import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import App from './App.tsx';

describe('App shell', () => {
  it('[REQ-UI-001] shows the synthetic-data banner', () => {
    render(<App />);
    expect(screen.getByRole('note', { name: 'Data notice' })).toHaveTextContent(
      'SYNTHETIC DATA — NOT FOR CLINICAL USE',
    );
  });

  it('[REQ-UI-002] has a skip link to the main landmark', () => {
    render(<App />);
    expect(screen.getByRole('link', { name: 'Skip to main content' })).toHaveAttribute(
      'href',
      '#main',
    );
    expect(screen.getByRole('main')).toHaveAttribute('id', 'main');
  });
});
