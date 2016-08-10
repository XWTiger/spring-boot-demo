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
   requestMethod            varchar(255) not null,
   params                   text not null,
   callBackUrl              varchar(255) not null,
   lockTask                     tinyint,
   farmId                   varchar(255),
   addTime                  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
   repeatTimes int(10) unsigned zerofill NOT NULL DEFAULT '0000000000',
   destinationFarmId text,
   primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists TaskResult
(
   id                       varchar(36) not null,
   cFarmId                  varchar(255),
   requestMethod            varchar(255) not null,
   requestUrl               text not null,
   params                   text not null,
   resultStatus             varchar(64) not null,
   info                     text,
   addTime                  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
   envId                    varchar(255),
   destinationFarmId text,
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

