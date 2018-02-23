#!/usr/bin/env bash

docker exec -i --user postgres cgg_db_1 createdb cggdb

docker exec -i --user postgres cgg_db_1 psql cggdb -a  <<__END
create user cgg_role password 'lacinia';
__END

docker exec -i cgg_db_1 psql -Ucgg_role cggdb -a <<__END
drop table if exists designer_to_game;
drop table if exists game_rating;
drop table if exists member;
drop table if exists board_game;
drop table if exists designer;

create table member (
  member_id uuid primary key,
  name text not null,
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp);

create table board_game (
  game_id uuid primary key,
  name text not null,
  summary text,
  min_players integer,
  max_players integer,
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp);

create table designer (
  designer_id uuid primary key,
  name text not null,
  uri text,
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp);

create table game_rating (
  game_id uuid  references board_game(game_id),
  member_id uuid  references member(member_id),
  rating integer not null,
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp);

create table designer_to_game (
  designer_id uuid  references designer(designer_id),
  game_id uuid  references board_game(game_id),
  primary key (designer_id, game_id));

insert into board_game (game_id, name, summary, min_players, max_players) values
  ('4de8fddf-be86-4bcd-b41a-74ad521daea1', 'Zertz',
    'Two player abstract with forced moves and shrinking board', 2, 2),
  ('356c39bd-069c-41cb-905a-5329c8cdda0e', 'Dominion',
    'Created the deck-building genre; zillions of expansions', 2, null),
  ('cb078a3c-6896-4728-92e3-5cb89d5203bf', 'Tiny Epic Galaxies',
    'Fast dice-based sci-fi space game with a bit of chaos', 1, 4),
  ('8cbc8da1-455e-4c2b-9c2b-bd3a8994b923', '7 Wonders: Duel',
    'Tense, quick card game of developing civilizations', 2, 2);

insert into member (member_id, name) values
  ('82483810-e366-47ca-a628-8e395c6c163a', 'curiousattemptbunny'),
  ('8b3dbff2-fcd7-4629-a07c-acdb900247a4', 'bleedingedge'),
  ('bfa60a88-032b-4ffe-8e21-e8dd52c84568', 'missyo');

insert into designer (designer_id, name, uri) values
  ('ad6272cd-0c46-431d-98fb-1ebd8916060b', 'Kris Burm', 'http://www.gipf.com/project_gipf/burm/burm.html'),
  ('113b6514-2672-46b9-b755-56554369544f', 'Antoine Bauza', 'http://www.antoinebauza.fr/'),
  ('4ac43e32-bca7-49c5-922c-ed526cb4b0da', 'Bruno Cathala', 'http://www.brunocathala.com/'),
  ('a30ba193-dfff-4ac5-8c11-03d411d2f881', 'Scott Almes', null),
  ('8f84ebf1-6358-47cc-bdc3-fb041addbe6b', 'Donald X. Vaccarino', null);

insert into designer_to_game (designer_id, game_id) values
  ('ad6272cd-0c46-431d-98fb-1ebd8916060b', '4de8fddf-be86-4bcd-b41a-74ad521daea1'),
  ('113b6514-2672-46b9-b755-56554369544f', '8cbc8da1-455e-4c2b-9c2b-bd3a8994b923'),
  ('8f84ebf1-6358-47cc-bdc3-fb041addbe6b', '356c39bd-069c-41cb-905a-5329c8cdda0e'),
  ('a30ba193-dfff-4ac5-8c11-03d411d2f881', 'cb078a3c-6896-4728-92e3-5cb89d5203bf'),
  ('4ac43e32-bca7-49c5-922c-ed526cb4b0da', '8cbc8da1-455e-4c2b-9c2b-bd3a8994b923');

insert into game_rating (game_id, member_id, rating) values
  ('4de8fddf-be86-4bcd-b41a-74ad521daea1', '82483810-e366-47ca-a628-8e395c6c163a', 3),
  ('4de8fddf-be86-4bcd-b41a-74ad521daea1', '8b3dbff2-fcd7-4629-a07c-acdb900247a4', 5),
  ('cb078a3c-6896-4728-92e3-5cb89d5203bf', '8b3dbff2-fcd7-4629-a07c-acdb900247a4', 4),
  ('8cbc8da1-455e-4c2b-9c2b-bd3a8994b923', '8b3dbff2-fcd7-4629-a07c-acdb900247a4', 4),
  ('8cbc8da1-455e-4c2b-9c2b-bd3a8994b923', 'bfa60a88-032b-4ffe-8e21-e8dd52c84568', 4),
  ('8cbc8da1-455e-4c2b-9c2b-bd3a8994b923', '82483810-e366-47ca-a628-8e395c6c163a', 5);
__END
