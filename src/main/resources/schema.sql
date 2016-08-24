/*
MySQL Data Transfer
Source Host: localhost
Source Database: mir-service-broker
Target Host: localhost
Target Database: mir-service-broker
Date: 2016/7/8 9:44:03
*/
create table if not exists TaskStack
(
   id                       varchar(36) not null,
   requestUrl               text not null,
   eventType                varchar(255),
   requestMethod            varchar(255) not null,
   params                   text not null,
   callBackUrl              varchar(255) not null,
   lockTask                     tinyint,
   farmId                   varchar(255) COMMENT '环境模板id',
   addTime                  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
   repeatTimes int(10) unsigned zerofill NOT NULL DEFAULT '0000000000',
   destinationFarmId text COMMENT '目标应用堆栈id (用于非申请事件)',
   primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists TaskResult
(
   id                       varchar(36) not null,
   cFarmId                  varchar(255) COMMENT '克隆出来的应用堆栈id',
   eventType                varchar(255),
   requestMethod            varchar(255) not null,
   requestUrl               text not null,
   params                   text not null,
   resultStatus             varchar(64) not null,
   info                     text,
   addTime                  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
   envId                    varchar(255),
   destinationFarmId text COMMENT '目标应用堆栈id (用于非申请事件)',
   primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
create table if not exists OrderRecord
(
   id                       varchar(36) not null,
   sysName                  varchar(255) not null,
   modelFarmId              varchar(255) not null,
   cFarmId                  varchar(255) not null,
   usrName                  varchar(255),
   serviceTemplateId       varchar(255),
   serviceTemplateName       varchar(255),
   tenantId                  varchar(255) not null,
   addTime                  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
   primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

