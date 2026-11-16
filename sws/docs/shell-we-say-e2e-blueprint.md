# Shell We Say: End-to-End Blueprint

## 1. Purpose and Scope

This document defines a complete build and development path for Shell We Say.

Game premise:
- You are a single mortarman operating from a fixed dugout command post.
- The enemy base is not directly visible.
- A short-range drone provides a probabilistic heatmap of likely base location.
- You have a limited number of live and dummy flare rounds.
- After each shot, a ground-mate spotter model reports how far the impact landed from the base over radio.
- You update your map pointer and estimate state until confidence is high enough to commit live rounds.


## 2. Core Design Pillars

1. Estimation over reflex:
- Players should win by reducing uncertainty, not by twitch aiming.

2. Bounded resources:
- Dummy rounds, live rounds, battery, and mission time are all limited.

3. Information as currency:
- Every action should trade resources for information gain.

4. Stylized realism:
- Physics should feel believable but remain computationally simple.

5. Clear feedback loop:
- Observe, hypothesize, test, update, commit.

6. Hidden target clarity:
- Target is never shown directly; only uncertainty products are shown.

7. Embodied station play:
- All decisions happen through physical equipment in the dugout: mortar controls, map board, notes, and radio.

## 3. Core Gameplay Loop

1. Deploy short-range drone around your current position.
2. Read heatmap probability zones for likely enemy base location.
3. Mark an initial pointer on the tactical map.
4. Propose firing solution from map pointer and current estimate.
5. Fire dummy flare round.
6. Receive model feedback: distance from impact to true base.
7. Update map pointer and state estimate.
8. Decide whether to fire another dummy or commit live round.
9. Fire live round when confidence threshold is met.
10. Score outcome, consume resources, continue mission.

Loop objective:
- Minimize expected miss distance and resource waste under uncertainty.

## 3.1 First-person dugout interaction model

The player does not walk around the battlefield.
The game is first-person, but station-locked inside a dugout.

Primary interactable surfaces:

1. Mortar station
- Set azimuth and elevation.
- Choose dummy flare or live round.
- Fire and observe time-of-flight feedback.

2. Map board
- View local terrain abstraction and drone heatmap layer.
- Place or adjust target pointer.
- Compare predicted impact and corrected estimate.

3. Notes board
- View prior shots, offsets, confidence trends, and suggested correction deltas.
- Keep fast-reference formulas and mission constraints.

4. Radio terminal
- Receive ground-mate model callouts after impact.
- Callouts are short and actionable, for example distance-off and direction cue if enabled by difficulty.

Design rule:
- Every action should map to a physical station interaction, not abstract menu spam.

## 4. Mission Rules and Constraints

### 4.1 Hard constraints

- Max round budget per mission.
- Max dummy flare budget per mission.
- Drone battery duration and recharge delay.
- Fog, wind, and sensor noise vary by mission seed.

### 4.2 Suggested default values for first prototype

- Live rounds: 8
- Dummy rounds: 6
- Drone battery: 180 seconds active
- Mission duration: 15 minutes
- Effective abstract range envelope: 200 to 1200 in-game meters

Use these only as design defaults, not fixed rules.

## 5. Coordinate Frames and World Model

Use one canonical coordinate system everywhere:

- World frame W: x east, y north, z up.
- Gun frame G: origin at mortar tube base.
- Drone frame D: local camera frame.

Data model:
- Terrain height map h(x, y)
- Obstacle mesh and line-of-sight blockers
- Camp target state with covariance

Store all persistent states in world frame.
Convert to local frames only for rendering and sensor projection.

## 6. Ballistic and Impact Model (Game Safe, Stylized)

Use a simplified projectile model for fast simulation:

State vector for projectile:
- p = [x, y, z]
- v = [vx, vy, vz]

Continuous update:
- dp/dt = v
- dv/dt = g + w + drag(v)

Where:
- g = [0, 0, -g]
- w is a small wind acceleration term
- drag(v) can be linear for first iteration: -k * v

Discretized step (dt):
- p_{k+1} = p_k + v_k * dt
- v_{k+1} = v_k + (g + w - k * v_k) * dt

Impact criterion:
- Projectile impacts when z <= h(x, y)

