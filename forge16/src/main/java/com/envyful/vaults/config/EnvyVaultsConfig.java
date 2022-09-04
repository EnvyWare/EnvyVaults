package com.envyful.vaults.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.type.SQLDatabaseDetails;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.api.player.SaveMode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@ConfigPath("config/EnvyVaults/config.yml")
public class EnvyVaultsConfig extends AbstractYamlConfig {

    private SaveMode saveMode = SaveMode.JSON;

    private SQLDatabaseDetails databaseDetails = new SQLDatabaseDetails(
            "example", "0.0.0.0", 3306, "username", "password", "database"
    );

    public EnvyVaultsConfig() {
        super();
    }

    public SaveMode getSaveMode() {
        return this.saveMode;
    }

    public SQLDatabaseDetails getDatabaseDetails() {
        return this.databaseDetails;
    }
}
