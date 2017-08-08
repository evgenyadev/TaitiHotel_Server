BEGIN TRANSACTION;
DROP TABLE IF EXISTS "users_pending";
CREATE TABLE "users_pending"
(
    rowid SERIAL PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    time_from INTEGER NOT NULL,
    time_to INTEGER NOT NULL,
	date_check_in DATE NOT NULL,
	date_check_out DATE NOT NULL
);
DROP TABLE IF EXISTS "rooms_pending";
CREATE TABLE "rooms_pending" (
	"rowid"	SERIAL NOT NULL,
	"user_id"	INTEGER NOT NULL,
	"room_type"	TEXT NOT NULL,
	"rooms_count"	INTEGER NOT NULL DEFAULT 1,
	"adult_count"	INTEGER NOT NULL DEFAULT 0,
	"child_3_10_count"	INTEGER NOT NULL DEFAULT 0,
	"child_3_count"	INTEGER NOT NULL DEFAULT 0,
	PRIMARY KEY("rowid")
);
DROP TABLE IF EXISTS "rooms_ordered";
CREATE TABLE "rooms_ordered" (
	"id"	SERIAL NOT NULL,
	"room_id"	INT NOT NULL,
	"date_begin"	DATE NOT NULL,
	"date_end"	DATE NOT NULL,
	PRIMARY KEY("id")
);
INSERT INTO "rooms_ordered" (id,room_id,date_begin,date_end) VALUES (4,4,'2017-08-16','2017-08-18'),
 (3,1,'2017-08-28','2017-08-29'),
 (2,1,'2017-08-23','2017-08-26'),
 (1,1,'2017-08-15','2017-08-19'),
 (5,2,'2017-08-21','2017-08-24'),
 (6,2,'2017-08-27','2017-09-02'),
 (7,3,'2017-08-15','2017-08-20'),
 (8,3,'2017-08-22','2017-08-25'),
 (9,3,'2017-08-27','2017-09-05'),
 (10,4,'2017-08-19','2017-08-25'),
 (11,4,'2017-08-30','2017-09-02'),
 (12,5,'2017-08-16','2017-08-17'),
 (13,5,'2017-08-19','2017-08-20'),
 (14,5,'2017-08-23','2017-08-25'),
 (15,5,'2017-08-28','2017-08-29'),
 (16,6,'2017-08-18','2017-08-20'),
 (17,6,'2017-08-22','2017-08-23'),
 (18,6,'2017-08-26','2017-08-28'),
 (19,6,'2017-08-31','2017-09-01'),
 (20,7,'2017-08-15','2017-08-25'),
 (21,7,'2017-08-28','2017-09-03'),
 (22,8,'2017-08-16','2017-08-18'),
 (23,8,'2017-08-21','2017-08-22'),
 (24,8,'2017-08-25','2017-08-27'),
 (25,8,'2017-08-30','2017-08-31'),
 (26,1,'2017-09-02','2017-09-05'),
 (27,1,'2017-09-11','2017-09-15'),
 (28,2,'2017-09-05','2017-09-08'),
 (29,2,'2017-09-10','2017-09-11'),
 (30,3,'2017-09-08','2017-09-15'),
 (31,4,'2017-09-04','2017-09-06'),
 (32,4,'2017-09-08','2017-09-10'),
 (33,4,'2017-09-12','2017-09-13'),
 (34,5,'2017-09-03','2017-09-04'),
 (35,5,'2017-09-07','2017-09-08'),
 (36,5,'2017-09-11','2017-09-12'),
 (37,5,'2017-09-14','2017-09-15'),
 (38,6,'2017-09-03','2017-09-04'),
 (39,6,'2017-09-07','2017-09-10'),
 (40,6,'2017-09-12','2017-09-15'),
 (41,7,'2017-09-06','2017-09-10'),
 (42,7,'2017-09-12','2017-09-15'),
 (43,8,'2017-09-03','2017-09-06'),
 (44,8,'2017-09-09','2017-09-10');