Output from one trajectory simulation:
- impact point in world coordinates
- time of flight
- predicted dispersion class

## 7. Sensor and Observation Model

Drone produces noisy observations of:
- heatmap intensity over candidate base regions
- smoke/light signatures from dummy impact
- optional wind cue proxy

Post-shot feedback model provides:
- scalar miss-distance estimate from impact point to true base
- optional confidence grade for that estimate

Observation model:
- z_k = H x_k + n_k

Where:
- x_k is hidden state (target location, wind bias, terrain correction terms)
- H maps hidden state to observable quantities
- n_k is measurement noise, modeled Gaussian in early builds

Noise tuning:
- Start with diagonal covariance matrix R
- Increase R with distance, weather severity, and occlusion

Distance-only correction channel:
- A scalar distance measurement constrains the target to a ring around impact.
- Multiple dummy rounds convert ring intersections into a localized estimate.

## 8. State Estimation Pipeline

Use recursive Bayesian estimation.
For first implementation, an Extended Kalman Filter is enough.

Hidden state candidate:
- x = [target_x, target_y, wind_x, wind_y, bias_range, bias_azimuth]

Prediction:
- x^-_k = f(x_{k-1}, u_k)
- P^-_k = F P_{k-1} F^T + Q

Correction:
- K_k = P^-_k H^T (H P^-_k H^T + R)^-1
- x_k = x^-_k + K_k (z_k - h(x^-_k))
- P_k = (I - K_k H) P^-_k

Design intention:
- Dummy rounds reduce covariance before live rounds are spent.

Interpretation in this game loop:
- Heatmap gives coarse prior.
- Distance-from-impact feedback provides iterative correction signal.
- Combined updates shrink uncertainty as the player cycles map updates and shot corrections well.

When nonlinearity is heavy:
- Move to Unscented Kalman Filter or Particle Filter.

## 9. Decision Policy for Dummy vs Live Round

At each shot opportunity, solve a simple value decision:

Action set:
- A = {fire_dummy, fire_live, reposition_drone}

Utility approximation:
- U(action) = expected_damage_gain - resource_cost - risk_penalty

Practical policy for prototype:
1. If covariance trace > threshold_high: fire dummy.
2. Else if expected hit probability > threshold_commit and live rounds remain: fire live.
3. Else reposition drone and observe again.

Single-player policy guardrail:
- Live commit requires confidence above threshold_commit and at least one correction cycle.
- If confidence is low, force one more information action (dummy or drone sweep).

Key metric:
- Expected Value of Information (EVI) from dummy shot.

If EVI(dummy) > opportunity cost, dummy shot is rational.

## 10. Procedural Terrain and Camp Placement

### 10.1 Terrain generation

Use layered noise and erosion approximation:
- base = fBm noise
- ridge mask for dramatic contours
- optional thermal map for visibility zones

Generate:
- traversability map
- line-of-sight complexity map
- impact accessibility map

### 10.2 Camp placement algorithm

Camp placement should satisfy constraints:
- not too close to spawn
- not impossible to hit under allowed envelope
- tactical value around terrain features

Camp generation steps:
1. Sample candidate points from terrain.
2. Reject by slope/cover/range invalidity.
3. Score each candidate by tactical heuristic.
4. Draw from weighted distribution.
5. Validate mission solvability by quick simulation.

Mission solvability check:
- At least one feasible firing solution exists under bounded uncertainty and resources.

## 11. Difficulty Scaling Through System Uncertainty

Do not scale only HP or enemy count.
Scale uncertainty and observability:

- Increase wind variability
- Increase sensor noise
- Add decoy signatures
- Shrink dummy round budget
- Add moving patrol camps with drift model

Difficulty dimension vector:
- D = [noise, wind, resource scarcity, map complexity, target deception]

## 12. Scoring and Outcome Metrics

Primary score terms:
- mission success
- live rounds conserved
- dummy rounds used efficiently
- time to neutralize targets
- collateral penalty

Secondary analytics:
- average covariance before live shot
- mean miss distance per live shot
- information gain per dummy shot

Use these metrics for both game balancing and player feedback.

## 13. System Architecture in Code

Target code boundaries:

Core module:
- domain: math models, state estimation, mission logic
- simulation: projectile, terrain query, hit resolution
- gameplay: mission state machine, scoring
- screens: menu, gameplay, debrief

