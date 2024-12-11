CREATE KEYSPACE IF NOT EXISTS Test
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1};

USE Test;

CREATE TABLE Tasks (
    id int PRIMARY KEY,
    factory_id int,
    tasks map <text, text>,
    status text
);

CREATE TABLE test.machines (
   id int PRIMARY KEY,
   factory_id int,
   product text,
   time int
);