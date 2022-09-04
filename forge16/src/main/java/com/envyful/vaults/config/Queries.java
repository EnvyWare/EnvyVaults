package com.envyful.vaults.config;

public class Queries {

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `envy_vaults_vaults`(" +
            "id     INT     UNSIGNED    AUTO_INCREMENT NOT NULL, " +
            "uuid       VARCHAR(64)     NOT NULL, " +
            "vault_id   INT             NOT NULL, " +
            "vault_name VARCHAR(256)    NOT NULL, " +
            "items      BLOB            NOT NULL, " +
            "display    BLOB            NOT NULL, " +
            "UNIQUE(uuid, vault_id), " +
            "PRIMARY KEY(id));";

    public static final String LOAD_PLAYER_DATA = "SELECT vault_id, vault_name, items, display FROM `envy_vaults_vaults` WHERE uuid = ?";

    public static final String UPDATE_DATA = "INSERT INTO `envy_vaults_vaults`(uuid, vault_id, vault_name, items, display)" +
            "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
            "vault_name = VALUES(`vault_name`), items = VALUES(`items`), display = VALUES(`display`);";

}
