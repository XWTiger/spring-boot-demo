
create table if not exists TaskStack
(
   id                       varchar(36) not null,
   requestUrl               text not null,
   requestMethod            varchar(255) not null,
   params                   text not null,
   callBackUrl              varchar(255) not null,
   lockTask                     tinyint,
   farmId                   int(10),
   addTime                  timestamp NOT NULL,
   primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists TaskResult
(
   id                       varchar(36) not null,
   cFarmId                  int(10),
   requestMethod            varchar(255) not null,
   requestUrl               text not null,
   params                   text not null,/*回调返回给外部系统的参数，是mir相关处理的结果*/
   resultStatus             varchar(64) not null,
   errorInfo                     text,/*如果回调返回结果失败存放错误信息，否则为空*/
   addTime                  timestamp NOT NULL,
   primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;