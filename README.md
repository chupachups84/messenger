# Messenger
Тестовое задание на стажировку от РЭЛЭКС

Задание: https://docs.google.com/document/d/1JJ0xWr8maPbsthWCR5oN8934VELzpoXmU-SobxY-8G0/edit
___
# Описание и пример работы
## Из тестового задания выполнены все обязательные и необязательные требования, пользователь может зарегестрироваться или авторизироваться в системе, редактировать свой профиль , отправлять сообщения другому пользователю и добавлять пользователей в друзья

### Авторизация:

#### Регистрация:
![image](https://github.com/pp848484/Messenger/assets/96766856/0aa20721-2b0f-40a2-83be-d7f2ce878617)
![image](https://github.com/pp848484/Messenger/assets/96766856/1bd8cfe7-ac9c-41ac-b5f7-f92ff5bbd4f4)
![image](https://github.com/pp848484/Messenger/assets/96766856/b9aeba2d-828f-4c8a-bdfe-0f3e77784a58)
![image](https://github.com/pp848484/Messenger/assets/96766856/e6cc2700-3ce2-49b6-9094-695f08c9c6b0)
![image](https://github.com/pp848484/Messenger/assets/96766856/3a6fc814-ff84-4495-8cb3-59b538c44fe3)
![image](https://github.com/pp848484/Messenger/assets/96766856/7b549868-2cb8-4aa6-97b9-93adc477318f)

##### Данные о пользователе сохраняются в таблицу, пароль шифруется, на указанные email приходит сообщение с подтверждением почты, выдается JWT токен(для доступа на 1 день, для обновления на 7 дней)

#### Вход:

![image](https://github.com/pp848484/Messenger/assets/96766856/26784c4a-3df7-4c0a-bc5b-86102e038b67)

#### Проверяется наличие пользователя в бд а также статус isActive 

#### Выход:
![image](https://github.com/pp848484/Messenger/assets/96766856/b2d70fa4-d82f-4cbb-9a78-d6349ef1ca1d)

#### Сессия очищается, токен инвалидируется.
#### Здесь и далее в Header под ключом Authorization будет передаваться Bearer JwtToken (выглядит примерно так Authorization: Bearer accesToken )для доступа к url, так как данные страницы будут работать только с авторизированным пользователем.

#### Обновление токена доступа:
![image](https://github.com/pp848484/Messenger/assets/96766856/83bbe170-1986-4fa5-a642-79bc184bd63d)

##### Чтобы пользователь не авторизировывался заново по истичении времени accesToken он может передавать refreshToken(токен для обновления) и получать новый accesToken

### Профиль пользователя:

#### Вывод информации о пользователе:
![image](https://github.com/pp848484/Messenger/assets/96766856/cc5dd53e-ff09-4e9a-af7e-5e095fb78e6d)

#### Редактирование основной информации( имя, фамилия и тд):
![image](https://github.com/pp848484/Messenger/assets/96766856/e18ba394-a47c-4fd6-89f9-1557c8955670)
#### Теперь если запросить информацию о пользователе
![image](https://github.com/pp848484/Messenger/assets/96766856/930ce954-54f2-433c-8135-b8f12393f31e)
#### В запросе можно передавать не все поля для редактирования, но если пользователь попытается передать пустые поля( "lastname":"") произойдет ошибка валидации , которая обрабатывается:
![image](https://github.com/pp848484/Messenger/assets/96766856/08bba77f-7ca3-4460-afe0-fce0cd5fd174)

#### Редактирование важной информации(username,email,password):
![image](https://github.com/pp848484/Messenger/assets/96766856/de602d3e-542c-4cd3-9b97-15ab920658cd)
![image](https://github.com/pp848484/Messenger/assets/96766856/e52d99ed-b13c-4261-ae26-36531eaac60f)
![image](https://github.com/pp848484/Messenger/assets/96766856/f2758ddb-137c-4e4b-a31d-f19488bbc00d)
![image](https://github.com/pp848484/Messenger/assets/96766856/83a7e9cd-aa40-4b01-b6bc-14431b68f07f)
#### Теперь если вывести информацию о пользователе , то мы увидим это:
![image](https://github.com/pp848484/Messenger/assets/96766856/08945d96-6e63-4f1e-9017-cc2894032637)
#### При изменении важной информации о пользователе , выдается новый JWT токен для авторизации

#### Изменение настроек приватности профиля:
![image](https://github.com/pp848484/Messenger/assets/96766856/b797ec6f-402e-4693-b2c8-dabb866ca86a)
![image](https://github.com/pp848484/Messenger/assets/96766856/c1911509-905f-495a-bee1-1596678dbc37)

#### Удаление профиля
![image](https://github.com/pp848484/Messenger/assets/96766856/4159a92b-638d-4548-b804-3871191e3b52)
#### В бд в колонке is_active устанавливается false, и при попытке зайти получить доступ к какой-нибудь API будет выбрасываться исключение UserDeactivated.
#### по выданному accesToken или refreshToken можно восстановить пользователя, в течении длительности refreshToken 

#### Восстановление профиля
![image](https://github.com/pp848484/Messenger/assets/96766856/e7cd1d31-7071-467b-8960-7d32f320b6ee)
#### В бд в колонке is_active устанавливается true

### Сообщения:
#### Для демонтрации создам нового пользователя:
![image](https://github.com/pp848484/Messenger/assets/96766856/347ccd03-f07c-4e56-980a-a0bebc3f2bd2)

#### как RESTful:
#### Отправка:
![image](https://github.com/pp848484/Messenger/assets/96766856/311b3eed-6db1-4b26-bb35-6755a670043a)
![image](https://github.com/pp848484/Messenger/assets/96766856/0fb8a38b-71a3-4dc6-8385-b96ad57a0511)
#### История сообщений с пользователем:
#### Со стоороны пользователя pp84
![image](https://github.com/pp848484/Messenger/assets/96766856/ae0ba486-3d7c-4abd-8c8d-40e0dff30beb)
#### Со стоороны пользователя igor
![image](https://github.com/pp848484/Messenger/assets/96766856/1a7c8175-004b-4491-953b-386f25bcc088)

#### как WebSocket:
#### Со стоороны пользователя igor
![image](https://github.com/pp848484/Messenger/assets/96766856/5224088c-9620-4187-bf84-95f1a5aad7ec)
#### Со стоороны пользователя pp84
![image](https://github.com/pp848484/Messenger/assets/96766856/a7e2be07-a7fb-4d57-9787-437ab5e4d718)

### В случае если пользователь пытается отправить сообщение другому пользователю, у которого стоит приватность профиля и при этом они не в друзьях друг у друга выкидвается исключение FriendshipException c cообщением Вы не можете отправить сообщение данному пользователю, так как он использует настройки приватности
![image](https://github.com/pp848484/Messenger/assets/96766856/34edba12-fd13-441d-9c1c-28b2df9f64ca)
![image](https://github.com/pp848484/Messenger/assets/96766856/d63162bf-b437-44fb-a2ce-bcd6fa84c3dd)
### Тоже самое в вебсокете
![image](https://github.com/pp848484/Messenger/assets/96766856/c900a068-4477-406d-bb82-34edea3668b8)

### Друзья
#### Отправка заявки в друзья:
![image](https://github.com/pp848484/Messenger/assets/96766856/6fbcd925-5f8d-4f87-9faf-40fe9f94ebc3)
![image](https://github.com/pp848484/Messenger/assets/96766856/28a29d10-1269-4f59-87cc-f02df9fb58cc)
#### При обоюдной заявки в друзья пользователи считаются друзьями и могу отправлять сообщения  друг другу или просматривать друзей пользователя, даже если у них установлен приватный профиль

#### Просмотр друзей:
#### Перед этим я сделаю их друзьями для наглядности
![image](https://github.com/pp848484/Messenger/assets/96766856/9dfd702b-5788-4695-bca2-785cd24c790c)
![image](https://github.com/pp848484/Messenger/assets/96766856/71986297-b623-41fb-8908-e671a33b08b5)
![image](https://github.com/pp848484/Messenger/assets/96766856/fd47faf6-e80c-41b1-a3f2-a4fa4221177a)
#### Так как у пользователя igor стоит приватный профиль , если они не будут в друзьях с пользователем pp84, получится это:
![image](https://github.com/pp848484/Messenger/assets/96766856/7a6a5775-0614-45c0-89c5-36c7ab06169a)
#### Дополнение к сообщениям, если профиль приватный и пользователи в друзьях друг у друга:
#### RESTful
![image](https://github.com/pp848484/Messenger/assets/96766856/3494198b-afc7-49df-ac62-a04601878b30)
![image](https://github.com/pp848484/Messenger/assets/96766856/9492aa6f-58c0-4026-a1d7-6046fd78eb1a)
#### WebSocket
![image](https://github.com/pp848484/Messenger/assets/96766856/7afab760-4082-45f3-a795-b341adcf7bab)
![image](https://github.com/pp848484/Messenger/assets/96766856/425f8496-47fe-41cb-a16c-0f7089eb0b24)

## Тут показана лишь пример работы этого сервиса, всякие исключения тестируется в test.
![image](https://github.com/pp848484/Messenger/assets/96766856/1e9b3a99-f331-48c8-9673-7bc02707191e)

![image](https://github.com/pp848484/Messenger/assets/96766856/51f8d9ab-e089-4de2-b7a2-1b14035f4aae)

![image](https://github.com/pp848484/Messenger/assets/96766856/69733f79-db70-41cb-b4ad-ab6f9c5ab0b5)

![image](https://github.com/pp848484/Messenger/assets/96766856/c202a29e-c53d-460b-abc4-ba1e527e746b)

![image](https://github.com/pp848484/Messenger/assets/96766856/c511790c-1294-4606-b7b0-bb5f36800f76)



___
# Что было выполнено помимо обязательныех требований
#### есть хэширование паролей; :white_check_mark:
#### есть подтверждение почты через ссылку  в письме, отправленном на указанную почту. :white_check_mark:
#### есть поддержка Spring Security; :white_check_mark:
#### есть механизмы защиты от обхода разлогина. :white_check_mark:
#### при изменении email есть подтверждение изменения ссылкой на указанный новый email. :white_check_mark:
#### реализован перевод профиля в статус “Не активен” с дальнейшей возможностью восстановить
#### профиль в течение некоторого времени. :white_check_mark:
#### обмен и просмотр сообщений реализован с помощью веб-сокетов. :white_check_mark:
#### есть API, позволяющий просматривать друзей, а также добавлять в друзья другого пользователя; :white_check_mark:
#### есть возможность ограничивать получение сообщений только своим кругом друзей; :white_check_mark:
#### есть возможность просматривать друзей другого пользователя :white_check_mark:
#### возможность скрывать свой список друзей. :white_check_mark:
#### использование базы данных (PostgreSQL) для хранения данных; :white_check_mark:
#### документирование запросов через Swagger; :white_check_mark:
#### написание тестов. :white_check_mark:
___
# Usage
## Postgre SQL , Java
### Изменить адрес для доступа к Postgres в application.properies и application-test.properties на свой.

#### application.properties
![image](https://github.com/pp848484/Messenger/assets/96766856/65538f60-abb3-45d2-92c2-6068a8f341ee)

#### application-test.properties
![image](https://github.com/pp848484/Messenger/assets/96766856/f86a0f77-a4a5-45c5-b9b0-0384529f16d5)


### Создать 2 базы данных messenger и messenger-test(для тестов) в postgreSQL
![image](https://github.com/pp848484/Messenger/assets/96766856/c175b11a-1f31-4b84-9daa-9d2d0577f881)


## Swagger
### Для просмотра Swagger документации http://localhost:8080/swagger-ui/index.html#/

## Email
#### Можно поменять почту с которой будет рассылка, также в application.properies(application.properties соответственно)
![image](https://github.com/pp848484/Messenger/assets/96766856/547e6f60-4512-4875-9677-fea5a220149c)
#### в 1 поле указывается хост это может быть mail,gmail,yande и тд
#### во 2 и 3 полях указывается данные от учетной записи , почта и пароль сгенерированный для сторонних приложений
#### далее надо искать информацию для конкретного хоста(в моем случае mail)




