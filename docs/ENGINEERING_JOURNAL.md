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