DROP TABLE IF EXISTS "rooms";
CREATE TABLE rooms
(
    id INT NOT NULL,
    floor INT DEFAULT 1 NOT NULL,
    room_type TEXT NOT NULL,
	PRIMARY KEY("id")
);
INSERT INTO "rooms" (id,floor,room_type) VALUES (1,1,'Стандарт'),
 (2,2,'Стандарт'),
 (3,1,'Улучшенный 1'),
 (4,2,'Улучшенный 1'),
 (5,1,'Улучшенный 2'),
 (6,2,'Улучшенный 2'),
 (7,1,'Двухкомнатный'),
 (8,2,'Двухкомнатный');
 DROP TABLE IF EXISTS "room_types";
CREATE TABLE "room_types" (
	"id"	SERIAL NOT NULL,
	"capacity"	INT NOT NULL,
	"type"	TEXT NOT NULL UNIQUE,
	"description"	TEXT NOT NULL,
	PRIMARY KEY("id")
);
INSERT INTO "room_types" (capacity,type,description) VALUES (6,'Двухкомнатный','Проживние: до 6 человек.  
Оплачивается ВЕСЬ НОМЕР в сутки.

В номере: евроремонт, 2-х спальная деревянная кровать(ортопедический матрац), мягкий раскладывающийся уголок, плазменный телевизор(кабельное TV), холодильник, кондиционер.

Удобства: в номере(душ, умывальник, туалет); горячая и холодная вода круглосуточно. '),
 (3,'Стандарт','Проживание: 2 - 3 человека.
Оплачивается каждый человек в сутки.
Детям от 3 до 10 лет скидка 30%
Детям до 3 лет цена проживания 40 грн.

В номере: евроремонт, холодильник, телевизор(кабельное TV).

Удобства: в номере(душ, умывальник, туалет), горячая и холодная вода круглосуточно.'),
 (5,'Улучшенный 1','Проживание: 2 - 5 человек.
Оплачивается каждый человек в сутки.
Детям от 3 до 10 лет скидка.
Детям до 3 лет цена проживания 40 грн.

В номере: евроремонт, холодильник, телевизор(кабельное TV), кондиционер.

Удобства: в номере(душ, умывальник, туалет); горячая и холодная вода круглосуточно).'),
 (5,'Улучшенный 2','Проживание: 2 - 5 человек.
Оплачивается каждый человек в сутки.
Детям от 3 до 10 лет скидка.
Детям до 3 лет цена проживания 40 грн.

В номере: новый евроремонт, холодильник, телевизор(кабельное TV), кондиционер.

Удобства: в номере(душ, умывальник, туалет); горячая и холодная вода круглосуточно).');
 DROP TABLE IF EXISTS "prices";
CREATE TABLE "prices" (
	"id"	SERIAL NOT NULL,
	"room_type"	TEXT NOT NULL UNIQUE,
	"may"	INT NOT NULL,
	"june"	INT NOT NULL,
	"july"	INT NOT NULL,
	"august"	INT NOT NULL,
	"september"	INT NOT NULL,
	"child_3_price"	INT NOT NULL,
	"child_3_10_discount"	INT NOT NULL,
	PRIMARY KEY("id")
);
INSERT INTO "prices" (room_type,may,june,july,august,september,child_3_price,child_3_10_discount) VALUES ('Стандарт',109,109,219,219,109,40,30),
 ('Улучшенный 1',139,149,249,249,149,40,30),
 ('Улучшенный 2',149,159,279,279,159,40,30),
 ('Двухкомнатный',399,699,1099,1099,399,40,30);
 DROP TABLE IF EXISTS "devices";
CREATE TABLE "devices" (
	"id"	SERIAL NOT NULL,
	"pseudo_id"	TEXT NOT NULL UNIQUE,
	"phone_num"	TEXT,
	"name"	TEXT,
	"access_level" INTEGER NOT NULL DEFAULT 0,
	PRIMARY KEY("id")
);
COMMIT;
