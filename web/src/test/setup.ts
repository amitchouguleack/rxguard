// Copyright (c) 2026 Amit Chougule. All rights reserved.
import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

// Vitest globals are off, so Testing Library cannot register its own auto-cleanup.
afterEach(() => {
  cleanup();
});
