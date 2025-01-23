CREATE KEYSPACE IF NOT EXISTS Test
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3};

USE Test;

CREATE TABLE Tasks (
    id int PRIMARY KEY,
    factory_id text,
    tasks map <text, text>,
    status text
);

CREATE TABLE test.machines (
   id int PRIMARY KEY,
   factory_id text,
   product text,
   time int
);