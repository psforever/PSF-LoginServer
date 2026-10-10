-- Scheduled actions the portal performs against the world server.
--
-- WHY THIS LIVES HERE
-- The portal has no database of its own -- it reads and writes everything through the PSF-HTTP API,
-- deliberately, so the login server owns its data outright. A schedule still has to be durable
-- (it must survive a portal restart) and it has to be shared (two portal processes must agree on
-- whether a run already happened), so it belongs in this database even though nothing in the world
-- server reads it. Hence the `portal` prefix: these rows are the portal's, stored here because this
-- is where durable shared state lives.
--
-- TRIGGERS
-- A schedule may carry a time trigger, a condition trigger, or both. With both, either one firing
-- runs the action -- they are alternatives, not a conjunction, because "at 04:00, and also whenever
-- the server empties out" is the useful reading and "only at 04:00 if the server is also empty" is
-- not what anyone asks for.
--
--   time      at_time 'HH:MM' plus every_days (1 = daily, 7 = weekly)
--   condition players_below / players_above a threshold
--
-- CONDITIONS FIRE ON A CROSSING, NOT A STATE
-- "Below 10 players" is true continuously all night, so firing on the state would re-apply the
-- layout every time the evaluator ticked. `armed` is what makes it an edge: it is cleared when the
-- condition fires and set again only once the population has gone back the other way, so one
-- crossing produces exactly one run. It is stored rather than kept in memory because a portal
-- restart in the middle of a quiet night must not re-arm and fire a second time.

CREATE TABLE IF NOT EXISTS "portalschedule" (
  "id"            SERIAL PRIMARY KEY,
  "name"          TEXT        NOT NULL,
  -- What to do. Only 'continent_layout' exists today; the column is text so adding another action
  -- is a row rather than a migration.
  "action"        TEXT        NOT NULL,
  -- The action's parameters as JSON. For 'continent_layout' this is the `control` map that
  -- /zones/apply-layout takes: zone number -> { building local id -> faction id }.
  "payload"       TEXT        NOT NULL,
  "enabled"       BOOLEAN     NOT NULL DEFAULT TRUE,

  -- Time trigger. Both NULL when the schedule is condition-only.
  -- 'HH:MM' in the server's own local time, kept as text because that is what it is -- a wall-clock
  -- time of day, not an instant, and storing it as a timestamp would invite a date to come with it.
  "at_time"       TEXT,
  "every_days"    INTEGER,

  -- Condition trigger. NULL when the schedule is time-only.
  "condition"     TEXT,
  "threshold"     INTEGER,
  -- Whether the condition is ready to fire; see the note above on crossings.
  "armed"         BOOLEAN     NOT NULL DEFAULT TRUE,

  -- Bookkeeping. `last_run` is also the claim: a process wins the right to run a schedule by
  -- advancing it, so two portals cannot both act on the same due schedule.
  "last_run"      TIMESTAMP,
  "last_result"   TEXT,
  "created_by"    TEXT        NOT NULL,
  "created"       TIMESTAMP   NOT NULL DEFAULT NOW(),

  CONSTRAINT "portalschedule_action_known"
    CHECK ("action" IN ('continent_layout')),
  CONSTRAINT "portalschedule_condition_known"
    CHECK ("condition" IS NULL OR "condition" IN ('players_below', 'players_above')),
  -- A condition without a threshold, or a threshold without a condition, is a half-written trigger
  -- that would either never fire or fire on nothing.
  CONSTRAINT "portalschedule_condition_complete"
    CHECK (("condition" IS NULL) = ("threshold" IS NULL)),
  -- Same for the time trigger.
  CONSTRAINT "portalschedule_time_complete"
    CHECK (("at_time" IS NULL) = ("every_days" IS NULL)),
  CONSTRAINT "portalschedule_every_days_positive"
    CHECK ("every_days" IS NULL OR "every_days" >= 1),
  -- A schedule with neither trigger can never run, so it is not a schedule.
  CONSTRAINT "portalschedule_has_a_trigger"
    CHECK ("at_time" IS NOT NULL OR "condition" IS NOT NULL)
);

-- The evaluator reads the enabled rows every tick and nothing else ever scans this table.
CREATE INDEX IF NOT EXISTS "portalschedule_enabled_idx" ON "portalschedule" ("enabled");
