package com.pepej.gungame.repository;

import com.pepej.papi.config.objectmapping.ConfigSerializable;
import com.pepej.papi.config.objectmapping.meta.Setting;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigSerializable
public class DatabaseConfig {

    @Setting
    String address;

    @Setting
    String databaseName;

    @Setting
    int port;

    @Setting
    String user;

    @Setting
    String password;
}
