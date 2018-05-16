CREATE TABLE media (
  id                        SERIAL PRIMARY KEY,
  owner_id                  BIGINT UNSIGNED,
  path                      VARCHAR(190) NOT NULL,
  mime_type                 VARCHAR(100),
  created                   BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE sessions (
  id                        SERIAL PRIMARY KEY,
  user_id                   BIGINT UNSIGNED,
  suid                      VARCHAR(60) NOT NULL UNIQUE
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE users (
  id                        SERIAL PRIMARY KEY,
  email                     VARCHAR(100) NOT NULL UNIQUE,
  hash                      VARCHAR(60),
  avatar_id                 BIGINT,
  user_status_id            INT UNSIGNED NOT NULL,
  account_status_id         INT UNSIGNED NOT NULL,
  name                      VARCHAR(100),
  surname                   VARCHAR(100),
  timezone_id               INT UNSIGNED NOT NULL,
  registered                BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE user_roles (
  user_id                   BIGINT UNSIGNED NOT NULL,
  role                      VARCHAR(100) NOT NULL,
  PRIMARY KEY (user_id, role)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE params (
  id                        SERIAL PRIMARY KEY,
  name                      VARCHAR(100) NOT NULL UNIQUE,
  `value`                   VARCHAR(100)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE segments (
  id                        SERIAL PRIMARY KEY,
  start_longitude           DOUBLE PRECISION,
  start_latitude			DOUBLE PRECISION,
  end_longitude				DOUBLE PRECISION,
  end_latitude				DOUBLE PRECISION
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE video (
  id                        SERIAL PRIMARY KEY,
  path_id                   BIGINT UNSIGNED NOT NULL,
  link                      VARCHAR(100) NOT NULL -- should be unique
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE segments_video_contexts (
  segment_id                BIGINT UNSIGNED NOT NULL,
  video_id                  BIGINT UNSIGNED NOT NULL,
  start_time    			DOUBLE PRECISION,
  end_time   				DOUBLE PRECISION,
  PRIMARY KEY (segment_id, video_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE pathes (
  id                        SERIAL PRIMARY KEY,
  descr                     VARCHAR(100) NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE path_segments (
  path_id                   BIGINT UNSIGNED NOT NULL,
  segment_id                BIGINT UNSIGNED NOT NULL,
  `order`                   INT NOT NULL,
  PRIMARY KEY (path_id, segment_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;







