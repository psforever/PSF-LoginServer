CREATE TABLE IF NOT EXISTS "buildings" (
  local_id INT NOT NULL,
  zone_id INT NOT NULL,
  faction_id INT NOT NULL,
  PRIMARY KEY (local_id, zone_id)
);