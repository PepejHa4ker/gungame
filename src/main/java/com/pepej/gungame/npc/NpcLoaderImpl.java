package com.pepej.gungame.npc;

import com.pepej.gungame.service.NpcService;
import com.pepej.papi.Papi;
import com.pepej.papi.config.ConfigFactory;
import com.pepej.papi.npc.Npc;
import com.pepej.papi.npc.NpcFactory;
import com.pepej.papi.services.Service;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.List;

public class NpcLoaderImpl implements NpcLoader {

    private final File file;
    @Service
    private NpcFactory npcFactory;
    @Service
    private NpcService npcService;

    public NpcLoaderImpl(final File file) {
        this.file = file;
    }

    @SneakyThrows
    @Override
    public void loadAngRegisterAllNpcs() {
        final List<NpcConfig> npcConfigs = ConfigFactory.gson().load(file).getList(NpcConfig.class);
        if (npcConfigs != null) {
            for (NpcConfig npcConfig : npcConfigs) {
                Npc npc = npcFactory.spawnNpc(npcConfig.getPosition().toLocation(), npcConfig.getDisplayName(), npcConfig.getTexture(), npcConfig.getSignature());
                npc.setClickCallback(player -> Papi.server().dispatchCommand(player, npcConfig.getCommand().replace("<player>", player.getName())));
                GunGameNpc gunGameNpc = new GunGameNpc(npc, npcConfig);
                npcService.register(npcConfig.getId(), gunGameNpc);
            }
        }

    }

    @SneakyThrows
    @Override
    public @Nullable GunGameNpc loadNpc(final String id) {
        final List<NpcConfig> npcConfigs = ConfigFactory.gson().load(file).getList(NpcConfig.class);
        if (npcConfigs != null) {
            for (NpcConfig npcConfig : npcConfigs) {
                if (npcConfig.getId().equals(id)) {
                    Npc npc = npcFactory.spawnNpc(npcConfig.getPosition().toLocation(), npcConfig.getDisplayName(), npcConfig.getTexture(), npcConfig.getSignature());
                    return new GunGameNpc(npc, npcConfig);
                }

            }
        }
        return null;
    }

}
