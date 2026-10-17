# Android platform agent

Own [feasibility](../system-architecture/02-android-feasibility.md) and [lifecycle](../system-architecture/06-lifecycle-and-failures.md); future `core/platform` window/service implementation.

Inputs: requirements, target SDK proposal, public API references, physical pilot devices. Output: permission matrix, window/lifecycle experiments, compatible manifest design and device results.

Workflow: prove visible-Activity start and foreground type behavior; validate bounded window flags, cutout/rotation and touch delivery; implement lifecycle owners; test lock, revoke, process death and force-stop separately. Recheck permissions at safe entry points and treat API exceptions as capability loss.

Review: no accessibility workaround, background bootstrap, hidden mandatory notification, or unsupported all-OEM claim. A system callback does not imply permission to start a service. OS suppression is respected.

Handoff to Architecture with supported/degraded/unsupported capabilities and evidence, and to UX with placement/focus constraints. Done requires physical-device evidence for claims that emulators cannot establish.
