// Copyright (c) 2026 Amit Chougule. All rights reserved.
import { SyntheticDataBanner } from './components/SyntheticDataBanner.tsx';
import { SERVICES } from './status/services.ts';
import { ServiceStatusPanel } from './status/ServiceStatusPanel.tsx';

const BUILD = import.meta.env.VITE_GIT_SHA?.slice(0, 7) ?? 'local';

export default function App() {
  return (
    <>
      <a className="skip-link" href="#main">
        Skip to main content
      </a>
      <SyntheticDataBanner />
      <header className="app-header">
        <h1>RxGuard</h1>
        <p>End-to-end e-prescribing simulation: HL7 v2, FHIR R4, SMART on FHIR, NCPDP, EPCS.</p>
      </header>
      <main id="main">
        <ServiceStatusPanel services={SERVICES} />
        <p>The clinical screens arrive in later phases.</p>
      </main>
      <footer className="app-footer">
        <p>
          Build <code>{BUILD}</code> · © Amit Chougule. All rights reserved.
        </p>
      </footer>
    </>
  );
}
