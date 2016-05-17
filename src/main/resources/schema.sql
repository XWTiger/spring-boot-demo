
create table if not exists RiskStack
(
   id                       varchar(36) not null,
   requestUrl               text not null,
   requestMethod            varchar(255) not null,
   params                   text not null,
   callBackUrl              varchar(255) not null,
   addTime                  timestamp NOT NULL,
   primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;