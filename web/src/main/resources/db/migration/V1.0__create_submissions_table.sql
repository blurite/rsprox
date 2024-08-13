create table "submissions"
(
	"id"           serial primary key,
	"created_at"   timestamp    not null,
	"processed"    BOOLEAN      not null,
	"delayed"      varchar(255) not null,
	"account_hash" varchar(64)  not null,
	"file_checksum"     varchar(40)  not null
)
