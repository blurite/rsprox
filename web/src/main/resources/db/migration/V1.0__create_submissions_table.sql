create table "submissions"
(
	"id"             serial primary key,
	"created_at"     timestamp    not null,
	"processed"      BOOLEAN      not null,
	"delayed"        varchar(255) not null,
	"account_hash"   varchar(64)  not null,
	"file_checksum"  varchar(40)  not null,
	"file_size"      bigint       not null,
	"revision"       int          not null,
	"sub_revision"   int          not null,
	"client_type"    int          not null,
	"platform_type"  int          not null,
	"world_activity" varchar(255) not null
)
