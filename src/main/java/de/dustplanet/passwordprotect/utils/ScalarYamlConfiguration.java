package de.dustplanet.passwordprotect.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

/**
 * YAML config extension to allow scalars which are not available per default.
 *
 * @author timbru31
 */
@SuppressWarnings({ "checkstyle:MissingCtor", "PMD.AtLeastOneConstructor", "PMD.CommentSize" })
public class ScalarYamlConfiguration extends YamlConfiguration {
    private final DumperOptions yamlOptions = new DumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given file.
     * <p>
     * Any errors loading the Configuration will be logged and then ignored. If the specified input is not a valid config, a blank config
     * will be returned. The encoding used may follow the system dependent default.
     * </p>
     *
     * @param file Input file
     * @return Resulting configuration
     * @throws IllegalArgumentException Thrown if file is null
     */
    @SuppressWarnings("checkstyle:JavadocParagraph")
    public static ScalarYamlConfiguration loadConfiguration(final File file) {
        Validate.notNull(file, "File cannot be null");

        final ScalarYamlConfiguration config = new ScalarYamlConfiguration();

        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file.getPath().replaceAll("[\r\n]", ""), ex);
        }

        return config;
    }

    @Override
    public String saveToString() {
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.LITERAL);
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        final String header = buildHeader();
        String dump = yaml.dump(getValues(false));

        if (BLANK_CONFIG.equals(dump)) {
            dump = "";
        }

        return header + dump;
    }
}
