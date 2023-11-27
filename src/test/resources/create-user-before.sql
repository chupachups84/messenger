delete from tokens;
delete from friend_request;
delete from messages;
delete from _user;

insert into _user (firstname,lastname,email,username,password,role,is_active,private_profile) values
('test1','test1','test1@gmail.com','test1234','$2a$10$qufoybQp/.bEY9RfGOqi/OV/q/Q3NMhiykD7LKt52eRxQCw/Z7B5q','USER',true,false),
('test2','test2','test2@gmail.com','test2345','$2a$10$OwstiLqgNiU7h.LYPAhu1OqTfMvjXmSGr0UMh.DNsl1e.hiSsKTWG','USER',true,true),
('test3','test3','test2@gmail.com','test3456','$2a$10$OwstiLqgNiU7h.LYPAhu1OqTfMvjXmSGr0UMh.DNsl1e.hiSsKTWG','USER',true,false);
