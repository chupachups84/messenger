delete from tokens;
delete from friends;
delete from messages;
delete from users;

insert into users (id,firstname,lastname,email,username,password,is_active
,is_friends_list_hidden,is_receipt_messages_friend_only) values
(1,'test1','test1','test1@gmail.com','test1234',
'$2a$10$qufoybQp/.bEY9RfGOqi/OV/q/Q3NMhiykD7LKt52eRxQCw/Z7B5q',true,false,false),

(2,'test2','test2','test2@gmail.com','test2345',
 '$2a$10$OwstiLqgNiU7h.LYPAhu1OqTfMvjXmSGr0UMh.DNsl1e.hiSsKTWG',true,true,true),

(3,'test3','test3','test3@gmail.com','test3456',
 '$2a$10$OwstiLqgNiU7h.LYPAhu1OqTfMvjXmSGr0UMh.DNsl1e.hiSsKTWG',true,false,false);

update users
set email_token='some_valid_email_token' where username = 'test1234';

insert into friends (id,user1_id,user2_id,status_type) values
(1,1,3,'APPROVED');