Desktop module:
- launcher
- input bindings
- platform runtime configuration

Recommended package map:
- org.greasenbrine.shellwesay.domain
- org.greasenbrine.shellwesay.sim
- org.greasenbrine.shellwesay.estimation
- org.greasenbrine.shellwesay.mission
- org.greasenbrine.shellwesay.ui
- org.greasenbrine.shellwesay.screens

## 14. Current Pipeline in This Repository

Current startup flow:
1. Desktop launcher starts LWJGL3 runtime.
2. Core engine is constructed with AppConfig.
3. Engine sets DefaultScreen as first screen.
4. Render loop clears and draws status text.

Files currently involved:
- [sws/desktop/src/main/java/org/greasenbrine/shellwesay/desktop/DesktopLauncher.java](sws/desktop/src/main/java/org/greasenbrine/shellwesay/desktop/DesktopLauncher.java)
- [sws/core/src/main/java/org/greasenbrine/shellwesay/ShellWeSayEngine.java](sws/core/src/main/java/org/greasenbrine/shellwesay/ShellWeSayEngine.java)
- [sws/core/src/main/java/org/greasenbrine/shellwesay/screens/DefaultScreen.java](sws/core/src/main/java/org/greasenbrine/shellwesay/screens/DefaultScreen.java)

Build pipeline:
1. Maven reactor builds core then desktop.
2. Desktop shade plugin outputs runnable jar.

Run path on macOS:
- Use JVM first-thread option for GLFW runtime.

## 15. Build and Run Workflow

From sws directory:

1. Build all modules
- mvn clean package

2. Run desktop jar on macOS
- java -XstartOnFirstThread -jar desktop/target/shell-we-say.jar

3. If running from IDE launch config
- Ensure VM args include -XstartOnFirstThread

## 16. Implementation Roadmap (No-Fluff)

### Milestone 1: Functional startup and screen flow

- DefaultScreen
- MenuScreen
- GameplayScreen
- DebriefScreen
- Screen router service

Exit criteria:
- Player can start mission and return to menu.

### Milestone 2: Terrain and camp generation

- Height map generator
- Camp candidate sampler
- Solvability validator

Exit criteria:
- Every mission is playable and non-trivial.

### Milestone 3: Ballistics and impact simulation

- Stylized projectile model
- Impact resolver against terrain
- Dummy impact marker visualization

Exit criteria:
- Dummy shots land with physically coherent spread.

### Milestone 4: Estimation system

- Observation model
- EKF state update
- Confidence UI

Exit criteria:
- Each dummy shot measurably reduces uncertainty.

### Milestone 5: Decision and scoring loop

- Action policy layer
- Live round commitment logic
- Mission scoring and debrief analytics

Exit criteria:
- Skilled play beats random play in measurable metrics.

## 17. Learning Stack and What to Study First

Study order is critical.

### Stage A: Math and simulation basics

Learn:
- vectors, transforms, and coordinate frames
- discrete-time integration
- noise, covariance, Gaussian intuition

References:
- 3D Math Primer for Graphics and Game Development (Dunn and Parberry)
- Mathematics for 3D Game Programming and Computer Graphics (Lengyel)

### Stage B: Probability and estimation

Learn:
- Bayesian update intuition
- Kalman Filter and Extended Kalman Filter
- observability and identifiability

References:
- Probabilistic Robotics (Thrun, Burgard, Fox) chapters on Bayes filters
- Kalman and Bayesian Filters in Python (open textbook by Labbe)

### Stage C: Control and decision under uncertainty

Learn:
- state-space models
- model predictive thinking
- expected value of information

References:
- Feedback Systems (Astrom and Murray, free online)
- Dynamic Programming and Optimal Control (Bertsekas)

### Stage D: Procedural generation and balancing

Learn:
- noise functions and terrain synthesis
- constrained random generation
- Monte Carlo testing for solvability

References:
- Texturing and Modeling: A Procedural Approach (Ebert et al.)
- Game AI Pro articles on procedural generation validation

## 18. Practical Validation Harnesses You Need

Create deterministic harnesses early:

1. Projectile test harness:
- fixed seed
- known initial conditions
- expected impact tolerance

