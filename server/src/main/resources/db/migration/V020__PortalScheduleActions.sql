-- Let the portal decide which scheduled actions exist.
--
-- V019 pinned `action` to a single value with a CHECK, on the assumption that constraining it here
-- was free. It is not: the actions are implemented in the portal, in a registry it can extend, and
-- a constraint in this database means every new action needs a migration in a repository that has
-- nothing to do with it -- and a deploy ordering, since the portal would be writing a value the
-- database still refused.
--
-- The action is validated where it is implemented instead: the portal refuses an unknown action at
-- creation, with a message naming the ones it does know. What this database keeps enforcing is the
-- part that is genuinely structural and outlives any particular action -- that a schedule has at
-- least one trigger, and that each trigger it has is complete.

ALTER TABLE "portalschedule" DROP CONSTRAINT IF EXISTS "portalschedule_action_known";
