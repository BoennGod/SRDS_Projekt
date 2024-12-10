Zadanie projektowe z cassandry, potrzebne tabele:



create table tasks ( id int PRIMARY KEY, factory_id int,  tasks map<text, text>);

create table machines ( id int PRIMARY KEY, factory_id int, product text, time int);