2. Estimator test harness:
- synthetic noisy observations
- expected covariance reduction trend

3. Mission generator harness:
- generate N maps
- report solvability rate
- reject invalid seeds

Without these harnesses, tuning will stall.

## 19. Data You Should Log Every Shot

For each shot record:
- mission seed
- shot type (dummy or live)
- estimated target mean and covariance before shot
- firing solution parameters
- predicted impact and observed impact
- post-update covariance
- score delta

Use logs to tune policies and difficulty objectively.

## 20. Immediate Next Build Tasks

1. Add ScreenRouter and MenuScreen.
2. Move DefaultScreen text to a reusable debug overlay widget.
3. Implement terrain height query service interface.
4. Add first projectile simulator class with deterministic unit tests.
5. Add first EKF skeleton with synthetic observation test.

## 21. Definition of Done for Alpha Prototype

Alpha is done when:
- complete mission loop works end-to-end
- at least 3 mission seeds are solvable and fun
- dummy rounds materially improve hit probability
- player can explain why they hit or missed
- scoring reflects estimation quality, not luck alone

## 22. Common Failure Modes to Avoid

1. Overcomplicated physics too early.
2. Noisy visuals with no uncertainty UI.
3. No deterministic tests for simulation.
4. Coupling gameplay logic to rendering classes.
5. Difficulty scaling by enemy count only.

## 23. Short Glossary

- Covariance: uncertainty shape around estimate.
- EVI: expected value of information.
- EKF: estimator for mildly nonlinear systems.
- Observability: whether hidden state can be inferred from measurements.
- Solvability: whether a generated mission can be completed with available resources.

## 24. Final Guidance

Build in this order:
- architecture boundaries
- deterministic simulation
- estimation loop
- decision policy
- visual polish

If this order is followed, the game scales cleanly from prototype to deep systems game without major rewrites.

## 25. Developer Handbook

This section is the practical guide for anyone implementing new systems in this codebase.

### 25.1 What the current pipeline does today

At a high level, the pipeline has four stages:

1. Build orchestration
- Maven starts at [sws/pom.xml](sws/pom.xml).
- Reactor order is core first, then desktop.

2. Core compilation
- Core compiles game code and shared domain logic from [sws/core/src/main/java](sws/core/src/main/java).

3. Desktop compilation
- Desktop compiles launcher and platform wiring from [sws/desktop/src/main/java](sws/desktop/src/main/java).
- Desktop depends on core output.

4. Runtime packaging
- Shade plugin assembles a runnable desktop jar at [sws/desktop/target/shell-we-say.jar](sws/desktop/target/shell-we-say.jar).

Runtime sequence:

1. [sws/desktop/src/main/java/org/greasenbrine/shellwesay/desktop/DesktopLauncher.java](sws/desktop/src/main/java/org/greasenbrine/shellwesay/desktop/DesktopLauncher.java) creates the LWJGL app.
2. It constructs [sws/core/src/main/java/org/greasenbrine/shellwesay/ShellWeSayEngine.java](sws/core/src/main/java/org/greasenbrine/shellwesay/ShellWeSayEngine.java).
3. Engine sets [sws/core/src/main/java/org/greasenbrine/shellwesay/screens/DefaultScreen.java](sws/core/src/main/java/org/greasenbrine/shellwesay/screens/DefaultScreen.java).
4. libGDX enters the frame loop and repeatedly calls render.

### 25.2 The library stack and why each one exists

Core runtime libraries:

1. com.badlogicgames.gdx:gdx
- Purpose: cross-platform game API (math, rendering abstractions, assets, app lifecycle).

2. com.badlogicgames.gdx:gdx-backend-lwjgl3
- Purpose: desktop backend implementation using LWJGL3.

3. com.badlogicgames.gdx:gdx-platform:natives-desktop
- Purpose: native binaries for desktop execution.

Build tooling:

1. Maven compiler plugin
- Purpose: consistent Java release target across modules.

2. Maven shade plugin
- Purpose: produce one runnable desktop jar with dependencies.

### 25.3 Graphics engine mapping

You are using libGDX as the engine-level framework and LWJGL3 as the desktop backend.

Responsibility split:

