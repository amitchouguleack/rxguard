// Copyright (c) 2026 Amit Chougule. All rights reserved.

export interface ServiceConfig {
  /** Display name, also used as the key. */
  name: string;
  baseUrl: string;
}

/** Backend services the UI depends on. Only services with a configured URL are listed. */
export const SERVICES: readonly ServiceConfig[] = [
  { name: 'rx-gateway', baseUrl: import.meta.env.VITE_GATEWAY_URL ?? '' },
].filter((service) => service.baseUrl !== '');
