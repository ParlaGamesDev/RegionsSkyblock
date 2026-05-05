package me.goomer.regionsSkyblock.hooks;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Hook for AuraSkills integration.
 */
public class AuraSkillsHook {

    private final AuraSkillsApi api;

    public AuraSkillsHook() {
        this.api = AuraSkillsApi.get();
    }

    /**
     * Get the SkillsUser for a player.
     */
    public SkillsUser getUser(UUID uuid) {
        return api.getUser(uuid);
    }

    /**
     * Get the AuraSkills API instance.
     */
    public AuraSkillsApi getApi() {
        return api;
    }

    /**
     * Add XP to a player's skill.
     *
     * @param player    The player.
     * @param skillName The name of the skill (e.g., "foraging").
     * @param amount    The amount of XP to add.
     */
    public void addXP(Player player, String skillName, double amount) {
        try {
            SkillsUser user = api.getUser(player.getUniqueId());
            if (user == null)
                return;

            dev.aurelium.auraskills.api.skill.Skill skill = api.getGlobalRegistry()
                    .getSkill(NamespacedId.fromDefault(skillName.toLowerCase()));
            if (skill == null)
                return;

            user.addSkillXp(skill, amount);
        } catch (Exception ignored) {
        }
    }
}