1. libGDX
- Game lifecycle (create, render, resize, pause, resume, dispose).
- Screen management APIs.
- Rendering abstraction and utility classes.

2. LWJGL3 backend
- Window creation and event loop.
- OpenGL context.
- Platform-specific native integration.

Practical implication:
- Game logic should stay in core and speak libGDX interfaces.
- Desktop module should only do startup and platform concerns.

### 25.4 How to add a new feature without breaking structure

Use this exact workflow for every non-trivial change.

1. Define the feature boundary
- Write a one-paragraph intent statement.
- Decide whether it belongs in core or desktop.

2. Add domain model first
- Create immutable value objects and interfaces in core.
- Avoid rendering dependencies in this step.

3. Add system behavior
- Implement simulation or state update logic in core.
- Add deterministic tests before UI wiring.

4. Wire into gameplay flow
- Integrate through Screen or service orchestration.
- Keep rendering code as a thin projection of state.

5. Add instrumentation
- Log key metrics to verify expected behavior.

6. Validate through reproducible scenario
- Fixed seed, fixed inputs, expected outputs.

7. Commit in narrow slices
- Prefer messages like feat(component): add x to y.

### 25.5 Module placement rules

Put code in core when it is:
- game rules
- simulation
- estimation
- mission logic
- scoring and analytics

Put code in desktop when it is:
- launcher config
- desktop-only integrations
- debug hotkeys specific to desktop runtime

Never let core import desktop classes.
Desktop can depend on core, never the reverse.

### 25.6 Recommended package growth plan

Add these packages in core as features arrive:

1. org.greasenbrine.shellwesay.domain
- mission entities, round inventory, target descriptors.

2. org.greasenbrine.shellwesay.sim
- trajectory integrator, impact solver, terrain query adapters.

3. org.greasenbrine.shellwesay.estimation
- observation model, EKF state, covariance update services.

4. org.greasenbrine.shellwesay.policy
- dummy versus live decision logic.

5. org.greasenbrine.shellwesay.telemetry
- shot logs, mission metrics, balancing statistics.

### 25.7 Developer runbook

Normal build:
- cd /Users/nepsauce/git/personal/shell-we-say/sws
- mvn clean package

Run on macOS:
- java -XstartOnFirstThread -jar desktop/target/shell-we-say.jar

Why this flag matters:
- GLFW on macOS must initialize on the first thread.
- Without it, startup fails before any screen renders.

IDE run configuration:
- Add VM argument -XstartOnFirstThread.
- Keep working directory at sws root so relative resources resolve consistently.

### 25.8 Troubleshooting quick reference

1. Symptom: GLFW may only be used on the main thread
- Cause: missing -XstartOnFirstThread.
- Fix: add JVM arg in command or IDE launch config.

2. Symptom: classpath or class not found in launcher
- Cause: desktop module not rebuilt after core changes.
- Fix: run mvn clean package from sws root.

3. Symptom: code compiles but behavior is unstable
- Cause: non-deterministic simulation and no fixed-seed test harness.
- Fix: add deterministic harness and compare expected trajectory envelopes.

4. Symptom: hard-to-balance missions
- Cause: no telemetry loop.
- Fix: log shot outcomes and covariance trends, then tune with data.

### 25.9 Learning path for engine-level competence

Phase 1: libGDX fundamentals
- Learn Game, ApplicationListener, Screen lifecycle.
- Learn SpriteBatch, ShapeRenderer, Camera basics.
- Learn asset loading flow and disposal discipline.

Phase 2: simulation architecture
- Keep simulation pure and step-based.
- Separate render frame rate from simulation update logic.

Phase 3: probabilistic systems
- Implement EKF with synthetic observations.
- Visualize uncertainty with clear UI cues.

Phase 4: production discipline
- deterministic tests
- telemetry-driven balancing
- strict module boundaries

Primary official references:
- libGDX wiki and javadocs
- LWJGL documentation for backend behavior
- Maven Shade plugin docs for packaging behavior

### 25.10 Definition of a clean feature merge

A feature is ready when all are true:

1. It respects module boundaries.
2. It has deterministic verification for core logic.
3. It has at least one runtime scenario demonstrating expected behavior.
4. It logs or exposes a metric tied to gameplay value.
5. It is committed in focused, readable slices.
