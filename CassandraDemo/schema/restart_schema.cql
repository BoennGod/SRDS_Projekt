USE Test;

TRUNCATE test.machines;
TRUNCATE test.tasks;

INSERT INTO test.machines (id, factory_id, product, time) VALUES (1, '0', 'productA', 1);
INSERT INTO test.machines (id, factory_id, product, time) VALUES (2, '0', 'productB', 2);
INSERT INTO test.machines (id, factory_id, product, time) VALUES (3, '0', 'productC', 3);
INSERT INTO test.machines (id, factory_id, product, time) VALUES (4, '0', 'productB', 1);
INSERT INTO test.machines (id, factory_id, product, time) VALUES (5, '0', 'productA', 3);
INSERT INTO test.machines (id, factory_id, product, time) VALUES (6, '0', 'productC', 2);