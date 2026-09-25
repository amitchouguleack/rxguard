// Copyright (c) 2026 Amit Chougule. All rights reserved.

export const SYNTHETIC_NOTICE = 'SYNTHETIC DATA — NOT FOR CLINICAL USE';

/** Shown on every screen. Uses text, not color alone, so the warning is always perceivable. */
export function SyntheticDataBanner() {
  return (
    <div className="synthetic-banner" role="note" aria-label="Data notice">
      <strong>{SYNTHETIC_NOTICE}</strong>
    </div>
  );
}
