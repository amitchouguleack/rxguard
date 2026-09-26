# Engineering Journal

Real problems hit while building RxGuard, and how each was debugged. Only things that actually happened are recorded here. Each bug has a GitHub Issue, closed by the PR that fixed it.

Entry format: Symptom → Impact → Investigation → Root cause → Fix → Prevention → Lesson.

---

### 2026-09-25 — `python3.12 -m venv` fails in the Codespace
**Symptom:** `The virtual environment was not created successfully because ensurepip is not available ... apt install python3.12-venv`
**Impact:** Could not install phi-shield's dependencies or run its tests locally.
**Investigation:** `which -a python3.12` showed only the system interpreter (`/usr/bin/python3.12`). The Codespace was created before `.devcontainer/` existed, so it uses the default image rather than the RxGuard dev container. A first `apt-get install` silently did nothing because the package index was stale. Running `apt-get update` first fixed that.
**Root cause:** Ubuntu splits `venv`/`ensurepip` into the separate `python3.12-venv` package, which the default image doesn't install.
**Fix:** `sudo apt-get update && sudo apt-get install -y python3.12-venv` in the current Codespace. The dev container ([5b429c4](https://github.com/amitchouguleack/rxguard/commit/5b429c4)) installs Python through the devcontainer Python feature, which includes venv. Issue [#2](https://github.com/amitchouguleack/rxguard/issues/2).
**Prevention:** CI installs Python with `actions/setup-python`, and new Codespaces use the dev container.
**Lesson:** An existing Codespace doesn't pick up a newly added `devcontainer.json` until it is rebuilt. Don't assume the environment matches the config file.

### 2026-09-25 — ESLint 10 vs `eslint-plugin-jsx-a11y` peer conflict
**Symptom:** `npm error ERESOLVE ... Conflicting peer dependency: eslint@9.39.5 ... peer eslint@"^3 || ^4 || ^5 || ^6 || ^7 || ^8 || ^9" from eslint-plugin-jsx-a11y@6.10.2`
**Impact:** Could not install the web lint toolchain.
**Investigation:** npm resolved `eslint` to the newest major (10). The newest `eslint-plugin-jsx-a11y` (6.10.2) declares peer support only up to ESLint 9. The options were `--legacy-peer-deps` (hides the conflict) or pinning ESLint to 9.
**Root cause:** The accessibility plugin has not yet declared support for ESLint 10.
**Fix:** Pinned `eslint@^9` and `@eslint/js@^9` ([016b807](https://github.com/amitchouguleack/rxguard/commit/016b807)). Issue [#3](https://github.com/amitchouguleack/rxguard/issues/3).
**Prevention:** `dependabot.yml` ignores major updates for `eslint` and `@eslint/js` until the plugin supports ESLint 10, so a Dependabot PR can't reintroduce the break.
**Lesson:** Accessibility linting is a requirement (WCAG 2.2 AA target), so the linter version follows the a11y plugin, not the other way round.

### 2026-09-25 — Second Vitest test finds duplicate elements
**Symptom:** `TestingLibraryElementError: Found multiple elements with the role "link" and name "Skip to main content"`
**Impact:** Web unit test `[REQ-UI-002]` failed. The first test passed.
**Investigation:** The DOM dump showed two identical app shells, so the first test's render was still mounted when the second ran. Testing Library's auto-cleanup relies on a global `afterEach`. The Vitest config does not enable `globals`, because tests import `describe`/`it` explicitly.
**Root cause:** No DOM cleanup between tests when Vitest globals are disabled.
**Fix:** Call `cleanup()` in `afterEach` in `web/src/test/setup.ts` ([016b807](https://github.com/amitchouguleack/rxguard/commit/016b807)). Issue [#4](https://github.com/amitchouguleack/rxguard/issues/4).
**Prevention:** The setup file runs for every test file, so every future component test gets cleanup automatically.
**Lesson:** Library "auto" behaviors often depend on test-runner globals. When a second test fails and the first passes, suspect leaked state first.

### 2026-09-26 — Containers on the compose network can't reach each other
**Symptom:** rx-gateway failed at startup: `FlywaySqlUnableToConnectToDbException: Unable to obtain connection from database ... Caused by: java.net.SocketTimeoutException: Connect timed out`
**Impact:** The gateway couldn't start in docker compose, which blocked measuring it under the 512 MB / 0.1 CPU limit.
**Investigation:**
- First hypothesis: 0.1 CPU starves the JVM so badly that the driver's connect timeout expires. That was ruled out when `pg_isready -h db` from a separate `postgres:17-alpine` container on the same network also got "no response", and so did `ping db` / `nc -z db 5432` from busybox. DNS resolved `db` to 172.18.0.2 correctly.
- From the host, `pg_isready -h 127.0.0.1` got "accepting connections". So Postgres was fine and only container-to-container traffic was blocked.
- The nftables Docker rules looked correct (the bridge's accept rule had matched 17 packets). But `iptables` warned "iptables-legacy tables present". `iptables-legacy -L FORWARD -v` showed **policy DROP, 17 packets / 1044 bytes**, the same traffic, and allow rules only for `docker0`.
**Root cause:** This Codespace image has leftover legacy-iptables Docker rules with a FORWARD policy of DROP. Docker 29 programs its rules through nftables, so new user-defined bridges (`br-*`) never get a legacy allow rule. With `net.bridge.bridge-nf-call-iptables=1`, traffic inside the bridge is filtered by both rule sets, and the legacy one drops it. Phase 0 didn't catch this because no container talked to another.
**Fix:** A local, non-persistent rule: `sudo iptables-legacy -I DOCKER-USER -i br-+ -o br-+ -j ACCEPT`. It only allows traffic that stays inside a single compose bridge. Issue [#13](https://github.com/amitchouguleack/rxguard/issues/13).
**Prevention:** The README's Codespaces section documents the symptom and the one-line fix. GitHub-hosted CI runners don't have the legacy rules, and Testcontainers talks to its database through a published host port, so CI isn't affected.
**Lesson:** When a timeout could be "slow" or "blocked", test the same path with a trivial client first. One `pg_isready` from a sibling container separated the CPU theory from the network theory in seconds. And when two firewall frameworks are present, check both.
